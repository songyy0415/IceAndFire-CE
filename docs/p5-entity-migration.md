# P5-A Entity Gameplay 迁移文档（MC 1.21.1 → 26.2）

> 对比：`/d/aiminecraftdev/IceAndFire-CE`（1.21.1 Mojmap 参照）vs 当前 26.2 工作区。
> 范围：EntityType.create、spawnAtLocation、knockback、entity lifecycle API（hurt/isInvulnerableTo/startRiding/GameRules/isAlliedTo/addCooldown/customServerAiStep/isBaby）。
> 禁止：Rendering / RenderState / Particle rendering / Screen / GUI / Shader / Mixin / Uranus 特性恢复（P6/P5-E）。

---

## 一、EntityType.create（1 参 → 2 参）

1.21.1：`EntityType.create(Level)` → 26.2：`EntityType.create(Level, EntitySpawnReason)`。

| 文件 | 修复 |
|---|---|
| DragonEggEntity.java | `create(world, EntitySpawnReason.LOAD)` |
| StoneStatueEntity.java | `create(parent.level(), EntitySpawnReason.LOAD)`（2 处） |
| TideTridentEntity.java | `create(world, EntitySpawnReason.LOAD)` |
| ServerEvents.java | `create(world, EntitySpawnReason.LOAD)` |
| SummonLightningAbility.java | `create(target.level(), EntitySpawnReason.LOAD)` + 补 `EntitySpawnReason` 导入 |
| EggInIceBlockEntity.java | `create(level, EntitySpawnReason.LOAD)` + 补导入 |
| GraveyardSoilBlock.java | `create(worldIn, EntitySpawnReason.LOAD)` |
| DragonFleshItem.java | `create(living.level(), EntitySpawnReason.LOAD)` + 补导入 |
| DragonHornItem.java | `create(world, EntitySpawnReason.LOAD)` |
| GhostChestBlockEntity.java | `create(this.level, EntitySpawnReason.SPAWNER)` |
| IafDragonTypes.java | `create(w, EntitySpawnReason.LOAD)`（FIRE/ICE/LIGHTNING，3 处）+ 补导入 |

## 二、spawnAtLocation（新增 ServerLevel 参数）

1.21.1：`spawnAtLocation(ItemStack[, float])` / `spawnAtLocation(ItemLike[, float])` → 26.2：`spawnAtLocation(ServerLevel, ItemStack[, float])` / `spawnAtLocation(ServerLevel, ItemLike)`。

- 所有调用点注入 `(ServerLevel) <receiver>.level()` 第一参（receiver 为 `this` 或实体变量）。
- 26.2 移除了 `(ServerLevel, ItemLike, float)` 重载 → 非 ItemStack 的带偏移调用改为 `new ItemStack(...)`：
  - DragonEggEntity:203 `this.getItem().getItem()` → `new ItemStack(...)`
  - ServerEvents:184 `Blocks.COBBLESTONE` → `new ItemStack(Blocks.COBBLESTONE)`
  - ServerEvents:269 `IafItems.CHAIN.get()` → `new ItemStack(...)`
- 受影响文件（11）：DragonBaseEntity、DragonEggEntity、DragonSkullEntity、HippocampusEntity、HippogryphEntity、MobSkullEntity、PixieChargeEntity、PixieEntity、StymphalianFeatherEntity、ServerEvents、MobEntityMixin。
- 补 `net.minecraft.server.level.ServerLevel` 导入。

## 三、knockback（3 参 → 5 参）

1.21.1：`knockback(double strength, double xRatio, double zRatio)` → 26.2：`knockback(double power, double xd, double zd, DamageSource, float)`。

| 文件 | 迁移 |
|---|---|
| AmphithereEntity / CyclopsEntity / DreadBeastEntity / DreadGhoulEntity / DreadScuttlerEntity / HydraEntity / TakeKnockbackAbility / IceSpikesBlock / DeathwormGauntletItem / HippogryphSwordItem / IafDragonLogic | 追加 `, <damageSource>, 0` |
| IafDragonDestructionManager:240 | `takeKnockback(3参)`（Yarn 遗留）→ `knockback(5参, lightningBolt(), 0)` |

## 四、IafDragonDestructionManager / BlockLaunchExplosion（Yarn 遗留 → Mojmap 26.2）

这两个文件是仓库中唯一的 **Yarn 映射遗留**（`net.minecraft.block.Block`、`net.minecraft.entity.Entity`、`World` 等），从 init commit 起从未迁移，编译报 282+32 错误。参照 1.21.1 Mojmap 版本整体重写：

### IafDragonDestructionManager（Yarn → Mojmap 26.2）
| Yarn | 26.2 Mojmap |
|---|---|
| `World` | `Level` |
| `BlockPos.stream` | `BlockPos.betweenClosedStream` |
| `center.add(x,y,z)` | `center.offset(x,y,z)` |
| `getSquaredDistance` | `distSqr` |
| `getNonSpectatingEntities` | `getEntitiesOfClass` |
| `Box` | `AABB` |
| `canSee` | `hasLineOfSight` |
| `isPartOf` | `is` |
| `isTeammate` | `isAlliedTo` |
| `target.damage` | `target.hurt` |
| `takeKnockback` | `knockback(power, xd, zd, source, 0)` |
| `Registries.STATUS_EFFECT.getEntry` | `BuiltInRegistries.MOB_EFFECT.wrapAsHolder` |
| `GameRules.DO_MOB_GRIEFING` | `((ServerLevel)level).getGameRules().get(GameRules.MOB_GRIEFING)` |
| `Explosion.DestructionType` | `Explosion.BlockInteraction` |
| `SpreadableBlock` | `SpreadingSnowyBlock` |
| `setOnFireFor` | `igniteForTicks` |
| `getDefaultState().with(prop,v)` | `defaultBlockState().setValue(prop,v)` |
| `isIn(TagKey)` / `isOf` | `is(TagKey)` |
| `getTranslationKey` | `getDescriptionId` |
| `setBlockState` | `setBlockAndUpdate` |
| `isOpaque` | `canOcclude` |

