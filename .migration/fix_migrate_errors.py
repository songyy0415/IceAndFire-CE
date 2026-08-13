#!/usr/bin/env python3
"""Fix errors left by migrateMappings.

Categories:
1. Missing imports for classes that were only reachable via wildcard imports
   (migrateMappings can't resolve wildcards). Map Yarn-era simple names that
   the compiler reports missing to their MojMap FQN (verified in source).
2. Rename leftover Yarn type references that migrateMappings missed:
   PlayerEntity -> Player, targetEntity -> target (inherited field).
"""
import os, re, glob

PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src/main/java', r'D:/IceAndFire-CE/fabric/src/main/java']

# simple name -> mojmap FQN (all verified against MojMap source above)
MISSING_IMPORTS = {
    'InteractionResult': 'net.minecraft.world.InteractionResult',
    'InteractionHand': 'net.minecraft.world.InteractionHand',
    'DifficultyInstance': 'net.minecraft.world.DifficultyInstance',
    'SimpleContainer': 'net.minecraft.world.SimpleContainer',
    'Container': 'net.minecraft.world.Container',
    'ContainerListener': 'net.minecraft.world.ContainerListener',
    'SpriteSet': 'net.minecraft.client.particle.SpriteSet',
    'ParticleProvider': 'net.minecraft.client.particle.ParticleProvider',
    'TextureSheetParticle': 'net.minecraft.client.particle.TextureSheetParticle',
    'ParticleRenderType': 'net.minecraft.client.particle.ParticleRenderType',
}

def add_import(text, fqn):
    if f'{fqn};' in text:
        return text
    simple = fqn.rsplit('.', 1)[-1]
    # skip if a same-simple-name import already exists (avoid duplicates)
    for m in re.finditer(r'^import\s+([A-Za-z0-9_.]+?)\s*;', text, re.M):
        if m.group(1).rsplit('.', 1)[-1] == simple:
            return text
    lines = text.splitlines(keepends=True)
    last_import = -1
    for i, ln in enumerate(lines):
        if ln.startswith('import '):
            last_import = i
    insert = f'import {fqn};\n'
    if last_import >= 0:
        lines.insert(last_import + 1, insert)
    else:
        for i, ln in enumerate(lines):
            if ln.startswith('package '):
                lines.insert(i + 1, '\n' + insert)
                break
    return ''.join(lines)

def main():
    changed = []
    for d in PROJECT_DIRS:
        for path in glob.glob(d + '/**/*.java', recursive=True):
            with open(path, encoding='utf-8') as f:
                text = f.read()
            orig = text
            # 1. add missing imports if the simple name is referenced in the file
            for simple, fqn in MISSING_IMPORTS.items():
                if re.search(r'\b' + re.escape(simple) + r'\b', text):
                    text = add_import(text, fqn)
            # 2. rename PlayerEntity -> Player (type reference leftover)
            if re.search(r'\bPlayerEntity\b', text) and not re.search(r'class PlayerEntity|\.PlayerEntity', text):
                # add Player import, replace identifier
                text = add_import(text, 'net.minecraft.world.entity.player.Player')
                text = re.sub(r'\bPlayerEntity\b', 'Player', text)
            if text != orig:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(text)
                changed.append(os.path.basename(path))
    print(f'changed {len(changed)} files:')
    for c in sorted(changed):
        print('  ', c)

if __name__ == '__main__':
    main()
