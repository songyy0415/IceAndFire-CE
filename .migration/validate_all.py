#!/usr/bin/env python3
"""Validation master script.

Reads the mapping tables (never writes them) and produces the full validation
dataset needed for the report:
  - class-level coverage for project-needed classes
  - member-level coverage for project-used members
  - consistency filtered to project-needed classes
  - descriptor coverage
  - unmatched classification
Outputs JSON report files used by later report-writing.
"""
import os, json, re, glob
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']
MOJ = r'D:/aiminecraftdev/minecraft1.21.1mojmap'
YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def base_name(y):
    if '(' in y:
        return y.split('(')[0]
    if ' : ' in y:
        return y.split(' : ')[0]
    return y

# ---------------------------------------------------------------- project scan
def project_files():
    for d in PROJECT_DIRS:
        for p in glob.glob(d + '/**/*.java', recursive=True):
            yield p

def collect_project_imports():
    """Map simple-name -> set of FQNs imported (net.minecraft / com.mojang)."""
    imp = defaultdict(set)
    files_using = defaultdict(set)
    for p in project_files():
        with open(p, encoding='utf-8', errors='replace') as f:
            t = f.read()
        for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', t, re.M):
            fqn = m.group(1)
            if fqn.startswith(('net.minecraft.', 'com.mojang.')) and not fqn.endswith('.*'):
                imp[fqn.rsplit('.', 1)[-1]].add(fqn)
                files_using[fqn].add(os.path.basename(p))
    return imp, files_using

def collect_project_class_refs():
    """All net.minecraft/com.mojang class FQNs referenced (imports + inline)."""
    refs = set()
    for p in project_files():
        with open(p, encoding='utf-8', errors='replace') as f:
            t = f.read()
        t = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', t, flags=re.S)
        for m in re.finditer(r'\b(?:net\.minecraft|com\.mojang)\.[A-Za-z0-9_.]+\b', t):
            refs.add(m.group(0).rstrip('.'))
    return sorted(r for r in refs if r.split('.')[-1][0].isupper())

# ---------------------------------------------------------------- used members
def collect_used_members():
    """For each project file, the member names used against each imported class.
    Over-approximation: any identifier in the file that equals a member name of
    an imported class counts. Never misses a usage (safe for validation)."""
    mm = load('member_map.json')
    cls_members = {}
    for cls, info in mm.items():
        names = set()
        for mem in info['members']:
            yn = mem['yarn']
            if mem['kind'] == 'method':
                b = yn.split('(')[0]
                if b and not b[0].isupper():
                    names.add(b)
            elif mem['kind'] == 'field':
                b = yn.split(' : ')[0]
                if b:
                    names.add(b)
            elif mem['kind'] in ('enum', 'record'):
                if yn:
                    names.add(yn)
        cls_members[cls] = names

    used = defaultdict(set)
    IDENT = re.compile(r'\b([A-Za-z_$][A-Za-z0-9_$]*)\b')
    for p in project_files():
        with open(p, encoding='utf-8', errors='replace') as f:
            t = f.read()
        t = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', t, flags=re.S)
        idents = set(IDENT.findall(t))
        for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', t, re.M):
            fqn = m.group(1)
            if fqn not in cls_members:
                continue
            used[fqn].update(idents & cls_members[fqn])
    return used, cls_members

