#!/usr/bin/env python3
"""Measure the TRUE blocking gap: project-used methods missing from the table
that have a DIFFERENT name in MojMap (i.e. a rename the table cannot provide).

For each project-used missing method, align it to its MojMap counterpart via
the MojMap source using signature matching (name-independent where needed) and
report whether names differ.

Method: for each (class, yarn_method) that the project uses but the table lacks,
  - load Yarn source, find the method's signature (params)
  - load MojMap source for the mapped class, search for a method with matching
    params; if exactly one MojMap method has that signature and its name != yarn
    name -> this is a REAL rename the table cannot provide -> BLOCKER.
  - if the MojMap method has the SAME name -> harmless gap.
  - if signature ambiguous / not found -> 'needs-verify'.
"""
import os, re, json, glob
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
MOJ = r'D:/aiminecraftdev/minecraft1.21.1mojmap'
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']

import build_index as bi
import memberparse as mp

def is_valid_fixed(name, ret, params):
    if name in mp.JAVA_KEYWORDS: return False
    first = ret.split(' ', 1)[0].strip()
    if first in ('return','new','if','else','for','while','switch','case','break','continue','throw','yield'): return False
    for p in params:
        if '::' in p or '=' in p or '{' in p or '}' in p or p.strip() in mp.JAVA_KEYWORDS: return False
    if ret.strip() == '': return False
    return True

def extract_method_sigs(path):
    """Return {name: set(param-type-tuples)} with fixed parser."""
    if not os.path.exists(path):
        return {}
    text = open(path, encoding='utf-8', errors='replace').read()
    strings, liny, flat = bi.preprocess(text)
    sigs = defaultdict(set)
    for m in mp.SIG_VIS_RE.finditer(flat):
        name, ret = m.group(2), mp.clean_type(m.group(1))
        params = tuple(mp.param_type(p) for p in mp.split_top(m.group(3)))
        if name[:1].isupper(): continue
        if is_valid_fixed(name, ret, params):
            sigs[name].add(params)
    return dict(sigs)

def base_type(t):
    # 'net.minecraft.world.World' -> 'World'; 'Map<A,B>' -> 'Map'; strip generics/arrays
    t = t.strip()
    t = re.sub(r'<.*>', '', t)
    t = t.replace('[]', '')
    if '.' in t:
        t = t.rsplit('.', 1)[-1]
    return t

def params_similar(a, b):
    if len(a) != len(b): return False
    for x, y in zip(a, b):
        if base_type(x) != base_type(y):
            return False
    return True

def main():
    mm = json.load(open(os.path.join(HERE, 'member_map.json'), encoding='utf-8'))
    cm = json.load(open(os.path.join(HERE, 'class_map.json'), encoding='utf-8'))
    gap = json.load(open(os.path.join(HERE, 'validate_voidgap.json'), encoding='utf-8'))

    # Also re-run full gap incl non-void: use validate_voidgap results only for now,
    # plus we recompute the missing set fresh to be safe.
    table_methods = {}
    for cls, info in mm.items():
        s = set()
        for m in info['members']:
            if m['kind'] == 'method':
                s.add(m['yarn'].split('(')[0])
        table_methods[cls] = s

    results = {'same_name': [], 'renamed': [], 'ambiguous': [], 'no_moj_match': []}
    for cls, missing_names in gap.items():
        moj_cls = cm.get(cls)
        if not moj_cls:
            continue
        ysig = extract_method_sigs(os.path.join(YARN, cls.replace('.', os.sep) + '.java'))
        msig = extract_method_sigs(os.path.join(MOJ, moj_cls.replace('.', os.sep) + '.java'))
        for nm in missing_names:
            yparams_list = ysig.get(nm, [()])
            # find MojMap candidates by params
            matches = []
            for mname, mparams_list in msig.items():
                for mp_ in mparams_list:
                    for yp in yparams_list:
                        if params_similar(yp, mp_):
                            matches.append(mname)
            matches = set(matches)
            if nm in matches and len(matches) == 1:
                results['same_name'].append((cls, nm))
            elif nm in matches:
                results['ambiguous'].append((cls, nm, sorted(matches)))
            elif len(matches) == 1:
                results['renamed'].append((cls, nm, next(iter(matches))))
            elif len(matches) == 0:
                results['no_moj_match'].append((cls, nm))
            else:
                results['ambiguous'].append((cls, nm, sorted(matches)))

    print('=== TRUE GAP (project-used missing methods) ===')
    print(f'total project-used missing methods: {len(results["same_name"])+len(results["renamed"])+len(results["ambiguous"])+len(results["no_moj_match"])}')
    print(f'  same-name (harmless): {len(results["same_name"])}')
    print(f'  RENAMED (blockers):   {len(results["renamed"])}')
    print(f'  ambiguous (need check): {len(results["ambiguous"])}')
    print(f'  no moj match (verify):  {len(results["no_moj_match"])}')
    print()
    print('--- RENAMED (table cannot provide these renames) ---')
    for cls, nm, mj in sorted(results['renamed']):
        print(f'  {cls} :: {nm} -> {mj}')
    print()
    print('--- AMBIGUOUS ---')
    for cls, nm, mjs in results['ambiguous'][:40]:
        print(f'  {cls} :: {nm} -> candidates {mjs}')
    print()
    print('--- NO MOJ MATCH ---')
    for cls, nm in results['no_moj_match'][:40]:
        print(f'  {cls} :: {nm}')

    json.dump(results, open(os.path.join(HERE, 'validate_realgap.json'), 'w', encoding='utf-8'), indent=1, ensure_ascii=False)

if __name__ == '__main__':
    main()
