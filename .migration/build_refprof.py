#!/usr/bin/env python3
"""Build reference profiles (set of referenced type simple names) for all classes.

Reference profile = simple type names in method return types, param types,
field types. Interface members (no visibility) included via line-anchored pass.

Writes ref_yarn.json and ref_moj.json: FQN -> {'simples': [...], 'extends': [...]}
"""
import os, re, json, glob
import build_index as bi

YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
MOJ = r'D:/aiminecraftdev/minecraft1.21.1mojmap'
HERE = os.path.dirname(os.path.abspath(__file__))

MODS = r'(?:static|final|abstract|synchronized|native|default|transient|volatile|strictfp)\s+'
VIS = r'(?:public|protected|private)\s+'
SIG_VIS_RE = re.compile(
    r'\b' + VIS + r'(?:' + MODS + r')*'
    r'([\w<>\[\],.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'\(([^)]*)\)'
)
SIG_PLAIN_RE = re.compile(
    r'(?m)^[ ]*(?!(?:public|protected|private)\s)(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'\(([^)]*)\)'
)
FIELD_VIS_RE = re.compile(
    r'\b' + VIS + r'(?:' + MODS + r')*'
    r'([\w<>\[\],.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'(?:=|;)'
)
FIELD_PLAIN_RE = re.compile(
    r'(?m)^[ ]*(?!(?:public|protected|private)\s)(?:' + MODS + r')*'
    r'([\w<>\[\],.?&.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'[=;,]'
)
ANNO_RE = re.compile(r'@[\w.]+')
EXT_RE = re.compile(r'\b(?:class|interface|enum|record)\s+[A-Za-z_$][A-Za-z0-9_$]*(?:<[^>]*>)?\s+(?:extends\s+([\w<>\[\],.\s]+?))?\s*(?:implements\s+([\w<>\[\],.\s]+?))?\s*\{')

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

def simples_in(t):
    t = ANNO_RE.sub(' ', t)
    return set(re.findall(r'\b[A-Z][A-Za-z0-9_$]*\b', t))

def sig_refs(sig_regex, text, sims):
    for m in sig_regex.finditer(text):
        ret = m.group(1)
        params = split_top(m.group(3))
        sims |= simples_in(ret)
        for p in params:
            parts = p.strip().rsplit(' ', 1)
            t = parts[0] if len(parts) == 2 else p.strip()
            sims |= simples_in(t)

def field_refs(field_regex, text, sims):
    for m in field_regex.finditer(text):
        sims |= simples_in(m.group(1))

def ref_profile(text):
    strings, liny, flat = bi.preprocess(text)
    sims = set()
    sig_refs(SIG_VIS_RE, flat, sims)
    field_refs(FIELD_VIS_RE, flat, sims)
    is_iface = bool(bi.IFACE_RE.search(flat))
    if is_iface:
        sig_refs(SIG_PLAIN_RE, liny, sims)
        field_refs(FIELD_PLAIN_RE, liny, sims)
    extends = []
    for m in EXT_RE.finditer(flat):
        for g in (1, 2):
            if m.group(g):
                for part in split_top(m.group(g)):
                    part = ANNO_RE.sub(' ', part)
                    parts = part.rsplit(' ', 1)
                    t = parts[0] if len(parts) == 2 else part
                    sims |= simples_in(t)
    return {'simples': sorted(sims)}

def rel_fqn(path, root):
    rel = os.path.relpath(path, root)
    if rel.endswith('.java'): rel = rel[:-5]
    return rel.replace(os.sep, '.')

def build(root):
    out = {}
    for sub in ('net', 'com'):
        base = os.path.join(root, sub)
        if not os.path.isdir(base):
            continue
        for path in glob.glob(base + '/**/*.java', recursive=True):
            with open(path, encoding='utf-8', errors='replace') as f:
                text = f.read()
            out[rel_fqn(path, root)] = ref_profile(text)
    return out

if __name__ == '__main__':
    y = build(YARN)
    m = build(MOJ)
    print('yarn ref profiles:', len(y))
    print('moj ref profiles:', len(m))
    with open(os.path.join(HERE, 'ref_yarn.json'), 'w', encoding='utf-8') as f:
        json.dump(y, f)
    with open(os.path.join(HERE, 'ref_moj.json'), 'w', encoding='utf-8') as f:
        json.dump(m, f)
    print('written.')
