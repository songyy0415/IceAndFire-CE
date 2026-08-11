# Fabric API 迁移文档（1.21.1 → 26.2）

> 文档日期：2026-08-09
> 项目：IceAndFire-CE（MC 1.21.1 → 26.2，Architectury，Fabric 目标）
> 依据：`docs/.research/fabric-api.md`（主参考）、`docs/.research/fabric-module.md`，并已对照 `D:/IceAndFire-CE/common/src` 与 `D:/IceAndFire-CE/fabric/src` 源码逐条核验。
> 未经验证的项一律标注「待确认」。

---

## 0. 结论速览（先读）

1. **Fabric Loader 0.16.7 → 0.19.3 对项目是 100% 兼容**。`ModInitializer` / `ClientModInitializer` / `EnvType` / `Environment` / `FabricLoader` 等全部符号不变，只需升版本号。
2. **Fabric API 0.116.5+1.21.1 → 0.156.0+26.2 只有 3 处硬性改动**：
   - 酿药注册类 `FabricBrewingRecipeRegistryBuilder` → `FabricPotionBrewingBuilder`（类级重命名，`fabric-content-registries-v0`）。
   - 粒子注册类 `ParticleFactoryRegistry` → `ParticleProviderRegistry`（类级重命名，`fabric-particles-v1`）。
   - 资源包 API：`fabric-resource-loader-v0` 整体移入 `deprecated/`，`ResourceManagerHelper` / `ResourcePackActivationType` 仍在（编译可用，产生弃用警告）；长期方向是 `fabric-resource-loader-v1` 的 `ResourceLoader.registerBuiltinPack`。
3. **所有 events / networking / rendering / registry 相关的 Fabric API 本项目均未使用**（grep 实测 0 命中），全部走 Architectury 路由，无需迁移。
4. **贯穿性改名**：`net.minecraft.resources.ResourceLocation` → `net.minecraft.resources.Identifier`（MC 26.2 全局），会改写 `IceAndFireFabricClient.java`、`IafAttachments.java` 以及 access widener 里的目标描述符。这是 Minecraft 层面的问题（详见 `docs/minecraft-api-migration.md`），但会直接改变 Fabric API 调用点文本。
5. **Access Widener 有 5~6 处必改**（粒子内部类删除、箭矢/三叉戟包移动、`getBaseExperienceReward` 签名、`GameRenderer.loadEffect` 改名）。
6. **附属风险独立于 Fabric API**：Trinkets 依赖被 PB4 全面重写（`dev.emi.trinkets.api` → `eu.pb4.trinkets.api`），`Item.inventoryTick` 签名也变了——这两处是 fabric 模块里最重的工作，但不属于 Fabric API 本身。

---

## 1. 版本概述

| 依赖 | 1.21.1（当前，`gradle.properties`） | 26.2 目标 | 备注 |
|---|---|---|---|
| `fabric_loader_version` | `0.16.7` | `0.19.3`（参考版） | 26.2 各 fabric-api 模块声明 `fabricloader >= 0.18.4` |
| `fabric_api_version` | `0.116.5+1.21.1` | `0.156.0+26.2`（参考版） | 实际最新版本号待确认 |
| `minecraft_version` | `1.21.1` | `26.2` | 配套的 fabric/architectury 坐标都依赖它 |

- 依赖注入点（两处都要升）：
  - `fabric/build.gradle:32` `modImplementation "net.fabricmc:fabric-loader:$rootProject.fabric_loader_version"`
  - `fabric/build.gradle:35` `modImplementation "net.fabricmc.fabric-api:fabric-api:$rootProject.fabric_api_version"`
  - `common/build.gradle:13` `modImplementation "net.fabricmc:fabric-loader:$rootProject.fabric_loader_version"`（common 只为了 `@Environment` 注解而依赖 loader，源码注释亦如此声明——见 `common/build.gradle:10-12`）
- 26.2 关键模块归属（与本项目相关的）：
  - `fabric-data-attachment-api-v1`：不变。
  - `fabric-object-builder-api-v1`：不变（本项目未使用）。
  - `fabric-content-registries-v0`：不变，内部酿药类改名。
  - `fabric-particles-v1`：不变，内部工厂→提供者改名。
  - `fabric-resource-loader-v0` → `deprecated/fabric-resource-loader-v0`（仍在 BOM 中发布）+ 新 `fabric-resource-loader-v1`。
  - `fabric-key-binding-api-v1` → `fabric-key-mapping-api-v1`：模块改名，本项目无按键绑定，不涉及。

