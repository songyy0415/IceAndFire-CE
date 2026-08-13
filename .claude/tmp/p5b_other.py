import re, io, collections, sys

path = sys.argv[1]
pats = {
    "DataComponent": r"DataComponent|DataComponents|\.set\(.*Iaf|getOrDefault|get\(Iaf",
    "ItemStack NBT": r"getOrCreateTag|copyWithCount|ItemStack.*tag|setCount|transmuteCopy|set\(.*DataComponent",
    "RegistryAccess/HolderLookup": r"HolderLookup|registryAccess|RegistryAccess",
    "Component data": r"\.get\(.*Component|\.set\(.*Component|DataComponentType",
    "attribute/holder": r"RegistrySupplier<.*Attribute|Holder<Attribute>|wrapAsHolder",
    "sound holder": r"Holder<SoundEvent>|SoundEvent",
    "enchantment": r"Enchantment|EnchantmentHelper|getEnchantmentLevel",
    "packet/buffer": r"FriendlyByteBuf|StreamCodec|BufOps|NbtOps",
    "save/load misc": r"\.save\(|\.load\(|saveAdditional|loadAdditional",
    "block pos/tag": r"getBlockPos|BlockPos.*CODEC|TagKey",
}
counts = collections.Counter()
samples = {}
for line in io.open(path, encoding="utf-8", errors="replace"):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+): error: (.*)", line)
    if not m:
        continue
    fn, ln, msg = m.group(1), int(m.group(2)), m.group(3)
    if re.search(r"render|Render|Screen|screen|Particle|Hud|Gui|Model|Tabula|Layer|layer|texture|Texture|ponder|[Ee]gg", fn):
        continue
    for name, pat in pats.items():
        if re.search(pat, msg):
            counts[name] += 1
            if name not in samples:
                samples[name] = f"{fn}:{ln}: {msg[:90]}"
for n in sorted(counts, key=lambda x: -counts[x]):
    print(f"{n}: {counts[n]}")
    if n in samples:
        print(f"    e.g. {samples[n]}")
