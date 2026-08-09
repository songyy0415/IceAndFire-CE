# BannerPatternItem 迁移分析（MC 1.21.1 → 26.2）

> 本文件为**分析文档**：说明 `BannerPatternItem` 删除后的 26.2 替代架构与迁移方案。**不修改代码**。
> 依据：1.21.1 与 26.2 Mojmap 源码逐项对照。

---

## 一、现状（项目用法）

### 1.1 `IafItems` 注册 21 个旗帜图案物品（`IafItems.java:181-199`）

```java
public static final RegistrySupplier<BannerPatternItem> PATTERN_FIRE = registerItem(
   "banner_pattern_fire",
   () -> new BannerPatternItem(IafBannerPatternTags.FIRE_BANNER_PATTERN, new Item.Properties().stacksTo(1))
);
```

共 21 个：`FIRE/ICE/LIGHTNING(/HEAD)/AMPHITHERE/BIRD/EYE/FAE/FEATHER/GORGON/HIPPOCAMPUS/HIPPOGRYPH_HEAD/MERMAID/SEA_SERPENT/TROLL/WEEZER/DREAD`。

### 1.2 `IafBannerPatternTags`（`registry/tag/IafBannerPatternTags.java`）

19 个 `TagKey<BannerPattern>`（`pattern_item/fire` 等）—— 1.21.1 中 `BannerPatternItem(TagKey, ...)` 用 tag 关联图案。

### 1.3 图案数据

`data/iceandfire/banner_pattern/*.json`（如 `amphithere.json`、`dread.json`）—— 1.21.1 已是数据驱动注册。

---

## 二、26.2 架构变化（源码依据）

### 2.1 `BannerPatternItem` 类被删除

26.2 全树无 `BannerPatternItem.java`（`find` 无结果）。

### 2.2 26.2 旗帜图案物品 = 普通 `Item` + `PROVIDES_BANNER_PATTERNS` 组件

26.2 vanilla 图案物品（`Items.java:1800`）：
```java
public static final Item FLOWER_BANNER_PATTERN = registerItem(
   ItemIds.FLOWER_BANNER_PATTERN,
   new Item.Properties()
      .stacksTo(1)
      .delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(BannerPatternTags.PATTERN_ITEM_FLOWER))
);
```

- 用 `DataComponents.PROVIDES_BANNER_PATTERNS` 组件（codec 为 `TagKey<BannerPattern>`）替代旧 `BannerPatternItem` 的 TagKey 构造参数。
- `delayedComponent(DataComponentType, Function<RegistryWrapper.WrapperLookup, V>)` 延迟解析 tag。

### 2.3 `BannerPattern` / `BannerPatternLayers` 仍存在

`net.minecraft.world.level.block.entity.BannerPattern` 与 `BannerPatternLayers` 在 26.2 保留（数据驱动 `banner_pattern` registry）。

---

## 三、迁移方案（暂不实施）

### 3.1 `IafItems` 21 个图案物品

```java
// 1.21.1
new BannerPatternItem(IafBannerPatternTags.FIRE_BANNER_PATTERN, new Item.Properties().stacksTo(1))
// 26.2
new Item(new Item.Properties().stacksTo(1)
   .delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, ctx -> ctx.getOrThrow(IafBannerPatternTags.FIRE_BANNER_PATTERN)))
```

- `BannerPatternItem` → `Item`（`RegistrySupplier<Item>`）。
- 构造：`delayedComponent(PROVIDES_BANNER_PATTERNS, ctx -> ctx.getOrThrow(tag))`。
- `import net.minecraft.world.item.BannerPatternItem;` → 删除；新增 `import net.minecraft.world.item.component.DataComponents;`（`DataComponents` 已在）。

### 3.2 图案 tag 与数据

- `IafBannerPatternTags` 的 `TagKey<BannerPattern>` 保留（`PROVIDES_BANNER_PATTERNS` 的 codec 用 `TagKey`）。
- `data/iceandfire/banner_pattern/*.json` 与 tag JSON 保持不变（数据驱动注册不变）。
- 需确认 26.2 `banner_pattern` 数据 JSON 的格式是否变化（`assetId` 等字段），待资源阶段核对。

### 3.3 其它引用

- 图案物品可能在配方/战利品/`DragonBannerFeatureRenderer` 中被引用 —— 后者属 P6 渲染，暂不处理。

---

## 四、验证

迁移后运行 `./gradlew :common:compileJava`，确认 21 处 `BannerPatternItem` 相关错误清除；`data/iceandfire/banner_pattern/` 与 tag 资源不动。
