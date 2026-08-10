# P12 Final Cleanup — Error Audit

Date: 2026-08-10
Baseline: `./gradlew :common:compileJava` → **24 total errors across 10 files**

---

## Error table

| File | Errors | Old API | 26.2 API | Fix |
| ---- | ------ | ------- | -------- | --- |
| util/RestrictWorldAccess | 5 | implements `ServerLevelAccessor` (removed); `levelEvent(Player,...)`; `getShade(Direction,boolean)` | `ServerLevelAccessor` removed; `LevelAccessor.levelEvent(Entity,...)`; `getShade` signature | Retarget interface; fix levelEvent param Entity; getShade override |
| util/ItemRandomizer | 1 | `BuiltInRegistries.ITEM.getOrCreateTag(tag)` | removed | `getTagOrEmpty(tag)` |
| data/component/PortalData | 5 | `DimensionTransition`; `ResourceKey.location()`; `changeDimension`; `sendSystemMessage` | renamed `TeleportTransition`; `identifier()`; `teleport()`; Player-only message | Migrate to TeleportTransition/teleport/identifier; cast living to Player for message |
| data/component/MiscData | 1 | `setLastHurtByPlayer(null)` | `setLastHurtByPlayer(Player,int)` / `(UUID,int)` | Pass `(null, 0)` or omit |
| data/TrollType | 1 | `IafRegistries.TROLL_TYPE.get(id)` | `getValue(id)` (get returns Optional<Reference>) | `getValueOrThrow` / `getValue` |
| data/SeaSerpentType | 1 | `IafItemTags` missing import | same | add import |
| data/DragonColor | 1 | `IafItemTags` missing import | same | add import |
| data/IafSkullType | 1 | `BuiltInRegistries.ITEM.get(id)` | `getValue(id)` | `getValue` |
| effect/FrozenStatusEffect | 1 | override orphaned | `MobEffect.applyEffectTick` signature? | javap-verify |
| network/ServerNetworkHelper | 3 | `interact(player,hand)`; `startRiding(e,true)` | `interact(player,hand,Vec3)`; `startRiding(e,true,false)` | Add Vec3; 3-arg startRiding |
| network/payload/LightningBoltS2CPayload | 3 | `Tuple::getA/getB/new` with `Pair` import | `Pair` methods | `Pair::getLeft/getRight/new` |
| IceAndFire.java | 1 | `ServerEvents::onBreakBlock` signature | `onBreakBlock(Level,BlockPos,BlockState,Player,IntValue)` | javap-verify event signature |

---

## 26.2 API verified (javap)

- `ServerLevelAccessor` removed; `LevelAccessor` abstract methods (incl. `levelEvent(Entity,int,BlockPos,int)`)
- `TeleportTransition(ServerLevel, Vec3, Vec3, float, float, PostTeleportTransition)` + `PLAY_PORTAL_SOUND`
- `Entity.teleport(TeleportTransition)`; `ResourceKey.identifier()`
- `Entity.startRiding(Entity)` / `startRiding(Entity, boolean, boolean)`
- `Entity.interact(Player, InteractionHand, Vec3)`
- `LivingEntity.setLastHurtByPlayer(Player,int)` / `(UUID,int)`
- `Registry.getValue(Identifier)`; `getTagOrEmpty(TagKey)`

## Plan

- **P12a (util)**: RestrictWorldAccess retarget + ItemRandomizer
- **P12b (data)**: PortalData, MiscData, 4 data type files
- **P12c (network + misc)**: ServerNetworkHelper, LightningBoltS2CPayload, FrozenStatusEffect, IceAndFire
- Final `clean compileJava` → 0 errors
