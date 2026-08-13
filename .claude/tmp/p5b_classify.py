import re, io, collections, sys

path = sys.argv[1]
pats = {
    "ValueOutput": r"ValueOutput|addAdditionalSaveData|saveAdditional",
    "ValueInput": r"ValueInput|readAdditionalSaveData|loadAdditional",
    "CompoundTag": r"CompoundTag",
    "getString/Int/etc Optional": r"getString\(|getInt\(|getBoolean\(|getLong\(|getFloat\(|getDouble\(|getList\(|getTag\(|getUUID\(|getCompound\(|getByte\(",
    "putString/Int": r"putString\(|putInt\(|putBoolean\(|putLong\(|putFloat\(|putDouble\(|putList\(|putUUID\(|put\(|putCompound",
    "UUID": r"UUIDUtil|getUUID|setUUID|read\(.*UUID",
    "Codec": r"Codec|codec|DynamicOps|RecordCodecBuilder|MapCodec",
    "NbtOps": r"NbtOps|NbtAccounter|NbtIo|NbtUtils|readNbt|writeNbt",
    "HolderLookup": r"HolderLookup|registryAccess|RegistryAccess",
    "SavedData": r"SavedData|PersistentData|DimensionalSavedData|save\(|load\(",
    "ItemStack NBT": r"getOrCreateTag|save\(|ItemStack.*tag|copyWithCount|setCount",
    "DataComponent": r"DataComponent|DataComponents|\.set\(|\.get\(.*Component|update\(",
    "BlockEntity NBT": r"BlockEntity|saveAdditional|loadAdditional|ContainerHelper",
    "Entity NBT": r"saveWithoutId|load\(|readAdditionalSaveData|addAdditionalSaveData",
}
counts = collections.Counter()
samples = {}
byfile = collections.Counter()
for line in io.open(path, encoding="utf-8", errors="replace"):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+): error: (.*)", line)
    if not m:
        continue
    fn, ln, msg = m.group(1), int(m.group(2)), m.group(3)
    if re.search(r"render|Render|Screen|screen|Particle|Hud|Gui|Model|Tabula|Layer|layer|texture|Texture|ponder", fn):
        continue
    for name, pat in pats.items():
        if re.search(pat, msg):
            counts[name] += 1
            if name not in samples:
                samples[name] = f"{fn}:{ln}: {msg[:100]}"
            byfile[(name, fn)] += 1
for n in sorted(counts, key=lambda x: -counts[x]):
    print(f"{n}: {counts[n]}")
    if n in samples:
        print(f"    e.g. {samples[n]}")
