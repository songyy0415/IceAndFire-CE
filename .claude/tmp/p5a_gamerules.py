import io

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

KEYMAP = {
    "RULE_MOBGRIEFING": "MOB_GRIEFING",
    "RULE_DOENTITYDROPS": "ENTITY_DROPS",
    "RULE_DOMOBLOOT": "MOB_DROPS",
}

for f in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content
    # 1. fix GameRules key name
    for old, new in KEYMAP.items():
        content = content.replace("GameRules." + old, "GameRules." + new)
    # 2. getBoolean(X) -> get(X)
    content = content.replace("getGameRules().getBoolean(GameRules.", "getGameRules().get(GameRules.")
    # 3. fix import package: net.minecraft.world.level.GameRules -> net.minecraft.world.level.gamerules.GameRules
    content = content.replace(
        "import net.minecraft.world.level.GameRules;",
        "import net.minecraft.world.level.gamerules.GameRules;")
    if content != orig:
        with io.open(f, 'w', encoding='utf-8', newline='') as fh:
            fh.write(content)
        print(f"FIXED: {f}")
    else:
        print(f"no change: {f}")
