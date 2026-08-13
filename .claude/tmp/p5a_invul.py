import io

# (file, argname)
FILES = [
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/DragonEggEntity.java", "i"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/DragonSkullEntity.java", "i"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/GhostEntity.java", "source"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/LightningDragonEntity.java", "i"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/MobSkullEntity.java", "i"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/MultipartPartEntity.java", "source"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/PixieEntity.java", "source"),
    ("common/src/main/java/com/iafenvoy/iceandfire/entity/SeaSerpentEntity.java", "source"),
]

for f, arg in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content
    # 1. signature: isInvulnerableTo(DamageSource <arg>) -> isInvulnerableTo(ServerLevel level, DamageSource <arg>)
    content = content.replace(
        f"public boolean isInvulnerableTo(DamageSource {arg}) {{",
        f"public boolean isInvulnerableTo(ServerLevel level, DamageSource {arg}) {{")
    # 2. super call: super.isInvulnerableTo(<arg>) -> super.isInvulnerableTo(level, <arg>)
    content = content.replace(
        f"super.isInvulnerableTo({arg})",
        f"super.isInvulnerableTo(level, {arg})")
    # 3. add ServerLevel import if missing
    if "public boolean isInvulnerableTo(ServerLevel" in content and "import net.minecraft.server.level.ServerLevel;" not in content:
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
