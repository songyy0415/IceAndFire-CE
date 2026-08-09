# P5-C Trade/Data System 迁移分析文档（MC 1.21.1 → 26.2）

> 对比基线：`/d/aiminecraftdev/minecraft1.21.1mojmap`（1.21.1 官方 Mojmap 源码）vs `/d/aiminecraftdev/minecraft26.2`（26.2 官方 Mojmap 源码，含 `data/` 数据包样例）。
> 参照 mod 源码：`/d/aiminecraftdev/IceAndFire-CE`（1.21.1 版）vs 当前 `common/`（26.2 工作区）。
> 范围：VillagerProfession / VillagerTrades / VillagerTrade / TradeSet / MerchantOffer / ItemCost / TradeCost / 自定义村民交易注册 / 交易相关数据结构。
> 本阶段为**纯分析**，未修改任何代码。
> 禁止：Renderer / Particle / Screen / GUI / Shader / EntityRenderer / SpawnEgg / Uranus raycoms / DynamicItemRenderer / **Mixin**。

---

## 摘要（TL;DR）

26.2 把交易系统从**「Java 静态 Map 注册」**彻底重构为**「Codec 数据驱动注册」**：

| 1.21.1 | 26.2 |
|---|---|
| `VillagerTrades.ItemListing` 函数式接口 + `VillagerTrades.TRADES` 静态 Map | **删除**。由新 `VillagerTrade` 数据条目（datapack registry `villager_trade`）替代 |
| `VillagerTrades.WANDERING_TRADER_TRADES` 静态 Map | **删除**。由 `TradeSets.WANDERING_TRADER_*`（datapack registry `trade_set`）替代 |
| 职业 record 只存 `String name`，不含交易 | 职业 record 新增 `Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel`，**按等级引用 TradeSet key** |
| 交易内容在 Java 代码里（mod 的 `IafTrades.init()`） | 交易内容必须进入 datapack JSON（`data/*/villager_trade`、`data/*/trade_set`、`data/*/tags/villager_trade`） |
| 村民 `updateTrades()` 读静态 Map | 村民 `updateTrades()` 经 `profession.getTrades(level)` → `ResourceKey<TradeSet>` → `registryAccess` 查 `TRADE_SET` |

**核心结论：**
1. ✅ **TradeSet 替代了 VillagerTrades**（是，功能层面完全替代）。
2. ✅ **交易已经数据驱动**（`VILLAGER_TRADE`、`TRADE_SET` 是 `RegistryDataLoader` 加载的 datapack registry，纯 JSON 定义）。
3. ⚠️ **自定义交易不再支持旧式 Java 注册**（`VillagerTrades.TRADES` 没了）。职业（Profession）与 POI 仍是内置 registry、仍可 Java 注册；但**交易内容**必须走 datapack JSON。本阶段禁用 Mixin，故推荐**数据包 JSON 方案**。
4. `MerchantOffer` / `ItemCost` 的**公开 API 未变**（构造器与 getter 相同），mod 对其调用无需改动。

---

## 一、旧系统结构（1.21.1）

### 1.1 VillagerProfession（`net.minecraft.world.entity.npc.VillagerProfession`）

6 字段 record：

```java
public record VillagerProfession(
    String name,                                  // 注意：String
    Predicate<Holder<PoiType>> heldJobSite,
    Predicate<Holder<PoiType>> acquirableJobSite,
    ImmutableSet<Item> requestedItems,
    ImmutableSet<Block> secondaryPoi,
    @Nullable SoundEvent workSound
) { ... }
```

- 内置职业常量是 `VillagerProfession` 实例（`ARMORER`、`LIBRARIAN`…）。
- 注册：`Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ...)`，mod 用 Architectury `DeferredRegister.create(MOD_ID, Registries.VILLAGER_PROFESSION)`（内置 registry，mod init 期写入）。
- **record 不含任何交易数据**。交易挂在外部的 `VillagerTrades.TRADES`。

### 1.2 VillagerTrades + ItemListing（`net.minecraft.world.entity.npc.VillagerTrades`）

- 核心静态结构（`VillagerTrades.java:79` 起）：

```java
public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> TRADES = Util.make(...);
public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> EXPERIMENTAL_TRADES = ...;
public static final VillagerTrades.ItemListing[] WANDERING_TRADER_TRADES = ...;
```

- `ItemListing` 是函数式接口：

```java
public interface ItemListing {
    @Nullable MerchantOffer getOffer(Entity entity, RandomSource randomSource);
}
```