def main():
    cm = load('class_map.json')
    mm = load('member_map.json')
    report = {}

    # ---------- classes referenced by project ----------
    refs = collect_project_class_refs()
    needed = [r for r in refs if r.startswith('net.minecraft.')]
    report['ref_classes_total'] = len(refs)
    report['ref_minecraft'] = len(needed)
    report['ref_class_mapped'] = sum(1 for r in needed if r in cm)
    missing_cls = [r for r in needed if r not in cm]
    report['ref_class_missing'] = missing_cls
    # also: mojmap classes must exist as .java files
    mapped_to_file = {}
    for r in needed:
        if r in cm:
            moj = cm[r]
            p = os.path.join(MOJ, moj.replace('.', os.sep) + '.java')
            mapped_to_file[r] = (moj, os.path.exists(p))
    report['mapped_class_missing_file'] = [
        (r, moj) for r, (moj, ok) in mapped_to_file.items() if not ok
    ]
    # com.mojang refs that need no remap
    com_mojang = [r for r in refs if r.startswith('com.mojang.')]
    report['ref_com_mojang'] = len(com_mojang)

    # ---------- member usage vs mapping table ----------
    used, cls_members = collect_used_members()
    # only classes that are in the needed set
    used_needed = {c: s for c, s in used.items() if c in needed}
    total_used_members = sum(len(s) for s in used_needed.values())
    report['used_classes_with_members'] = len(used_needed)

    # per used member: status in the table
    member_coverage = {}   # (class, member) -> status
    for cls, used_names in used_needed.items():
        info = mm.get(cls)
        if not info:
            for n in used_names:
                member_coverage[(cls, n)] = 'CLASS_UNMAPPED'
            continue
        # build yarn-name -> status lookup
        name_status = {}
        for mem in info['members']:
            yn = base_name(mem['yarn'])
            name_status[yn] = mem['status']
        for n in used_names:
            st = name_status.get(n, 'NOT_IN_TABLE')
            member_coverage[(cls, n)] = st
    # count coverage by status
    status_cov = defaultdict(int)
    for (cls, n), st in member_coverage.items():
        status_cov[st] += 1
    report['member_usage_total'] = len(member_coverage)
    report['member_usage_by_status'] = dict(status_cov)

    # unmatched among project-used
    used_unmatched = [(c, n) for (c, n), st in member_coverage.items()
                      if st in ('unmatched', 'NOT_IN_TABLE', 'CLASS_UNMAPPED')]
    report['used_unmatched'] = sorted(used_unmatched)
    report['used_unmatched_count'] = len(used_unmatched)

    # ---------- consistency filtered to needed classes ----------
    cons = []
    for cls, info in mm.items():
        if cls not in needed:
            continue
        for kind in ('method', 'field', 'enum', 'record'):
            seen = defaultdict(set)
            for m in info['members']:
                if m['kind'] != kind or m['status'] == 'unmatched' or not m['moj']:
                    continue
                seen[base_name(m['yarn'])].add(base_name(m['moj']))
            for yn, mns in seen.items():
                if len(mns) > 1:
                    cons.append((cls, kind, yn, sorted(mns)))
    report['consistency_conflicts_needed'] = cons
    report['consistency_conflicts_needed_count'] = len(cons)

    # ---------- descriptor coverage ----------
    # For every method signature in the table for needed classes, every param/ret
    # type token must either be a primitive/java/lang, or resolve via class_map
    # (or be an inner class / com.mojang unchanged). Collect unresolvable tokens.
    def classify_type(tok):
        """Return 'primitive'|'lang'|'mapped'|'unmapped'|'generic/other'"""
        t = tok.strip()
        if t in ('int','double','float','boolean','long','short','byte','char','void'):
            return 'primitive'
        if t.startswith('java.'):
            return 'lang'
        if t.startswith('com.mojang.'):
            return 'mapped' if t in cm or True else 'mapped'  # com.mojang unchanged
        if t.startswith('net.minecraft.'):
            return 'mapped' if t in cm else 'unmapped'
        if '.' in t:  # inner class like net.minecraft...A.B or mapped-inner
            return 'inner'
        return 'other'
    unresolvable = []
    all_tokens = set()
    for cls, info in mm.items():
        if cls not in needed:
            continue
        for m in info['members']:
            if m['kind'] != 'method':
                continue
            sig = m['moj']
            if not sig or m['status'] == 'unmatched':
                continue
            mname = sig.split('(')[0]
            args = sig[sig.find('(')+1:sig.rfind(')')]
            toks = []
            if args.strip():
                toks += [a.strip() for a in args.split(',')]
            # return type not in signature string (moj sig is name(params) only)
            for tok in toks:
                c = classify_type(tok)
                all_tokens.add(tok)
                if c == 'unmapped':
                    unresolvable.append((cls, m['yarn'], m['moj'], tok))
    report['descriptor_unresolvable'] = unresolvable
    report['descriptor_unresolvable_count'] = len(unresolvable)

    with open(os.path.join(HERE, 'validate_report.json'), 'w', encoding='utf-8') as f:
        json.dump(report, f, indent=1, ensure_ascii=False, default=list)

    # print summary
    print('=== ② COMPLETENESS (project class refs) ===')
    print(f'  referenced classes total: {report["ref_classes_total"]}')
    print(f'  net.minecraft refs: {report["ref_minecraft"]}, mapped: {report["ref_class_mapped"]}, missing: {len(report["ref_class_missing"])}')
    print(f'  com.mojang refs (no remap): {report["ref_com_mojang"]}')
    print('  mapped-but-no-mojmap-file:', len(report['mapped_class_missing_file']))
    for r, mj in report['mapped_class_missing_file']:
        print(f'    {r} -> {mj} (no file)')
    print('=== member usage coverage ===')
    print(f'  used member instances: {report["member_usage_total"]}')
    for st, n in sorted(status_cov.items()):
        print(f'    {st}: {n}')
    print(f'  used-but-unmatched/NOT_IN_TABLE: {report["used_unmatched_count"]}')
    print('=== ⑤ consistency (needed classes only) ===')
    print(f'  conflicts: {report["consistency_conflicts_needed_count"]}')
    for cls, kind, yn, mns in cons[:30]:
        print(f'    {cls} :: {kind} :: {yn} -> {mns}')
    print('=== ⑦ descriptor ===')
    print(f'  unresolvable type tokens: {report["descriptor_unresolvable_count"]}')
    for x in unresolvable[:20]:
        print(f'    {x}')

if __name__ == '__main__':
    main()