---

## 2. 分族迁移

### 2.1 Registry / BuiltInRegistries

**本项目不直接使用任何 `net.fabricmc.fabric.api.registry.*` / `fabric.api.object.builder` 的注册 API**（grep `common/src` + `fabric/src` = 0 命中）。所有方块/物品/实体等注册都走 vanilla `BuiltInRegistries`（common 侧）或 Architectury 的注册抽象。

- `IceAndFireFabric.java:8` 的 `import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;` 是**死导入**（方法体内未调用），删除即可；若保留，该 API 在 26.2 仍存在（`register(EntityType, AttributeSupplier.Builder)` / `register(EntityType, AttributeSupplier)` 均未变），不会报错。
- Fabric API 的注册模块本身在 26.2 无影响本项目的破坏性变化。
- 真正的注册层变化在 Minecraft 侧（`ResourceLocation`→`Identifier`、注册表键类型、`BuiltInRegistries` 内部条目类型等），见 `docs/minecraft-api-migration.md`。

### 2.2 Events

**本项目不直接使用任何 `net.fabricmc.fabric.api.event.*`**（grep = 0 命中）。事件全部经 Architectury 路由：
- `IafAttachments.init()` 订阅的是 common 侧的 `com.iafenvoy.iceandfire.event.CommonEvents.LIVING_TICK`（`IafAttachments.java:22`）。
- 26.2 下不需要改动；只需确认 Architectury 26.2 提供对应的 `LIVING_TICK` 事件（属于 `docs/dependency-migration.md` 范畴）。

### 2.3 Networking / payload

**本项目不直接使用任何 `net.fabricmc.fabric.api.networking.*`**（grep = 0 命中）。数据包（payload）经 Architectury 网络栈收发（common 侧），`fabric-networking-api-v1` 模块 26.2 未变且本项目未直接用。无需迁移。

> 注：Fabric `AttachmentType` 的 `syncWith` 用的是 `StreamCodec<RegistryFriendlyByteBuf, A>`，项目 `ChainData/MiscData/PortalData` 的 `PACKET_CODEC` 已是该类型（`ChainData.java:24` 等），与 26.2 签名完全一致。

### 2.4 Client — 粒子（Particles）

**类重命名（硬性）**：`net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry` → `...ParticleProviderRegistry`（同模块 `fabric-particles-v1`、同包）。

| 旧 | 新 |
|---|---|
| `ParticleFactoryRegistry.getInstance()` | `ParticleProviderRegistry.getInstance()`（26.2 :41） |
| `register(ParticleType<T>, ParticleProvider<T>)` | `register(ParticleType<T>, ParticleProvider<T>)`（26.2 :52，Mojmap 签名一致） |
| `register(ParticleType<T>, PendingParticleFactory<T>)` | `register(ParticleType<T>, PendingParticleProvider<T>)`（26.2 :62） |
| `PendingParticleFactory<T>.create(FabricSpriteProvider)` | `PendingParticleProvider<T>.create(FabricSpriteSet)` |

**关键缓解点（已核验）**：`IafRenderers.registerParticleRenderers`（`common/src/.../registry/IafRenderers.java:98-108`）注册的全部 10 个粒子都用 `ParticleProviderHolder<ParticleType, ParticleProvider>` 的**单参构造器**（传入 `BloodParticle::factory` 等 `ParticleProvider` 工厂，全部 particle 类 `factory(...)` 均返回 `ParticleProvider`）。**没有任何粒子走扩展的 `SpriteParticleRegistration` 路径**，所以 `IceAndFireFabricClient.java:20` 里的第二个 lambda（扩展工厂分支）实际是死代码，可直接删掉。

**连带影响（common 侧，须一起改）**：`common/src/.../impl/ParticleProviderHolder.java` 的字段、第二构造器、`applyRegister` 签名都引用 vanilla `net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration`。该内部类在 **MC 26.2 已被移除**（26.2 `ParticleEngine` 无嵌套 `SpriteParticleRegistration`，粒子提供者改由 `resourceManager.getProviders()` 按 `BuiltInRegistries.PARTICLE_TYPE.getId(...)` 解析）。因此 `ParticleProviderHolder` 必须去掉扩展分支（推荐），否则 common 无法编译。这同时使 access widener 中的对应条目失效（见 2.8）。

