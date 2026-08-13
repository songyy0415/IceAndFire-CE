#!/usr/bin/env python3
"""Loose matching: for given yarn classes, score ALL unclaimed mojmap classes
by reference-profile Jaccard (normalized) + signature agreement + enum + arity
closeness. Reports top candidates per yarn class for manual verification.
"""
import os, json, sys, re
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
MOJ = r'D:/aiminecraftdev/minecraft1.21.1mojmap'

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def simple(fqn):
    return fqn.rsplit('.', 1)[-1]

def build_translation(class_map):
    d = {}
    for y, m in class_map.items():
        d.setdefault(simple(y), set()).add(simple(m))
    return {ys: next(iter(s)) for ys, s in d.items() if len(s) == 1}

def norm_type(t, T):
    def repl(m):
        return T.get(m.group(0), m.group(0))
    return re.sub(r'\b[A-Z][A-Za-z0-9_$]*\b', repl, t)

def arity_close(a, b):
    # fraction of b's arity multiset covered by a
    from collections import Counter
    ca, cb = Counter(a), Counter(b)
    inter = sum((ca & cb).values())
    union = sum((ca | cb).values())
    return inter / max(1, union)

def main():
    yidx = load('yarn_index.json')
    midx = load('moj_index.json')
    ry = load('ref_yarn.json')
    rm = load('ref_moj.json')
    cm = load('class_map.json')
    import sigparse as sp

    SIG_Y = {}
    SIG_M = {}
    for yfqn in list(yidx):
        p = os.path.join(YARN, yfqn.replace('.', os.sep) + '.java')
        if os.path.exists(p):
            SIG_Y[yfqn] = sp.extract(p)['sigs']
    for mfqn in list(midx):
        p = os.path.join(MOJ, mfqn.replace('.', os.sep) + '.java')
        if os.path.exists(p):
            SIG_M[mfqn] = set(sp.extract(p)['sigs'])

    claimed = set(cm.values())
    targets = [t for t in sys.argv[1:] if t]

    for y in targets:
        T = build_translation(cm)
        ysim = set(ry.get(y, {}).get('simples', []))
        ysim_n = {T.get(s, s) for s in ysim}
        ynorm_sigs = set()
        for ret, pts, static in SIG_Y.get(y, []):
            ynorm_sigs.add((norm_type(ret, T), tuple(norm_type(p, T) for p in pts), static))
        yf = yidx[y]
        scored = []
        for mfqn in midx:
            if mfqn in claimed:
                continue
            msim = set(rm.get(mfqn, {}).get('simples', []))
            union = len(ysim_n | msim)
            ref_jac = len(ysim_n & msim) / max(1, union) if union else 0.0
            msig = SIG_M.get(mfqn, set())
            agree = len(ynorm_sigs & msig) / max(1, len(ynorm_sigs | msig)) if ynorm_sigs else 0.0
            ye, me = set(yf['enum_consts']), set(midx[mfqn]['enum_consts'])
            enum_s = 0.0
            if ye or me:
                enum_s = 6.0 * (len(ye & me) / max(1, len(ye | me)))
            else:
                enum_s = 0.5
            ac = arity_close(yf['arities'], midx[mfqn]['arities'])
            s = ref_jac * 10.0 + agree * 8.0 + enum_s + ac * 3.0
            scored.append((s, mfqn))
        scored.sort(key=lambda x: -x[0])
        print(f'\n===== {y} (yarn) strings={len(yf["strings"])} enum={yf["enum_consts"]} ====')
        for s, m in scored[:6]:
            print(f'   {s:6.2f}  {m}')

if __name__ == '__main__':
    main()
