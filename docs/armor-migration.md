# Armor 系统迁移分析（MC 1.21.1 → 26.2）

> 本文件为**分析文档**：说明 `ArmorItem` 删除后 26.2 的替代架构与迁移方案。**不修改代码**。
> 依据：1.21.1 与 26.2 Mojmap 源码逐项对照。

---

## 一、26.2 架构变化（源码依据）

### 1.1 `ArmorItem` 类被删除

- 1.21.1：`net.minecraft.world.item.ArmorItem`（`extends Item`，含 `Type` 枚举、`getMaterial()`、`getEquipmentSlot()` 等）。
- 26.2：`ArmorItem.java` **不存在**（全树搜索无结果）。装甲物品改为 **普通 `Item`** + `Item.Properties().humanoidArmor(ArmorMaterial, ArmorType)`：

```java
// 26.2 Items.java:1050
public static final Item LEATHER_HELMET = registerItem(ItemIds.LEATHER_HELMET,
   new Item.Properties().humanoidArmor(ArmorMaterials.LEATHER, ArmorType.HELMET));
```

`humanoidArmor`（`Item.java:550`）内部：`durability(type.getDurability(material.durability()))` + `attributes(material.createAttributes(type))` + `enchantable(...)` + `component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()).setEquipSound(...).setAsset(material.assetId()).build())`。

### 1.2 `ArmorMaterial` record 变化

| 分量 | 1.21.1 | 26.2 |
|---|---|---|
| 防御 | `Map<ArmorItem.Type, Integer> defense` | `Map<ArmorType, Integer> defense`（`ArmorItem.Type`→`ArmorType`） |
| 附魔值 | `int enchantmentValue` | 同 |
| 声音 | `Holder<SoundEvent> equipSound` | 同 |
| 修复 | `Supplier<Ingredient> repairIngredient` | **`TagKey<Item> repairIngredient`**（同工具系统） |
| 层/纹理 | `List<ArmorMaterial.Layer> layers` | **`ResourceKey<EquipmentAsset> assetId`** |
| 韧性/击退 | `float toughness, float knockbackResistance` | 同 |

### 1.3 `Registries.ARMOR_MATERIAL` 被移除

- 26.2 `Registries.java` 中 **0 处 `ARMOR`**。装甲材料不再注册为游戏内 registry —— `ArmorMaterial` 是普通 record，随物品的 `Equippable` 组件嵌入。
- 纹理资产改用独立的 **`equipment_asset` datapack registry**：`EquipmentAssets.ROOT_ID = ResourceKey.createRegistryKey("equipment_asset")`（`EquipmentAssets.java:11`）。自定义装甲需提供 `data/<mod>/equipment_asset/<name>.json` + `assets/<mod>/equipment/` 纹理。

### 1.4 `ArmorType` 枚举（`net.minecraft.world.item.equipment.ArmorType`）

`HELMET(HEAD,11) / CHESTPLATE(CHEST,16) / LEGGINGS(LEGS,15) / BOOTS(FEET,13) / BODY(BODY,16)`（`ArmorType.java:8-12`）。
对应 1.21.1 的 `ArmorItem.Type`。

---

## 二、项目现状

### 2.1 `IafArmorMaterials`（`registry/IafArmorMaterials.java`）

- `DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(MOD_ID, Registries.ARMOR_MATERIAL)` → **26.2 编译失败**（`Registries.ARMOR_MATERIAL` 不存在）。
- `register(name, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness, float knockBackResistance, Supplier<Ingredient> repairIngredients)` → 需改为构造 record：`Map<ArmorType,Integer>` 防御 + `TagKey<Item>` 修复 + `ResourceKey<EquipmentAsset>` 资产。
- `createMaterial`：`Util.make(new EnumMap<>(ArmorItem.Type.class), map -> { map.put(ArmorItem.Type.HELMET, protection[3]); ... })` → 改 `ArmorType`。

### 2.2 装甲物品（`item/armor/`，6 个）

`BlindfoldItem / DragonScaleArmorItem / DragonSteelArmorItem / EarPlugsArmorItem / SeaSerpentArmorItem / TrollArmorItem`，均 `extends ArmorItem`：

```java
// 1.21.1 DragonScaleArmorItem
public DragonScaleArmorItem(DragonColor color, Type slot) {
    super(color.getMaterial(), slot, new Properties().durability(...));   // Type = ArmorItem.Type
    ...
}
// 使用 this.type.getName()、this.getEquipmentSlot() 等 ArmorItem 成员
```

26.2 迁移：
- `extends ArmorItem` → `extends Item`。
- `super(material, Type.HELMET, props)` → `super(new Item.Properties().humanoidArmor(material, ArmorType.HELMET))`。
- `Type.HELMET` → `ArmorType.HELMET`（`net.minecraft.world.item.equipment.ArmorType`）。
- `this.type.getName()` → 需存储 `ArmorType` 或改 `ArmorType` 字段。
- `this.getEquipmentSlot()` → `ArmorType.getSlot()`（`ArmorType.getSlot()` 存在，`ArmorType.java`）。
- `inventoryTick` 中 `this.getEquipmentSlot()` → 用存储的 `ArmorType`。

### 2.3 `ArmorItem.Type` 其它引用（15+ 文件）

`DragonColor/SeaSerpentType/TrollType`（`getMaterial()` 返回）、`DragonBaseEntity`、`ServerEvents`、`IafItems`、`BipedArmorFeatureRenderer`/`ScaleArmorRenderer`（渲染）、`DragonArmorSlot` 等 —— 全部 `ArmorItem.Type` → `ArmorType`，`ArmorItem` → `Item`（渲染相关在 P6 处理）。

---

## 三、迁移方案（分阶段）

1. **`IafArmorMaterials` 重构**：去掉 `DeferredRegister`（`Registries.ARMOR_MATERIAL` 已删除），改为直接构造 `ArmorMaterial` record。防御 int[4] → `Map<ArmorType,Integer>`（顺序：BOOTS/LEGGINGS/CHEST/HELMET ↔ protection[0..3]）。修复 `Supplier<Ingredient>` → `TagKey<Item>`（新增装甲修复 tag，如复用工具系统模式）。资产 `List<Layer>` → `ResourceKey<EquipmentAsset>`。
2. **`EquipmentAsset` 注册**：新增 `data/iceandfire/equipment_asset/*.json` + `assets/iceandfire/equipment/` 纹理（P5 数据资源阶段）。
3. **6 个装甲物品**：`extends Item` + `humanoidArmor`，`Type`→`ArmorType`。
4. **`ArmorItem.Type` 全项目引用**：15+ 文件 → `ArmorType`。
5. 渲染相关（`BipedArmorFeatureRenderer`/`ScaleArmorRenderer`）留 P6。

---

## 四、注意事项

- 26.2 `humanoidArmor` 自动生成 `Equippable` 组件（含 `assetId`），物品无需手动构造。
- 自定义装甲**纹理资产**是数据驱动（`equipment_asset` datapack），需要资源文件；这是与 1.21.1 最大的运维差异。
- `ArmorType.BODY`（动物装甲）新增，项目现有 HELMET/CHESTPLATE/LEGGINGS/BOOTS 语义不变。
