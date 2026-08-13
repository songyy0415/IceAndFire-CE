#!/usr/bin/env python3
"""Build fingerprint index for all classes in Yarn and MojMap sources.

Extraction:
  - strings (literals)
  - vis arities/fields: declarations prefixed with public/protected/private
    (reliable for class bodies; interfaces often omit visibility so these
    undercount there)
  - plain arities/fields: line-anchored declarations with NO visibility
    (only counted when the file's top-level type is an interface)
  - enum constants (line-anchored UPPER_NAME [,;])
  - interface flag

Writes yarn_index.json / moj_index.json (FQN -> fingerprint).
"""
import os, re, json, glob

YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
MOJ = r'D:/aiminecraftdev/minecraft1.21.1mojmap'
HERE = os.path.dirname(os.path.abspath(__file__))

def preprocess(text):
    strings = []
    out = []
    i = 0
    n = len(text)
    while i < n:
        c = text[i]
        if c == '/' and i+1 < n and text[i+1] == '/':
            while i < n and text[i] != '\n':
                i += 1
            out.append('\n')
            continue
        if c == '/' and i+1 < n and text[i+1] == '*':
            j = text.find('*/', i+2)
            i = j+2 if j != -1 else n
            out.append(' ')
            continue
        if c == '"':
            j = i+1
            buf = []
            while j < n:
                if text[j] == '\\' and j+1 < n:
                    buf.append(text[j+1]); j += 2
                elif text[j] == '"':
                    break
                else:
                    buf.append(text[j]); j += 1
            strings.append(''.join(buf))
            out.append(' "" ')
            i = j+1
            continue
        if c == "'":
            j = i+1
            while j < n and text[j] != "'":
                j += 1
            out.append(' ')
            i = j+1
            continue
        out.append(c)
        i += 1
    liny = ''.join(out)
    liny = re.sub(r'[ \t\r\f]+', ' ', liny)
    flat = re.sub(r'\s+', ' ', liny)
    return strings, liny, flat

MODS = r'(?:static|final|abstract|synchronized|native|default|transient|volatile|strictfp)\s+'
VIS = r'(?:public|protected|private)\s+'

# visibility-qualified method/field (run on flat)
METHOD_VIS_RE = re.compile(
    r'\b' + VIS + r'(?:' + MODS + r')*'
    r'([\w<>\[\],.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'\(([^)]*)\)'
    r'(?:\s*throws\s+[\w.,\s]+)?\s*[;{]'
)
FIELD_VIS_RE = re.compile(
    r'\b' + VIS + r'(?:' + MODS + r')*'
    r'([\w<>\[\],.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'(?:=|;)'
)
# line-anchored no-visibility method/field (run on liny, per line)
METHOD_PLAIN_RE = re.compile(
    r'(?m)^[ ]*(?!(?:public|protected|private)\s)(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'\(([^)]*)\)'
    r'(?:\s*throws\s+[\w.,\s]+)?\s*[;{]'
)
FIELD_PLAIN_RE = re.compile(
    r'(?m)^[ ]*(?!(?:public|protected|private)\s)(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'[=;,]'
)
ENUM_RE = re.compile(r'(?m)^[ ]*([A-Z][A-Z0-9_]*)\s*(?:\([^)]*\))?\s*[,;]')
IFACE_RE = re.compile(r'\binterface\s+[A-Za-z_$][A-Za-z0-9_$]*')

def count_params(s):
    if not s.strip():
        return 0
    depth = 0; parts = []; cur = []
    for ch in s:
        if ch in '<([': depth += 1
        elif ch in '>)]': depth -= 1
        if ch == ',' and depth == 0:
            parts.append(''.join(cur)); cur = []
        else:
            cur.append(ch)
    if cur: parts.append(''.join(cur))
    return len([p for p in parts if p.strip() and p.strip() != 'var'])

def collect_methods(regex, text, ctor_arities, arities):
    for m in regex.finditer(text):
        name = m.group(2)
        n = count_params(m.group(3))
        if name[:1].isupper():
            ctor_arities.append(n)
        else:
            arities.append(n)

def fingerprint(path):
    with open(path, encoding='utf-8', errors='replace') as f:
        text = f.read()
    strings, liny, flat = preprocess(text)
    is_iface = bool(IFACE_RE.search(flat))
    vis_arities = []; vis_ctors = []; vis_fields = 0
    collect_methods(METHOD_VIS_RE, flat, vis_ctors, vis_arities)
    vis_fields = len(FIELD_VIS_RE.findall(flat))
    arities = vis_arities
    ctors = vis_ctors
    field_count = vis_fields
    if is_iface:
        plain_arities = []; plain_ctors = []
        collect_methods(METHOD_PLAIN_RE, liny, plain_ctors, plain_arities)
        arities = vis_arities + plain_arities
        ctors = vis_ctors + plain_ctors
        field_count = vis_fields + len(FIELD_PLAIN_RE.findall(liny))
    enum_consts = [m.group(1) for m in ENUM_RE.finditer(liny)]
    return {
        'strings': sorted(strings),
        'arities': sorted(arities),
        'ctor_arities': sorted(ctors),
        'field_count': field_count,
        'enum_consts': sorted(enum_consts),
        'is_iface': is_iface,
        'src_len': len(text),
    }

def rel_fqn(path, root):
    rel = os.path.relpath(path, root)
    if rel.endswith('.java'): rel = rel[:-5]
    return rel.replace(os.sep, '.')

def build(root):
    idx = {}
    for sub in ('net', 'com'):
        base = os.path.join(root, sub)
        if not os.path.isdir(base):
            continue
        for path in glob.glob(base + '/**/*.java', recursive=True):
            idx[rel_fqn(path, root)] = fingerprint(path)
    return idx

if __name__ == '__main__':
    y = build(YARN)
    m = build(MOJ)
    print('yarn classes:', len(y))
    print('mojmap classes:', len(m))
    with open(os.path.join(HERE, 'yarn_index.json'), 'w', encoding='utf-8') as f:
        json.dump(y, f)
    with open(os.path.join(HERE, 'moj_index.json'), 'w', encoding='utf-8') as f:
        json.dump(m, f)
    print('written.')
