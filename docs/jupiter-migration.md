# Jupiter 迁移文档（2.3.7 → 2.4.2）

> 针对 IceAndFire-CE（MC 1.21.1 Mojmap, Architectury common/fabric/neoforge → MC 26.2 Mojmap, Fabric 目标）的 Jupiter 配置库迁移。
> 依据 `docs/.research/jupiter.md`（主）与 `docs/.research/common-arch.md`（使用上下文），并已对照 `common/src` 与 `fabric/src` 源码核实。

## 0. 版本与总体结论

| 项 | 旧版本 | 新版本 |
|---|---|---|
| Jupiter 版本 | **2.3.7**（MC 1.18.2–1.21.10, fabric+forge+neoforge） | **2.4.2**（仅 MC 26.1.2 / 26.2, neoforge-universal + fabric compileOnly） |
| 项目当前坐标 | `maven.modrinth:jupiter:5pNXzmee`（common / fabric），`maven.modrinth:jupiter:m2itNS7Z`（neoforge） | 26.x 新坐标 **待确认**（见 §5） |

**核心结论（最重要，先读）**：

1. **包根未变**：新旧均为 `com.iafenvoy.jupiter`，所有 import 路径保持有效，无需改写。
2. **唯一真正的代码改动来自 MC 层而非 Jupiter 层**：新 Jupiter 基于 MC 26.2 mojmap 编译，id 类型由 `net.minecraft.resources.ResourceLocation` 更名为 **`net.minecraft.resources.Identifier`**。项目仅在两个配置容器构造函数参数上用了这个类型，因此只需把 `ResourceLocation.fromNamespaceAndPath(...)` 换成 `Identifier.fromNamespaceAndPath(...)`（同名同签名，`Identifier.fromNamespaceAndPath` 在 MC 26.2 `net/minecraft/resources/Identifier.java:40` 已核实存在）。
3. **项目用到的其它所有 Jupiter API 均未变化**（签名与行为一致）：`ConfigManager`、`ServerConfigManager`（含 `PermissionChecker.IS_OPERATOR`）、`AutoInitConfigContainer` 及嵌套 `AutoInitConfigCategoryBase`、`FileConfigContainer`、`BooleanEntry` / `DoubleEntry` / `IntegerEntry` / `SeparatorEntry` 构建器、`ConfigSelectScreen.builder(...)`。
4. 新 Jupiter **完全放弃 Forge loader**（仅 fabric + neoforge），`ResourceLocationEntry` → `IdentifierEntry`，删除了 `com.iafenvoy.jupiter.interfaces` 包，内部改用 `JupiterProxies` 服务定位器。这些均不影响本项目的当前用法（见 §2 附表）。
5. **neoforge 模块不在本次迁移范围**：`neoforge/src/.../IceAndFireNeoForgeClient.java` 使用 Jupiter 的 `ConfigSelectScreen`，但整个 neoforge 模块是 **Yarn 映射、已过时**（`net.minecraft.resource.*`、`net.minecraft.text.Text`、`net.minecraft.util.Identifier` 均为 Yarn 名，`common-arch.md` §4.6），在 Mojmap 仓库中无法编译，需重写而非迁移。目标为 Fabric，neoforge 模块应整体废弃（待确认，见 §5）。

---

## 1. 主对照表（旧 → 新）

### 1a. 项目实际使用（迁移相关）

