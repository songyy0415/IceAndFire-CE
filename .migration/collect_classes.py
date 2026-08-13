#!/usr/bin/env python3
"""Collect the set of Yarn classes referenced by the project.

Outputs needed_classes.txt (one Yarn FQN per line) and yarn_class_index.json
(path -> FQN for every Yarn source class).
"""
import os, re, json, glob

YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'needed_classes.txt')

def rel_fqn(path, root):
    rel = os.path.relpath(path, root)
    if rel.endswith('.java'):
        rel = rel[:-5]
    return rel.replace(os.sep, '.')

# 1. Index all yarn classes
yarn_index = {}
for root_dir in (os.path.join(YARN, 'net'), os.path.join(YARN, 'com')):
    for path in glob.glob(root_dir + '/**/*.java', recursive=True):
        fqn = rel_fqn(path, YARN)
        yarn_index[fqn] = path
print('Yarn classes indexed:', len(yarn_index))

# 2. Collect imports + FQN refs from project
imports = set()
fqn_refs = set()
for proj_dir in PROJECT_DIRS:
    for path in glob.glob(proj_dir + '/**/*.java', recursive=True):
        try:
            with open(path, encoding='utf-8', errors='replace') as f:
                text = f.read()
        except Exception:
            continue
        for m in re.finditer(r'^\s*import\s+(?:static\s+)?([A-Za-z0-9_.]+?)\s*;', text, re.M):
            imports.add(m.group(1))
        for m in re.finditer(r'\bnet\.minecraft\.[A-Za-z0-9_.]+\b', text):
            fqn_refs.add(m.group(0))

# 3. Expand imports to class list
needed = set()
wildcards = set()   # package wildcards
for imp in imports:
    if imp.endswith('.*'):
        pkg = imp[:-2]
        wildcards.add(pkg)
        # all classes directly in this package
        prefix = pkg.replace('.', '/')
        for fqn, path in yarn_index.items():
            if fqn.startswith(pkg + '.') and '$' not in fqn and fqn.count('.') == pkg.count('.') + 1:
                needed.add(fqn)
    elif imp.startswith('net.minecraft.') or imp.startswith('com.mojang.'):
        needed.add(imp)

# 4. FQN refs: keep only ones that resolve to an indexed class (strip trailing )
for ref in fqn_refs:
    # strip any trailing chars that aren't part of a class (rare)
    r = ref.rstrip('.')
    # direct class hit
    if r in yarn_index:
        needed.add(r)
    else:
        # maybe it's pkg + class where class is simple: try longest prefix that exists
        parts = r.split('.')
        for i in range(len(parts), 0, -1):
            cand = '.'.join(parts[:i])
            if cand in yarn_index:
                needed.add(cand)
                break

needed = {n for n in needed if not n.endswith('.')}
# sort for determinism
needed_sorted = sorted(needed)
with open(OUT, 'w', encoding='utf-8') as f:
    f.write('\n'.join(needed_sorted) + '\n')

print('Needed classes:', len(needed_sorted))
print('Wildcard packages:', sorted(wildcards))
missing = [n for n in needed_sorted if n not in yarn_index]
print('Needed classes NOT found in yarn source:', len(missing))
for m in missing[:60]:
    print('   MISSING:', m)
