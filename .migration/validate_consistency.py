#!/usr/bin/env python3
"""Validation ⑤: Consistency check over class_map.json + member_map.json.

Checks for:
  1. One yarn class -> multiple mojmap classes (within class_map)
  2. Multiple yarn classes -> same mojmap class (within class_map)  [OK if merged classes]
  3. One yarn member (name+kind within a class) -> multiple distinct mojmap members
  4. Multiple yarn members -> same mojmap member within a class (collision)

For method overloads (same name, different params) a single yarn name mapping to
the same mojmap name is normal and NOT a conflict. We flag only *name-level*
divergence (same yarn simple-name mapping to different mojmap names, or two
different yarn names collapsing to one mojmap name), which indicates wrong
alignment.
"""
import os, json, re
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def base_name(y):
    # 'method(...)' -> method ; 'field : Type' -> field ; 'Name' -> Name
    if '(' in y:
        return y.split('(')[0]
    if ' : ' in y:
        return y.split(' : ')[0]
    return y

def main():
    cm = load('class_map.json')
    mm = load('member_map.json')
    issues = []

    # ---- class-level: one yarn -> multiple moj (only if same yarn key present once)
    # class_map is dict yarn->moj so key uniqueness is inherent; but a yarn FQN
    # could be spelled with differing simple-name capitalization -> check simple.
    by_simple = defaultdict(set)
    for y, m in cm.items():
        ys = y.rsplit('.', 1)[-1]
        ms = m.rsplit('.', 1)[-1]
        by_simple[ys].add((y, m))
    multi = {ys: v for ys, v in by_simple.items() if len({m for y, m in v}) > 1}
    if multi:
        issues.append(('class: yarn simple-name -> multiple mojmap', multi))

    # ---- member-level consistency
    mem_conflicts = []
    for cls, info in mm.items():
        # group yarn base-name -> set of moj base-names, per kind
        for kind in ('method', 'field', 'enum', 'record'):
            seen = defaultdict(set)   # yarn base -> {moj base}
            rev = defaultdict(set)    # moj base -> {yarn base}
            for m in info['members']:
                if m['kind'] != kind:
                    continue
                if m['status'] == 'unmatched' or not m['moj']:
                    continue
                yn = base_name(m['yarn'])
                mn = base_name(m['moj'])
                seen[yn].add(mn)
                rev[mn].add(yn)
            for yn, mns in seen.items():
                if len(mns) > 1:
                    mem_conflicts.append((cls, kind, yn, sorted(mns)))
            for mn, yns in rev.items():
                if len(yns) > 1:
                    # multiple yarn names -> one moj name. For fields this is suspicious;
                    # for methods with same moj name but different yarn overloads could be
                    # legitimate (yarn renamed overloads differently). Report anyway.
                    mem_conflicts.append((cls, kind, f'{mn} (rev)', sorted(yns)))

    # dedup
    seen_set = set()
    uniq = []
    for c in mem_conflicts:
        k = (c[0], c[1], c[2], tuple(c[3]))
        if k not in seen_set:
            seen_set.add(k)
            uniq.append(c)

    print(f'=== Consistency check ===')
    print(f'class-level yarn-simple-name -> multiple mojmap: {len(multi)}')
    for ys, v in sorted(multi.items()):
        print(f'  {ys}: {sorted(set(v))}')
    print(f'member-level name conflicts: {len(uniq)}')
    for cls, kind, yn, mns in uniq[:120]:
        print(f'  {cls} :: {kind} :: {yn} -> {mns}')

    # Summary of conflict types
    fwd = [c for c in uniq if not c[2].endswith('(rev)')]
    rev = [c for c in uniq if c[2].endswith('(rev)')]
    print(f'  forward (1 yarn -> N moj): {len(fwd)}')
    print(f'  reverse (N yarn -> 1 moj): {len(rev)}')
    with open(os.path.join(HERE, 'validate_consistency.txt'), 'w', encoding='utf-8') as f:
        for cls, kind, yn, mns in uniq:
            f.write(f'{cls} :: {kind} :: {yn} -> {mns}\n')

if __name__ == '__main__':
    main()