| 类 | 旧成员 | 新成员 | 项目使用位置 |
|---|---|---|---|
| `com.iafenvoy.jupiter.ConfigManager` | `getInstance().registerConfigHandler(AbstractConfigContainer)` | **无变化** | `common/.../IceAndFire.java:42`、`common/.../IceAndFireClient.java:22` |
| `com.iafenvoy.jupiter.ServerConfigManager` | `registerServerConfig(AbstractConfigContainer, PermissionChecker)` | **无变化** | `common/.../IceAndFire.java:43` |
| `com.iafenvoy.jupiter.ServerConfigManager.PermissionChecker` | `IS_OPERATOR`（常量） | **无变化** | `common/.../IceAndFire.java:43` |
| `com.iafenvoy.jupiter.config.container.AutoInitConfigContainer` | `#<init>(ResourceLocation, String, String)` | `#<init>(Identifier, String, String)` | `common/.../config/IafCommonConfig.java:36`（`super(ResourceLocation.fromNamespaceAndPath(MOD_ID, "common"), ...)`） |
| `...AutoInitConfigContainer.AutoInitConfigCategoryBase`（嵌套类） | `#<init>(String id, String translateKey)` | **无变化** | `common/.../config/IafCommonConfig.java:40` 起的 20 个子类 |
| `com.iafenvoy.jupiter.config.container.FileConfigContainer` | `#<init>(ResourceLocation, String, String)` | `#<init>(Identifier, String, String)` | `common/.../config/IafClientConfig.java:15`（`super(ResourceLocation.fromNamespaceAndPath(MOD_ID, "client"), ...)`） |
| `com.iafenvoy.jupiter.config.container.AbstractConfigContainer` + `com.iafenvoy.jupiter.config.ConfigGroup` | `createTab(String, String)` + `addEntry(ConfigEntry<?>)` | **无变化** | `common/.../config/IafClientConfig.java:20-23` |
| `com.iafenvoy.jupiter.config.entry.BooleanEntry` | `builder(String, boolean).key(String).build()` | **无变化** | `IafCommonConfig.java`（多处）、`IafClientConfig.java:10-12` |
| `com.iafenvoy.jupiter.config.entry.DoubleEntry` | `builder(String, double).min(double).range(double,double).key(String).build()` | **无变化** | `IafCommonConfig.java`（多处） |
| `com.iafenvoy.jupiter.config.entry.IntegerEntry` | `builder(String, int).min(int).range(int,int).key(String).build()` | **无变化** | `IafCommonConfig.java`（多处） |
| `com.iafenvoy.jupiter.config.entry.SeparatorEntry` | `builder().build()` | **无变化**（新版本删除了已弃用的实例方法 `.text()` / `.tooltip()` 与公共无参构造，项目只用 builder） | `IafCommonConfig.java:46,51,80,264` |
| `com.iafenvoy.jupiter.render.screen.ConfigSelectScreen` | `builder(Component, Screen).server(...).client(...).build()` | **无变化**（已弃用的 `(Component, Screen, FileConfigContainer, FileConfigContainer)` 公共构造已删除，项目用 builder） | `fabric/.../ModMenu.java:16`、`neoforge/.../IceAndFireNeoForgeClient.java:29`（Yarn 模块，超出范围） |

### 1b. 已弃用旧 API → 新版本已删除（项目未使用，仅存档）

| 旧 API | 新版本状态 | 说明 |
|---|---|---|
| `BooleanEntry(String, boolean)`、`IntegerEntry(String, int[, int, int])`、`DoubleEntry(String, double[, double, double])`、`SeparatorEntry()`、`BaseEntry(String, T)`、`ConfigGroup.add(IConfigEntry<?>)`、`BaseEntry.json(String)`、`BaseEntry.visible(boolean)` | **已删除** | 全部是旧版标 `@Deprecated(forRemoval=true)` 的构造器/方法，新版仅保留 builder 模式。项目当前全部走 builder，天然兼容。 |
| `SeparatorEntry.text(...)` / `.tooltip(...)`（实例方法） | **已删除** | 仅保留 `Builder.text` / `Builder.tooltip`。 |
| `AbstractConfigContainer#getBackgroundTexture(boolean)` | **已删除** | 返回类型为 `ResourceLocation`/`Identifier`，方法被删除。 |
| `AbstractConfigContainer#shouldLoad(JsonObject)` / `#readCustomData` / `#writeCustomData` | **已删除** | 原已 `@Deprecated(forRemoval=true)`。 |

