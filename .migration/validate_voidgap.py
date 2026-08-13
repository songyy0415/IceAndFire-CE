#!/usr/bin/env python3
"""Measure the void-method extraction gap from the project's perspective.

The member extraction (memberparse.is_valid_method) drops all void methods
(ret type 'void' is in JAVA_KEYWORDS). So the mapping table is missing every
void method. This script:
  1. Re-extracts each needed class's FULL Yarn method list with a corrected
     parser (allowing void), from the authoritative Yarn source.
  2. Finds project files that import the class and use those void method names.
  3. Reports which project-used void methods are missing from member_map.json.

Output: validate_voidgap.json + printed summary.
"""
import os, re, json, glob
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']

# ---- corrected member extraction (allow void) ----
import build_index as bi
import memberparse as mp
import types

# monkeypatch is_valid_method to allow void
def is_valid_method_fixed(name, ret, params):
    if name in mp.JAVA_KEYWORDS:
        return False
    first = ret.split(' ', 1)[0].strip()
    if first in ('return', 'new', 'if', 'else', 'for', 'while', 'switch', 'case', 'break', 'continue', 'throw', 'yield'):
        return False
    for p in params:
        if '::' in p or '=' in p or '{' in p or '}' in p or p.strip() in mp.JAVA_KEYWORDS:
            return False
    if ret.strip() == '':
        return False
    return True

def extract_all(path):
    with open(path, encoding='utf-8', errors='replace') as f:
        text = f.read()
    strings, liny, flat = bi.preprocess(text)
    names = set()
    # visibility-qualified
    for m in mp.SIG_VIS_RE.finditer(flat):
        name = m.group(2)
        ret = mp.clean_type(m.group(1))
        params = [mp.param_type(p) for p in mp.split_top(m.group(3))]
        if name[:1].isupper():
            continue
        if is_valid_method_fixed(name, ret, params):
            names.add(name)
    # interface plain
    if mp.top_level_is_interface(flat):
        liny_clean = re.sub(r'\b@[\w.]+', '', liny)
        for m in mp.SIG_PLAIN_RE.finditer(liny_clean):
            name = m.group(2)
            ret = mp.clean_type(m.group(1))
            params = [mp.param_type(p) for p in mp.split_top(m.group(3))]
            if name[:1].isupper():
                continue
            if is_valid_method_fixed(name, ret, params):
                names.add(name)
    return names

def main():
    mm = json.load(open(os.path.join(HERE, 'member_map.json'), encoding='utf-8'))
    cm = json.load(open(os.path.join(HERE, 'class_map.json'), encoding='utf-8'))

    # build table method-name set per class (what the table knows)
    table_methods = {}
    for cls, info in mm.items():
        s = set()
        for m in info['members']:
            if m['kind'] == 'method':
                s.add(m['yarn'].split('(')[0])
        table_methods[cls] = s

    # per project file: imported yarn classes + used identifiers
    used = defaultdict(set)   # cls -> set(member names)
    file_cls = defaultdict(set)  # file -> set(classes)
    IDENT = re.compile(r'\b([A-Za-z_$][A-Za-z0-9_$]*)\b')
    for d in PROJECT_DIRS:
        for p in glob.glob(d + '/**/*.java', recursive=True):
            with open(p, encoding='utf-8', errors='replace') as f:
                t = f.read()
            t2 = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', t, flags=re.S)
            idents = set(IDENT.findall(t2))
            for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', t, re.M):
                fqn = m.group(1)
                if fqn.startswith('net.minecraft.'):
                    file_cls[p].add(fqn)
                    used[fqn].update(idents)

    # For each project-used class: full Yarn method set vs table set
    gap = {}   # cls -> [method names used by project, missing from table]
    gap_counts = defaultdict(int)
    for cls in sorted(used):
        yp = os.path.join(YARN, cls.replace('.', os.sep) + '.java')
        if not os.path.exists(yp):
            continue
        if cls not in cm:
            continue
        yarn_methods = extract_all(yp)
        missing = yarn_methods - table_methods.get(cls, set())
        used_missing = missing & used[cls]
        if used_missing:
            gap[cls] = sorted(used_missing)
            gap_counts[len(used_missing)] += 1

    total_used_missing = sum(len(v) for v in gap.values())
    print('=== project-used void/absent methods missing from table ===')
    print(f'classes affected: {len(gap)}')
    print(f'project-used method names missing from table: {total_used_missing}')
    # list all with usage file evidence
    out = {}
    for cls, ms in sorted(gap.items()):
        # find files+lines evidence
        ev = []
        for p, clss in file_cls.items():
            if cls in clss:
                base = os.path.basename(p)
                for nm in ms:
                    ev.append((base, nm))
        out[cls] = ms
        for nm in ms:
            print(f'  {cls} :: {nm}')
    json.dump(out, open(os.path.join(HERE, 'validate_voidgap.json'), 'w', encoding='utf-8'), indent=1, ensure_ascii=False)

if __name__ == '__main__':
    main()