### 2.5 Client — 资源包（Resource Packs）

模块 `fabric-resource-loader-v0` 在 26.2 移入 `deprecated/fabric-resource-loader-v0`（仍构建并随 fabric-api BOM 发布）。项目使用的两个符号**仍可编译**，但标 `@Deprecated`：

| 旧 | 26.2 状态 |
|---|---|
| `net.fabricmc.fabric.api.resource.ResourceManagerHelper` | 仍存在（deprecated v0 中，FQN 不变，:41） |
| `registerBuiltinResourcePack(Identifier, ModContainer, Component, ResourcePackActivationType)` | 仍存在、签名不变（v0 :126） |
| `ResourcePackActivationType`（NORMAL / ALWAYS_ENABLED / DEFAULT_ENABLED） | 仍存在（v0 :25-29），`NORMAL` 包装 v1 `PackActivationType.NORMAL` |

- **近期做法（推荐，改动最小）**：保留 deprecated v0 的调用，仅把 `ResourceLocation` → `Identifier`。编译会有弃用警告，可接受。
- **长期方向**：新模块 `fabric-resource-loader-v1` 的 `net.fabricmc.fabric.api.resource.v1.ResourceLoader.registerBuiltinPack(Identifier, ModContainer, Component, PackActivationType)` 与 `net.fabricmc.fabric.api.resource.v1.pack.PackActivationType`。v1 会把每个 mod 的资源目录自动注册为资源包（`ModPackResourcesUtil`，`ALWAYS_ENABLED`），**目前 v1 没有与 `registerBuiltinResourcePack` 等价的公开 API**；若 `iceandfire:iaf_legacy` 内置包需要 v1 能力再迁移，否则先留在 deprecated v0。
- 使用点：`IceAndFireFabricClient.java:22`（注册 `iaf_legacy` 包，非开发环境时）。

### 2.6 Client — 渲染（Rendering）

**本项目不直接使用 `fabric.api.client.rendering.*` / `fabric.api.renderer.*`**（grep = 0 命中）。渲染走 vanilla 或 Architectury。与 Fabric API 相关的仅两条 access widener 条目（见 2.8）：`ParticleEngine$SpriteParticleRegistration`（删除）与 `GameRenderer.loadEffect`（改名）。其余渲染改动（如 shader、`PostChain`、渲染管线）属 Minecraft 层面，见 `docs/minecraft-api-migration.md`。

### 2.7 入口点（Entrypoints）与 fabric.mod.json

- Loader 0.19.3 仍读 `schemaVersion: 1`（`ModMetadataParser.java:65-99`；缺省按 0 处理）。**无需改。**
- `entrypoints` 的 `main` / `client` / `emi` / `jade` / `modmenu` 都是**自定义字符串键**，Loader 当作数据不做固定名称注册，0.19.3 无新增必填入口点。**无需改。**
- `mixins: ["iceandfire.mixins.json"]` 指向 **common** 资源（`common/src/main/resources/iceandfire.mixins.json`），经 shadow 打进 fabric jar，概念不变。
- `accessWidener: "iceandfire.accesswidener"` 字段不变，头部 `accessWidener v2 named` 在 0.19.3 不变。
- **必须改**：`depends.minecraft` `"1.21.x"` → `"26.x"`；建议补 `"fabricloader": ">=0.18.4"` 与 `"fabric-api": "*"`（当前 fabric 依赖只在 build.gradle 声明）。
- `jupiter`/`uranus` 版本约束需随 26.2 版升级（见 `docs/dependency-migration.md`）。

### 2.8 Access Widener

文件 `fabric/src/main/resources/iceandfire.accesswidener`（与 `common/src/main/resources/iceandfire.accesswidener` **逐字节相同**，已 diff 核验）。**任何修复必须同时改 common 副本**（`fabric/build.gradle:11` 通过 `project(":common").loom.accessWidenerPath` 继承 AW，common 是唯一权威副本）。

20 条对 26.2 的状态与处理：

