# P7-E Feature / ConfiguredFeature Migration — Error Audit

Baseline: 307 errors. World module = 13 errors across 7 files.

## World error table (verified against 26.2 compile jar)

| 文件 | 行 | API | 旧代码 | 26.2变化 | 修改方案 |
| -- | -- | --- | --- | --- | ---- |
| DangerousGeneration | 23 | `LevelData.getSpawnPos` | `world.getLevelData().getSpawnPos()` | 移除；新增 `getRespawnData().pos()` (LevelData$RespawnData record) | `getLevelData().getRespawnData().pos()` |
| DeathWormSpawnFeature | 26 | `EntityType.create` | `create(ServerLevel)` | `create(Level, EntitySpawnReason)` | `+ EntitySpawnReason.STRUCTURE` |
| DragonSkeletonSpawnFeature | 30 | 同上 | 同上 | 同上 | `+ STRUCTURE` (+import) |
| HippocampusSpawnFeature | 31 | 同上 | 同上 | 同上 | `+ STRUCTURE` (+import) |
| HippocampusSpawnFeature | 34 | `Entity.moveTo` | `moveTo(x,y,z,yaw,pitch)` | `moveTo` 从 Entity 移除 | `setPos(x,y,z)` + `setYRot(0)` + `setXRot(0)` |
| SeaSerpentSpawnFeature | 31 | `EntityType.create` | `create(ServerLevel)` | 同上 | `+ STRUCTURE` (+import) |
| SeaSerpentSpawnFeature | 34 | `Entity.moveTo` | 同上 | 移除 | setPos + setYRot + setXRot |
| StymphalianBirdSpawnFeature | 30 | `EntityType.create` | `create(ServerLevel)` | 同上 | `+ STRUCTURE` (+import) |
| StymphalianBirdSpawnFeature | 32 | `Entity.moveTo` | 同上 | 移除 | setPos + setYRot + setXRot |
| WanderingCyclopsSpawnFeature | 30 | `EntityType.create` | `create(ServerLevel)` | 同上 | `+ STRUCTURE` |
| WanderingCyclopsSpawnFeature | 36 | `BuiltInRegistries` / `EntityType<?>` | `EntityType.SHEEP.create(level, reason)` | `BuiltInRegistries` 缺 import；`getValue` 返回 `EntityType<?>` | 补 import + cast `EntityType<? extends Sheep>` |
| WanderingCyclopsSpawnFeature | 39 | `Sheep.getRandomSheepColor` | `getRandomSheepColor(RandomSource)` | 新签名 `getRandomSheepColor(ServerLevelAccessor, BlockPos)` | `Sheep.getRandomSheepColor(world, pos)` |

## API notes (verified against 26.2 compiled jar)

- `Feature<FC extends FeatureConfiguration>` / `place(FeaturePlaceContext<FC>)` — 未变；`context.level()/random()/origin()` 未变
- `FeaturePlaceContext` — `level()` 仍返回 `WorldGenLevel`（是 `ServerLevelAccessor`）
- `EntityType.create(Level, EntitySpawnReason)` — 单参 create 移除；`EntitySpawnReason.STRUCTURE` 存在
- `Entity.moveTo(double,double,double,float,float)` — **从 Entity 整体移除** → `setPos` + `setYRot` + `setXRot`
- `Sheep.getRandomSheepColor(ServerLevelAccessor, BlockPos) -> DyeColor` — 不再吃 RandomSource
- `LevelData.getSpawnPos()` 移除 → `LevelData.getRespawnData()` 返回 `RespawnData(pos, yaw, pitch)` record；`.pos()` 得 spawn
- `Feature` 构造器 `Feature(Codec<FC>)` 未变；`IafFeatures` 注册（`Feature` codec 注册表）未报错

## Commit plan
1. `migration: migrate feature generation api to mc26.2` — 7 files (spawn pos + EntityType.create + moveTo + sheep)
