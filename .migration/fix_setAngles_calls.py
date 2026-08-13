#!/usr/bin/env python3
"""Rename 6-arg EntityModel.setAngles calls to setupAnim (MojMap).
Only matches calls whose first argument is a bare identifier (entity var),
not numeric ModelPart.setAngles(pitch,yaw,roll) calls."""
import re

FILES = [
    'common/src/main/java/com/iafenvoy/iceandfire/particle/GhostAppearanceParticle.java',
    'common/src/main/java/com/iafenvoy/iceandfire/render/entity/ChainTieEntityRenderer.java',
    'common/src/main/java/com/iafenvoy/iceandfire/render/entity/feature/HydraHeadFeatureRenderer.java',
    'common/src/main/java/com/iafenvoy/iceandfire/render/entity/GhostEntityRenderer.java',
    'common/src/main/java/com/iafenvoy/iceandfire/render/model/DreadBaseModel.java',
    'common/src/main/java/com/iafenvoy/iceandfire/render/model/DreadLichModel.java',
    'common/src/main/java/com/iafenvoy/iceandfire/render/model/DreadThrallModel.java',
]
# .setAngles(ident, a, b, c, d, e) - first arg bare identifier
PAT = re.compile(
    r'\.setAngles\(\s*([A-Za-z_][A-Za-z0-9_]*)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^)]+?)\s*\)'
)
for f in FILES:
    with open(f, encoding='utf-8') as fh:
        t = fh.read()
    new, n = PAT.subn(
        lambda m: f'.setupAnim({m.group(1)}, {m.group(2)}, {m.group(3)}, {m.group(4)}, {m.group(5)}, {m.group(6)})', t)
    if n:
        with open(f, 'w', encoding='utf-8') as fh:
            fh.write(new)
        print(f'{f}: replaced {n}')
    else:
        print(f'{f}: no match')