### 1c. 重命名 / 删除 / 新增的类（项目未使用，仅作上下文）

| 旧 | 新 | 类型 |
|---|---|---|
| `config/entry/ResourceLocationEntry` | `config/entry/IdentifierEntry` | 重命名（类型 `ResourceLocation`→`Identifier`；`ConfigTypes.RESOURCE_LOCATION` 常量未变） |
| `interfaces/IConfigEntry`、`interfaces/IConfigEnumEntry` | —（删除） | `com.iafenvoy.jupiter.interfaces` 包整体删除 |
| `render/JupiterRenderContext` | —（删除） | 渲染抽象删除 |
| `render/screen/ClientConfigScreen`、`ServerConfigScreen` | —（删除） | 并入 `ConfigSelectScreen` 流程 |
| `render/widget/StringWidget`、`SimpleButtonTooltip` | —（删除） | 组件工具类删除 |
| `util/TextUtil` | `util/MinecraftHelper` | `TextUtil.translatable(...)` 改为 `Component.translatable(key, new Object[]{})`；新增 `MinecraftHelper.openScreen(...)` |
| `util/RLUtil`、`util/CopyOnWriteHashMap` | —（删除） | 由 `Identifier.fromNamespaceAndPath` / `ConcurrentHashMap` 取代 |
| `network/NetworkConstants` | —（删除） | 常量内联 |
| `mixin/AutoConfigMixin` | `mixin/AutoConfigClientMixin` | 目标类改为 `me.shedaniel.autoconfig.AutoConfigClient` |
| `_loader/fabric/network/ClientNetworkHelperImpl`、`ServerNetworkHelperImpl` | `_loader/fabric/network/FabricClientNetworkHelper`、`FabricServerNetworkHelper` | 重命名 |
| `_loader/neoforge/network/ClientNetworkHelperImpl`、`ServerNetworkHelperImpl` | `_loader/neoforge/network/NeoForgeClientNetworkHelper`、`NeoForgeServerNetworkHelper` | 重命名 |
| `_loader/forge/*`（整个 loader，含 network 与 packet/ByteBufC2S、ByteBufS2C） | —（删除） | 新版本放弃 Forge |
| `_loader/neoforge/network/packet/ByteBufC2S`、`ByteBufS2C` | —（删除） | 1.20.5 前 bytebuf 包路径移除 |
| `_loader/fabric/reloader/ClientConfigReloader`、`ServerConfigReloader` | —（删除） | 重载改由 `ServerConfigManager`（实现 `ResourceManagerReloadListener`）+ 新 Fabric `ResourceLoader` API 内联处理 |
| `Platform`（final 类，静态方法） | `Platform`（接口） | 通过 `JupiterProxies.PLATFORM` 获取；由 `FabricPlatform` / `NeoForgePlatform` 实现 |
| — | `com.iafenvoy.jupiter.JupiterProxies` | 新增服务定位器，持有 `PLATFORM`、`CLIENT_NETWORKING`、`SERVER_NETWORKING` |
| — | `_loader/fabric/FabricPlatform`、`_loader/neoforge/NeoForgePlatform` | 新增平台实现 |
| — | `util/MinecraftHelper` | 新增工具类 |

---

## 2. 逐项变更（AGENTS.md 格式，按包排序）

以下条目覆盖项目实际使用的全部 Jupiter 符号。**未变化**的条目同时填写新旧 API（二者一致），原因注明"无变化"。

### 包 `com.iafenvoy.jupiter`（Manager 层）

---
旧API:
com.iafenvoy.jupiter.ConfigManager#getInstance().registerConfigHandler(AbstractConfigContainer)
新API:
com.iafenvoy.jupiter.ConfigManager#getInstance().registerConfigHandler(AbstractConfigContainer)（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/IceAndFire.java（第 42 行）
原因:
Jupiter 2.3.7 → 2.4.2 中 `ConfigManager` 及其 `getInstance()` / `registerConfigHandler(AbstractConfigContainer)` 签名与行为完全一致（新版本 ConfigManager.java:19,29），无需改动。包根 `com.iafenvoy.jupiter` 未变，import 亦无需改写。
---