- 内置了大量交易工厂内部类（`EmeraldForItems`、`ItemsForEmeralds`、`ItemsAndEmeraldsToItems`…）——它们都是 `ItemListing` 实现，运行时现算 `MerchantOffer`。
- 村民运行时时序（`Villager.java:845-859`）：`updateTrades()` → `VillagerTrades.TRADES.get(profession).get(level)` → `addOffersFromItemListings(merchantOffers, itemListings, 2)`（**每等级随机抽 2 笔**）。

### 1.3 MerchantOffer / ItemCost（`net.minecraft.world.item.trading`）

- `MerchantOffer`：普通类，字段 `baseCostA / costB(Optional) / result / uses / maxUses / rewardExp / specialPriceDiff / demand / priceMultiplier / xp`。公开构造器：
  - `MerchantOffer(ItemCost buy, ItemStack result, int maxUses, int xp, float priceMultiplier)`
  - `MerchantOffer(ItemCost buy, Optional<ItemCost> buyB, ItemStack result, int maxUses, int xp, float priceMultiplier)`
- `ItemCost`：record `(Holder<Item> item, int count, DataComponentExactPredicate components)`，构造器 `ItemCost(ItemLike, int)`。
- `priceMultiplier` 参与需求定价（`getModifiedCostCount`：`basePrice + floor(basePrice*demand*priceMultiplier) + specialPriceDiff`）。

### 1.4 本 mod 的旧注册方式（1.21.1 参照源码）

- `registry/IafTrades.java`：
  - `POI_REGISTRY`（`POINT_OF_INTEREST_TYPE`）+ `PROFESSION_REGISTRY`（`VILLAGER_PROFESSION`）两个 DeferredRegister；
  - `SCRIBE_PROFESSION = PROFESSION_REGISTRY.register("scribe", () -> new VillagerProfession("scribe", ...6字段...))`；
  - `init()`：`PoiTypes.TYPE_BY_STATE.put(state, holder)`（把 Lectern 的方块状态挂到 POI，经 access widener 放开 private 访问），再 `TradeOfferHelper.registerVillagerOffers(profession, level, new BuyWithPrice(...))` 注册 1–5 级共 27 笔交易。
- `util/trade/TradeOfferHelper.java`：`registerVillagerOffers(profession, level, ItemListing...)` / `registerWanderingTraderOffers(...)`，转发到 Internals。
- `util/trade/TradeOfferInternals.java`：`VillagerTrades.TRADES.computeIfAbsent(profession, ...)` / `VillagerTrades.WANDERING_TRADER_TRADES` 追加 `ItemListing[]`。
- `util/trade/factory/BuyWithPrice.java`：实现 `VillagerTrades.ItemListing`，`getOffer(Entity, RandomSource)` 返回 `new MerchantOffer(new ItemCost(input1.item, input1.count), Optional.of(new ItemCost(input2.item, input2.count)), output, maxUses, experience, multiplier)`。

---

## 二、新系统结构（26.2）

### 2.1 VillagerProfession（**包移动** → `net.minecraft.world.entity.npc.villager.VillagerProfession`）

**7 字段 record**：

```java
public record VillagerProfession(
    Component name,                               // String → Component
    Predicate<Holder<PoiType>> heldJobSite,
    Predicate<Holder<PoiType>> acquirableJobSite,
    ImmutableSet<Item> requestedItems,
    ImmutableSet<Block> secondaryPoi,
    @Nullable SoundEvent workSound,
    Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel   // 新增：等级 → TradeSet key
) { ... }
```

- 内置职业常量变为 `ResourceKey<VillagerProfession>`（`NONE`、`ARMORER`…都是 `ResourceKey`），由 `bootstrap(Registry<VillagerProfession>)` 填充。
- `getTrades(int level)` 返回 `@Nullable ResourceKey<TradeSet>`。
- 注册：仍是内置 registry（`BuiltInRegistries.java:212` `registerDefaulted(Registries.VILLAGER_PROFESSION, "none", VillagerProfession::bootstrap)`），**mod 仍可用 DeferredRegister Java 注册自定义职业**（工作区现状 `IafTrades.java` 已能编译通过即为佐证）。
- 名字 Component 的 vanilla 约定（`VillagerProfession.register` 内）：`Component.translatable("entity." + namespace + ".villager." + path)`。

### 2.2 新类 VillagerTrade（`net.minecraft.world.item.trading.VillagerTrade`）

