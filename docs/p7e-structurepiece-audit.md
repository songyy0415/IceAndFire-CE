# P7-E StructurePiece Migration — Error Audit

Baseline: 319 errors. Structure module = 13 errors across 8 files.

## Structure error table (verified against 26.2 compile jar)

| 文件 | 行 | API | 旧签名 (1.21.1) | 新签名 (26.2 jar) | 修改方案 |
| -- | -- | --- | --- | --- | ---- |
| GraveyardStructure | 56 | `JigsawPlacement.addPieces` | `addPieces(..., int maxDistanceFromCenter, ...)` | `addPieces(..., JigsawStructure$MaxDistance, ...)` | `new JigsawStructure.MaxDistance(this.maxDistanceFromCenter)` |
| MausoleumStructure | 57 | 同上 | 同上 | 同上 | 同上 |
| GorgonTempleStructure | 57 | 同上 | 同上 | 同上 | 同上 |
| DragonRoostStructure | 304 | `EntityType.create` | `create(ServerLevel)` | `create(Level, EntitySpawnReason)` | `create(world.getLevel(), EntitySpawnReason.STRUCTURE)` |
| DragonRoostStructure | 77 | `BuiltInRegistries.BLOCK.get` | `get(Identifier) -> Block` | `get(Identifier) -> Optional<Reference<Block>>`; `getValue(Identifier) -> T` | `BLOCK.getValue(Identifier)` |
| CyclopsCaveStructure | 146 | `EntityType.create` | `create(ServerLevel)` | `create(Level, EntitySpawnReason)` | `+ EntitySpawnReason.STRUCTURE` |
| CyclopsCaveStructure | 167 | `BuiltInRegistries.ENTITY_TYPE.getValue` | 返回 `EntityType<Sheep>` | 返回 `EntityType<?>` | cast `(EntityType<? extends Sheep>)` |
| DragonCaveStructure | 243 | `EntityType.create` | `create(ServerLevel)` | `create(Level, EntitySpawnReason)` | `+ EntitySpawnReason.STRUCTURE` |
| DragonCaveStructure | 198 | `Registry.getTag` | `getTag(TagKey) -> Optional<Named>` | 移除；`getTagOrEmpty(TagKey) -> Iterable<Holder<T>>` | StreamSupport → Holder::value |
| HydraCaveStructure | 105 | `RegistryAccess.registryOrThrow` / `TreeFeatures.SWAMP_OAK` / `Registry.getHolder` | `registryOrThrow(key).getHolder(holder)` | `lookupOrThrow(key).get(ResourceKey)`；TreeFeatures 类移除 | ResourceKey `minecraft:swamp_oak` 查找 |
| HydraCaveStructure | 155 | `Mob.restrictTo` | `restrictTo(BlockPos, int)` | 移除；`setHomeTo(BlockPos, int)` | `hydra.setHomeTo(pivot, 15)` |
| PixieVillageStructure | 111 | `EntityType.create` | `create(ServerLevel)` | `create(Level, EntitySpawnReason)` | `+ EntitySpawnReason.STRUCTURE` |

## API notes (verified against 26.2 compiled jar)

- `StructurePiece` — 仍是抽象类；`StructurePiece(Type, int, BoundingBox)` protected + `(Type, CompoundTag)` public 构造器未变
- `StructurePieceType` — 现在是 `load(StructurePieceSerializationContext, CompoundTag)` functional interface；`IafStructurePieces` 注册方式未报错，不动
- `addAdditionalSaveData(StructurePieceSerializationContext, CompoundTag)` — 已带 context 参数，本模块已适配
- `JigsawStructure.MaxDistance(int)` = `this(value, value)`（horizontal==vertical）；`MaxDistance.CODEC` 接受 int 或 `{horizontal, vertical}`
- `EntityType.create(Level, EntitySpawnReason)` — 单参 create 移除
- `RegistryAccess.lookupOrThrow(ResourceKey)` 取代 `registryOrThrow`
- `Registry.get(ResourceKey) -> Optional<Reference<T>>`（来自 HolderGetter）
- `Registry.getTagOrEmpty(TagKey) -> Iterable<Holder<T>>` 取代 `getTag`
- `Mob.setHomeTo(BlockPos, int)` 取代 `restrictTo`
- `DefaultedRegistry.getValue(Identifier) -> T` 取代 `get(Identifier) -> Optional`

## Commit plan
1. `migration: migrate structure piece api to mc26.2` — 8 structure files (MaxDistance wrap + EntityType.create + registry lookups + setHomeTo)
