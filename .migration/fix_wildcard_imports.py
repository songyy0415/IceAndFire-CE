#!/usr/bin/env python3
"""Fix leftover Yarn wildcard imports after migrateMappings.

migrateMappings converts explicit imports but leaves wildcard imports like
`import net.minecraft.block.*;` (it can't know which classes the wildcard
resolves to). This script:

1. For every project file with a Yarn wildcard import `net.minecraft.<pkg>.*`,
2. Removes that wildcard line,
3. Scans the file for identifiers that are Yarn class simple-names in that package,
4. Maps each to the MojMap FQN via class_map.json (verified mapping),
5. Inserts the correct MojMap import for each used class.

Only classes actually referenced in the file are imported. Idempotent.
"""
import os, re, glob, json

HERE = os.path.dirname(os.path.abspath(__file__))
YARN = r'D:/aiminecraftdev/minecraft 1.21.1 yarn'
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src/main/java', r'D:/IceAndFire-CE/fabric/src/main/java']

cm = json.load(open(os.path.join(HERE, 'class_map.json'), encoding='utf-8'))

# ---- build yarn package -> { simple name -> mojmap fqn } ----
pkg_classes = {}   # 'net.minecraft.block' -> { 'Block': 'net.minecraft.world.level.block.Block', ... }
for yfqn, mfqn in cm.items():
    if not yfqn.startswith('net.minecraft.'):
        continue
    pkg, simple = yfqn.rsplit('.', 1)
    # skip inner classes (already covered by outer in source scan)
    pkg_classes.setdefault(pkg, {})[simple] = mfqn

WILDCARD_RE = re.compile(r'^import\s+(net\.minecraft\.[a-z0-9_.]+?)\.\*\s*;', re.M)

def identifier_set(text):
    t = re.sub(r'//[^\n]*|/\*.*?\*/|"[^"]*"|\'[^\']*\'', ' ', text)
    return set(re.findall(r'\b[A-Z][A-Za-z0-9_$]*\b', t))

def main():
    total_fixed = 0
    for d in PROJECT_DIRS:
        for path in glob.glob(d + '/**/*.java', recursive=True):
            with open(path, encoding='utf-8') as f:
                text = f.read()
            wildcards = WILDCARD_RE.findall(text)
            if not wildcards:
                continue
            idents = identifier_set(text)
            # imports currently present (to avoid duplicate class imports)
            existing_imports = set(re.findall(r'^import\s+([A-Za-z0-9_.]+?)\s*;', text, re.M))
            existing_simple = set()
            for imp in existing_imports:
                if not imp.startswith(('net.minecraft.', 'com.mojang.')):
                    continue
                existing_simple.add(imp.rsplit('.', 1)[-1])
            new_imports = {}   # simple -> mojmap fqn
            removed = []
            for pkg in wildcards:
                classes = pkg_classes.get(pkg, {})
                for simple, mfqn in classes.items():
                    if simple in idents and simple not in existing_simple:
                        # only import if not already imported under same simple name
                        new_imports[simple] = mfqn
                removed.append(pkg)
            if not new_imports and not removed:
                continue
            # remove wildcard lines
            new_text = WILDCARD_RE.sub('', text)
            # insert new imports after the last existing import block
            if new_imports:
                insert_lines = sorted(f'import {f};\n' for f in new_imports.values())
                lines = new_text.splitlines(keepends=True)
                last_import_idx = -1
                for i, ln in enumerate(lines):
                    if ln.startswith('import '):
                        last_import_idx = i
                if last_import_idx >= 0:
                    # insert after last import, preserving newline
                    lines.insert(last_import_idx + 1, ''.join(insert_lines))
                else:
                    # prepend after package line
                    for i, ln in enumerate(lines):
                        if ln.startswith('package '):
                            lines.insert(i + 1, '\n' + ''.join(insert_lines))
                            break
                new_text = ''.join(lines)
            if new_text != text:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_text)
                total_fixed += 1
                print(f'FIXED {path}: removed {removed}, added {sorted(new_imports)}')
    print(f'\ntotal files fixed: {total_fixed}')

if __name__ == '__main__':
    main()