| 行 | 条目 | 26.2 处理 |
|---|---|---|
| 2 | `accessible class ...ParticleEngine$SpriteParticleRegistration` | **删除**。26.2 已无此内部类（替换为 `ParticleProvider$Sprite`） |
| 6 | `...projectile/ThrownTrident ID_LOYALTY` | **改包名** → `...projectile/arrow/ThrownTrident ID_LOYALTY` |
| 7 | `...projectile/ThrownTrident ID_FOIL` | **改包名** → `...projectile/arrow/ThrownTrident ID_FOIL` |
| 8 | `...projectile/ThrownTrident dealtDamage Z` | **改包名** → `...projectile/arrow/ThrownTrident dealtDamage Z` |
| 9 | `...projectile/AbstractArrow pickupItemStack ...` | **改包名** → `...projectile/arrow/AbstractArrow pickupItemStack ...` |
| 10 | `...projectile/AbstractArrow setPierceLevel (B)V` | **改包名** → `...projectile/arrow/AbstractArrow setPierceLevel (B)V` |
| 13 | `...LivingEntity getBaseExperienceReward ()I` | **改描述符** → `(Lnet/minecraft/server/level/ServerLevel;)I`（26.2 方法带 `ServerLevel` 参数） |
| 17 | `...GameRenderer loadEffect (Lnet/minecraft/resources/ResourceLocation;)V` | **改方法名+描述符**。`loadEffect` 已不存在，改为 `setPostEffect (Lnet/minecraft/resources/Identifier;)V`（同时 `ResourceLocation`→`Identifier`）；若不需要访问 shader 后处理则直接删除 |
| 19 | `...PoiTypes TYPE_BY_STATE Ljava/util/Map;` | 值类型语义变化（`Map<BlockState, Holder<PoiType>>`），擦除描述符仍匹配，**AW 无需改**；但读它的 common 代码需处理 `Holder<PoiType>`（属 Minecraft 侧） |
| 其余 11 条 | `EntityRenderDispatcher.renderers`、`Mob.goalSelector/targetSelector`、`NodeEvaluator.mob`、`ClientLevel.entityStorage`、`CombatTracker.getMostSignificantFall`、`Camera.getMaxZoom/move`、`Screen.renderables`、`SimpleParticleType.<init>` | **OK，无需改**（其中部分私有度变化，`accessible` 仍然适用；`getMostSignificantFall`/`getMaxZoom` 现在为 `private`，仍可访问） |

---

## 3. 逐项改动明细（旧API → 新API / 修改位置 / 原因）

