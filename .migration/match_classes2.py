#!/usr/bin/env python3
"""Refine class matching for string-less / previously unmatched classes.

Uses reference profiles + normalized signature alignment, iterating to
bootstrap the yarn_simple -> mojmap_simple translation table.

Outputs: class_map.json (extended), class_map_review.txt
"""
import os, json
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def simple(fqn):
    return fqn.rsplit('.', 1)[-1]

def build_translation(class_map):
    """yarn_simple -> mojmap_simple, only unambiguous mappings."""
    d = {}
    for y, m in class_map.items():
        ys, ms = simple(y), simple(m)
        if ys not in d:
            d[ys] = set()
        d[ys].add(ms)
    T = {}
    for ys, mset in d.items():
        if len(mset) == 1:
            T[ys] = next(iter(mset))
    return T

def norm_type(t, T):
    import re
    def repl(m):
        return T.get(m.group(0), m.group(0))
    return re.sub(r'\b[A-Z][A-Za-z0-9_$]*\b', repl, t)

def main():
    yidx = load('yarn_index.json')
    midx = load('moj_index.json')
    ry = load('ref_yarn.json')
    rm = load('ref_moj.json')
    class_map = load('class_map.json')

    claimed = set(class_map.values())
    unmatched_yarn = [y for y in yidx if y not in class_map]

    # iteration to grow translation table
    added = True
    round_no = 0
    while added and unmatched_yarn:
        round_no += 1
        added = False
        T = build_translation(class_map)
        print(f'round {round_no}: T size={len(T)}')

        # group unmatched yarn by key bucket of mojmap candidates
        key_bucket = defaultdict(list)
        for mfqn in midx:
            if mfqn in claimed:
                continue
            mf = midx[mfqn]
            key_bucket[(tuple(mf['arities']), tuple(mf['ctor_arities']), mf['field_count'])].append(mfqn)

        results = []
        for y in unmatched_yarn:
            yf = yidx[y]
            key = (tuple(yf['arities']), tuple(yf['ctor_arities']), yf['field_count'])
            cands = [m for m in key_bucket.get(key, []) if m not in claimed]
            if not cands:
                continue
            ysim = set(ry[y]['simples'])
            ysim_n = {T.get(s, s) for s in ysim}
            ynorm_sigs = set()
            for m in SIG_CACHE[y]:
                ret, pts, static = m
                ynorm_sigs.add((norm_type(ret, T), tuple(norm_type(p, T) for p in pts), static))
            scored = []
            for mfqn in cands:
                msim = set(rm[mfqn]['simples'])
                inter = len(ysim_n & msim)
                union = len(ysim_n | msim)
                ref_jac = inter / max(1, union) if union else 0.0
                msig_set = SIG_CACHE_M[mfqn]
                agree = len(ynorm_sigs & msig_set) / max(1, len(ynorm_sigs | msig_set)) if ynorm_sigs else 0.0
                # enum feature
                ye, me = set(yidx[y]['enum_consts']), set(midx[mfqn]['enum_consts'])
                enum_s = 0.0
                if ye or me:
                    enum_s = 6.0 * (len(ye & me) / max(1, len(ye | me)))
                    if ye == me and ye:
                        enum_s += 2.0
                else:
                    enum_s = 0.5
                # arity/field bonuses
                s = 0.0
                s += ref_jac * 10.0
                s += agree * 8.0
                s += enum_s
                if yf['field_count'] == midx[mfqn]['field_count']:
                    s += 1.0
                if yf['ctor_arities'] == midx[mfqn]['ctor_arities']:
                    s += 1.0
                scored.append((s, mfqn))
            scored.sort(key=lambda x: -x[0])
            results.append((y, scored))

        # resolve: process best-confidence first
        results.sort(key=lambda r: (-r[1][0][0], len(r[1])))
        for y, scored in results:
            if not scored:
                continue
            best_s, best_m = scored[0]
            if len(scored) > 1 and scored[1][0] >= best_s:
                continue  # ambiguous tie
            if best_s < 4.0 and not yidx[y]['strings']:
                continue
            class_map[y] = best_m
            claimed.add(best_m)
            added = True

        unmatched_yarn = [y for y in unmatched_yarn if y not in class_map]

    # final: handle package-info by package
    pkg_map = {}
    for y, m in class_map.items():
        if y.endswith('.package-info') or simple(y) in ('package-info',):
            continue
        ypkg = y.rsplit('.', 1)[0]
        mpkg = m.rsplit('.', 1)[0]
        if ypkg not in pkg_map:
            pkg_map[ypkg] = set()
        pkg_map[ypkg].add(mpkg)
    pkg_map = {k: v for k, v in pkg_map.items() if len(v) == 1}
    pkg_map = {k: next(iter(v)) for k, v in pkg_map.items()}

    pkg_matched = 0
    for y in unmatched_yarn:
        if y.endswith('.package-info'):
            ypkg = y.rsplit('.', 1)[0]
            if ypkg in pkg_map:
                class_map[y] = pkg_map[ypkg] + '.package-info'
                claimed.add(class_map[y])
                pkg_matched += 1

    unmatched_yarn = [y for y in unmatched_yarn if y not in class_map]
    unclaimed_moj = [m for m in midx if m not in claimed]

    print(f'\nFinal: matched={len(class_map)} unmatched_yarn={len(unmatched_yarn)} unclaimed_moj={len(unclaimed_moj)}')

    with open(os.path.join(HERE, 'class_map.json'), 'w', encoding='utf-8') as f:
        json.dump(class_map, f, indent=0, sort_keys=True)

    with open(os.path.join(HERE, 'class_map_review.txt'), 'w', encoding='utf-8') as f:
        f.write('=== UNMATCHED YARN ===\n')
        for y in sorted(unmatched_yarn):
            f.write(y + '\n')
        f.write('\n=== MOJMAP UNCLAIMED (sample/full) ===\n')
        for m in sorted(unclaimed_moj):
            f.write(m + '\n')
    print('wrote class_map.json + class_map_review.txt')

# lazy sig caches populated in main via sigparse
SIG_CACHE = {}
SIG_CACHE_M = {}

if __name__ == '__main__':
    import sigparse as sp
    YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
    MOJ = r'D:/aiminecraftdev/minecraft1.21.1mojmap'
    for yfqn in list(json.load(open(os.path.join(HERE,'yarn_index.json'),encoding='utf-8'))):
        p = os.path.join(YARN, yfqn.replace('.', os.sep) + '.java')
        if os.path.exists(p):
            SIG_CACHE[yfqn] = sp.extract(p)['sigs']
    for mfqn in list(json.load(open(os.path.join(HERE,'moj_index.json'),encoding='utf-8'))):
        p = os.path.join(MOJ, mfqn.replace('.', os.sep) + '.java')
        if os.path.exists(p):
            r = sp.extract(p)
            SIG_CACHE_M[mfqn] = set(r['sigs'])
    print('sig caches loaded:', len(SIG_CACHE), len(SIG_CACHE_M))
    main()
