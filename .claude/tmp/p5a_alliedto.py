import io

FILES = [
    "common/src/main/java/com/iafenvoy/iceandfire/entity/AmphithereEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/CockatriceEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DeathWormEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DragonBaseEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DreadBeastEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DreadHorseEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DreadLichEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DreadMobEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/DreadScuttlerEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/HippocampusEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/HippogryphEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/entity/PixieEntity.java",
]

for f in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content
    # rename the override method (with the preceding @Override absent in some; keep it simple)
    # replace "public boolean isAlliedTo(Entity entityIn) {" -> "@Override\n    public boolean considersEntityAsAlly(Entity entityIn) {"
    content = content.replace(
        "public boolean isAlliedTo(Entity entityIn) {",
        "@Override\n    public boolean considersEntityAsAlly(Entity entityIn) {")
    # fix super calls inside method body
    content = content.replace(
        "return super.isAlliedTo(entityIn);",
        "return super.considersEntityAsAlly(entityIn);")
    if content != orig:
        with io.open(f, 'w', encoding='utf-8', newline='') as fh:
            fh.write(content)
        print(f"FIXED: {f}")
    else:
        print(f"no change: {f}")