### BlockLaunchExplosion（`extends Explosion` → `implements Explosion`）
26.2 中 `Explosion` 从具体类改为**接口**（8 个抽象方法），`getToBlow()`/`getSeenPercent()`/`explode()` 不在接口。重写：
- `implements Explosion`，自持 `toBlow` 列表、`source` 等字段
- 实现 8 个接口方法：`level()`/`getBlockInteraction()`/`getIndirectSourceEntity()`/`getDirectSourceEntity()`/`radius()`/`center()`/`canTriggerBlocks()`/`shouldAffectBlocklikeEntities()`
- 自实现 `explode()`（径向球体收集方块）与 `finalizeExplosion(boolean)`（发射下落方块）
- `getSeenPercent` → `ServerExplosion.getSeenPercent`（static）
- `world.random` → `world.getRandom()`（protected 访问）
- `wasExploded(ServerLevel, ...)` → 需要 `ServerLevel` cast
- 移除 `world.getProfiler()`（26.2 移除）

## 五、GameRules API（26.2 大改）

1.21.1：`Level.getGameRules().getBoolean(GameRules.RULE_X)` → 26.2：
- 包迁移：`net.minecraft.world.level.GameRules` → `net.minecraft.world.level.gamerules.GameRules`
- `getGameRules()` 从 `Level` 移到 `ServerLevel` → 调用点 `((ServerLevel) level).getGameRules()`
- `getBoolean(GameRule)` → `get(GameRule)`（泛型返回）
- 规则 key 改名：`RULE_MOBGRIEFING`→`MOB_GRIEFING`、`RULE_DOENTITYDROPS`→`ENTITY_DROPS`、`RULE_DOMOBLOOT`→`MOB_DROPS`

受影响文件（11）：DeathWormEntity、DragonBaseEntity、GorgonEntity、HydraBreathEntity、SeaSerpentBubblesEntity、SeaSerpentEntity、TrollEntity、DragonAIMateGoal、HippogryphAIMateGoal、DragonUtils、IafDragonDestructionManager。

## 六、Entity lifecycle API

| API | 1.21.1 | 26.2 | 迁移 |
|---|---|---|---|
| `Entity.hurt(DamageSource,float)` | boolean | **final void** + `hurtServer(ServerLevel,DS,float)` 返回 boolean | `IafDragonLogic.attackTarget`：`return target.hurtServer((ServerLevel)target.level(), src, dmg)` |
| `isInvulnerableTo(DamageSource)` | 可覆盖 | 移除；`LivingEntity.isInvulnerableTo(ServerLevel,DS)` + `Entity.isInvulnerableToBase` | 8 个覆盖改 `isInvulnerableTo(ServerLevel level, DS)` + `super.isInvulnerableTo(level, x)` |
| `startRiding(Entity,boolean)` | 存在 | 移除 2 参 → `startRiding(Entity,boolean,boolean)` | `startRiding(x, true)` → `startRiding(x, true, false)` |
| `isAlliedTo(Entity)` | 可覆盖 | **final**（内部调 `considersEntityAsAlly`） | 12 个覆盖改为覆盖 `considersEntityAsAlly(Entity)` |
| `customServerAiStep()` | 无参 | `customServerAiStep(ServerLevel)` | DragonBaseEntity / DreadQueenEntity 加 ServerLevel 参数 |
| `AgeableMob.isBaby()` | 可覆盖 | **final**（`canBeABaby() && getAge()<0`） | DragonBaseEntity 覆盖改为 `isDragonBaby()`（`getDragonStage()<2`），龙类 9 处调用同步改名 |
| `ItemCooldowns.addCooldown(Item,int)` | Item | `addCooldown(ItemStack,int)` / `addCooldown(Identifier,int)` | 8 个物品：`addCooldown(this,N)` → `addCooldown(new ItemStack(this),N)`；`addCooldown(stack.getItem(),N)` → `addCooldown(stack,N)` |
| `Level.random` | public | protected | `level.getRandom()` |

## 七、验证

- `./gradlew :common:compileJava`：修复语法错误后 javac 首次走完全程。
- 错误数轨迹：P4 末 2182（部分编译）→ P5-A 各批次 2271/2479/2427/2399/2394（完整编译）。
- P5-A 目标（EntityType.create / spawnAtLocation / knockback / entity lifecycle）已全部迁移。
- 剩余 2394 错误分布：Rendering(P6) ~1298、Egg/Data(P5-D) ~115、NBT/Data(P5-B) ~24、其它 entity/block/item API（P5-B/C）+ Uranus raycoms(P5-E)。