**单笔交易**，codec 驱动，注册于 datapack registry `Registries.VILLAGER_TRADE`：

```java
public class VillagerTrade implements Validatable {
    private final TradeCost wants;                          // 玩家要付出的物品（TradeCost）
    private final Optional<TradeCost> additionalWants;      // 玩家额外付出的第二件物品
    private final ItemStackTemplate gives;                  // 村民给出的物品
    private final NumberProvider maxUses;
    private final NumberProvider reputationDiscount;        // 等价旧 priceMultiplier
    private final NumberProvider xp;
    private final Optional<LootItemCondition> merchantPredicate;
    private final List<LootItemFunction> givenItemModifiers;
    private final Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments;
    // public @Nullable MerchantOffer getOffer(LootContext)
}
```

- `TradeCost`（`net.minecraft.world.item.trading.TradeCost`）：record `(Holder<Item> item, NumberProvider count, DataComponentExactPredicate components)`，`toItemCost(LootContext, additionalCost)` → `ItemCost`。
- `ItemStackTemplate`（`net.minecraft.world.item.ItemStackTemplate`）：record `(Holder<Item> item, int count, DataComponentPatch components)`，是数据驱动的 ItemStack 描述（1.21.1 交易工厂里用 `ItemStack` 的部分在 26.2 由它替代）。
- `getOffer(LootContext)` 返回 `new MerchantOffer(itemCost, additionalItemCost, result, max(uses,1), max(xp,0), max(reputationDiscount,0))`。

### 2.3 新类 TradeSet / TradeSets（`net.minecraft.world.item.trading`）

```java
public class TradeSet {
    public static final Codec<TradeSet> CODEC = RecordCodecBuilder.create(i -> i.group(
        RegistryCodecs.homogeneousList(Registries.VILLAGER_TRADE).fieldOf("trades").forGetter(...),  // HolderSet<VillagerTrade>：tag 引用或列表
        NumberProviders.CODEC.fieldOf("amount").forGetter(...),                                     // 每级生成 offer 数
        Codec.BOOL.optionalFieldOf("allow_duplicates", false).forGetter(...),
        Identifier.CODEC.optionalFieldOf("random_sequence").forGetter(...)
    ).apply(i, TradeSet::new));
    ...
    public int calculateNumberOfTrades(LootContext) { return amount.getInt(lootContext); }
}
```

- `TradeSets` 提供全部 `ResourceKey<TradeSet>` 常量（`ARMORER_LEVEL_1`…`WANDERING_TRADER_*`）与 `bootstrap(BootstrapContext<TradeSet>)`。
- `TradeSets.register(...)` 默认 `amount = ConstantValue.exactly(2.0F)`（即每级 2 笔），`random_sequence = <id>.withPrefix("trade_set/")`。

### 2.4 新 VillagerTrades（`net.minecraft.world.item.trading.VillagerTrades`）

- **只**是 `ResourceKey<VillagerTrade>` 常量集合（`FARMER_1_WHEAT_EMERALD`…）+ `bootstrap(BootstrapContext<VillagerTrade>)` + 若干构建 `LootItemFunction` 的静态助手（`enchantedBook` 等）。
- **`ItemListing`、`TRADES`、`WANDERING_TRADER_TRADES`、`EXPERIMENTAL_TRADES` 全部删除**（对 26.2 `VillagerTrades.java` 全文 grep 无任何命中）。

### 2.5 MerchantOffer / ItemCost（`net.minecraft.world.item.trading`）

- `MerchantOffer`：**公开 API 与 1.21.1 完全一致**（字段、构造器、getter 逐一同名同参，见对照 diff）。`STREAM_CODEC` 字段顺序也一致。
- `ItemCost`：record 增加第 4 个成分 `ItemStack itemStack`（缓存派生栈），但公开构造器 `ItemCost(ItemLike)` / `ItemCost(ItemLike, int)` 不变。
- 结论：**mod 对 `MerchantOffer` / `ItemCost` 的既有调用零改动**。

### 2.6 注册与运行时（26.2）

