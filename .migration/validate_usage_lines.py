#!/usr/bin/env python3
"""For each used-but-unmatched member, find the actual project source lines that
reference it, to judge whether it's a real usage (call/field-access on the
class) or an over-approximation false positive (local var, unrelated ident).

We look for lines containing the member name AND (class simple name or 'this'
or an obvious accessor) near it. Simpler heuristic: report the project file(s)
that import the class and contain the member name, plus the matched lines.
"""
import os, re, json, glob
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']
mm = json.load(open(os.path.join(HERE, 'member_map.json'), encoding='utf-8'))

def base_name(y):
    if '(' in y:
        return y.split('(')[0]
    if ' : ' in y:
        return y.split(' : ')[0]
    return y

def build_cls_members():
    d = {}
    for cls, info in mm.items():
        names = set()
        for mem in info['members']:
            b = base_name(mem['yarn'])
            if b:
                names.add(b)
        d[cls] = names
    return d

cls_members = build_cls_members()
unmatched_names = {}
for cls, info in mm.items():
    for mem in info['members']:
        if mem['status'] == 'unmatched':
            unmatched_names.setdefault(cls, set()).add(base_name(mem['yarn']))

# usage lines per (class, member)
usage_lines = defaultdict(list)
for p in glob.glob(PROJECT_DIRS[0] + '/**/*.java', recursive=True) + \
         glob.glob(PROJECT_DIRS[1] + '/**/*.java', recursive=True):
    with open(p, encoding='utf-8', errors='replace') as f:
        text = f.read()
    imports = set()
    for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', text, re.M):
        fqn = m.group(1)
        if fqn.startswith('net.minecraft.'):
            imports.add(fqn)
    for line_no, line in enumerate(text.splitlines(), 1):
        if '//' in line:
            line = line.split('//')[0]
        for fqn in imports:
            if fqn not in unmatched_names:
                continue
            simple = fqn.rsplit('.', 1)[-1]
            for nm in unmatched_names[fqn]:
                # member name appears and the class simple name appears in the same line
                if re.search(r'\b' + re.escape(nm) + r'\b', line) and \
                   re.search(r'\b' + re.escape(simple) + r'\b', line):
                    usage_lines[(fqn, nm)].append((os.path.basename(p), line_no, line.strip()))

out = {}
for (cls, nm), lines in sorted(usage_lines.items()):
    out[f'{cls}::{nm}'] = lines[:3]
    for b, ln, l in lines[:3]:
        print(f'{cls}::{nm}  @ {b}:{ln}  {l}')
json.dump(out, open(os.path.join(HERE, 'validate_usage_lines.json'), 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