---
旧API:
com.iafenvoy.jupiter.ConfigManager#getInstance().registerConfigHandler(AbstractConfigContainer)
新API:
com.iafenvoy.jupiter.ConfigManager#getInstance().registerConfigHandler(AbstractConfigContainer)（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/IceAndFireClient.java（第 22 行）
原因:
同上；客户端配置容器注册方式未变。
---

---
旧API:
com.iafenvoy.jupiter.ServerConfigManager#registerServerConfig(AbstractConfigContainer, PermissionChecker)
新API:
com.iafenvoy.jupiter.ServerConfigManager#registerServerConfig(AbstractConfigContainer, PermissionChecker)（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/IceAndFire.java（第 43 行）
原因:
`registerServerConfig(AbstractConfigContainer, PermissionChecker)` 新旧签名一致（新版本 ServerConfigManager.java:21-23）。无需改动。
---

---
旧API:
com.iafenvoy.jupiter.ServerConfigManager.PermissionChecker.IS_OPERATOR
新API:
com.iafenvoy.jupiter.ServerConfigManager.PermissionChecker.IS_OPERATOR（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/IceAndFire.java（第 43 行）
原因:
常量列表新旧一致：`ALWAYS_TRUE`、`ALWAYS_FALSE`、`IS_DEDICATE_SERVER`、`IS_LOCAL_GAME`、`IS_OPERATOR`（新版本 ServerConfigManager.java:59）。`IS_LOCAL_GAME` 内部实现随 MC 26.2 改了（`player.getGameProfile()` → `player.nameAndId()` 等），但这是 Jupiter 内部实现，不影响本项目使用的 `IS_OPERATOR` 公共常量。
---

### 包 `com.iafenvoy.jupiter.config.container`（容器层）

---
旧API:
com.iafenvoy.jupiter.config.container.AutoInitConfigContainer#<init>(net.minecraft.resources.ResourceLocation, String, String)
新API:
com.iafenvoy.jupiter.config.container.AutoInitConfigContainer#<init>(net.minecraft.resources.Identifier, String, String)
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java（第 36 行）
原因:
**唯一真正的代码改动。** Jupiter 2.4.2 基于 MC 26.2 mojmap 编译，id 类型由 `ResourceLocation` 更名为 `Identifier`。构造函数第一个参数类型随之改名，因此须将 `ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "common")` 改为 `Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "common")`（`Identifier.fromNamespaceAndPath` 同名同签名，MC 26.2 `net/minecraft/resources/Identifier.java:40` 已核实存在）。同时把 import `net.minecraft.resources.ResourceLocation` 换成 `net.minecraft.resources.Identifier`。这属于 MC API 迁移（`ResourceLocation`→`Identifier` 是 26.2 mojmap 更名），可对照 minecraft-api-migration.md。
---

---
旧API:
com.iafenvoy.jupiter.config.container.AutoInitConfigContainer.AutoInitConfigCategoryBase#<init>(String id, String translateKey)
新API:
com.iafenvoy.jupiter.config.container.AutoInitConfigContainer.AutoInitConfigCategoryBase#<init>(String id, String translateKey)（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java（第 40 行起的 20 个子类，如 DragonConfig / HippogryphsConfig 等）
原因:
嵌套静态类 `AutoInitConfigCategoryBase` 及其 `(String id, String translateKey)` 构造函数与基于反射的自动初始化机制新旧完全一致（IafCommonConfig 内以非限定名引用），无需改动。
---

