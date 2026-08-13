#!/usr/bin/env python3
"""Extract detailed member info from a source file for member alignment.

Members extracted:
  methods: [(name, static, ret_type, [param_types], raw_arity)]
  fields:  [(name, static, type)]
  ctors:   [arity]
  enums:   [const_name]  (declaration order)
  records: [component_name] (declaration order)

Uses comment/string-stripped collapsed text; visibility-qualified decls plus
line-anchored plain decls inside interfaces.
"""
import re
import build_index as bi

MODS = r'(?:static|final|abstract|synchronized|native|default|transient|volatile|strictfp)\s+'
VIS = r'(?:public|protected|private)\s+'
SIG_VIS_RE = re.compile(
    r'\b' + VIS + r'(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'\(([^)]*)\)'
    r'(?:\s*throws\s+[\w.,\s]+)?\s*[;{]'
)
SIG_PLAIN_RE = re.compile(
    r'(?m)^[ ]*(?!(?:public|protected|private)\s)(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'\(([^)]*)\)'
    r'(?:\s*throws\s+[\w.,\s]+)?\s*[;{]'
)
FIELD_VIS_RE = re.compile(
    r'\b' + VIS + r'(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'(?:=|;)'
)
FIELD_PLAIN_RE = re.compile(
    r'(?m)^[ ]*(?!(?:public|protected|private)\s)(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'[=;,]'
)
ENUM_RE = re.compile(r'(?m)^[ ]*([A-Z][A-Z0-9_]*)\s*(?:\([^)]*\))?\s*[,;]')
RECORD_RE = re.compile(r'\brecord\s+[A-Za-z_$][A-Za-z0-9_$]*\s*\(([^)]*)\)')
ANNO_RE = re.compile(r'@[\w.]+')

def split_top(s):
    if not s.strip():
        return []
    depth = 0; parts = []; cur = []
    for ch in s:
        if ch in '<([': depth += 1
        elif ch in '>)]': depth -= 1
        if ch == ',' and depth == 0:
            parts.append(''.join(cur).strip()); cur = []
        else:
            cur.append(ch)
    if cur: parts.append(''.join(cur).strip())
    return [p for p in parts if p]

def clean_type(t):
    t = ANNO_RE.sub(' ', t).strip()
    # varargs -> array
    if t.endswith('...'):
        t = t[:-3].strip() + '[]'
    return t

def param_type(ptok):
    ptok = ptok.strip()
    ptok = ANNO_RE.sub(' ', ptok).strip()
    if ' ' in ptok:
        *types, name = ptok.rsplit(' ', 1)
        return clean_type(' '.join(types))
    return clean_type(ptok)

JAVA_KEYWORDS = {
    'abstract','assert','boolean','break','byte','case','catch','char','class','const','continue',
    'default','do','double','else','enum','extends','final','finally','float','for','goto','if',
    'implements','import','instanceof','int','interface','long','native','new','package','private',
    'protected','public','return','short','static','strictfp','super','switch','synchronized','this',
    'throw','throws','transient','try','void','volatile','while','true','false','null','record','yield','var',
    'int8','int9','int10','int11','int12','int13','int14','int15',
}

def is_valid_method(name, ret, params):
    if name in JAVA_KEYWORDS:
        return False
    # return type must not start with a control-flow keyword
    first = ret.split(' ', 1)[0].strip()
    if first in ('return', 'new', 'if', 'else', 'for', 'while', 'switch', 'case', 'break', 'continue', 'throw', 'yield'):
        return False
    # params must be type-like (no method refs / assignment expressions)
    for p in params:
        if '::' in p or '=' in p or '{' in p or '}' in p or p.strip() in JAVA_KEYWORDS:
            return False
    # bare name with no return type and no params looks like an expression
    if ret.strip() in JAVA_KEYWORDS or ret.strip() == '':
        return False
    return True

def collect_sigs(regex, text, out, name_idx, ret_idx, param_idx, static_of):
    for m in regex.finditer(text):
        name = m.group(name_idx)
        ret = clean_type(m.group(ret_idx))
        params = [param_type(p) for p in split_top(m.group(param_idx))]
        if not is_valid_method(name, ret, params):
            continue
        is_ctor = name[:1].isupper()
        if is_ctor:
            out['ctors'].append(len(params))
        else:
            out['methods'].append((name, 'static' in m.group(0), ret, params))

def top_level_is_interface(flat):
    """True if the first top-level type declaration in the file is an interface."""
    m = re.search(r'\b(class|interface|enum|record)\s+[A-Za-z_$][A-Za-z0-9_$]*', flat)
    return bool(m and m.group(1) == 'interface')

def extract(file_path):
    with open(file_path, encoding='utf-8', errors='replace') as f:
        text = f.read()
    strings, liny, flat = bi.preprocess(text)
    out = {'methods': [], 'ctors': [], 'fields': [], 'enums': [], 'records': []}
    collect_sigs(SIG_VIS_RE, flat, out, 2, 1, 3, None)
    is_iface = top_level_is_interface(flat)
    if is_iface:
        # strip annotation tokens so the no-visibility regex doesn't grab
        # annotated visibility-methods or bogus spans
        liny_clean = re.sub(r'\b@[\w.]+', '', liny)
        collect_sigs(SIG_PLAIN_RE, liny_clean, out, 2, 1, 3, None)
    # fields: also capture initializer literal by scanning the ORIGINAL text
    def field_value(name):
        m = re.search(r'(?<![\w.])' + re.escape(name) + r'\s*=\s*', text)
        if not m:
            return None
        seg = text[m.end():m.end() + 180]
        mm = re.search(r'"((?:[^"\\]|\\.)*)"|\'((?:[^\'\\]|\\.)*)\'|([-]?\b\d+[Ll]?\b)', seg)
        if mm:
            if mm.group(1) is not None:
                return ('str', mm.group(1))
            if mm.group(2) is not None:
                return ('str', mm.group(2))
            if mm.group(3) is not None:
                return ('int', mm.group(3))
        return None
    for m in FIELD_VIS_RE.finditer(flat):
        out['fields'].append((m.group(2), 'static' in m.group(0), clean_type(m.group(1)), field_value(m.group(2))))
    if is_iface:
        for m in FIELD_PLAIN_RE.finditer(liny):
            if m.group(2) not in [f[0] for f in out['fields']]:
                out['fields'].append((m.group(2), 'static' in m.group(0), clean_type(m.group(1)), field_value(m.group(2))))
    out['enums'] = [m.group(1) for m in ENUM_RE.finditer(liny)]
    for m in RECORD_RE.finditer(flat):
        for comp in split_top(m.group(1)):
            parts = comp.strip().rsplit(' ', 1)
            out['records'].append(parts[-1] if len(parts) == 2 else comp.strip())
    return out

if __name__ == '__main__':
    import sys, pprint
    r = extract(sys.argv[1])
    pprint.pprint(r)