| # | 旧API | 新API | 修改位置 | 原因 |
|---|---|---|---|---|
| 1 | `fabric_loader_version=0.16.7` | `fabric_loader_version=0.19.3` | `gradle.properties:15` | 26.2 生态要求 loader ≥ 0.18.4 |
| 2 | `fabric_api_version=0.116.5+1.21.1` | `fabric_api_version=0.156.0+26.2` | `gradle.properties:16` | 26.2 Fabric API 版本 |
| 3 | `minecraft_version=1.21.1` | `minecraft_version=26.2` | `gradle.properties:12` | 目标版本 |
| 4 | `net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder` | `net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder` | `IceAndFireFabric.java:9`（import）+ `:19`（`BUILD` 引用） | 26.2 fabric-content-registries-v0 类级重命名 |
| 5 | `FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(Potions.WATER, IafItems.SHINY_SCALES.get(), Potions.WATER_BREATHING))` | `FabricPotionBrewingBuilder.BUILD.register(builder -> builder.addMix(Potions.WATER, IafItems.SHINY_SCALES.get(), Potions.WATER_BREATHING))` | `IceAndFireFabric.java:19` | 同 #4；lambda 参数 `PotionBrewing.Builder.addMix(Holder<Potion>, Item, Holder<Potion>)` 在 26.2 未变 |
| 6 | `import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;` | （删除） | `IceAndFireFabric.java:8` | 死导入，方法体未使用 |
| 7 | `import net.fabricmc.loader.api.FabricLoader;` | （删除） | `IceAndFireFabric.java:10` | 死导入，方法体未使用 |
| 8 | `net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry` | `...ParticleProviderRegistry` | `IceAndFireFabricClient.java:8`（import）+ `:20`（`getInstance()` ×2） | 26.2 fabric-particles-v1 类级重命名 |
| 9 | `holder.applyRegister(ParticleFactoryRegistry.getInstance()::register, (t, f) -> ParticleFactoryRegistry.getInstance().register(t, f::create))` | 推荐：`holder.applyRegister(ParticleProviderRegistry.getInstance()::register)`（删掉第二个 lambda） | `IceAndFireFabricClient.java:20` | 扩展分支无人使用；且 common `ParticleProviderHolder` 需同步去掉 `SpriteParticleRegistration` 分支 |
| 10 | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | `IceAndFireFabricClient.java:13,22`、`IafAttachments.java:12,17-19` | MC 26.2 全局改名（`Identifier.fromNamespaceAndPath`） |
| 11 | `ResourceManagerHelper.registerBuiltinResourcePack(...)` + `ResourcePackActivationType.NORMAL` | 近期：保留（仅改 #10 的 Identifier）；长期：`ResourceLoader.registerBuiltinPack(..., PackActivationType.NORMAL)` | `IceAndFireFabricClient.java:22` | v0 移入 deprecated 但仍发布；v1 无等价公开 API |
| 12 | `AttachmentRegistry.create(ResourceLocation, Consumer<Builder>)` | `AttachmentRegistry.create(Identifier, Consumer<Builder>)` | `IafAttachments.java:17-19` | 仅参数类型改名，Builder 全部方法（`initializer/persistent/syncWith/copyOnDeath`）26.2 未变 |
| 13 | `fabric.mod.json` `depends.minecraft: "1.21.x"` | `"26.x"`；建议加 `"fabricloader": ">=0.18.4"`、`"fabric-api": "*"` | `fabric/src/main/resources/fabric.mod.json:43-47` | 目标版本约束 |
| 14 | AW：`accessible class ...ParticleEngine$SpriteParticleRegistration` | （删除行） | `common/src/main/resources/iceandfire.accesswidener:2` + fabric 副本 | 26.2 已无此内部类 |
| 15 | AW：`...projectile/ThrownTrident` / `...projectile/AbstractArrow`（5 行） | 包名改为 `...projectile/arrow/...` | AW 行 6-10（两副本） | 26.2 箭矢类移入 `world/entity/projectile/arrow/` |
| 16 | AW：`LivingEntity getBaseExperienceReward ()I` | `(Lnet/minecraft/server/level/ServerLevel;)I` | AW 行 13（两副本） | 26.2 方法增加 `ServerLevel` 参数 |
| 17 | AW：`GameRenderer loadEffect (Lnet/minecraft/resources/ResourceLocation;)V` | `setPostEffect (Lnet/minecraft/resources/Identifier;)V` 或删除 | AW 行 17（两副本） | 26.2 `loadEffect` 改名，`ResourceLocation`→`Identifier` |
| 18 | `dev.emi.trinkets.api.TrinketsApi.registerTrinket(Item, Trinket)` | `eu.pb4.trinkets.api.callback.TrinketCallback.setCallback(Item, TrinketCallback)`（或 Item 实现 `TrinketCallback`） | `TrinketsRegistry.java:4,13` | 26.2 Trinkets 由 PB4 全面重写 |
| 19 | `dev.emi.trinkets.api.Trinket` / `dev.emi.trinkets.api.SlotReference` | `eu.pb4.trinkets.api.callback.TrinketCallback` / `eu.pb4.trinkets.api.TrinketSlotAccess` | `SimpleTickItemWrapper.java:3-4,9,17` | 同 #18 |
| 20 | `Item.inventoryTick(ItemStack, Level, Entity, int, boolean)` | `Item.inventoryTick(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)` | `SimpleTickItemWrapper.java:18` | MC 26.2 `Item.inventoryTick` 签名变更（`Item.java:289`）；需从 trinket 槽推导 `EquipmentSlot`、强转 `ServerLevel` |

---

## 4. 必须修改的文件清单与精确编辑

### 4.1 `gradle.properties`（根）
```
minecraft_version=1.21.1        →  minecraft_version=26.2
fabric_loader_version=0.16.7    →  fabric_loader_version=0.19.3
fabric_api_version=0.116.5+1.21.1 →  fabric_api_version=0.156.0+26.2
```
（真实最新版本号待确认；Java 工具链 `org.gradle.java.home=.../zulu-21` 是否适配 26.2 待确认。）

### 4.2 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java`
- 第 8 行：删除死导入 `import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;`
- 第 9 行：`import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;` → `import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;`
- 第 10 行：删除死导入 `import net.fabricmc.loader.api.FabricLoader;`
- 第 19 行：`FabricBrewingRecipeRegistryBuilder.BUILD` → `FabricPotionBrewingBuilder.BUILD`（lambda 体不变）

