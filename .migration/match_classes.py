#!/usr/bin/env python3
"""Match Yarn classes to MojMap classes by fingerprint (round 1: string-based).

Round 1 buckets by exact string-set; scores candidates on:
  string Jaccard (heavy), enum-const match, arity match, field count, ctors.
Outputs class_map.json (round-1 partial), class_map_review.txt.
"""
import os, json
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def score(yf, mf):
    s = 0.0
    ys = set(yf['strings']); ms = set(mf['strings'])
    if ys or ms:
        s += 10.0 * (len(ys & ms) / max(1, len(ys | ms)))
        s += 1.0 * (1 - abs(len(ys) - len(ms)) / max(1, max(len(ys), len(ms))))
    # enum
    ye, me = set(yf['enum_consts']), set(mf['enum_consts'])
    if ye or me:
        inter = len(ye & me); union = len(ye | me)
        s += 6.0 * (inter / max(1, union))
        if ye == me and ye:
            s += 2.0
    else:
        s += 1.0  # both non-enums (mild preference)
    if yf['arities'] == mf['arities']:
        s += 5.0
    if yf['field_count'] == mf['field_count']:
        s += 2.0
    if yf['ctor_arities'] == mf['ctor_arities']:
        s += 2.0
    return s

def main():
    yidx = load('yarn_index.json')
    midx = load('moj_index.json')

    str_bucket = defaultdict(list)
    for mfqn, mf in midx.items():
        if mf['strings']:
            str_bucket[tuple(mf['strings'])].append(mfqn)

    tasks = []
    for yfqn, yf in yidx.items():
        if yf['strings']:
            cands = str_bucket.get(tuple(yf['strings']), [])
        else:
            cands = []
        tasks.append((yfqn, cands))

    mapping = {}
    claimed = set()
    ambiguous = []
    unmatched = []

    tasks.sort(key=lambda t: (len(t[1]) != 1, len(t[1])))

    for yfqn, cands in tasks:
        if not cands:
            unmatched.append(yfqn)
            continue
        yf = yidx[yfqn]
        scored = []
        for mfqn in cands:
            if mfqn in claimed:
                continue
            scored.append((score(yf, midx[mfqn]), mfqn))
        scored.sort(key=lambda x: -x[0])
        if not scored:
            unmatched.append(yfqn)
            continue
        best_s, best_m = scored[0]
        if len(scored) > 1 and scored[1][0] >= best_s:
            ambiguous.append((yfqn, [(s, m) for s, m in scored[:4]]))
            continue
        mapping[yfqn] = best_m
        claimed.add(best_m)

    unclaimed = [m for m in midx if m not in claimed]
    print('Round1 matched:', len(mapping), 'unmatched:', len(unmatched), 'ambiguous:', len(ambiguous), 'moj unclaimed:', len(unclaimed))

    with open(os.path.join(HERE, 'class_map.json'), 'w', encoding='utf-8') as f:
        json.dump(mapping, f, indent=0, sort_keys=True)
    with open(os.path.join(HERE, 'class_map_review.txt'), 'w', encoding='utf-8') as f:
        f.write('=== ROUND1 UNMATCHED (yarn) ===\n')
        for y in sorted(unmatched):
            f.write(y + '\n')
        f.write('\n=== ROUND1 AMBIGUOUS ===\n')
        for y, cands in ambiguous:
            f.write(y + ' -> ' + '; '.join(f'{m}({s:.1f})' for s, m in cands) + '\n')
    print('wrote class_map.json + class_map_review.txt')

if __name__ == '__main__':
    main()
