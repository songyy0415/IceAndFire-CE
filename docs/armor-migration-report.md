# Armor 系统迁移报告（MC 1.21.1 → 26.2）

> 依据：`docs/armor-migration.md` 分析方案 + 1.21.1/26.2 Mojmap 源码逐项核对。

---

## 一、旧 API → 新 API

| 1.21.1 | 26.2 | 说明 |
|---|---|---|
| `net.minecraft.world.item.ArmorItem`（类） | **删除** → `Item` + `Item.Properties().humanoidArmor(ArmorMaterial, ArmorType)` | 装甲物品不再有独立类；`humanoidArmor` 自动生成 `Equippable` 组件 |
| `net.minecraft.world.item.ArmorItem.Type` | `net.minecraft.world.item.equipment.ArmorType`（枚举） | `HELMET/CHESTPLATE/LEGGINGS/BOOTS/BODY` |
| `net.minecraft.world.item.ArmorMaterial`（record，`Map<ArmorItem.Type,Integer> defense, ..., List<Layer> layers`） | `net.minecraft.world.item.equipment.ArmorMaterial`（record，新增 `int durability`、`Map<ArmorType,Integer>`、`TagKey<Item> repairIngredient`、`ResourceKey<EquipmentAsset> assetId`） | 修复由 `Supplier<Ingredient>` → `TagKey<Item>`；纹理层 → `equipment_asset` 资产引用 |
| `Registries.ARMOR_MATERIAL` + `DeferredRegister<ArmorMaterial>` | **删除**（26.2 `Registries` 无任何 ARMOR 常量） | `IafArmorMaterials` 改为直接构造 `ArmorMaterial` record（不再注册） |
| `ArmorItem.getDescriptionId()`（可覆写） | **`final`**（`Item.java:338`） | 覆写必须移除；物品名改用默认 `item.<ns>.<path>` 翻译键 |
| `Holder<ArmorMaterial>`（物品构造参数） | `ArmorMaterial`（纯 record） | |
| `ArmorMaterial.Layer`（纹理层列表） | `ResourceKey<EquipmentAsset>` + `assets/<mod>/equipment/*.json`（客户端 `EquipmentClientInfo`） | 纹理数据驱动 |

---

## 二、修改文件

**Java（15 个）**
- `registry/IafArmorMaterials.java` — 重构：去 `DeferredRegister`/`RegistrySupplier`，字段改纯 `ArmorMaterial`，`create(name, int[] protection, enchant, sound, toughness, knockback, TagKey<Item> repair)` 构造 26.2 record；`BASE_DURABILITY=15`（1.21.1 材料无耐久、所有 IAF 装甲物品以 `.durability(...)` 覆盖，故基值仅作占位）。
- `registry/tag/IafItemTags.java` — 新增 10 个修复 tag 常量（deathworm_chitin ×3、troll_leather ×3、dragon_scales、sea_serpent_scales 及工具阶段已加的 copper/dragon_steels）。
- `registry/IafItems.java` — 24 处 `new ArmorItem(material, Type, props)` → `new Item(new Item.Properties().humanoidArmor(material, ArmorType.X).durability(y))`；`ArmorItem.Type` → `ArmorType`（36 处）。
- `item/armor/{Blindfold,DragonScale,DragonSteel,EarPlugs,SeaSerpent,Troll}ArmorItem.java` — `extends Item` + `humanoidArmor`；`Holder<ArmorMaterial>`→`ArmorMaterial`；`Type`→`ArmorType`；存 `ArmorType` 字段供 `getSlot()`/`getName()`；移除 `getDescriptionId` 覆写（26.2 final）；`inventoryTick` 改 26.2 签名；`isInWaterOrRain()`→`isInWater()`；`MobEffects.DAMAGE_BOOST`→`STRENGTH`。
- `item/DragonArmorItem.java` — 移除 `getDescriptionId` 覆写（26.2 final）。
- `data/{DragonColor,SeaSerpentType,TrollType}.java` — `material` 字段 `RegistrySupplier`/`Holder<ArmorMaterial>`→`ArmorMaterial`；`IafArmorMaterials.register()`→`create()`+TagKey 修复；`ArmorItem.Type`→`ArmorType`。
- `registry/IafTrollTypes.java` — `register` 参数 `Holder<ArmorMaterial>`→`ArmorMaterial`。
- `IceAndFire.java` — 移除 `IafArmorMaterials.REGISTRY.register()`（registry 已删）。

**资源（~74 个）**
- `data/iceandfire/tags/item/*.json` — 8 个新修复 tag（deathworm_chitin_yellow/red/white、troll_leather_mountain/forest/frost、dragon_scales、sea_serpent_scales）。
- `data/iceandfire/equipment_asset/*.json` — 33 个（空 `{}`，equipment_asset datapack 注册项）。
- `assets/iceandfire/equipment/*.json` — 33 个（`EquipmentClientInfo`，`layers.humanoid`→`<name>_layer_1.png`、`humanoid_leggings`→`<name>_layer_2.png`）。

**未处理（P6 客户端渲染）**：`render/item/armor/{BasicArmorRenderer,ScaleArmorRenderer}.java`、`render/entity/feature/{BipedArmorFeatureRenderer,DragonArmorFeatureRenderer}.java` 仍引用旧装甲 API —— 按用户要求留待 P6，其错误计入剩余。

---

## 三、验证结果

- `./gradlew :common:compileJava`：**javac 2,935 errors**；去重 **2,585 个独立错误位置**（P2-C 开始 2,745 → 2,585，降 160）。
- 剩余错误主体为渲染管线（P6）、`Optional` 返回（P4）、`EntityRendererRegistry`（P6）等，与装甲系统无关。
- `git diff --check`：**通过**（仅 LF→CRLF 换行警告，无空白错误）。

**语义映射核对（要求一/二）**
- durability：1.21.1 无材料级耐久（物品自设）→ 26.2 材料新增基值（占位 15），物品 `.durability()` 覆盖，行为保持。
- defense：`int[4]`（boots/legs/chest/helm 顺序）→ `Map<ArmorType,Integer>`，映射 `protection[0..3]`→`BOOTS/LEGGINGS/CHESTPLATE/HELMET`。
- enchantment / equip sound / toughness / knockback：1:1 保留。
- repair ingredient：`Supplier<Ingredient>`（单物品）→ `TagKey<Item>`（8+2 个 tag，覆盖全部原修复材料，含 DEATHWORM_WHITE/RED 的原始交叉映射）。
- texture：`ArmorMaterial.Layer` → `equipment_asset` + 客户端 `EquipmentClientInfo`（映射到既有 `_layer_1`/`_layer_2` 纹理）。

**未删功能**：所有装甲材料、物品、修复、特效均保留；`getDescriptionId` 覆写因 26.2 final 移除（物品翻译键改为默认 `item.iceandfire.<path>`，lang 文件需在 P5 资源阶段同步）。