### 4.3 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java`
- 第 8 行：`import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;` → `...ParticleProviderRegistry;`
- 第 13 行：`import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- 第 20 行（推荐，删扩展分支）：
```java
IafRenderers.registerParticleRenderers(holder -> holder.applyRegister(ParticleProviderRegistry.getInstance()::register));
```
  （若保留两分支写法，则仅把两处 `ParticleFactoryRegistry` 改为 `ParticleProviderRegistry`，但要求 `ParticleProviderHolder.applyRegister` 第二参数类型改为 26.2 的 `ParticleProvider$Sprite` 语义——不推荐。）
- 第 22 行：`ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`；`ResourceManagerHelper` / `ResourcePackActivationType` 保留（deprecated，出弃用警告）；长期迁移（可选）：
```java
// import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
// import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "iaf_legacy"), container, Component.translatable("resourcePack.iceandfire.legacy.name"), PackActivationType.NORMAL);
```

### 4.4 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java`
- 第 12 行：`import net.minecraft.resources.ResourceLocation;` → `import net.minecraft.resources.Identifier;`
- 第 17-19 行：`ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`（×3）
- 其余（`AttachmentRegistry.create`、`initializer/persistent/syncWith/copyOnDeath`、`AttachmentSyncPredicate.all()`、`@SuppressWarnings("UnstableApiUsage")`）**不改**。

### 4.5 `common/src/main/java/com/iafenvoy/iceandfire/impl/ParticleProviderHolder.java`（连带，common 侧）
- 删除字段 `private final ParticleEngine.SpriteParticleRegistration<T> extendedFactory;`
- 删除第二构造器 `ParticleProviderHolder(ParticleType<T>, ParticleEngine.SpriteParticleRegistration<T>)`
- `applyRegister` 改为单参数：`applyRegister(BiConsumer<ParticleType<T>, ParticleProvider<T>> common)`
- 删除 `import net.minecraft.client.particle.ParticleEngine;`

### 4.6 `fabric/src/main/resources/fabric.mod.json`
- `depends.minecraft`：`"1.21.x"` → `"26.x"`
- （可选）`depends` 增补 `"fabricloader": ">=0.18.4"`、`"fabric-api": "*"`
- `jupiter`/`uranus` 版本约束由 deps 文档决定（待确认）

### 4.7 Access Widener（**common + fabric 两副本同步改**）
`common/src/main/resources/iceandfire.accesswidener`（权威副本）与 `fabric/src/main/resources/iceandfire.accesswidener`：
- 删除第 2 行：`accessible class net/minecraft/client/particle/ParticleEngine$SpriteParticleRegistration`
- 第 6-10 行：`projectile/ThrownTrident`、`projectile/AbstractArrow` → `projectile/arrow/ThrownTrident`、`projectile/arrow/AbstractArrow`（5 行）
- 第 13 行：`getBaseExperienceReward ()I` → `getBaseExperienceReward (Lnet/minecraft/server/level/ServerLevel;)I`
- 第 17 行：`loadEffect (Lnet/minecraft/resources/ResourceLocation;)V` → `setPostEffect (Lnet/minecraft/resources/Identifier;)V`（或删除；`setPostEffect` 精确描述符待确认）

### 4.8 Trinkets 相关（fabric 模块，但属依赖重写，详见 deps 文档）
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/TrinketsRegistry.java`：改用 `eu.pb4.trinkets.api.callback.TrinketCallback.setCallback(Item, TrinketCallback)`
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/SimpleTickItemWrapper.java`：`implements TrinketCallback`，`tick(ItemStack, TrinketSlotAccess, LivingEntity)`；内部转发改为 `item.inventoryTick(stack, (ServerLevel) ..., entity, equipmentSlot)`
- 三份 trinkets JSON 数据文件（`data/trinkets/...`）结构不变（26.2 加载器仍读 `data/<ns>/trinkets`，`legs/belt` = `DefaultTrinketSlots.LEGS_BELT`）——**文件不用动，Java API 要重写**。