---
旧API:
com.iafenvoy.jupiter.config.container.FileConfigContainer#<init>(net.minecraft.resources.ResourceLocation, String, String)
新API:
com.iafenvoy.jupiter.config.container.FileConfigContainer#<init>(net.minecraft.resources.Identifier, String, String)
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafClientConfig.java（第 15 行）
原因:
与 AutoInitConfigContainer 相同的类型改名。须将 `ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "client")` 改为 `Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "client")`，并把 import 换成 `net.minecraft.resources.Identifier`。
---

---
旧API:
com.iafenvoy.jupiter.config.container.AbstractConfigContainer#createTab(String, String) + com.iafenvoy.jupiter.config.ConfigGroup#addEntry(ConfigEntry<?>)
新API:
com.iafenvoy.jupiter.config.container.AbstractConfigContainer#createTab(String, String) + com.iafenvoy.jupiter.config.ConfigGroup#addEntry(ConfigEntry<?>)（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafClientConfig.java（第 20-23 行）
原因:
`createTab(String, String)` 与 `addEntry(ConfigEntry<?>)` 新旧签名一致（旧 AbstractConfigContainer.java:46 / ConfigGroup.java:32，新版本同位置），`IafClientConfig.init()` 重写无需改动。
---

### 包 `com.iafenvoy.jupiter.config.entry`（条目构建器）

---
旧API:
com.iafenvoy.jupiter.config.entry.BooleanEntry#builder(String, boolean).key(String).build()
新API:
com.iafenvoy.jupiter.config.entry.BooleanEntry#builder(String, boolean).key(String).build()（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java（多处，如第 44-45 行等）、common/src/main/java/com/iafenvoy/iceandfire/config/IafClientConfig.java（第 10-12 行）
原因:
`builder(String, boolean)` / `Builder.key(String)` / `Builder.build()` 新旧一致（新版本 BooleanEntry.java:31、BaseEntry.java:118,157）。旧版已弃用的直接构造 `BooleanEntry(String, boolean)` 在新版已删除，但项目只用 builder，天然兼容。
---

---
旧API:
com.iafenvoy.jupiter.config.entry.DoubleEntry#builder(String, double).min(double).range(double, double).key(String).build()
新API:
com.iafenvoy.jupiter.config.entry.DoubleEntry#builder(String, double).min(double).range(double, double).key(String).build()（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java（多处，如第 41,48-50 行等）
原因:
builder 链 `min(double)` / `range(double,double)` / `key(String)` / `build()` 新旧一致（新版本 DoubleEntry.java:66-97、BaseEntry）。项目未使用旧版弃用构造 `DoubleEntry(String, double[, double, double])`。
---

---
旧API:
com.iafenvoy.jupiter.config.entry.IntegerEntry#builder(String, int).min(int).range(int, int).key(String).build()
新API:
com.iafenvoy.jupiter.config.entry.IntegerEntry#builder(String, int).min(int).range(int, int).key(String).build()（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java（多处，如第 42-43 行等）
原因:
builder 链 `min(int)` / `range(int,int)` / `key(String)` / `build()` 新旧一致（新版本 IntegerEntry.java:62-97）。
---

---
旧API:
com.iafenvoy.jupiter.config.entry.SeparatorEntry#builder().build()
新API:
com.iafenvoy.jupiter.config.entry.SeparatorEntry#builder().build()（无变化）
修改位置:
common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java（第 46,51,80,264 行）
原因:
`SeparatorEntry.builder()` → `Builder.build()` 新旧一致。注意新版本删除了已弃用的实例方法 `.text()` / `.tooltip()` 与公共无参构造，但项目仅使用 builder，不受影响。
---

### 包 `com.iafenvoy.jupiter.render.screen`（配置选择界面）

