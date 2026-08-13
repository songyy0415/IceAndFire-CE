#!/usr/bin/env python3
"""Align members (methods/fields/enums/records/ctors) between Yarn and MojMap
for every matched class, using normalized signatures + order-based disambiguation.

Output: member_map.json : { yarn_fqn : { 'moj': moj_fqn, 'members': [ ... ] } }
"""
import os, json, re
from collections import defaultdict
import memberparse as mp

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

def sig(name, params):
    return f'{name}({", ".join(params)})'

def align_file(yarn_path, moj_path, T):
    y = mp.extract(yarn_path)
    m = mp.extract(moj_path)
    members = []

    # ================= METHODS =================
    y_methods = list(y['methods'])     # (name, static, ret, [params])
    m_methods = list(m['methods'])
    ny, nm = len(y_methods), len(m_methods)

    # indexes over m_methods indices
    idx_fullname = defaultdict(list)   # (name, static, arity, nparams, nret) -> [mi]
    idx_nameparams = defaultdict(list)
    idx_exact = defaultdict(list)
    idx_params = defaultdict(list)
    idx_arity = defaultdict(list)
    for mi, (mname, mstatic, mret, mparams) in enumerate(m_methods):
        nr = norm_type(mret, T)
        np_ = tuple(norm_type(p, T) for p in mparams)
        idx_fullname[(mname, mstatic, len(mparams), np_, nr)].append(mi)
        idx_nameparams[(mname, mstatic, len(mparams), np_)].append(mi)
        idx_exact[(mstatic, len(mparams), np_, nr)].append(mi)
        idx_params[(mstatic, len(mparams), np_)].append(mi)
        idx_arity[(mstatic, len(mparams))].append(mi)

    used_m = set()   # moj method indices already matched
    def pick(cands):
        fresh = [c for c in cands if c not in used_m]
        return (fresh[0], fresh) if len(fresh) == 1 else (None, fresh)

    # matched[m mi] = (yi, status)
    match_m = {}
    # status per yarn method
    y_status = [None] * ny
    for yi, (yname, ystatic, yret, yparams) in enumerate(y_methods):
        nr = norm_type(yret, T)
        np_ = tuple(norm_type(p, T) for p in yparams)
        best = None; status = None
        cand, fresh = pick(idx_fullname.get((yname, ystatic, len(yparams), np_, nr), []))
        if cand is not None:
            best, status = cand, 'exact'
        else:
            cand2, fresh2 = pick(idx_nameparams.get((yname, ystatic, len(yparams), np_), []))
            if cand2 is not None:
                best, status = cand2, 'name+params'
            else:
                cand3, fresh3 = pick(idx_exact.get((ystatic, len(yparams), np_, nr), []))
                if cand3 is not None:
                    best, status = cand3, 'exact'
                else:
                    cand4, fresh4 = pick(idx_params.get((ystatic, len(yparams), np_), []))
                    if cand4 is not None:
                        best, status = cand4, 'params'
                    else:
                        cand5, fresh5 = pick(idx_arity.get((ystatic, len(yparams)), []))
                        if cand5 is not None:
                            best, status = cand5, 'arity'
        if best is not None:
            match_m[best] = yi
            used_m.add(best)
            y_status[yi] = status

    # Phase 2: order-based pairing for remaining unmatched methods.
    # Group by (static, arity); pair in declaration order, requiring the
    # normalized full signature to agree (protects against order drift).
    remaining_y = [yi for yi in range(ny) if y_status[yi] is None]
    def norm_key(static, params, ret):
        return (static, len(params), tuple(norm_type(p, T) for p in params), norm_type(ret, T))
    y_sigs = [norm_key(ym[1], ym[3], ym[2]) for ym in y_methods]
    m_sigs = [norm_key(mm[1], mm[3], mm[2]) for mm in m_methods]
    for key in sorted({(ym[1], len(ym[3])) for ym in y_methods}):
        y_list = [yi for yi in range(ny) if y_status[yi] is None and (y_methods[yi][1], len(y_methods[yi][3])) == key]
        m_list = [mi for mi in range(nm) if mi not in used_m and (m_methods[mi][1], len(m_methods[mi][3])) == key]
        # signature-confirmed positional pairing
        for j, yi in enumerate(y_list):
            if j >= len(m_list):
                break
            mi = m_list[j]
            if y_sigs[yi] == m_sigs[mi]:
                match_m[mi] = yi
                used_m.add(mi)
                y_status[yi] = 'order'

    for yi, (yname, ystatic, yret, yparams) in enumerate(y_methods):
        st = y_status[yi]
        mi = None
        for mm, yy in match_m.items():
            if yy == yi:
                mi = mm
                break
        if mi is None:
            members.append({'kind': 'method', 'yarn': sig(yname, yparams), 'moj': '', 'status': 'unmatched'})
        else:
            mname, mstatic, mret, mparams = m_methods[mi]
            members.append({'kind': 'method', 'yarn': sig(yname, yparams), 'moj': sig(mname, mparams), 'status': st})

    # ================= FIELDS =================
    y_fields = list(y['fields'])      # (name, static, type, initval)
    m_fields = list(m['fields'])
    nfy, nfm = len(y_fields), len(m_fields)

    idx_fname = defaultdict(list)
    idx_fvalue = defaultdict(list)
    idx_fkey = defaultdict(list)
    for fi, (fname, fstatic, ftype, fval) in enumerate(m_fields):
        idx_fname[fname].append(fi)
        if fval is not None:
            idx_fvalue[fval].append(fi)
        idx_fkey[(fstatic, norm_type(ftype, T))].append(fi)

    used_f = set()
    def fpick(cands):
        fresh = [c for c in cands if c not in used_f]
        return (fresh[0], fresh) if len(fresh) == 1 else (None, fresh)

    fmatch_m = {}
    f_status = [None] * nfy
    for fi, (yname, ystatic, ytype, yval) in enumerate(y_fields):
        cand, fresh = fpick(idx_fname.get(yname, []))
        if cand is not None:
            fmatch_m[cand] = fi; used_f.add(cand); f_status[fi] = 'name'
        else:
            cand2, fresh2 = fpick(idx_fvalue.get(yval, [])) if yval is not None else (None, [])
            if cand2 is not None:
                fmatch_m[cand2] = fi; used_f.add(cand2); f_status[fi] = 'value'
            else:
                key = (ystatic, norm_type(ytype, T))
                cand3, fresh3 = fpick(idx_fkey.get(key, []))
                if cand3 is not None:
                    fmatch_m[cand3] = fi; used_f.add(cand3); f_status[fi] = 'type'

    # Phase 2: order-within-(static,type) for fields matched by 'type'
    type_groups = defaultdict(list)
    for fi, st in enumerate(f_status):
        if st == 'type':
            type_groups[(y_fields[fi][1], norm_type(y_fields[fi][2], T))].append(fi)
    for key, f_idxs in type_groups.items():
        for fi in f_idxs:
            for mfi in list(fmatch_m.keys()):
                if fmatch_m[mfi] == fi:
                    used_f.discard(mfi)
                    del fmatch_m[mfi]
        f_sorted = sorted(f_idxs)
        m_cands = [mfi for mfi in range(nfm) if mfi not in used_f and (m_fields[mfi][1], norm_type(m_fields[mfi][2], T)) == key]
        for j, fi in enumerate(f_sorted):
            if j < len(m_cands):
                fmatch_m[m_cands[j]] = fi
                used_f.add(m_cands[j])
                f_status[fi] = 'type-order'

    for fi, (yname, ystatic, ytype, yval) in enumerate(y_fields):
        st = f_status[fi]
        mfi = None
        for mm, yy in fmatch_m.items():
            if yy == fi:
                mfi = mm
                break
        if mfi is None:
            members.append({'kind': 'field', 'yarn': f'{yname} : {ytype}', 'moj': '', 'status': 'unmatched'})
        else:
            mname, mstatic, mtype, mval = m_fields[mfi]
            members.append({'kind': 'field', 'yarn': f'{yname} : {ytype}', 'moj': f'{mname} : {mtype}', 'status': st})

    # ================= ENUM / RECORD / CTOR =================
    y_enum = y['enums']; m_enum = m['enums']
    if y_enum or m_enum:
        for i in range(max(len(y_enum), len(m_enum))):
            ye = y_enum[i] if i < len(y_enum) else ''
            me = m_enum[i] if i < len(m_enum) else ''
            members.append({'kind': 'enum', 'yarn': ye, 'moj': me, 'status': 'exact' if ye == me else 'bypos'})

    y_rec = y['records']; m_rec = m['records']
    if y_rec or m_rec:
        for i in range(max(len(y_rec), len(m_rec))):
            yr = y_rec[i] if i < len(y_rec) else ''
            mr = m_rec[i] if i < len(m_rec) else ''
            members.append({'kind': 'record', 'yarn': yr, 'moj': mr, 'status': 'exact' if yr == mr else 'bypos'})

    y_ctors = y['ctors']; m_ctors = m['ctors']
    for i in range(max(len(y_ctors), len(m_ctors))):
        ya = y_ctors[i] if i < len(y_ctors) else None
        ma = m_ctors[i] if i < len(m_ctors) else None
        members.append({'kind': 'ctor', 'yarn': '' if ya is None else f'<init>({ya})', 'moj': '' if ma is None else f'<init>({ma})', 'status': 'exact' if ya == ma else 'bypos'})

    return members

def main():
    class_map = load('class_map.json')
    T = build_translation(class_map)
    print('translation table size:', len(T))
    out = {}
    n = len(class_map)
    for i, (yarn_fqn, moj_fqn) in enumerate(class_map.items()):
        yp = os.path.join(YARN, yarn_fqn.replace('.', os.sep) + '.java')
        mpth = os.path.join(MOJ, moj_fqn.replace('.', os.sep) + '.java')
        if not os.path.exists(yp) or not os.path.exists(mpth):
            continue
        out[yarn_fqn] = {'moj': moj_fqn, 'members': align_file(yp, mpth, T)}
        if i % 500 == 0:
            print(f'  {i}/{n}', flush=True)
    print('done', len(out))
    with open(os.path.join(HERE, 'member_map.json'), 'w', encoding='utf-8') as f:
        json.dump(out, f)
    print('wrote member_map.json')

if __name__ == '__main__':
    main()
