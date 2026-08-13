import re, io, collections, sys

path = sys.argv[1]
pats = {
    "addCooldown": r"addCooldown",
    "startRiding": r"startRiding",
    "isInvulnerableTo": r"isInvulnerableTo",
    "experience": r"shouldDropExperience|getExperienceReward|isAlwaysExperienceDropper",
    "finalizeSpawn": r"finalizeSpawn",
    "entitiesOfClass": r"getEntitiesOfClass|getNonSpectating",
    "entity_remove": r"RemovalReason|\.discard\(|\.remove\(",
    "hasLineOfSight": r"hasLineOfSight|canSee",
    "damage": r"damageSources|DamageSource|\.damage\(|\.hurt\(",
    "knockback": r"knockback",
    "spawnAtLocation": r"spawnAtLocation",
    "entity_create": r"\.create\(",
    "nbt": r"ValueInput|ValueOutput|CompoundTag|saveWithoutId|\.load\(|readAdditionalSaveData|addAdditionalSaveData",
    "component": r"DataComponent|\.set\(|DataComponents|getOrDefault",
    "uuid": r"UUID|getUUID|setUUID",
    "trade": r"Villager|Trade|Merchant|Offer|getOffers",
    "riding": r"getRidingPlayer|getControlledVehicle",
    "banner": r"BannerPattern",
    "armor": r"ArmorItem|ArmorMaterial|EquipmentSlot",
    "tool": r"ToolMaterial|Tier|ItemAbility",
    "block_api": r"getBlockState|setBlockAndUpdate|destroyBlock|isOf|isIn|defaultBlockState|canOcclude",
    "entity_api": r"getBoundingBox|getEyeHeight|getPos\(|getX\(|getY\(|getZ\(|position\(|blockPosition",
    "goal": r"Goal|Pathfinder|Navigation",
}
counts = collections.Counter()
samples = {}
for line in io.open(path, encoding="utf-8", errors="replace"):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+): error: (.*)", line)
    if not m:
        continue
    fn, msg = m.group(1), m.group(3)
    if re.search(r"render|Render|Screen|screen|Particle|Hud|Gui|Model|Tabula|Layer|layer|texture|Texture", fn):
        continue
    for name, pat in pats.items():
        if re.search(pat, msg):
            counts[name] += 1
            if name not in samples:
                samples[name] = fn + ": " + msg[:90]
for n in sorted(counts, key=lambda x: -counts[x]):
    print(f"{n}: {counts[n]}")
    if n in samples:
        print(f"    e.g. {samples[n]}")
