import io, re

FILES = [
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DeathWormEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DragonBaseEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/GorgonEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/HydraBreathEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/SeaSerpentBubblesEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/SeaSerpentEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/TrollEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/ai/DragonAIMateGoal.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/ai/HippogryphAIMateGoal.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/util/dragon/DragonUtils.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/util/dragon/IafDragonDestructionManager.java",
]

# Replace <receiver>.getGameRules() with ((ServerLevel) <receiver>).getGameRules()
# Receiver is the expr before ".getGameRules()" -- typically this.level(), level, world, theWorld, entity.level()
pat = re.compile(r'(\b(?:this\.level\(\)|[a-zA-Z_][a-zA-Z0-9_]*\.[a-zA-Z_][a-zA-Z0-9_]*\(\)|[a-zA-Z_][a-zA-Z0-9_]*))(?=\.getGameRules\(\))')

def repl(m):
    r = m.group(1)
    return f"((ServerLevel) {r})"

for f in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content
    content = pat.sub(repl, content)
    # add ServerLevel import if missing
    if "((ServerLevel)" in content and "import net.minecraft.server.level.ServerLevel;" not in content:
        # insert before first net.minecraft.world import (or after last com. import)
        lines = content.split('\n')
        idx = None
        for i, ln in enumerate(lines):
            if ln.startswith('import net.minecraft.world.'):
                idx = i
                break
        if idx is None:
            idx = 0
        lines.insert(idx, "import net.minecraft.server.level.ServerLevel;")
        content = '\n'.join(lines)
    if content != orig:
        with io.open(f, 'w', encoding='utf-8', newline='') as fh:
            fh.write(content)
        print(f"FIXED: {f}")
    else:
        print(f"no change: {f}")
