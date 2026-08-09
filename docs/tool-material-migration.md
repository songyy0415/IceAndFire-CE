# Tool 材料与工具系统迁移（MC 1.21.1 → 26.2）

> 范围：`common` 模块工具系统（`IafToolMaterials` + 剑/镐/斧/铲/锄物品）。
> 依据：1.21.1 与 26.2 Mojmap 源码逐项对照（每处均有源码证据，无猜测）。

---

## 一、26.2 架构变化（源码依据）

### 1.1 `Tier` 接口被删除 → `ToolMaterial` record

- 1.21.1：`net.minecraft.world.item.Tier`（接口，`getUses()/getSpeed()/getAttackDamageBonus()/getLevel()/getEnchantmentValue()/getRepairIngredient()`）；`SwordItem/PickaxeItem/AxeItem/HoeItem/ShovelItem/DiggerItem/ToolItem` 均基于 `Tier`。
- 26.2：`Tier.java` **不存在**（全树搜索无结果）；改为 **`net.minecraft.world.item.ToolMaterial` record**：

```java
// 26.2 ToolMaterial.java
public record ToolMaterial(
   TagKey<Block> incorrectBlocksForDrops,   // 对应旧 getIncorrectBlocksForDrops()
   int durability,                          // 对应旧 getUses()
   float speed,                             // 对应旧 getSpeed()
   float attackDamageBonus,                 // 对应旧 getAttackDamageBonus()
   int enchantmentValue,                    // 对应旧 getEnchantmentValue()
   TagKey<Item> repairItems                 // 对应旧 getRepairIngredient()（但旧是 Ingredient，新是 TagKey<Item>）
) { ... }
```

### 1.2 工具物品类变化（26.2 `net/minecraft/world/item/`）

| 类 | 1.21.1 | 26.2 |
|---|---|---|
| `SwordItem` | 存在 | **删除** → 用 `Item` + `Item.Properties().sword(ToolMaterial, float atk, float speed)` |
| `PickaxeItem` | 存在 | **删除** → `Item` + `Item.Properties().pickaxe(...)` |
| `DiggerItem` / `ToolItem` | 存在 | **删除** |
| `AxeItem` | 存在 | 存在，构造改为 `(ToolMaterial, float atkBaseline, float speedBaseline, Item.Properties)` |
| `HoeItem` | 存在 | 存在，构造同 `(ToolMaterial, float, float, Item.Properties)` |
| `ShovelItem` | 存在 | 存在，构造同 `(ToolMaterial, float, float, Item.Properties)` |

`Item.Properties` 26.2 提供：`tool()/pickaxe()/axe()/hoe()/shovel()/sword(ToolMaterial, float, float)`（`Item.java:456-482`）。

### 1.3 修复材料变化

- 1.21.1：`Tier.getRepairIngredient()` 返回 `Ingredient`（可以是单个物品，如 `Ingredient.of(Items.STONE)`）。
- 26.2：`ToolMaterial.repairItems` 是 **`TagKey<Item>`**（严格 tag，不能是单个物品）。

---

## 二、项目现状（`IafToolMaterials`）

`IafToolMaterials` 是 enum，`implements Tier`，字段：
`name, durability, damage, speed, enchantability, inverted(TagKey<Block>), ingredient(Ingredient)`。

`init()`（`IceAndFire.process()` 调用）设置修复材料，全部是 `Ingredient.of(...)`：