- `Registries.java`：`VILLAGER_PROFESSION`(251)、`TRADE_SET`(296)、`VILLAGER_TRADE`(301) 三个 registry key。
- `BuiltInRegistries.java:212`：`VILLAGER_PROFESSION` 是**内置 DefaultedRegistry**（bootstrap 填充，mod init 仍可写入）。
- `RegistryDataLoader.java:129-130`：`VILLAGER_TRADE` 与 `TRADE_SET` 在**首张 datapack registry 列表**（`WORLDGEN_REGISTRIES`）中，用 `VillagerTrade.CODEC` / `TradeSet.CODEC` 从数据包 JSON 加载。
- `VanillaRegistries.java:110-111`：`.add(Registries.VILLAGER_TRADE, VillagerTrades::bootstrap)` / `.add(Registries.TRADE_SET, TradeSets::bootstrap)` 提供内置内容；**mod 数据包 JSON 与其共存（additive）**。
- 村民运行时（`Villager.java:802-808`）：`updateTrades(ServerLevel)` → `profession.getTrades(data.level())` → `ResourceKey<TradeSet>` → `addOffersFromTradeSet(...)`。
- `AbstractVillager.java:232-251`：`addOffersFromTradeSet` → `registryAccess().lookupOrThrow(Registries.TRADE_SET).getOptional(key)` → `TradeSet.calculateNumberOfTrades(lootContext)`（amount）→ 遍历 `HolderSet<VillagerTrade>` 抽 offer。
- 流浪商人（`wanderingtrader/WanderingTrader.java:130-134`）：`updateTrades` 固定用 `TradeSets.WANDERING_TRADER_BUYING / UNCOMMON / COMMON`。

### 2.7 数据包 JSON 结构（vanilla 样例，`minecraft26.2/data/`）

`data/minecraft/villager_trade/farmer/1/wheat_emerald.json`：
```json
{
  "gives": { "id": "minecraft:emerald" },
  "max_uses": 16.0,
  "reputation_discount": 0.05,
  "wants": { "count": 20.0, "id": "minecraft:wheat" },
  "xp": 2.0
}
```

`data/minecraft/tags/villager_trade/farmer/level_1.json`：
```json
{ "values": [ "minecraft:farmer/1/wheat_emerald", ... ] }
```

`data/minecraft/trade_set/farmer/level_1.json`：
```json
{
  "amount": 2.0,
  "random_sequence": "minecraft:trade_set/farmer/level_1",
  "trades": "#minecraft:farmer/level_1"
}
```

---

## 三、API 映射表

> 说明列：**改名** = 仅类/方法改名；**架构** = 调用方式/注册方式变化；**数据** = 需要数据迁移（JSON/存档）。

### 3.1 类级别映射

| 旧 API（1.21.1） | 新 API（26.2） | 类型 |
|---|---|---|
| `net.minecraft.world.entity.npc.VillagerProfession`（6 字段 record） | `net.minecraft.world.entity.npc.villager.VillagerProfession`（7 字段 record） | **架构**：包移动 + `name` String→Component + 新增 `tradeSetsByLevel` |
| `net.minecraft.world.entity.npc.VillagerTrades` | `net.minecraft.world.item.trading.VillagerTrades` | **架构**：包移动 + 内容剧变（只留 `ResourceKey` 常量 + bootstrap） |
| `VillagerTrades.ItemListing`（接口） | **删除** → `VillagerTrade`（数据条目，codec） | **架构**（无对应 Java 接口） |
| `VillagerTrades` 内部交易工厂类（`EmeraldForItems` 等） | **删除** → `VillagerTrade` JSON 条目 | **架构 + 数据** |
| — | **新增** `VillagerTrade`（`world.item.trading`） | 数据驱动单笔交易 |
| — | **新增** `TradeSet` / `TradeSets`（`world.item.trading`） | 数据驱动交易集 / key 常量 |
| — | **新增** `TradeCost`（`world.item.trading`） | 数据驱动价格（替代 `ItemStack` 工厂参数） |
| — | **新增** `ItemStackTemplate`（`world.item`） | 数据驱动结果物描述 |
| `MerchantOffer`（`world.item.trading`） | `MerchantOffer`（同包） | **改名**：公开 API 完全不变 |
| `ItemCost`（`world.item.trading`） | `ItemCost`（同包） | **改名**：公开构造器不变（内部加缓存字段） |

### 3.2 字段 / 方法映射

