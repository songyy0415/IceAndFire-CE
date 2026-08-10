# P7-E World API Migration — Error Audit

Baseline: 359 errors (world = 45 real). Generated after sheep/level fixes: 355.

## World error table (by module)

| 文件 | 错误 | API原因 | 迁移方案 |
| -- | -- | ----- | ---- |
| world/processor/*Processor.java (4) | ~2-3 each | `StructureProcessorType` generic removed; process override signature changed | 26.2 `StructureProcessorType` 不再泛型；override 需匹配新签名 |
| world/structure/GraveyardStructure.java | 1 | `int cannot be converted to MaxDistanceToFeature` | MaxDistanceToFeature 参数类型变化 |
| world/structure/MausoleumStructure.java | 1 | 同上 | 同上 |
| world/structure/GorgonTempleStructure.java | 1 | 同上 | 同上 |
| world/structure/CyclopsCaveStructure.java | 1 | `create(ServerLevel)` no suitable | StructurePiece create 签名变化 |
| world/structure/DragonRoostStructure.java | 1 | `create(ServerLevel)` | 同上 |
| world/structure/DragonCaveStructure.java | 1 | `create(ServerLevel)` | 同上 |
| world/structure/HydraCaveStructure.java | 1 | `create(ServerLevel)` | 同上 |
| world/feature/*SpawnFeature.java (3) | 1 each | `moveTo(float,float,float,int,int)` / entity spawn | setPos + rotation; 26.2 实体创建 |
| world/DangerousGeneration.java | 1 | `getSpawnPos()` | 结构 spawn pos API |
| world/feature/WanderingCyclopsSpawnFeature.java | ~1 | `getTag(TagKey<Block>)` | BlockState tag API |
| world/feature/DeathWormSpawnFeature.java | ~1 | `registryOrThrow(ConfiguredFeature)` | Holder registry access |
| world/feature/DragonSkeletonSpawnFeature.java | ~1 | `restrictTo(BlockPos,int)` | Mob restriction 移除 |

## API notes (verified against 26.2 compiled jar)

- `EntityType.SHEEP` / `LIGHTNING_BOLT` — **全部 EntityType 常量从 jar 移除** → `BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("sheep"))`
- `Sheep` class → `animal.sheep.Sheep`（已修）
- `StructureProcessorType<T>` 泛型移除；StructurePiece `create(ServerLevel)` 签名
- `MaxDistanceToFeature` 结构参数类型

## Commit plan
1. `migration: migrate structure processor api` — 4 processors
2. `migration: migrate structure piece create api` — 5 structures
3. `migration: migrate feature spawn api` — 3 features + DangerousGeneration
4. `migration: migrate chunk/level api` — 剩余
