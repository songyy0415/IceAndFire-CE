import io

# (file, list-of-simple-NBT-methods?)
FILES = [
    "common/src/main/java/com/iafenvoy/iceandfire/item/block/entity/JarBlockEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/item/block/entity/LecternBlockEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/item/block/entity/PixieHouseBlockEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/item/block/entity/PodiumBlockEntity.java",
    "common/src/main/java/com/iafenvoy/iceandfire/item/block/entity/GhostChestBlockEntity.java",
]

for f in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content

    # 1. method signatures
    content = content.replace(
        "public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {",
        "public void loadAdditional(ValueInput nbt) {")
    content = content.replace(
        "protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {",
        "protected void loadAdditional(ValueInput nbt) {")
    content = content.replace(
        "public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {",
        "public void saveAdditional(ValueOutput nbt) {")
    content = content.replace(
        "protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {",
        "protected void saveAdditional(ValueOutput nbt) {")

    # 2. super calls
    content = content.replace(
        "super.loadAdditional(nbt, registryLookup);",
        "super.loadAdditional(nbt);")
    content = content.replace(
        "super.saveAdditional(nbt, registryLookup);",
        "super.saveAdditional(nbt);")

    # 3. ContainerHelper
    content = content.replace(
        "ContainerHelper.loadAllItems(nbt, this.", "ContainerHelper.loadAllItems(nbt, this.")
    content = content.replace(
        ", registryLookup);", ");")
    # careful: this replaces ", registryLookup);" everywhere in file - only desired in NBT methods

    # 4. add imports
    lines = content.split('\n')
    has_vi = any("import net.minecraft.world.level.storage.ValueInput;" in l for l in lines)
    has_vo = any("import net.minecraft.world.level.storage.ValueOutput;" in l for l in lines)
    if not has_vi or not has_vo:
        # insert after last net.minecraft.world.* import
        idx = None
        for i, ln in enumerate(lines):
            if ln.startswith('import net.minecraft.world.'):
                idx = i
        if idx is None:
            idx = 0
        # ensure storage after world.block
        insert = []
        if not has_vi:
            insert.append("import net.minecraft.world.level.storage.ValueInput;")
        if not has_vo:
            insert.append("import net.minecraft.world.level.storage.ValueOutput;")
        # find position after block imports
        pos = None
        for i, ln in enumerate(lines):
            if ln.startswith('import net.minecraft.world.level.block.state.BlockState;') or \
               ln.startswith('import net.minecraft.world.level.block.Block;'):
                pos = i
        if pos is not None:
            for k, imp in enumerate(insert):
                lines.insert(pos + 1 + k, imp)
        else:
            lines.insert(idx + 1, insert[0])
            if len(insert) > 1:
                lines.insert(idx + 2, insert[1])
        content = '\n'.join(lines)

    if content != orig:
        with io.open(f, 'w', encoding='utf-8', newline='') as fh:
            fh.write(content)
        print(f"FIXED: {f}")
    else:
        print(f"no change: {f}")
