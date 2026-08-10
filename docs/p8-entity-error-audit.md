# P8 Entity API Migration — Batch1 Error Audit

Date: 2026-08-10
Baseline: `./gradlew :common:compileJava` → **225 total errors** (entity: **114** errors across **49** files)

---

## Summary

Entity errors split into **8 distinct API-change patterns**, all mechanical Mojmap/26.2 surface
adaptations. No behavioral rewrite needed — entity AI/combat/taming/animation logic is preserved
verbatim; only the API call sites change.

```
@Override duplicated                   10  (remove the extra @Override)
doHurtTarget signature change          13  (add ServerLevel first arg)
OwnableEntity.getOwnerReference         5  (implement new abstract method)
Selector vs Predicate<LivingEntity>     5  (target goal predicate arity)
BuiltInRegistries missing import        3  (add the import)
isSolidRender() no-arg change           3  (drop level+pos args)
misc one-offs                          ~40  (spawnAtLocation, isAlive, Explosion, ServerBossEvent, ...)
```

---

## Category 1 — `@Override @Override` duplicated annotation (10)

`Override is not a repeatable annotation interface`.

Files (one occurrence each):
- AmphithereEntity:520, CockatriceEntity:199, DeathWormEntity:505, DreadBeastEntity:215,
  DreadHorseEntity:84, DreadLichEntity:319, DreadMobEntity:121, DreadScuttlerEntity:229,
  HippogryphEntity:975, PixieEntity:386

Fix: remove the duplicate `@Override` line. Purely mechanical.

## Category 2 — `doHurtTarget` now takes `ServerLevel` first (13)

1.21.1: `boolean doHurtTarget(Entity target)`
26.2: `boolean doHurtTarget(ServerLevel level, Entity target)` (javap-verified on `Mob`)

Files:
- AmphithereEntity:660, CockatriceEntity:189, CockatriceEntity:246, CyclopsEntity:163,
  DeathWormEntity:237, DeathWormEntity:263, DeathWormEntity:495, DreadBeastEntity:109,
  DreadGhoulEntity:109, DreadScuttlerEntity:117, FireDragonEntity:83, GhostEntity:241,
  GorgonEntity:139, HippogryphEntity:715, HydraEntity:122, IceDragonEntity:105,
  IceDragonEntity:263, LightningDragonEntity:145, MultipartPartEntity:251,
  SeaSerpentEntity:261, SeaSerpentEntity:791, SirenEntity:143, StymphalianBirdEntity:220,
  TrollEntity:155, TrollEntity:238, DreadSpawnerBaseLogic:41,
  GhostEntity:97 (also affects GhostEntity:257 / StoneStatueEntity:207/212 — need per-site check)

Also affects AI goals:
- CyclopsAIAttackMeleeGoal:23, DragonAIAttackMeleeGoal:97, IafDragonLogic:233

Fix: `this.doHurtTarget(target)` → `this.doHurtTarget((ServerLevel) this.level(), target)`.
For override declarations, update the `@Override` signature to match.

## Category 3 — `OwnableEntity.getOwnerReference()` new abstract method (5)

26.2 `OwnableEntity` (javap-verified):
```java
public interface OwnableEntity {
    EntityReference<LivingEntity> getOwnerReference();  // NEW abstract
    Level level();                                       // abstract (was inherited)
    default LivingEntity getOwner() { ... }
    default LivingEntity getRootOwner() { ... }
}
```
The old `getOwnerUUID()` is gone. Concrete classes implementing `OwnableEntity` must now provide
`getOwnerReference()`. Files reporting "is not abstract and does not override abstract method":
- DragonPartEntity, SlowPartEntity, HydraHeadEntity, CyclopsEyeEntity, MultipartPartEntity (base)

Fix: MultipartPartEntity delegates to its parent (already has `getOwnerUUID()`); replace with
`EntityReference<LivingEntity> getOwnerReference()` returning the parent's reference (or `null`).
Part entities inherit the base impl.

## Category 4 — `Selector` vs `Predicate<LivingEntity>` in target goals (5)

`TargetingConditions.selector(...)` now takes `TargetingConditions$Selector` whose SAM is
`boolean test(LivingEntity, ServerLevel)` (javap-verified) — two args, not one.

Files:
- DeathWormEntity:86, DreadBeastEntity:86, DreadGhoulEntity:90, DreadScuttlerEntity:87,
  CockatriceAIAggroLookGoal:21, DeathWormAITargetGoal:18, CyclopsAITargetSheepPlayersGoal:10

Fix: change lambda `x -> ...` to `(x, level) -> ...` where the body uses only `x`. This is the
NearestAttackableTargetGoal 6-arg ctor whose last param is now a `Selector` (javap-verified:
`NearestAttackableTargetGoal(Mob, Class<T>, int, boolean, boolean, TargetingConditions$Selector)`).

## Category 5 — `BuiltInRegistries` missing import (3)

The class exists in 26.2 (`net.minecraft.core.registries.BuiltInRegistries`, javap-verified) but
the file no longer imports it.
- HippogryphEntity:192 (`BuiltInRegistries.ITEM.getOrThrow(IafItemTags.TEMPT_HIPPOGRYPH)`),
  TideTridentEntity:76, DragonUtils:247

Fix: add `import net.minecraft.core.registries.BuiltInRegistries;`.

