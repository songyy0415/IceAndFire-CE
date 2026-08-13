#!/usr/bin/env python3
"""Check which project-used members are unmatched in the member map.

Over-approximation: for each project file and each imported yarn class, any
identifier in the file that is a member name of that class is treated as a
usage. Conservative (never misses a real usage), may over-attribute.

Output: used_unmatched.txt
"""
import os, re, json, glob

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def main():
    mm = load('member_map.json')
    yidx = load('yarn_index.json')
    # per class: yarn member names
    cls_members = {}
    for cls, info in mm.items():
        names = set()
        for mem in info['members']:
            yn = mem['yarn']
            if mem['kind'] in ('method',):
                base = yn.split('(')[0]
                if base and not base[0].isupper():
                    names.add(base)
            elif mem['kind'] == 'field':
                base = yn.split(' : ')[0]
                if base:
                    names.add(base)
            elif mem['kind'] in ('enum', 'record'):
                if yn: names.add(yn)
        cls_members[cls] = names

    # unmatched member names per class
    cls_unmatched = {}
    for cls, info in mm.items():
        names = set()
        for mem in info['members']:
            if mem['status'] != 'unmatched':
                continue
            yn = mem['yarn']
            if mem['kind'] in ('method',):
                base = yn.split('(')[0]
            elif mem['kind'] == 'field':
                base = yn.split(' : ')[0]
            else:
                base = yn
            if base: names.add(base)
        cls_unmatched[cls] = names

    used = {}
    IDENT_RE = re.compile(r'\b([A-Za-z_$][A-Za-z0-9_$]*)\b')
    for proj_dir in PROJECT_DIRS:
        for path in glob.glob(proj_dir + '/**/*.java', recursive=True):
            with open(path, encoding='utf-8', errors='replace') as f:
                text = f.read()
            text_nc = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', text, flags=re.S)
            imports = {}
            for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', text, re.M):
                imp = m.group(1)
                if imp.startswith('net.minecraft.') and not imp.endswith('.*'):
                    imports[imp.rsplit('.', 1)[-1]] = imp
            idents = set(IDENT_RE.findall(text_nc))
            for simple, fqn in imports.items():
                if fqn not in cls_members:
                    continue
                used.setdefault(fqn, set()).update(idents & cls_members[fqn])

    # used-but-unmatched
    used_unmatched = []
    for cls, used_set in used.items():
        if cls not in cls_unmatched:
            continue
        inter = used_set & cls_unmatched[cls]
        for m in sorted(inter):
            used_unmatched.append((cls, m))

    print(f'classes with any used member: {len(used)}')
    print(f'used-but-unmatched members: {len(used_unmatched)}')
    with open(os.path.join(HERE, 'used_unmatched.txt'), 'w', encoding='utf-8') as f:
        for cls, m in sorted(used_unmatched):
            f.write(f'{cls} :: {m}\n')
    for cls, m in sorted(used_unmatched):
        print(f'   {cls} :: {m}')

if __name__ == '__main__':
    main()
