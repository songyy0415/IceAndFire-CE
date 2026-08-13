import io

FILES = [
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DragonBaseEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DragonEggEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DragonSkullEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/HippocampusEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/HippogryphEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/MobSkullEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/PixieChargeEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/PixieEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/StymphalianFeatherEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/event/ServerEvents.java",
    "common/src/main/java/com/iafenvoy/iceandfire/mixin/MobEntityMixin.java",
]

for f in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content
    # fix double-injected this
    content = content.replace(
        "this.spawnAtLocation((ServerLevel) this.level(), (ServerLevel) this.level(), ",
        "this.spawnAtLocation((ServerLevel) this.level(), ")
    # sanity: ensure no remaining double
    assert "level(), (ServerLevel)" not in content, f
    if content != orig:
        with io.open(f, 'w', encoding='utf-8', newline='') as fh:
            fh.write(content)
        print(f"FIXED: {f}")
