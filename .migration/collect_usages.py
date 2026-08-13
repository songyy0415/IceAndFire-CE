#!/usr/bin/env python3
"""Extract per-class member usages from the project source.

For each project Java file:
  - resolve imports (simple name -> yarn FQN), incl. wildcard expansion
  - find `SimpleName.member` static-qualified accesses and enum uses
  - also collect method call names and field access names as candidates
Output: usage_members.json : { yarn_fqn : { 'static': set(member), 'all': set(member) } }
"""
import os, re, json, glob

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']
YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def main():
    yidx = load('yarn_index.json')
    # build simple -> set of yarn FQNs
    simple_index = {}
    for fqn in yidx:
        simple_index.setdefault(fqn.rsplit('.', 1)[-1], set()).add(fqn)

    usage = {}   # yarn_fqn -> {'static': set, 'calls': set}
    MEMBER_ACCESS_RE = re.compile(r'\b([A-Za-z_$][A-Za-z0-9_$]*)\.([A-Za-z_$][A-Za-z0-9_$]*)\b')
    IDENT_RE = re.compile(r'\b([A-Za-z_$][A-Za-z0-9_$]*)\b')

    for proj_dir in PROJECT_DIRS:
        for path in glob.glob(proj_dir + '/**/*.java', recursive=True):
            with open(path, encoding='utf-8', errors='replace') as f:
                text = f.read()
            # strip comments
            text_nc = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', text, flags=re.S)
            imports = {}
            for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', text, re.M):
                imp = m.group(1)
                if imp.startswith('net.minecraft.'):
                    if imp.endswith('.*'):
                        pkg = imp[:-2]
                        for fqn in yidx:
                            if fqn.startswith(pkg + '.') and '$' not in fqn and fqn.count('.') == pkg.count('.') + 1:
                                imports[fqn.rsplit('.',1)[-1]] = fqn
                    else:
                        imports[imp.rsplit('.', 1)[-1]] = imp
            # static-qualified accesses
            for m in MEMBER_ACCESS_RE.finditer(text_nc):
                owner, member = m.group(1), m.group(2)
                if owner in imports:
                    fqn = imports[owner]
                    usage.setdefault(fqn, {'static': set(), 'calls': set()})
                    usage[fqn]['static'].add(member)
    # total
    tot_static = sum(len(v['static']) for v in usage.values())
    print('classes with static usages:', len(usage))
    print('total static-qualified member usages:', tot_static)
    with open(os.path.join(HERE, 'usage_members.json'), 'w', encoding='utf-8') as f:
        json.dump({k: {'static': sorted(v['static'])} for k, v in usage.items()}, f)
    print('wrote usage_members.json')

if __name__ == '__main__':
    main()