---
旧API:
com.iafenvoy.jupiter.render.screen.ConfigSelectScreen#builder(Component, Screen).server(AbstractConfigContainer).client(AbstractConfigContainer).build()
新API:
com.iafenvoy.jupiter.render.screen.ConfigSelectScreen#builder(Component, Screen).server(AbstractConfigContainer).client(AbstractConfigContainer).build()（无变化）
修改位置:
fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java（第 16 行）
原因:
`builder(Component, Screen)` / `Builder.server(...)` / `.client(...)` / `.build()` 新旧一致（新版本 ConfigSelectScreen.java:122-168）。已弃用的公共构造 `(Component, Screen, FileConfigContainer, FileConfigContainer)` 在新版本删除，但项目走 builder，无需改动。ModMenu 集成零改动。
---

---
旧API:
com.iafenvoy.jupiter.render.screen.ConfigSelectScreen#builder(Text, Screen).server(...).client(...).build()
新API:
com.iafenvoy.jupiter.render.screen.ConfigSelectScreen#builder(Text, Screen).server(...).client(...).build()（无变化）
修改位置:
neoforge/src/main/java/com/iafenvoy/iceandfire/neoforge/IceAndFireNeoForgeClient.java（第 29 行）
原因:
此文件使用 Yarn 映射（`net.minecraft.text.Text`、`net.minecraft.util.Identifier` 等），属于**过时、无法在 Mojmap 仓库中编译**的 neoforge 模块（common-arch.md §4.6），应整体重写而非迁移；迁移目标为 Fabric 时该模块直接废弃。本条仅记录存在，不在迁移范围内。
---

### 构建依赖（build.gradle，坐标更新）

---
旧API:
modImplementation "maven.modrinth:jupiter:5pNXzmee"
新API:
待确认（Jupiter 2.4.2 / MC 26.x 的 Modrinth 版本哈希或 mavenLocal/JitPack 坐标）
修改位置:
common/build.gradle（第 21 行）、fabric/build.gradle（第 44 行）
原因:
Jupiter 由 2.3.7 升级到 2.4.2。`Jupiter new` 磁盘树是本地 git checkout，未记录已发布的 26.x Modrinth 坐标，需向 Mod 作者或 Modrinth API 确认后再改。包根未变，故坐标更新后无需改写任何 import。
---

---
旧API:
modImplementation "maven.modrinth:jupiter:m2itNS7Z"
新API:
待确认（同上；若废弃 neoforge 模块则删除该行）
修改位置:
neoforge/build.gradle（第 42 行）
原因:
Jupiter 2.4.2 已放弃 Forge loader（neoforge-universal + fabric compileOnly）。若迁移目标为 Fabric 且废弃 neoforge 模块，此依赖应随模块一并删除；若保留 neoforge，需使用 2.4.2 的新坐标（待确认）。
---

---

## 3. 项目中所有引用 Jupiter 的文件

| 文件 | 引用内容 | 迁移状态 |
|---|---|---|
| `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/IceAndFire.java`（L11-12,42-43） | `ConfigManager.getInstance().registerConfigHandler(...)`、`ServerConfigManager.registerServerConfig(..., PermissionChecker.IS_OPERATOR)` | 无改动 |
| `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/IceAndFireClient.java`（L14,22） | `ConfigManager.getInstance().registerConfigHandler(...)` | 无改动 |
| `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java`（L4-8,11,36,40 起） | `AutoInitConfigContainer`（extends）、`AutoInitConfigCategoryBase`、`BooleanEntry` / `DoubleEntry` / `IntegerEntry` / `SeparatorEntry` | **改：L36 构造参数 `ResourceLocation`→`Identifier`**；其余无改动 |
| `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/config/IafClientConfig.java`（L4-6,8,15,20-23） | `FileConfigContainer`（extends）、`BooleanEntry`、`createTab(...).addEntry(...)` | **改：L15 构造参数 `ResourceLocation`→`Identifier`**；其余无改动 |
| `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java`（L5,16） | `ConfigSelectScreen.builder(...).server(...).client(...).build()` | 无改动 |
| `D:/IceAndFire-CE/neoforge/src/main/java/com/iafenvoy/iceandfire/neoforge/IceAndFireNeoForgeClient.java`（L8,29） | `ConfigSelectScreen.builder(...)`（Yarn 映射） | 超出范围：neoforge 模块 Yarn、过时，需重写/废弃 |
| `D:/IceAndFire-CE/common/build.gradle`（L21） | `maven.modrinth:jupiter:5pNXzmee` | 坐标待确认 |
| `D:/IceAndFire-CE/fabric/build.gradle`（L44） | `maven.modrinth:jupiter:5pNXzmee` | 坐标待确认 |
| `D:/IceAndFire-CE/neoforge/build.gradle`（L42） | `maven.modrinth:jupiter:m2itNS7Z` | 坐标待确认；若废弃 neoforge 则删除 |

