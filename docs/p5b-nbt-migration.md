# P5-B NBT/Data 迁移文档（MC 1.21.1 → 26.2）

> 对比：`/d/aiminecraftdev/IceAndFire-CE`（1.21.1 Mojmap 参照）vs 当前 26.2 工作区 + `javap` 26.2 merged jar。
> 范围：ValueInput/ValueOutput、CompoundTag API、UUID/NBT Codec、SavedData、Loot Codec。
> 禁止：Rendering / Particle / Screen / GUI / EntityRenderer / Trade / SpawnEgg / Uranus raycoms。

---

## 一、NBT 体系变化（26.2 核心重构）

| 1.21.1 | 26.2 |
|---|---|
| `CompoundTag` 直接作为保存目标 | `CompoundTag implements Tag`（不再是唯一）；新增 `ValueInput`/`ValueOutput` 接口 |
| `BlockEntity.saveAdditional(CompoundTag, HolderLookup.Provider)` | `BlockEntity.saveAdditional(ValueOutput)` |
| `BlockEntity.loadAdditional(CompoundTag, HolderLookup.Provider)` | `BlockEntity.loadAdditional(ValueInput)` |
| `Entity.addAdditionalSaveData(CompoundTag)` | `Entity.addAdditionalSaveData(ValueOutput)`（抽象） |
| `CompoundTag.getInt(name)` 返回 `int` | `CompoundTag.getInt(name)` 返回 `Optional<Integer>`；新增 `getIntOr(name, def)` |
| `CompoundTag.getString(name)` 返回 `String` | 返回 `Optional<String>`；`getStringOr(name, def)` |
| `CompoundTag.getCompound(name)` 返回 `CompoundTag` | 返回 `Optional<CompoundTag>`；`getCompoundOrEmpty(name)` |
| `Level.getGameRules()` | 移到 `ServerLevel.getGameRules()` |
| `SavedData.Factory` + `save(CompoundTag, HolderLookup)` | `SavedDataType<T>` + `Codec<T>` |
| `LootItemFunctionType` 注册（`Registries.LOOT_FUNCTION_TYPE`） | 移除；`LootItemFunction.codec()` 抽象方法 |
| `LootContext.getParamOrNull(LootContextParam)` | `getOptionalParameter(ContextKey)` |

## 二、ValueInput / ValueOutput 迁移模式

- `ValueOutput`：`putX(name, value)` 保留；`store(name, codec, value)` 存 Codec 对象；`child(name)`/`childrenList(name)` 嵌套。
- `ValueInput`：`getX(name)` 返回 Optional（int/long/string/intArray）；`getXOr(name, def)` 返回默认值；**没有 `getFloat`**（只有 `getFloatOr`）；`read(name, codec)` 返回 Optional。
- `ValueInput` **没有 `contains(name)`** → 用 `getX(name).isPresent()` 替代。
- `TagValueOutput.createWithoutContext(ProblemReporter)` → 构造写回 CompoundTag 的 ValueOutput；`buildResult()` 导出 CompoundTag。
- `TagValueInput.create(ProblemReporter, HolderLookup.Provider, CompoundTag)` → CompoundTag 包装为 ValueInput。
- 新增工具类 `com.iafenvoy.iceandfire.util.TagValueUtil`：
  - `asInput(CompoundTag, HolderLookup.Provider)` → ValueInput（实体/方块实体加载）
  - `output()` → TagValueOutput（实体保存到 CompoundTag）

## 三、BlockEntity 迁移（8 个文件）

| 文件 | 迁移 |
|---|---|
| DragonForgeBlockEntity | `saveAdditional(ValueOutput)`/`loadAdditional(ValueInput)`；`ContainerHelper.saveAllItems(nbt, list)` / `loadAllItems(nbt, list)`（去 HolderLookup）；`putInt("CookTime", (int) cookTime)`（double→int） |
| DreadSpawnerBlockEntity | `BaseSpawner.load(Level, BlockPos, ValueInput)` / `save(ValueOutput)`；`save(ValueOutput)` 替代 `save(CompoundTag, HolderLookup)`；`getUpdateTag` → `saveCustomOnly(registryLookup)` |
| EggInIceBlockEntity | `saveAdditional(ValueOutput)`（putString/putByte/putInt/putIntArray）；`loadAdditional(ValueInput)`（getString/getInt Optional + UUIDUtil.CODEC）；`getUpdateTag` → `saveCustomOnly` |
| JarBlockEntity / PixieHouseBlockEntity | 同上模式（getBooleanOr/getInt orElse/UUIDUtil.CODEC.read） |
| LecternBlockEntity / PodiumBlockEntity | `ContainerHelper.loadAllItems(nbt, list)` / `saveAllItems(nbt, list)` |
| GhostChestBlockEntity | `startOpen(ContainerUser)`（26.2 参数 Player→ContainerUser）；`loadAdditional/saveAdditional(ValueInput/ValueOutput)` |