| 旧 API | 新 API | 类型 |
|---|---|---|
| `VillagerProfession(String name, ...)` | `VillagerProfession(Component name, ..., Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel)` | **架构**：+1 字段，`name` 类型变 |
| `VillagerProfession.name()` → String | `VillagerProfession.name()` → Component | **改名** |
| — | `VillagerProfession.getTrades(int level)` → `@Nullable ResourceKey<TradeSet>` | **新增** |
| `VillagerTrades.TRADES` / `EXPERIMENTAL_TRADES` / `WANDERING_TRADER_TRADES` | **删除** → `Registries.TRADE_SET` + `TradeSets.*` | **架构 + 数据** |
| `VillagerTrades.ItemListing#getOffer(Entity, RandomSource)` | `VillagerTrade#getOffer(LootContext)` | **架构**：签名彻底不同 |
| `new MerchantOffer(ItemCost, ItemStack, maxUses, xp, priceMultiplier)` | 同名同参 | **改名**（无变化） |
| `new MerchantOffer(ItemCost, Optional<ItemCost>, ItemStack, maxUses, xp, priceMultiplier)` | 同名同参 | **改名**（无变化） |
| `new ItemCost(ItemLike, int)` | `new ItemCost(ItemLike, int)` | **改名**（无变化） |
| 交易 `priceMultiplier`（0.05/0.2） | `VillagerTrade.reputation_discount` | **改名**（语义一致：进 `MerchantOffer.priceMultiplier`） |
| 交易 `maxUses` / `experience` | `VillagerTrade.max_uses` / `xp` | **改名**（数值原样迁移） |
| 每级 2 笔（`addOffersFromItemListings(..., 2)`） | `TradeSet.amount = 2.0` | **数据**（保留行为） |

### 3.3 数据迁移需求

- **存档数据**：`MerchantOffers`（村民身上）的序列化经 `MerchantOffer.CODEC` / `STREAM_CODEC`，两版本字段名与顺序一致，**无存档迁移**。
- **交易定义数据**：mod 的 27 笔交易需从 Java 迁移到 datapack JSON（详见第五节）。这是本次迁移**唯一需要做数据迁移**的部分。
- 旧 `VillagerTrades.TRADES` 中按「职业×等级」组织的 `ItemListing[]`，迁移为「等级 tag → TradeSet → 职业 tradeSetsByLevel」。

---

## 四、运行时数据流对比

```
1.21.1:
  Villager.updateTrades()
    → VillagerTrades.TRADES.get(profession).get(level)      // 静态 Java Map
    → addOffersFromItemListings(offers, itemListings, 2)     // 抽 2 笔
    → ItemListing.getOffer(entity, random) → MerchantOffer

26.2:
  Villager.updateTrades(ServerLevel)
    → profession.getTrades(level)                            // Int2ObjectMap 查 ResourceKey<TradeSet>
    → addOffersFromTradeSet(level, offers, tradeSetKey)
    → registryAccess().lookupOrThrow(TRADE_SET).getOptional(key)   // datapack registry
    → tradeSet.calculateNumberOfTrades(lootContext)          // amount
    → 遍历 tradeSet.getTrades() (HolderSet<VillagerTrade>)
    → VillagerTrade.getOffer(lootContext) → MerchantOffer
```

---

## 五、风险分析

### 5.1 TradeSet 是否替代 VillagerTrades？

**是，完全替代。** 1.21.1 的 `VillagerTrades`（`TRADES`/`WANDERING_TRADER_TRADES` 静态 Map）在 26.2 已删除；其职责由两个 datapack registry 承担：
- `villager_trade`：单笔交易（对应旧 `ItemListing` 工厂产生的结果）；
- `trade_set`：按等级/来源组织的交易集合（对应旧「职业×等级→ItemListing[]」），并通过 `VillagerProfession.tradeSetsByLevel` 与职业绑定。

### 5.2 是否已经数据驱动？

**是。** `VILLAGER_TRADE`、`TRADE_SET` 由 `RegistryDataLoader` 从数据包 JSON 加载（内置内容经 `VanillaRegistries.bootstrap` 提供）。村民/流浪商人运行时经 `registryAccess` 查询。这意味着：
- 交易内容（价格/数量/次数/经验/等级）**必须落到 `data/` 下的 JSON**，才能被服务器加载并同步到客户端。
- 只改 Java 不再生效；`IafTrades.init()` 里旧式 `registerVillagerOffers` 已无对应 API。

### 5.3 自定义交易是否仍然 Java 注册？

**分层结论：**
| 层面 | 是否仍可 Java 注册 | 说明 |
|---|---|---|
| 自定义职业 `VillagerProfession` | ✅ 是 | 内置 DefaultedRegistry，DeferredRegister 在 mod init 写入（工作区现状 `IafTrades` 已编译通过） |
| 自定义 POI `PoiType` | ✅ 是 | 内置 SimpleRegistry（`BuiltInRegistries:215`），`PoiTypes.TYPE_BY_STATE` 经现有 access widener 可达 |
| 自定义交易 `VillagerTrade` / `TradeSet` | ❌ 否（本阶段无 Mixin 路径） | 属 datapack registry，无 Java mod-init 写入点。**必须提供 datapack JSON** |

