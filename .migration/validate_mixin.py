#!/usr/bin/env python3
"""Validation ⑥: Mixin check.

Every @Mixin/@Shadow/@Inject/@Invoker/@Accessor target must resolve through the
mapping table. Extract targets from the 10 mixin files and cross-check.
"""
import os, re, json
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
MIXIN_DIR = r'D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/mixin'

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def base_name(y):
    if '(' in y:
        return y.split('(')[0]
    if ' : ' in y:
        return y.split(' : ')[0]
    return y

def main():
    cm = load('class_map.json')
    mm = load('member_map.json')
    mixin_files = [f for f in os.listdir(MIXIN_DIR) if f.endswith('.java')]
    results = []

    def cls_ok(yarn_fqn):
        return yarn_fqn in cm

    def member_status(yarn_cls, yarn_member):
        info = mm.get(yarn_cls)
        if not info:
            return 'CLASS_NOT_IN_TABLE'
        for m in info['members']:
            if base_name(m['yarn']) == yarn_member:
                return m['status']
        return 'MEMBER_NOT_IN_TABLE'

    for fn in sorted(mixin_files):
        path = os.path.join(MIXIN_DIR, fn)
        with open(path, encoding='utf-8') as f:
            t = f.read()
        # @Mixin target
        mixin_m = re.search(r'@Mixin\(\s*(?:value\s*=\s*)?([A-Za-z0-9_.]+)\.class', t)
        mixin_cls = mixin_m.group(1) if mixin_m else '?'
        # resolve class: could be simple name or FQN; find import
        imports = {}
        for m in re.finditer(r'^\s*import\s+([A-Za-z0-9_.]+?)\s*;', t, re.M):
            fqn = m.group(1)
            if fqn.startswith(('net.minecraft.', 'com.mojang.')):
                imports[fqn.rsplit('.', 1)[-1]] = fqn
        mixin_fqn = mixin_cls if '.' in mixin_cls else imports.get(mixin_cls, mixin_cls)

        # @Shadow members
        shadows = []
        shadow_block = ''
        for m in re.finditer(r'@Shadow[^\n]*\n(?:\s*@\w+[^\n]*\n)*\s*(?:public|protected|private|static|final|abstract|native|\?)\s*([A-Za-z_$][A-Za-z0-9_$<>?,\[\] .]*)\s+([A-Za-z_$][A-Za-z0-9_$]*)\s*[=(;]', t):
            shadows.append((m.group(2), m.group(1).strip()))
        # simpler: lines after @Shadow
        lines = t.splitlines()
        for i, line in enumerate(lines):
            if '@Shadow' in line:
                j = i + 1
                while j < len(lines) and (lines[j].strip().startswith('@') or not lines[j].strip()):
                    j += 1
                if j < len(lines):
                    decl = lines[j].strip()
                    nm = re.search(r'\b([A-Za-z_$][A-Za-z0-9_$]*)\s*(?:;|=|\()', decl)
                    if nm and not decl.startswith('import'):
                        shadows.append((nm.group(1), decl))

        # @Inject methods
        injects = []
        for m in re.finditer(r'@Inject\s*\(\s*method\s*=\s*"([^"]+)"', t):
            injects.append(m.group(1))
        for m in re.finditer(r'@Inject\s*\(\s*method\s*=\s*\{[^}]*"([^"]+)"', t):
            injects.append(m.group(1))

        # INVOKE targets inside @At
        invokes = []
        for m in re.finditer(r'target\s*=\s*"(L[^;]+;)V?"', t):
            desc = m.group(1)
            invokes.append(desc)

        results.append({
            'file': fn, 'mixin_class': mixin_fqn, 'mixin_cls_mapped': cm.get(mixin_fqn),
            'shadows': shadows, 'injects': injects, 'invokes': invokes
        })

    # ---- report
    print('=== ⑥ MIXIN CHECK ===')
    problems = []
    for r in results:
        print(f'\n## {r["file"]}')
        mc = r['mixin_class']
        print(f'  @Mixin {mc} -> {r["mixin_cls_mapped"]}')
        if r['mixin_cls_mapped'] is None:
            problems.append((r['file'], 'MIXIN CLASS', mc))
        for nm, decl in r['shadows']:
            st = member_status(mc, nm)
            flag = 'OK' if st in ('exact','name','verified','order','value','type','name+params','params','arity','bypos') else '!!'
            print(f'  @Shadow {nm} [{decl}] -> {st} {flag}')
            if flag == '!!':
                problems.append((r['file'], 'SHADOW', f'{mc}::{nm} ({decl})'))
        for m in r['injects']:
            mname = m.split('(')[0]
            st = member_status(mc, mname)
            flag = 'OK' if st in ('exact','name','verified','order','value','type','name+params','params','arity','bypos') else '!!'
            print(f'  @Inject {m} -> {st} {flag}')
            if flag == '!!':
                problems.append((r['file'], 'INJECT', f'{mc}::{m}'))
        for d in r['invokes']:
            print(f'  INVOKE target {d}')
            # parse descriptor class
            cls_part = d[1:].split(';')[0]
            ycls = cls_part.replace('/', '.')
            ymem = d.split(';')[1].split('(')[0] if ';' in d else '?'
            st = member_status(ycls, ymem) if ycls in cm else ('CLASS_UNMAPPED' if ycls.startswith(('net.minecraft.','com.mojang.')) else 'external')
            flag = 'OK' if st != 'MEMBER_NOT_IN_TABLE' and st != 'CLASS_UNMAPPED' and st != 'CLASS_NOT_IN_TABLE' else '!!'
            print(f'    -> {ycls}::{ymem} status={st} {flag}')
            if flag == '!!':
                problems.append((r['file'], 'INVOKE', f'{ycls}::{ymem} ({d})'))

    print('\n=== PROBLEMS ===')
    for f, kind, what in problems:
        print(f'  {f} :: {kind} :: {what}')
    print(f'total problems: {len(problems)}')
    json.dump(results, open(os.path.join(HERE, 'validate_mixin.json'), 'w', encoding='utf-8'), indent=1, ensure_ascii=False)

if __name__ == '__main__':
    main()