### 4.9 无需改动的文件
- `fabric/src/main/java/com/iafenvoy/iceandfire/impl/fabric/ComponentManagerImpl.java`（`getAttachedOrCreate` 来自 Fabric `AttachmentTarget`，26.2 稳定）
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java`（`ModMenuApi` / `ConfigScreenFactory` 跨版本稳定，但 26.2 版 ModMenu 未在参考树内，见待确认）

---

## 5. 风险与待确认

### 高风险
1. **酿药注册的可用性存在两份研究结论冲突**：
   - `fabric-api.md`（主参考）：26.2 `fabric-content-registries-v0` 中类改名为 `FabricPotionBrewingBuilder`，`BUILD` 事件形状不变，仅需改名；`PotionBrewing.Builder.addMix(Holder<Potion>, Item, Holder<Potion>)` 在 26.2 未变（`PotionBrewing.java:141-152`）。
   - `fabric-module.md`：称酿造 API 整体移除、无等价物，建议 Mixin 进 `PotionBrewing.bootstrap`/`addVanillaMixes`（26.2 vanilla 在 `MinecraftServer.<init>` 里纯代码构建 `PotionBrewing`，无数据包 `potion_brewing` 类型）。
   - **建议**：先按改名方案改，实际构建/运行验证 `BUILD` 是否触发；若事件不再触发，回退到 Mixin 方案。此点列为「待确认（编译/运行冒烟）」。

2. **Trinkets 全面重写 + `Item.inventoryTick` 签名变更**：fabric 模块最重的工作，`TrinketsRegistry` 与 `SimpleTickItemWrapper` 需重写，依赖坐标从 `dev.emi:trinkets` 改为 PB4 工件（坐标待确认）。

3. **Access Widener 多条破坏**：若漏改 `arrow/` 包移动或 `getBaseExperienceReward` 描述符，运行时抛 `LinkageError`；AW 两副本（common + fabric）必须同步改，否则 fabric 副本会覆盖 common 的改动导致 `AccessWidenerTransformer` 校验失败。`setPostEffect` 的精确方法名/描述符待确认。

### 中风险
4. **粒子链**：`ParticleProviderRegistry` 改名简单，但 common `ParticleProviderHolder` 与 AW 中 `SpriteParticleRegistration` 条目一起断；扩展分支删除后需确认无遗留引用。
5. **内置资源包**：deprecated v0 仍编译（推荐短期沿用），但 `deprecated/fabric-resource-loader-v0` 源码布局特殊（Mojmap 但 import `net.minecraft.resources.Identifier`），需在真实构建中冒烟确认与 Mojmap mod 编译一致。v1 目前无内置包注册的公开 API（无 `registerBuiltinPack` 等价物），是否完全迁移到 v1 需进一步确认。

### 低风险
6. **`ResourceLocation` → `Identifier`**：机械但广泛，影响 `IceAndFireFabricClient`、`IafAttachments`、AW 描述符及 common 全部调用点（common 部分属 Minecraft 文档范畴）。

### 待确认清单
- `fabric-loader 0.19.3` / `fabric-api 0.156.0+26.2` 是否为 26.2 的最新可用版本（`gradle.properties` 最终值）。
- 26.2 下 `FabricLoader.getInstance().getModContainer(id)` 行为是否与 1.21.1 完全一致（签名已确认不变）。
- `deprecated/fabric-resource-loader-v0` 与 Mojmap mod 的实际编译兼容性（冒烟测试）。
- `EntityRenderDispatcher.renderers` 在 26.2 的精确 AW 描述符（`Map<EntityType<?>, EntityRenderer<?, ?>>`，当前擦除描述符匹配，需复核）。
- `fabric-data-attachment-api-v1` 的 `@ApiStatus.Experimental` 是否仍需 `@SuppressWarnings("UnstableApiUsage")`（`IafAttachments.java:15`、`ComponentManagerImpl.java:10`）——预计需要，无害。
- 26.2 版 ModMenu（`com.terraformersmc.modmenu.api.ModMenuApi` / `ConfigScreenFactory`）的兼容性（26.2 ModMenu 不在参考树内）。
- PB4 Trinkets 的依赖坐标、`setCallback` 精确签名、`EquipmentSlot` 从 `TrinketSlotAccess` 推导方式。
- 26.2 `GameRenderer` shader 后处理方法名（`setPostEffect(Identifier)`）及对应 AW 描述符。
- 26.2 Java 工具链要求（`org.gradle.java.home` 当前指向 Zulu 21）是否需调整。