**推荐方案（无 Mixin，保持功能）：**
1. `data/iceandfire/villager_trade/scribe/<level>/<trade>.json`：27 笔交易逐笔转 JSON（价格/数量/maxUses/xp/reputation_discount 原样）。
2. `data/iceandfire/tags/villager_trade/scribe_level_<n>.json`：5 个等级 tag，列出该级交易 id。
3. `data/iceandfire/trade_set/scribe/level_<n>.json`：5 个 TradeSet，`trades` 引用对应 tag，`amount = 2.0`（保留旧每级 2 笔行为），`random_sequence` 按 vanilla 约定。
4. `IafTrades` 中 `SCRIBE_PROFESSION` 的 `tradeSetsByLevel` 填入 `ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("iceandfire", "scribe/level_<n>"))`（1–5）。
5. 交易内容离开 Java 后，`TradeOfferHelper` / `TradeOfferInternals` / `BuyWithPrice` 三个文件失去作用对象（其「注册交易」职责被数据包替代），按**清理死代码**移除；`IafTrades.init()` 只保留 POI 挂载与职业注册。

> 备选方案（**本阶段明确排除**）：Mixin 注入 `VanillaRegistries.bootstrap` 或 `RegistryDataLoader`，在 datapack bootstrap 期把 27 笔交易注册进 `VILLAGER_TRADE`/`TRADE_SET`。该方案能保留纯 Java 写法，但依赖 Mixin，违反本阶段「禁止 Mixin」约束。

### 5.4 其他风险点

- **功能静默丢失**：若 `tradeSetsByLevel` 为空 Map（当前 `IafTrades` 传 `new Int2ObjectOpenHashMap<>()`），`getTrades(level)` 返回 null → Scribe 村民**完全无交易**。迁移必须把 5 个 TradeSet key 填进职业，否则编译虽过、功能已废。
- **行为保留**：
  - 每级 offer 数：1.21.1 固定抽 2 笔 → `amount=2.0`。
  - `reputation_discount` 承载旧 `priceMultiplier`（0.05 / 0.05 / 0.2 三个 multiplier 原样迁移）。
  - `maxUses`、`xp`、物品 id/数量逐一对应（见第六节清单）。
- **职业显示名**：1.21.1 是 `String name`，显示 key 走 `entity.minecraft.villager.<name>`；26.2 是 `Component`，bootstrap 约定 key `entity.<ns>.villager.<path>`。mod 现有 lang 里有 `entity.minecraft.villager.scribe` / `entity.minecraft.villager.iceandfire.scribe`，迁移后需补/改用 `entity.iceandfire.villager.scribe`（或显式构造与旧 key 一致的 `Component`），避免显示成原文。
- **POI 侧**：`PoiType` record（`(Set<BlockState>, int, int)`）与 `PoiTypes.TYPE_BY_STATE`（经 access widener）均无需改动；`PoiTypes` 26.2 仍是静态注册（`PoiTypes.java` `TYPE_BY_STATE.put`），行为保持。
- **网络同步**：`VILLAGER_TRADE`/`TRADE_SET` 属首张 datapack registry 列表，客户端随数据包同步，mod JSON 自动下发，无需额外处理。
- **不触碰**：P6（Rendering）/ P5-D（SpawnEgg）涉及的交易无关 API 一律不动。

---

## 六、本 mod 需迁移的交易全清单（27 笔，内容保留基准）

> 来源：`common/src/main/java/com/iafenvoy/iceandfire/registry/IafTrades.java`（与 1.21.1 参照源码逐字一致）。
> 字段含义：`wants`（玩家付出）→ `gives`（村民给出），`maxUses` / `xp` / `reputation_discount`（原 multiplier）。
> multiplier 常量：`emeraldForItems=0.05F`、`itemForEmerald=0.05F`、`rareItemForEmerald=0.2F`。