`ContainerHelper` 26.2：`saveAllItems(ValueOutput, NonNullList)` / `loadAllItems(ValueInput, NonNullList)`（无 HolderLookup）。

## 四、Entity NBT 迁移

| 文件 | 迁移 |
|---|---|
| MultipartPartEntity（父类） | `readAdditionalSaveData(ValueInput)` / `addAdditionalSaveData(ValueOutput)` 空实现（子类 DragonPart/SlowPart/HydraHead/CyclopsEye 自动继承） |
| StoneStatueEntity.buildStatueEntity | `parent.saveWithoutId(ValueOutput)` 用 `TagValueOutput`，`buildResult()` 得 CompoundTag |
| DragonBaseEntity | `save(ValueOutput)`（26.2 返回 boolean）；`saveAsPassenger(ValueOutput)` |
| SirenEntity | `nbt.putIntArray("Uuid", uuidToIntArray(uuid))` 修正 `entry.getKey().getUUID()` |
| HydraEntity | `getFloatOr("HeadDamage"+i, 0.0F)`（ValueInput 无 getFloat） |
| HomePosition | `ValueInput/ValueOutput` 替代 CompoundTag；`contains` → `getX().isPresent()` |

## 五、SavedData 迁移（DragonPosWorldData）

1.21.1：`SavedData.Factory` + `save(CompoundTag, HolderLookup)` / 静态 `fromNbt`。
26.2：`SavedDataType<T>` + `Codec<T>`（参考 vanilla `WeatherData`）。

```java
public static final Codec<DragonPosWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, BlockPos.CODEC).fieldOf("DragonMap").forGetter(data -> data.lastDragonPositions)
).apply(instance, map -> { ... }));
public static final SavedDataType<DragonPosWorldData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("iceandfire", "dragonPositions"), DragonPosWorldData::new, CODEC, DataFixTypes.CHUNK);
```

`get(Level)`：`serverWorld.getDataStorage().computeIfAbsent(TYPE)`（`SavedDataStorage` 替代 `DimensionDataStorage`）。

**兼容性**：保持旧字段名（`DragonMap`/`DragonUUID`/`DragonPosX/Y/Z`）、旧存档格式、缺省行为。

## 六、Loot Function Codec 迁移

26.2 移除 `LootItemFunctionType` 注册表：
- `LootItemConditionalFunction.codec()` 抽象替代 `getType()`
- DragonLootFunction / SeaSerpentLootFunction：`codec()` 返回 `CODEC`
- `IafLoots`：移除 REGISTRY/DRAGON_LOOT/SEA_SERPENT_LOOT（loot function 不再注册）
- `IceAndFire.java`：移除 `IafLoots.REGISTRY.register()`
- `getParamOrNull` → `getOptionalParameter`

## 七、其他 26.2 NBT/Codec 相关修复

- `ElementalFlowerBlock`：`codec()` 返回 `MapCodec<BushBlock>`（26.2 具体类型）且 `public`；`noCollission` → `noCollision`；`BlockTags.SNOW_LAYER_CAN_SURVIVE_ON` → `SUPPORT_OVERRIDE_SNOW_LAYER`
- `DragonColor.getById`：`Registry.get(id)` 返回 `Optional<Holder.Reference>` → `.map(Holder.Reference::value).orElseThrow()`
- `DragonForgeInputBlockEntity`：`getAttributeValue(RegistrySupplier)` → `.asHolder()`
- `SoundEvents.X`：部分字段 26.2 是 `Holder<SoundEvent>`（ARMOR_EQUIP_LEATHER/CHAIN、GENERIC_EXPLODE）→ `.value()` 解包
- `GhostChestBlock`：`ChestBlock(Supplier, SoundEvent open, SoundEvent close, Properties)`（26.2 重构）

## 八、验证

- `./gradlew :common:compileJava`：2394 → **2301**（-93，NBT/Codec/SavedData/UUID 全部清零）。
- P5-B 目标（ValueInput/ValueOutput、CompoundTag Optional、UUID/NBT Codec、SavedData、Loot Codec）全部完成。
- 剩余 2301 错误分布：Rendering(P6) 1298、Egg(P5-D) 109、其它 entity/block/item API(P5-A/C/E) + Uranus raycoms(P5-E)。
- 保持：字段名、数据格式、缺省行为、旧存档兼容（BlockEntity 用旧 put/get 名，SavedData 用旧字段名）。
