import re, io, sys

path = sys.argv[1]
lines = io.open(path, encoding="utf-8", errors="replace").read().splitlines()
errors = []
for i, ln in enumerate(lines):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+): error: (.*)", ln)
    if m:
        errors.append((m.group(1), int(m.group(2)), m.group(3)))

# P5-A entity gameplay error patterns (knockback, hurt, spawnAtLocation, EntityType.create, etc)
patterns = {
    "knockback": re.compile(r"knockback|takeKnockback", re.I),
    "hurt/damage": re.compile(r"hurtServer|\.hurt\(|hurtOrSimulate|damage\(", re.I),
    "spawnAtLocation": re.compile(r"spawnAtLocation"),
    "EntityType.create": re.compile(r"EntityType\.create|\.create\(.*EntitySpawnReason|create\(Level\)"),
    "ignite": re.compile(r"ignite|setOnFire", re.I),
    "isAlliedTo/teammate": re.compile(r"isAlliedTo|isTeammate|isPartOf", re.I),
    "addEffect/status": re.compile(r"addEffect|addStatusEffect|MobEffectInstance", re.I),
    "GameRules": re.compile(r"GameRules", re.I),
    "Entity.lifecycle": re.compile(r"getBoundingBox|getEyeHeight|getPos\(|RemovalReason|isRemoved|discard", re.I),
}
counts = {}
samples = {}
for fn, ln, msg in errors:
    for name, pat in patterns.items():
        if pat.search(msg):
            counts[name] = counts.get(name, 0) + 1
            if name not in samples:
                samples[name] = f"{fn}:{ln}: {msg}"
for name in sorted(counts):
    print(f"{name}: {counts[name]}")
    print(f"   e.g. {samples[name]}")
print("\nTOTAL errors:", len(errors))