| Lv | # | wants | gives | maxUses | xp | rep.discount |
|---|---|---|---|---|---|---|
| 1 | 1 | 1 emerald | 4 manuscript | 25 | 2 | 0.05 |
| 1 | 2 | 6 manuscript | 1 emerald | 10 | 5 | 0.05 |
| 1 | 3 | 3 bookshelf | 1 emerald | 8 | 3 | 0.05 |
| 1 | 4 | 15 paper | 2 emerald | 4 | 4 | 0.05 |
| 1 | 5 | 10 ash | 1 emerald | 8 | 4 | 0.05 |
| 2 | 6 | 5 silver_ingot | 1 emerald | 3 | 5 | 0.05 |
| 2 | 7 | 8 fire_lily | 1 emerald | 3 | 5 | 0.05 |
| 2 | 8 | 7 lightning_lily | 3 emerald | 2 | 5 | 0.05 |
| 2 | 9 | 3 emerald | 4 frost_lily | 3 | 3 | 0.05 |
| 2 | 10 | 2 emerald | 7 dragon_ice_spikes | 2 | 3 | 0.05 |
| 2 | 11 | 1 sapphire_gem | 2 emerald | 30 | 3 | 0.2 |
| 2 | 12 | 2 emerald | 1 pixie_jar_empty | 3 | 4 | 0.05 |
| 2 | 13 | 1 amethyst_shard | 3 emerald | 20 | 3 | 0.2 |
| 3 | 14 | 6 dragonbone | 1 emerald | 7 | 4 | 0.05 |
| 3 | 15 | 2 chain | 3 emerald | 4 | 2 | 0.05 |
| 3 | 16 | 6 emerald | 2 pixie_dust | 8 | 3 | 0.05 |
| 3 | 17 | 6 emerald | 2 fire_dragon_flesh | 8 | 3 | 0.05 |
| 3 | 18 | 7 emerald | 1 ice_dragon_flesh | 8 | 3 | 0.05 |
| 3 | 19 | 8 emerald | 1 lightning_dragon_flesh | 8 | 3 | 0.05 |
| 4 | 20 | 10 emerald | 2 dragonbone | 20 | 5 | 0.05 |
| 4 | 21 | 4 emerald | 1 shiny_scales | 5 | 2 | 0.05 |
| 4 | 22 | 5 dread_shard | 1 emerald | 10 | 4 | 0.05 |
| 4 | 23 | 8 emerald | 12 stymphalian_bird_feather | 3 | 6 | 0.05 |
| 4 | 24 | 4 emerald | 12 troll_tusk | 7 | 3 | 0.05 |
| 5 | 25 | 15 emerald | 3 sea_serpent_fang | 20 | 3 | 0.05 |
| 5 | 26 | 12 emerald | 1 hydra_fang | 20 | 3 | 0.05 |
| 5 | 27 | 6 ectoplasm | 1 emerald | 7 | 3 | 0.05 |

**对应 registry id**（JSON `wants`/`gives` 的 `id` 用）：
- minecraft：`minecraft:emerald`、`minecraft:bookshelf`、`minecraft:paper`、`minecraft:amethyst_shard`
- iceandfire 物品：`iceandfire:manuscript`、`iceandfire:silver_ingot`、`iceandfire:sapphire_gem`、`iceandfire:dragonbone`、`iceandfire:fire_dragon_flesh`、`iceandfire:ice_dragon_flesh`、`iceandfire:lightning_dragon_flesh`、`iceandfire:pixie_dust`、`iceandfire:shiny_scales`、`iceandfire:stymphalian_bird_feather`、`iceandfire:troll_tusk`、`iceandfire:sea_serpent_fang`、`iceandfire:chain`、`iceandfire:dread_shard`、`iceandfire:hydra_fang`、`iceandfire:ectoplasm`
- iceandfire 方块：`iceandfire:ash`、`iceandfire:fire_lily`、`iceandfire:frost_lily`、`iceandfire:lightning_lily`、`iceandfire:dragon_ice_spikes`、`iceandfire:pixie_jar_empty`

**JSON 模板**（按 vanilla 样例）：

`data/iceandfire/villager_trade/scribe/1/manuscript_emerald.json`（对应 #1）：
```json
{
  "wants": { "id": "minecraft:emerald", "count": 1.0 },
  "gives": { "id": "iceandfire:manuscript", "count": 4.0 },
  "max_uses": 25.0,
  "xp": 2.0,
  "reputation_discount": 0.05
}
```

`data/iceandfire/tags/villager_trade/scribe_level_1.json`：
```json
{
  "values": [
    "iceandfire:scribe/1/manuscript_emerald",
    "iceandfire:scribe/1/emerald_manuscript",
    "iceandfire:scribe/1/bookshelf_emerald",
    "iceandfire:scribe/1/paper_emerald",
    "iceandfire:scribe/1/ash_emerald"
  ]
}
```

