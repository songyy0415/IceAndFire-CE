# Registry 系统迁移分析（MC 1.21.1 → 26.2）

> 本文件为**分析文档**：说明项目自定义注册表与 `Registries` 常量在 26.2 的兼容性。**不修改代码**（除 P2-A 已完成的 `ResourceLocation`→`Identifier`）。
> 依据：1.21.1 与 26.2 Mojmap 源码逐项对照。

---

## 一、自定义注册表：基本兼容（核心结论）

### 1.1 `IafRegistries`（`registry/IafRegistries.java`）

```java
// 项目当前（1.21.1 风格）
private static <T> DefaultedRegistry<T> create(String defaultId, ResourceKey<Registry<T>> key) {
    return new DefaultedMappedRegistry<>(defaultId, key, Lifecycle.stable(), false);
}
```

- 26.2 `DefaultedMappedRegistry` 构造签名**未变**：
  `public DefaultedMappedRegistry(String defaultKey, ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, boolean intrusiveHolders)`（`DefaultedMappedRegistry.java:14-17`）。
- 与 1.21.1 的唯一内部差异：`this.defaultKey = ResourceLocation.parse(string)` → `Identifier.parse(string)`（`DefaultedMappedRegistry.java:20`）——属于 `ResourceLocation`→`Identifier`（P2-A 已完成），**项目代码无需改动**。
- 6 个自定义注册表（`BESTIARY_PAGE/DRAGON_COLOR/DRAGON_TYPE/HIPPOGRYPH_TYPE/SEA_SERPENT_TYPE/TROLL_TYPE`）的 `create(...)` 调用直接兼容。

### 1.2 `IafRegistryKeys`（`registry/IafRegistryKeys.java`）

- `ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(...))` —— 26.2 `ResourceKey.createRegistryKey(final Identifier)` 存在（`ResourceKey.java:30`），`Identifier` 已在 P2-A 迁移，**无需改动**。

### 1.3 结论

自定义注册表系统**在 26.2 下基本原样可用**，无需重构。唯一前置条件（`Identifier`）已完成。

---

## 二、`Registries.*` 常量：项目使用清单核对

对 mod 全部 `Registries.XXX` 用法逐一对照 26.2 `Registries.java`：

| 状态 | 常量 |
|---|---|
| ✅ 存在（26.2） | `ITEM, ENTITY_TYPE, MOB_EFFECT, BLOCK, DAMAGE_TYPE, LOOT_TABLE, POINT_OF_INTEREST_TYPE, CONFIGURED_FEATURE, VILLAGER_PROFESSION, STRUCTURE_TYPE, STRUCTURE_PROCESSOR, STRUCTURE_PIECE, SOUND_EVENT, BANNER_PATTERN, MENU, BLOCK_ENTITY_TYPE, PARTICLE_TYPE, RECIPE_SERIALIZER, RECIPE_TYPE, CREATIVE_MODE_TAB, DATA_COMPONENT_TYPE, ATTRIBUTE, FEATURE, PLACED_FEATURE, BIOME, DIMENSION` |
| ⛔ 已移除 | **`ARMOR_MATERIAL`**（26.2 `Registries.java` 无任何 `ARMOR`）→ 见 `docs/armor-migration.md` |
| 🟡 自定义键 | `BESTIARY_PAGE, DRAGON_COLOR, DRAGON_TYPE, HIPPOGRYPH_TYPE, SEA_SERPENT_TYPE, TROLL_TYPE`（mod 自建，在 `IafRegistryKeys`，不在 vanilla `Registries`）—— 兼容 |

> 结论：除 `Registries.ARMOR_MATERIAL`（装甲材料不再注册为 registry，见 armor 文档）外，项目所有 vanilla 注册表常量在 26.2 均存在。

---

## 三、相关 Registry API 兼容性

| API | 1.21.1 | 26.2 | 判定 |
|---|---|---|---|
| `Registry.register(...)` / `DefaultedRegistry.register(...)` | 存在 | 存在（`register(ResourceKey<T>, T, RegistrationInfo)` 重载保留） | ✅ |
| `BuiltInRegistries.wrapAsHolder(...)` | 存在 | 存在（`Registry.wrapAsHolder`） | ✅ |
| `Holder<...>` / `Holder.Reference` | 存在 | 存在（`DefaultedMappedRegistry` 内部用 `Holder.Reference`） | ✅ |
| `ResourceKey.create(RegistryKey, Identifier)` | 存在 | 存在（第二参数变 `Identifier`，P2-A 已迁移） | ✅ |
| `Lifecycle.stable()` | 存在 | 存在 | ✅ |

---

## 四、迁移结论与遗留

1. **无需修改**：`IafRegistries`、`IafRegistryKeys`、以及所有 vanilla `Registries.*` 使用点（除 ARMOR_MATERIAL）。
2. **`Registries.ARMOR_MATERIAL`**：随 26.2 装甲架构变化移除，需按 `docs/armor-migration.md` 方案处理（构造 `ArmorMaterial` record + `equipment_asset` datapack 资产）。
3. **`PoiTypes.TYPE_BY_STATE`**（`IafTrades`）：值类型 1.21.1→26.2 变 `Map<BlockState, Holder<PoiType>>`，项目右值已是 `wrapAsHolder(...)`，语义自洽（见 `minecraft-api-migration.md` §15）。