| 材料 | 旧修复（1.21.1） | 26.2 需改为 TagKey<Item> |
|---|---|---|
| SILVER | `CommonItemTags.INGOTS_SILVER`（已是 TagKey） | `CommonItemTags.INGOTS_SILVER` |
| DRAGONBONE / BLOODED_DRAGONBONE | `Ingredient.of(IafItems.DRAGON_BONE.get())` | 新 tag `dragon_bone` |
| TROLL_WEAPON | `Ingredient.of(Items.STONE)` | `ItemTags.STONE_CRAFTING_MATERIALS`（26.2 已有，`ItemTags.java:153`） |
| HIPPOGRYPH_SWORD | `Ingredient.of(IafItems.HIPPOGRYPH_TALON.get())` | 新 tag |
| HIPPOCAMPUS_SWORD | `Ingredient.of(IafItems.SHINY_SCALES.get())` | 新 tag |
| AMPHITHERE_SWORD | `Ingredient.of(IafItems.AMPHITHERE_FEATHER.get())` | 新 tag |
| STYMHALIAN_SWORD | `Ingredient.of(IafItems.STYMPHALIAN_BIRD_FEATHER.get())` | 新 tag |
| DREAD_SWORD / DREAD_KNIGHT | `Ingredient.of(IafItems.DREAD_SHARD.get())` | 新 tag |
| COPPER | `Ingredient.of(Items.COPPER_INGOT)` | 新 tag（或铜锭相关） |
| DRAGONSTEEL_FIRE/ICE/LIGHTNING | `Ingredient.of(IafItems.DRAGONSTEEL_*_INGOT.get())` | 新 tag `dragonsteel_*` |

**决策**：在 `IafItemTags` 新增修复 tag 常量 + `data/iceandfire/tags/item/*.json` 数据文件，`IafToolMaterials.setRepairMaterial(Ingredient)` 改为 `setRepairItems(TagKey<Item>)`。

---

## 三、迁移方案

### 3.1 `IafToolMaterials`

- 去掉 `implements Tier`（26.2 无 `Tier`）。
- 字段 `ingredient(Ingredient)` → `repairItems(TagKey<Item>)`。
- `setRepairMaterial(Ingredient)` → `setRepairItems(TagKey<Item>)`。
- 新增 `public ToolMaterial toolMaterial()`：
  ```java
  return new ToolMaterial(this.inverted, this.durability, this.speed, this.damage, this.enchantability, this.repairItems);
  ```
  （字段名与 record 分量一一对应，均有 1.21.1/26.2 源码依据）
- `init()` 内 `Ingredient.of(X)` → 对应 TagKey。

### 3.2 工具物品（`item/tool/`）

| 物品 | 1.21.1 | 26.2 |
|---|---|---|
| `ActivePostHitSwordItem` / `GhostSwordItem` / `HippogryphSwordItem` / `HippocampusSlapperItem` / `AmphithereMacuahuitlItem` / `StymphalianDaggerItem` / `TrollWeaponItem` | `extends SwordItem` | `extends Item` + `super(new Item.Properties().sword(material, atk, speed))` |
| `ActivePostHitPickaxeItem` | `extends PickaxeItem` | `extends Item` + `super(new Item.Properties().pickaxe(material, atk, speed))` |
| `ActivePostHitAxeItem` | `extends AxeItem` | `extends AxeItem` + `super(material, atk, speed, properties)` |
| `ActivePostHitHoeItem` | `extends HoeItem` | `extends HoeItem` + `super(material, atk, speed, properties)` |
| `ActivePostHitShovelItem` | `extends ShovelItem` | `extends ShovelItem` + `super(material, atk, speed, properties)` |

- 物品构造参数改为 `(ToolMaterial, float attackDamage, float attackSpeed, Item.Properties, ability)`（26.2 ctor 需要 atk/speed）。
- `IafItems` 23 处注册点已传 atk/speed（`createAttributes(material, atk, speed)`），提取传入即可。

### 3.3 `IafItems` 注册点

`new ActivePostHitSwordItem(IafToolMaterials.X, properties, ability)` → 增加 atk/speed 参数：
`new ActivePostHitSwordItem(IafToolMaterials.X.toolMaterial(), 3.0F, -2.4F, properties, ability)`。
（atk/speed 值从现有 `createAttributes(material, 3.0F, -2.4F)` 调用中复用。）

---

## 四、验证

每步后运行 `./gradlew :common:compileJava` 记录错误数；最终 commit `migration: migrate tool api`。