`data/iceandfire/trade_set/scribe/level_1.json`：
```json
{
  "amount": 2.0,
  "random_sequence": "iceandfire:trade_set/scribe/level_1",
  "trades": "#iceandfire:scribe_level_1"
}
```

---

## 七、验证与错误基准

### 7.1 当前（迁移前）编译错误（`./gradlew :common:compileJava`）

- 全量错误：**约 2301**（本次实测 javac 原始行数 4602，含重复/汇总段；其中交易相关文件错误为以下 3 个文件 19 个错误行）：
  - `util/trade/factory/BuyWithPrice.java:13`（`VillagerTrades.ItemListing` 不存在）、`:35`（`getOffer` 不再 override 任何父接口）——2 行。
  - `util/trade/TradeOfferHelper.java:5,21,32,50,60,70,80`（`npc.VillagerProfession` 包不存在、`ItemListing` 不存在）——7 行。
  - `util/trade/TradeOfferInternals.java:13,25,27,30,31,35,36,39,40,42`（import 失败、`TRADES`/`WANDERING_TRADER_TRADES`/`ItemListing` 不存在）——11 行（含多处一行多符号）。
- **`IafTrades.java` 当前 0 错误**（import 已换新包 `npc.villager.VillagerProfession`，`PoiTypes.TYPE_BY_STATE` 经 access widener 放开）。但它传入 `new Int2ObjectOpenHashMap<>()`，`tradeSetsByLevel` 为空 —— **功能层面交易已丢失**，仅编译可通过。

### 7.2 迁移后目标

- **交易相关错误清零**：`BuyWithPrice` / `TradeOfferHelper` / `TradeOfferInternals` 不再引用已删除 API；`IafTrades` 的 `tradeSetsByLevel` 填 5 个 TradeSet key 后保持可编译。
- **不动 P6/P5-D**：Rendering（约 1298）、SpawnEgg（约 109）、Uranus raycoms、其他 Entity/Block/Item API 错误一律不处理，剩余错误数应 ≈ 2301 − 交易错误行数。

---

## 八、计划修改文件清单（供第二阶段执行）

| 文件 | 动作 |
|---|---|
| `registry/IafTrades.java` | `SCRIBE_PROFESSION` 的 7 参构造填 `tradeSetsByLevel`（5 个 `ResourceKey<TradeSet>`）；`init()` 去掉 `TradeOfferHelper.registerVillagerOffers(...)` 段，保留 POI 挂载与职业注册 |
| `util/trade/factory/BuyWithPrice.java` | 删除（功能被 `VillagerTrade` JSON 替代） |
| `util/trade/TradeOfferHelper.java` | 删除（注册职责被数据包替代） |
| `util/trade/TradeOfferInternals.java` | 删除（同上） |
| `data/iceandfire/villager_trade/scribe/<lv>/*.json` | **新增** 27 笔交易 JSON |
| `data/iceandfire/tags/villager_trade/scribe_level_<n>.json` | **新增** 5 个等级 tag |
| `data/iceandfire/trade_set/scribe/level_<n>.json` | **新增** 5 个 TradeSet JSON |
| `assets/iceandfire/lang/en_us.json` 等 | 补 `entity.iceandfire.villager.scribe` 显示 key（多语言各一份，或显式构造 Component） |

> 说明：删除 `TradeOfferHelper`/`TradeOfferInternals`/`BuyWithPrice` 属于**清理被 26.2 架构淘汰的死代码**，不是删除交易功能——全部 27 笔交易内容通过数据包 JSON 完整保留（价格/数量/maxUses/experience/villager level 与 1.21.1 逐字一致）。

---

## 九、结论

- 迁移核心不是「改 API 名」，而是**交易内容从 Java 搬进 datapack JSON** + 职业 record 的 `tradeSetsByLevel` 引用 TradeSet key。
- `MerchantOffer`/`ItemCost` 无需改动；`VillagerProfession` 需要换包、换构造（7 参、`Component`、`Int2ObjectMap<ResourceKey<TradeSet>>`）；`VillagerTrades.ItemListing`/静态 Map 已被 `VillagerTrade`+`TradeSet` 完全取代。
- 推荐纯数据包方案（无 Mixin），备选的 bootstrap Mixin 注入方案本阶段明确排除。
- 交易内容 27 笔完整清单、JSON 模板与 registry id 已在第六节给出，供第二阶段直接落地。