---

## 4. 迁移步骤

1. **更新 Jupiter 依赖坐标**：common/fabric/neoforge 三个 `build.gradle` 中的 Jupiter 改为 26.x 制品（2.4.2）坐标，哈希待确认。包根 `com.iafenvoy.jupiter` 未变，因此无需改写任何 import 路径。
2. **`IafCommonConfig.java:36`**：把 `net.minecraft.resources.ResourceLocation` → `net.minecraft.resources.Identifier`，`ResourceLocation.fromNamespaceAndPath(...)` → `Identifier.fromNamespaceAndPath(...)`（工厂名与签名不变）。
3. **`IafClientConfig.java:15`**：同上，把 `super(ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "client"), ...)` 改为 `Identifier.fromNamespaceAndPath(...)`，import 同步替换。
4. **其余全部照旧编译**：Managers、四个条目构建器、容器继承、`createTab().addEntry()`、`ConfigSelectScreen` builder 均可在 2.4.2 下原样编译。
5. **Fabric ModMenu 无需改动**：`ConfigSelectScreen.builder(Component.translatable(...), parent).server(IafCommonConfig.INSTANCE).client(IafClientConfig.INSTANCE).build()` 在 2.3.7 与 2.4.2 中均合法。
6. **不要依赖任何旧版已弃用 API**：项目已全部走 builder，已天然规避 §1b 中在新版本删除的弃用构造/方法，无需额外处理。
7. **fabric-loader / Fabric API 联动**：Jupiter 2.4.2 内部改用新 Fabric `ResourceLoader` API（`ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Identifier, listener)`）——这是 Jupiter 内部与 26.2 Fabric API 的联动，Mod 作者无需动作；但项目自身的 `fabric_loader_version` 与 Fabric API 版本需随整体 MC 26.2 迁移更新（属 fabric-api-migration.md 范围）。

---

## 5. 待确认（to be confirmed）

1. **Jupiter 2.4.2（MC 26.x）的精确 Modrinth 版本哈希 / Maven 坐标**。`D:/aiminecraftdev/Jupiter new` 磁盘树是本地 git checkout（2.4.2），未记录已发布坐标。需向 Mod 作者或通过 Modrinth API 确认后，再编辑 common/fabric/neoforge 的 `build.gradle`。可选 mavenLocal / JitPack 坐标。
2. **neoforge 模块去留**。目标为 Fabric，而 neoforge 模块是 Yarn 映射且过时（`IceAndFireNeoForgeClient.java` 等无法在 Mojmap 下编译），需确认是整体废弃（连带删除 `neoforge/build.gradle:42` 的 Jupiter 依赖）还是后续重写。
3. **新 Jupiter 对 fabric-loader / Fabric API 的版本要求**（26.x），需与项目的 `fabric_loader_version` 引脚协调，在 fabric-api-migration.md 中一并处理。
4. `AutoInitConfigContainer` / `FileConfigContainer` 构造器的 `String titleKey` / `String path` 语义在新版本是否改变未逐字核对，但研究文件记录其签名未变（new `AbstractConfigContainer.java:13` 仅 id 类型改名为 `Identifier`），如编译遇到行为差异需复核。
