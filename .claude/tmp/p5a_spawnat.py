import re, io

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

# Pattern: <receiver>.spawnAtLocation(<arg>[, <float/int>])
# 26.2 needs: <receiver>.spawnAtLocation((ServerLevel) <level>, <arg>[, <float>])
# where <level> is derived from the receiver entity: receiver.level()
# For `this.spawnAtLocation` -> `this.spawnAtLocation((ServerLevel) this.level(), ...)`
# For `statue.spawnAtLocation` -> `statue.spawnAtLocation((ServerLevel) statue.level(), ...)`

def transform(content):
    # Match:  <expr>.spawnAtLocation(  -- need to find receiver expr
    # We handle "this.spawnAtLocation(" and "NAME.spawnAtLocation(" generically.
    # Receiver expr: any identifier or method chain ending before ".spawnAtLocation("
    # We'll capture receiver as the text immediately before ".spawnAtLocation(".
    pat = re.compile(r'([A-Za-z_][A-Za-z0-9_]*)(\.spawnAtLocation\()')

    def repl(m):
        receiver = m.group(1)
        level = f"{receiver}.level()" if receiver != "this" else "this.level()"
        return f"{receiver}.spawnAtLocation((ServerLevel) {level}, "
        # NOTE: adds the space+arg handling below via the inner replace

    # The simple approach: replace ".spawnAtLocation(" -> ".spawnAtLocation((ServerLevel) X.level(), "
    # But need to handle int second arg -> float (26.2 third param is float, int widens fine)
    # First, fix the call opener for each receiver.
    # We'll do two passes:
    # 1. this.spawnAtLocation( -> this.spawnAtLocation((ServerLevel) this.level(),
    # 2. NAME.spawnAtLocation( -> NAME.spawnAtLocation((ServerLevel) NAME.level(),
    return content

def fix(content):
    # pass 1: this.spawnAtLocation(
    content = content.replace("this.spawnAtLocation(", "this.spawnAtLocation((ServerLevel) this.level(), ")
    # pass 2: other receivers: X.spawnAtLocation(  where X is simple identifier (statue, etc.)
    # Only replace when preceded by non-alnum (avoid this., already done) -- but "this." no longer matches now.
    # Use regex for X.spawnAtLocation( with X simple identifier, not preceded by '.'
    pat = re.compile(r'(?<![\w.>])([A-Za-z_][A-Za-z0-9_]*)(\.spawnAtLocation\()')
    def repl(m):
        return f"{m.group(1)}.spawnAtLocation((ServerLevel) {m.group(1)}.level(), "
    content = pat.sub(repl, content)
    return content

for f in FILES:
    with io.open(f, 'r', encoding='utf-8') as fh:
        content = fh.read()
    orig = content
    content = fix(content)
    if content != orig:
        with io.open(f, 'w', encoding='utf-8', newline='') as fh:
            fh.write(content)
        print(f"FIXED: {f}")
    else:
        print(f"no change: {f}")