## Category 6 — `isSolidRender()` no-arg (3)

1.21.1: `BlockState.isSolidRender(BlockGetter, BlockPos)`
26.2: `BlockBehaviour$BlockStateBase.isSolidRender()` (javap-verified, no args)

Files:
- DeathWormEntity:636, AquaticAIGetOutOfWaterGoal:59, SeaSerpentPathNavigatorGoal:129

Fix: `state.isSolidRender(level, pos)` → `state.isSolidRender()`.

## Category 7 — `spawnAtLocation` now requires `ServerLevel` (2)

1.21.1: `LivingEntity.spawnAtLocation(ItemStack)`
26.2: `Entity.spawnAtLocation(ServerLevel, ItemStack[, float|Vec3])` (javap-verified)

Files:
- SeaSerpentEntity:572, SeaSerpentEntity:575

Fix: `spawnAtLocation(stack, 0F)` → `spawnAtLocation((ServerLevel) this.level(), stack, 0F)`;
`spawnAtLocation(stack)` → `spawnAtLocation((ServerLevel) this.level(), stack)`.

## Category 8 — Misc one-off API changes (~40)

| File:line | Issue | 26.2 fix |
|---|---|---|
| GhostEntity:201 | `Entity.isAlive(entity)` — no static form | `entity.isAlive()` |
| GhostEntity:241 / GorgonEntity:139 / others | doHurtTarget (Cat 2) | see Cat 2 |
| GhostEntity:99 | bad conditional expression | inspect site |
| HippogryphEntity:421, SeaSerpentEntity:162/363 | cannot find symbol | inspect site |
| HippogryphEntity:468, SeaSerpentEntity:774 | `Optional<Reference<T>>` vs `T` | `.value()` on registry get |
| HippogryphEntity:867, AmphithereEntity:867 | duplicate `player` in tickRidden | rename local |
| DreadQueenEntity:45 | `ServerBossEvent` ctor now `(UUID, Component, BossBarColor, BossBarOverlay)` | add UUID arg |
| TrollEntity:373 | `Explosion` is now an interface (ServerExplosion impl) | use `level().explode(...)` or `ServerExplosion` |
| DreadLichSkullEntity:139 | `level().random` now protected | `this.getRandom()` |
| LightningDragonEntity:90 / TrollAIFleeSunGoal:28 | `!level().getSkyDarken() < 4` — `getSkyDarken()` is `int` | reorder parens |
| ChainTieEntity:31 | `BlockAttachedEntity.dropItem(ServerLevel, Entity)` new abstract | implement |
| ChainTieEntity:99/127, StoneStatueEntity:207/212 | method does not override | per-site |
| SeaSerpentPathNavigatorGoal:16 | `PathNavigation.canNavigateGround()` new abstract | implement |
| SirenEntity:394 | `FabricValueInput.contains` arg mismatch | inspect site |
| DragonChargeEntity:46, DragonSkullEntity:146, FireDragonEntity:47, GhostSwordEntity:179, IceDragonEntity:267, SeaSerpentEntity:162, SirenEntity:381/395, StoneStatueEntity:38/99/150/167, TideTridentEntity:78, LightningDragonEntity:55, BlockLaunchExplosion:90, PixieAIFollowOwnerGoal:90, AmphithereAIFollowOwnerGoal:87, GhostEntity:99, DragonUtils:251/267 | cannot find symbol / misc | per-site |

---

## 26.2 API verified (javap)

- `Mob.doHurtTarget(ServerLevel, Entity)`
- `OwnableEntity` — `getOwnerReference()`, `getOwner()` default, `level()` abstract
- `TargetingConditions$Selector.test(LivingEntity, ServerLevel)`
- `NearestAttackableTargetGoal(Mob, Class<T>, int, boolean, boolean, Selector)`
- `BlockBehaviour$BlockStateBase.isSolidRender()`
- `Entity.spawnAtLocation(ServerLevel, ItemStack[, float|Vec3])`
- `EntityType.create(Level, EntitySpawnReason)` — used by Cat-independent sites
- `BuiltInRegistries` exists at `net.minecraft.core.registries.BuiltInRegistries`
- `ServerBossEvent(UUID, Component, BossBarColor, BossBarOverlay)`
- `Explosion` → interface; `ServerExplosion` impl; `Level.explode(...)` factory
- `Level.getSkyDarken()` returns `int`
- `PathNavigation.canNavigateGround()` abstract
- `EntityReference<T>` — `of(T)`, `of(UUID)`, `getEntity(Level, Class)`

---

## Batch plan

- **Batch1a (mechanical)**: Cat 1 (10 files, remove dup @Override) + Cat 5 (3 files, add import)
  + Cat 6 (3 files, isSolidRender no-arg) — zero-risk, compile → commit
- **Batch1b (core API)**: Cat 2 (doHurtTarget ServerLevel) + Cat 7 (spawnAtLocation) + Cat 8
  one-offs (getRandom, ServerBossEvent, getSkyDarken parens, Explosion, Entity.isAlive)
- **Batch1c (owner/AI)**: Cat 3 (OwnableEntity.getOwnerReference) + Cat 4 (Selector lambdas)
- Verify `./gradlew :common:compileJava` after each, `before/after/delta`, `new errors = 0`,
  one commit per logical batch.

Target after Batch1: 225 → ~180–190 (all 114 entity errors cleared).
