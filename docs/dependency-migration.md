# 依赖迁移文档 — IceAndFire-CE（MC 1.21.1 → MC 26.2）

> 项目根目录：`D:/IceAndFire-CE`（Architectury common/fabric/neoforge，Mojmap，Fabric 目标）
> 本文所有版本与坐标均依据 `D:/IceAndFire-CE/docs/.research/` 下的研究文档及 `D:/aiminecraftdev` 参照源码核对。
> 无法在磁盘上核实的条目一律标注 **待确认 (to be confirmed)**，请在改动 `build.gradle` 前通过 Modrinth API / 与作者确认后填写。
> 本文只做文档，不修改任何代码。

---

## 1. 当前依赖清单（MC 1.21.1）

### 1.1 平台与构建基础（`D:/IceAndFire-CE/gradle.properties` / `build.gradle` / `settings.gradle`）

| 项 | 当前值 | 位置 |
|---|---|---|
| enabled_platforms | `fabric` | `gradle.properties:10` |
| settings.gradle 包含模块 | `common`、`fabric`（neoforge 在磁盘上但未纳入构建） | `settings.gradle:12-13` |
| minecraft_version | `1.21.1` | `gradle.properties:12` |
| mod_version | `2.0-beta.16` | `gradle.properties:7` |
| Java | Zulu `21` 固定，`release 21` | `gradle.properties:5`、`build.gradle:62-68` |
| Loom 插件 | `dev.architectury.loom` `1.11-SNAPSHOT` | `build.gradle:2` |
| architectury-plugin | `3.4-SNAPSHOT` | `build.gradle:3` |
| Shadow 插件 | `com.github.johnrengelman.shadow` `8.1.1` | `build.gradle:4` |
| 映射 | `loom.layered { officialMojangMappings() }` | `build.gradle:51-53` |
| Gradle wrapper | `8.14` | `gradle/wrapper/gradle-wrapper.properties:3` |
| CI Java / Gradle | `21` (temurin) / `8.14` | `.github/workflows/build.yml:18,24` |

### 1.2 `common/build.gradle`（`D:/IceAndFire-CE/common/build.gradle`）

| group:artifact:version | 配置 | 用途 |
|---|---|---|
| `net.fabricmc:fabric-loader:0.16.7`（`fabric_loader_version`） | `modImplementation` (:13) | 仅为 `@Environment` 注解 |
| `dev.architectury:architectury:13.0.8`（`architectury_api_version`） | `modImplementation` (:16) | Architectury 通用 API |
| `com.github.IAFEnvoy.Integration:integration-common:0.2`（`integration_version`，jitpack） | `modImplementation` (:18) | `IntegrationExecutor` 可选集成加载器 |
| `maven.modrinth:uranus:FH0tB0dy` | `modImplementation` (:20) | 动画引擎 / 实体与 AI 助手（~40 文件使用） |
| `maven.modrinth:jupiter:5pNXzmee` | `modImplementation` (:21) | 配置库 |
| `dev.emi:emi-fabric:1.1.19+1.21.1:api`（`emi_version`） | `modCompileOnly` (:23) | EMI 配方兼容 |
| `mezz.jei:jei-1.21.1-fabric-api:19.21.0.247`（`jei_version`） | `modCompileOnly` (:24) | JEI 配方兼容 |
| `maven.modrinth:jade:pA0xvozk` | `modImplementation` (:26) | Jade 提示兼容 |
| `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61`（`ponder_version`） | `modImplementation` (:28) | Create Ponder 场景 |

### 1.3 `fabric/build.gradle`（`D:/IceAndFire-CE/fabric/build.gradle`）

| group:artifact:version | 配置 | 用途 |
|---|---|---|
| `net.fabricmc:fabric-loader:0.16.7` | `modImplementation` (:32) | Fabric 平台运行时 |
| `net.fabricmc.fabric-api:fabric-api:0.116.5+1.21.1`（`fabric_api_version`） | `modImplementation` (:35) | Fabric API 模块 |
| `dev.architectury:architectury-fabric:13.0.8` | `modImplementation` (:38) | Architectury Fabric 实现（**不打包**，生产环境单独安装） |
| `:common` namedElements / transformProductionFabric | `common(...)` (:40) / `shadowBundle` (:41) | common 类与打包 |
| `maven.modrinth:uranus:FH0tB0dy` | `modImplementation` (:43) | 同 common |
| `maven.modrinth:jupiter:5pNXzmee` | `modImplementation` (:44) | 同 common |
| `com.github.IAFEnvoy.Integration:integration-fabric:0.2` | `modImplementation(include(...))` (:45) | 随包发布 |
| `com.terraformersmc:modmenu:11.0.3`（`modmenu_version`） | `modApi` (:49) | 配置界面 |
| `dev.emi:emi-fabric:1.1.19+1.21.1` | `modLocalRuntime` (:52) | 仅开发运行时 |
| `maven.modrinth:jade:pA0xvozk` | `modImplementation` (:55) | 同 common |
| `maven.modrinth:trinkets:JagCscwi` | `modApi` (:58) | Trinket 槽位 |
| `dev.onyxstudios.cardinal-components-api:cardinal-components-base:6.1.1`（`cca_version`） | `modApi` (:59) | **源码无任何 import**（待确认，可能为 Trinkets 传递依赖/冗余） |
| `dev.onyxstudios.cardinal-components-api:cardinal-components-entity:6.1.1` | `modApi` (:60) | 同上（待确认） |
| `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61` | `modCompileOnly` (:62) | Ponder（Fabric 侧仅编译） |

### 1.4 `neoforge/build.gradle`（未构建，仅供参考）（`D:/IceAndFire-CE/neoforge/build.gradle`）

| group:artifact:version | 配置 | 备注 |
|---|---|---|
| `net.neoforged:neoforge:$rootProject.neoforge_version` | `neoForge` (:32) | **`neoforge_version` 未在任何 gradle.properties 定义**，模块当前无法求值（被 settings.gradle 排除） |
| `dev.architectury:architectury-neoforge:13.0.8` | `modImplementation` (:35) | |
| `com.github.IAFEnvoy.Integration:integration-neoforge:0.2` | `modImplementation(include(...))` (:39) | |
| `maven.modrinth:uranus:BBb3HOQ5` | `modImplementation` (:41) | NeoForge 专用 modrinth 文件 |
| `maven.modrinth:jupiter:m2itNS7Z` | `modImplementation` (:42) | NeoForge 专用 modrinth 文件 |
| `dev.emi:emi-neoforge:1.1.19+1.21.1` | `modLocalRuntime` (:44) | |
| `maven.modrinth:jade:JkFFfEao` | `modImplementation` (:46) | |
| `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1`（`curios_version`） | `modApi` (:48) | |
| `curse.maven:projecte-226410:6611984` | `modCompileOnly` (:51) | ProjectE |
| `net.createmod.ponder:Ponder-NeoForge-1.21.1:1.0.61` | `modCompileOnly` (:53) | |
| `com.hollingsworth.ars_nouveau:ars_nouveau-1.21.1:5.10.3.1204`（`ars_version`） | `modCompileOnly` (:56) | |
| `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.7`（`geckolib_version`） | `modCompileOnly` (:57) | |
| `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` | `modCompileOnly` (:58) | 与 :48 重复 |

### 1.5 `gradle.properties` 变量汇总

| 变量 | 当前值 |
|---|---|
| `architectury_api_version` | `13.0.8` |
| `fabric_loader_version` | `0.16.7` |
| `fabric_api_version` | `0.116.5+1.21.1` |
| `cca_version` | `6.1.1` |
| `emi_version` | `1.1.19+1.21.1` |
| `modmenu_version` | `11.0.3` |
| `jei_version` | `19.21.0.247` |
| `ponder_version` | `1.0.61` |
| `integration_version` | `0.2` |
| `ars_version` | `5.10.3.1204` |
| `geckolib_version` | `4.7.7` |
| `curios_version` | `9.5.1+1.21.1` |

---

## 2. 目标版本对照表（MC 1.21.1 → MC 26.2）

> 以下 26.2 目标值依据 `D:/aiminecraftdev/` 下同代际参照项目（Uranus26.2 / architectury-api26.2 / Jupiter new / emi26.2 / Jade26.2 / trinkets26.2）的 `gradle.properties` / `build.gradle` 核实。

| 依赖 | 旧版（1.21.1） | 新版（26.2） | 声明位置 | 备注 |
|---|---|---|---|---|
| Minecraft | `1.21.1` | `26.2` | `gradle.properties:12` | `version.json` 要求 **Java 25**；26.2 直接内嵌官方 mojmap 类名，**无独立 mappings 构件** |
| fabric-loader | `0.16.7` | **`0.19.3`** | `common/build.gradle:13`、`fabric/build.gradle:32`、`gradle.properties:15` | 参照：Uranus26.2 / architectury-api26.2 / trinkets26.2 / Jade26.2 / emi26.2 均为 0.19.3（Jupiter 26.2 用 0.19.2） |
| fabric-api | `0.116.5+1.21.1` | **`0.156.0+26.2`** | `fabric/build.gradle:35`、`gradle.properties:16` | 参照：Uranus26.2 / Jupiter 26.2 用 0.156.0；architectury 26.2 自身用 0.154.2，EMI/Jade/Trinkets 用 0.152–0.154。取最接近同类（Uranus/Jupiter）的 0.156.0 |
| Architectury | `13.0.8`（`architectury` / `-fabric`） | **`21.0.7`**（`base_version=21.0`） | `common/build.gradle:16`、`fabric/build.gradle:38`、`gradle.properties:14` | Uranus26.2 引用 `21.0.7`；本地 Gradle 缓存已确认存在 `dev.architectury:architectury/21.0.7` 与 `architectury-fabric/21.0.7` |
| architectury-plugin | `3.4-SNAPSHOT` | **`3.5-SNAPSHOT`** | `build.gradle:3` | 参照：architectury-api26.2 / Uranus26.2 |
| Loom 插件 | `dev.architectury.loom` `1.11-SNAPSHOT` | **`dev.architectury.loom-no-remap` `1.17-SNAPSHOT`** | `build.gradle:2` | 参照：Uranus26.2 / architectury-api26.2（两者插件 ID 均有 1.17-SNAPSHOT 缓存；release `1.17.491` 亦已缓存）。**注意插件 ID 变更**：26.2 生态共识为 Architectury 工程使用 `loom-no-remap` |
| Shadow 插件 | `com.github.johnrengelman.shadow` `8.1.1` | **`com.gradleup.shadow` `8.3.10`**（Uranus）/ `9.4.3`（architectury） | `build.gradle:4` | 旧坐标已废弃迁移至 `com.gradleup.shadow`；取 Uranus 同款 8.3.10。另：loom-no-remap 下 Fabric 最终产物**不是 shadowJar**（见 §3） |
| Gradle wrapper | `8.14` | **`9.5.1`** | `gradle/wrapper/gradle-wrapper.properties:3` | 参照：architectury-api26.2 / fabric-api26.2 / Uranus26.2 wrapper |
| Java | Zulu `21` / `release 21` | **Zulu `25` / `release 25`** | `gradle.properties:5`、`build.gradle:62-68` | 参照：MC 26.2 `version.json` `java_version=25`；architectury 26.2 `<25` 直接抛错；Uranus26.2 固定 `zulu-25` |
| 映射 | `loom.layered { officialMojangMappings() }` | **无 `mappings` 行** | `build.gradle:51-53` | MC 26.2 内嵌 mojmap，无独立映射构件（见 §3） |
| ModMenu | `11.0.3` | **`20.0.1`** | `fabric/build.gradle:49`、`gradle.properties:19` | 参照：architectury-api26.2 / Jupiter 26.2 |
| EMI | `dev.emi:emi-fabric:1.1.19+1.21.1` | **`dev.emi:emi-fabric:1.1.24+26.2`（后缀 待确认）** | `common/build.gradle:23`、`fabric/build.gradle:52`、`gradle.properties:18` | EMI 26.2 `mod_version=1.1.24`；`+26.2` 后缀为按惯例推测，**待确认**；`:api` classifier 保留 |
| JEI | `mezz.jei:jei-1.21.1-fabric-api:19.21.0.247` | **`mezz.jei:jei-26.2-fabric-api:30.7.0.39`** | `common/build.gradle:24`、`gradle.properties:20` | 参照：emi26.2 `jei_version=jei-26.2-fabric:30.7.0.39`（fabric-api 分类） |
| Jade（modrinth 文件坐标） | `maven.modrinth:jade:pA0xvozk` | 版本 **`26.2.10`**，文件 hash **待确认** | `common/build.gradle:26`、`fabric/build.gradle:55` | Jade26.2 `mod_version=26.2.10`；modrinth 坐标第三段为**文件 hash**，需用 Modrinth API 解析新 hash |
| Trinkets（modrinth 文件坐标） | `maven.modrinth:trinkets:JagCscwi` | 版本 **`4.1.0-beta.3+26.2`**，文件 hash **待确认** | `fabric/build.gradle:58` | **Patbox "Trinkets Updated" 完全重写**：包名 `dev.emi.trinkets.api` → `eu.pb4.trinkets.api`，mod id `trinkets` → `trinkets_updated`，移除 CCA |
| Uranus（modrinth 文件坐标） | `maven.modrinth:uranus:FH0tB0dy` | 版本 **`2.4.1-bugfix`**，文件 hash **待确认** | `common/build.gradle:20`、`fabric/build.gradle:43` | 26.2 构件为 **Mojmap 官方映射、fabric-only**（Uranus26.2 已放弃 neoforge） |
| Jupiter（modrinth 文件坐标） | `maven.modrinth:jupiter:5pNXzmee` | 版本 **`2.4.2`**，文件 hash **待确认** | `common/build.gradle:21`、`fabric/build.gradle:44` | Jupiter 26.2 为 Stonecutter **universal jar**；包根 `com.iafenvoy.jupiter` 不变；仅 `ResourceLocation`→`Identifier` 影响两处容器构造 |
| Integration | `com.github.IAFEnvoy.Integration:integration-common/-fabric:0.2`（jitpack） | 版本仍 **`0.2`**，能否在 26.2 运行 **待确认** | `common/build.gradle:18`、`fabric/build.gradle:45` | 参照源码停在 MC 1.20.4；Integration 为 loader 级、版本无关库，理论上可编译，需运行时验证 `IntegrationExecutor` 兼容性 |
| Ponder | `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61` | **待确认**（新坐标推测 `net.createmod.ponder:Ponder-Fabric-26.2:<v>`） | `common/build.gradle:28`、`fabric/build.gradle:62`、`gradle.properties:21` | 磁盘上无 26.2 参照源 |
| CCA（cardinal-components-base/entity） | `6.1.1` | **很可能移除（待确认）** | `fabric/build.gradle:59-60`、`gradle.properties:17` | 源码无任何 import；Trinkets 26.2 改用 `dev.yumi.mc.core:yumi-mc-foundation:1.1.1+26.2`（Ladysnake），不再依赖 CCA → 可删除 |
| Curios（neoforge-only） | `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1` | **待确认** | `neoforge/build.gradle:48,58`、`gradle.properties:25` | 无 26.2 参照源 |
| Ars Nouveau（neoforge-only） | `com.hollingsworth.ars_nouveau:ars_nouveau-1.21.1:5.10.3.1204` | **待确认** | `neoforge/build.gradle:56`、`gradle.properties:23` | 无 26.2 参照源 |
| GeckoLib（neoforge-only） | `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.7` | **待确认** | `neoforge/build.gradle:57`、`gradle.properties:24` | 无 26.2 参照源 |
| ProjectE（neoforge-only） | `curse.maven:projecte-226410:6611984` | **待确认** | `neoforge/build.gradle:51` | 无 26.2 参照源 |

### 2.1 关键结论

- **Minecraft 坐标由 `net.minecraft:minecraft` 改为 `com.mojang:minecraft`**（Uranus26.2 注释原文：*"MC 26.2 ships official (mojmap) class names directly — no separate mappings artifact exists, so loom-no-remap mode needs no `mappings` line."*）。
- **Architectury 跳变 13.0.8 → 21.0.x**：存在跨多个大版本的消费者可见变化，代码迁移开始时需核对 `architectury-api26.2` 是否改名包。
- **Trinkets 是破坏性重写**（包名 + mod id + 注册 API 全变），不是简单换版本。
- **modrinth 文件坐标第三段是文件 hash 而非语义版本**，26.2 全部需要重新解析（见 §5）。
- **neoforge 模块命运待定**：Uranus/Jupiter 26.2 均为 fabric-only（Jupiter 为 universal neoforge+compileOnly fabric）；本项目 `enabled_platforms=fabric`，neoforge 继续排除即可，若要保留需自行解决 `neoforge_version` 未定义问题。

---

## 3. 构建 / 插件更新（loom-no-remap 模式）

> 以下 DSL 变化以 `Uranus26.2` 为蓝本（同结构 26.2 mod），与 `architectury-api26.2` 交叉印证。

### 3.1 必须执行的构建变更

| 位置 | 当前（1.21.1） | 目标（26.2） | 待确认 |
|---|---|---|---|
| `build.gradle:2` 插件 | `dev.architectury.loom` `1.11-SNAPSHOT` | `dev.architectury.loom-no-remap` `1.17-SNAPSHOT` | ✅ 参照已核实 |
| `build.gradle:3` 插件 | `architectury-plugin` `3.4-SNAPSHOT` | `architectury-plugin` `3.5-SNAPSHOT` | ✅ 参照已核实 |
| `build.gradle:4` 插件 | `com.github.johnrengelman.shadow` `8.1.1` | `com.gradleup.shadow` `8.3.10` | ✅ 参照已核实（Uranus） |
| `build.gradle:46` 仓库 | `flatDir { dirs '../mappings-patch' }` | **删除** | ✅ 该映射 jar 无任何 `mappings` 配置引用，loom-no-remap 下无意义 |
| `build.gradle:50` | `minecraft "net.minecraft:minecraft:..."` | `minecraft "com.mojang:minecraft:..."` | ✅ 参照已核实 |
| `build.gradle:51-53` | `mappings loom.layered { officialMojangMappings() }` | **整块删除**（26.2 无独立映射） | ✅ 参照已核实 |
| `build.gradle:62-68` | `sourceCompatibility/targetCompatibility = VERSION_21`、`release 21` | `VERSION_25`、`release 25` | ✅ 参照已核实 |
| `gradle.properties:5` | `org.gradle.java.home=...zulu-21` | `...zulu-25` | ✅ 参照已核实 |
| `gradle/wrapper/...properties` | `gradle-8.14-bin.zip` | `gradle-9.5.1-bin.zip` | ✅ 参照已核实 |
| `.github/workflows/build.yml:18` | `java-version: '21'` | `'25'` | ✅ 由 Java 25 需求推导，需同步 |
| `.github/workflows/build.yml:24` | `gradle-version: '8.14'` | `'9.5.1'` | ✅ 同上 |

### 3.2 loom-no-remap 对 Gradle DSL 的全局影响（均来自 `Uranus26.2`）

1. **`modImplementation` / `modApi` / `modCompileOnly` / `modLocalRuntime` / `modRuntimeOnly` 配置不存在**，一律改用普通 Gradle `implementation` / `api` / `compileOnly` / `runtimeOnly`。
   - 影响：`common/build.gradle` 与 `fabric/build.gradle` 中所有 `mod*` 关键字重写；`fabric/build.gradle` 中 loader/fabric-api 参照 Uranus 用 `api`。
2. **`remapJar` 不存在、`include(...)` 不再使用**。需打包的依赖以 `.jar` 原样放入 `META-INF/jars/`，并在 `fabric.mod.json` 增加 `"jars": [ {"file": "META-INF/jars/<name>.jar"} ]`。
   - 影响：`fabric/build.gradle:45` 的 `include("...integration-fabric:0.2")` 改为 `nestedJars` 风格配置 → `META-INF/jars/` + `jars` 条目。
   - **architectury-fabric**：当前生产环境不打包（用户单独安装）；loom-no-remap 下可继续保持不打包，也可像 Uranus 一样嵌套（决策点）。若保持不打包，仍需 `fabric.mod.json` 依赖 architectury。**待确认**。
3. **标准 `jar` 任务即最终产物**（不是 shadowJar）。访问加宽器通过 `loom { injectAccessWidener(tasks.named('jar')) }` 注入。**避免 Shadow 的 `from(zipTree(...))`**，因为会递归展开内嵌 `.jar`（Uranus26.2 `jar { }` 注释原文）。
   - 影响：`fabric/build.gradle:73-80` 的 `shadowJar` + `remapJar { input.set shadowJar.archiveFile }` 整段重构为 `jar { ... }`。
4. **AW + mixin JSON 需从 `:common` 复制进 fabric 模块资源**（dev 启动器要求），保持 `:common` 为唯一来源（Uranus26.2 `processResources` 用 `from(project(':common').file(...))`）。
   - 本项目当前 `fabric.mod.json` 的 `mixins`/`accessWidener` 已在 fabric 资源中；需确保 26.2 下同步。`iceandfire.accesswidener` 在 fabric 与 common 两份逐字节一致，loom-no-remap 下应统一到 common 并复制。
5. **`neoforge` 模块**：当前因 `neoforge_version` 未定义而无法求值，被 settings.gradle 排除；loom-no-remap 下如不保留，无额外动作。

### 3.3 待确认项汇总（build 相关）

- **Ponder 26.2 坐标**：磁盘无参照源，新版本/hash 待确认。
- **`emi-fabric:1.1.24` 的 `+26.2` 版本后缀**：推测值，需按 Modrinth/EMI 实际发布确认。
- **所有 `maven.modrinth:*` 文件 hash**：需 Modrinth API 解析（见 §5）。
- **architectury-fabric 是否嵌套进 `META-INF/jars/`**：Uranus 选择嵌套并声明 `jars`；本项目可二选一，需决策。
- **`loom.ignoreDependencyLoomVersionValidation=true`**：architectury 26.2 设置了该项；本项目按需决定是否添加（待确认）。
- **GeckoLib / Ars / Curios / ProjectE（neoforge-only）26.2 版本**：无参照源。
- **Integration 0.2 在 26.2 的运行时兼容性**：参照源码停在 MC 1.20.4，需运行时验证。

---

## 4. `fabric.mod.json` 变更（`D:/IceAndFire-CE/fabric/src/main/resources/fabric.mod.json`）

| 字段 | 当前（1.21.1） | 目标（26.2） | 参照 |
|---|---|---|---|
| `depends.minecraft` | `"1.21.x"` (:43) | `"26.x"` | Uranus26.2 用 `"26.x"`；Jupiter 26.2 用 `">=26.2"`。**待确认**策略（可二者择一） |
| `depends.jupiter` | `">=2.3"` (:44) | 下限上提到新版本 `>=2.4.2`，或保留宽松 `>=2.3` | **待确认** 策略 |
| `depends.uranus` | `">=2.3.2"` (:45) | 下限上提到 `>=2.4.1`，或保留宽松 `>=2.3.2` | **待确认** 策略 |
| `depends.fabric-api` / `depends.fabricloader` | 不存在 | 建议新增 `"fabric-api": "*"`、`"fabricloader": ">=0.18.4"` | fabric-api 26.2 模块声明 `fabricloader >= 0.18.4`；Uranus26.2 声明 `fabric-api:*`。**建议**新增 |
| `jars` 数组 | **不存在** | 一旦 Integration（以及可选的 architectury-fabric）嵌套进 `META-INF/jars/`，必须新增 `"jars": [ {"file": "META-INF/jars/<name>.jar"} ]` | Uranus26.2 示例：`{"file": "META-INF/jars/architectury-fabric.jar"}` |
| `schemaVersion` | `1` | `1`（loader 0.19.3 仍读 schemaVersion 1） | ✅ 无需变更 |
| `entrypoints` | `main/client/emi/jade/modmenu`（:21-37） | 形状不变；EMI/Jade 26.2 插件入口契约一致 | ✅ 参照已核实 |
| `accessWidener` | `iceandfire.accesswidener` | 字段不变；**但 AW 内容需 26.2 更新**（见 `minecraft-api-migration.md`） | |
| `mixins` | `iceandfire.mixins.json` | 概念不变 | |

---

## 5. 必须更新的 Modrinth 版本文件 URL

以下坐标使用 **modrinth 版本文件 hash**（第三段为每文件 hash，而非语义版本）。26.2 新 hash 需通过 **Modrinth API 在线解析**，解析前一律 **待确认**：

| 依赖 | 当前 hash（1.21.1） | 声明位置 | 26.2 已知版本 | 新 hash |
|---|---|---|---|---|
| `maven.modrinth:uranus` | `FH0tB0dy` | `common/build.gradle:20`、`fabric/build.gradle:43` | `2.4.1-bugfix` | **待确认** |
| `maven.modrinth:jupiter` | `5pNXzmee` | `common/build.gradle:21`、`fabric/build.gradle:44` | `2.4.2`（universal jar） | **待确认** |
| `maven.modrinth:jade` | `pA0xvozk` | `common/build.gradle:26`、`fabric/build.gradle:55` | `26.2.10` | **待确认** |
| `maven.modrinth:trinkets` | `JagCscwi` | `fabric/build.gradle:58` | `4.1.0-beta.3+26.2` | **待确认** |
| （neoforge）`maven.modrinth:uranus` | `BBb3HOQ5` | `neoforge/build.gradle:41` | —（neoforge 命运待定） | **待确认** |
| （neoforge）`maven.modrinth:jupiter` | `m2itNS7Z` | `neoforge/build.gradle:42` | — | **待确认** |
| （neoforge）`maven.modrinth:jade` | `JkFFfEao` | `neoforge/build.gradle:46` | — | **待确认** |

> 解析方式（网络访问，需授权）：向 Modrinth API（如 `https://api.modrinth.com/v2/version_file/<旧hash>` 或按项目/版本查询）取得 26.2 版本的 `files[0].hashes.sha1` 作为新坐标第三段。

---

## 6. 仓库（repositories）增删建议

### 6.1 当前声明的仓库（`D:/IceAndFire-CE/build.gradle:26-47`）

| 仓库 | URL | 说明 |
|---|---|---|
| terraformersmc | `https://maven.terraformersmc.com/` | **保留**：ModMenu |
| ladysnake | `https://maven.ladysnake.org/releases` | **保留**：若后续引入 Yumi（Trinkets 26.2 传递依赖）需要 |
| neoforged | `https://maven.neoforged.net/releases/` | **保留（无害）**：neoforge 模块排除后无实际用途 |
| theillusivec4（curios） | `https://maven.theillusivec4.top/` | **保留（无害）**：neoforge-only |
| curse maven | `https://cursemaven.com` | **保留（无害）**：ProjectE，neoforge-only |
| modrinth maven | `https://api.modrinth.com/maven` | **必须保留**：Uranus/Jupiter/Jade/Trinkets 的 `maven.modrinth:*` 坐标 |
| jitpack | `https://jitpack.io` | **必须保留**：Integration `com.github.IAFEnvoy.Integration:*` |
| nucleoid | `https://maven.nucleoid.xyz/` | 保留（无害） |
| blamejared | `https://maven.blamejared.com/` | **保留（无害）**：JEI |
| modmaven.dev | `https://modmaven.dev` | 保留（无害） |
| Fuzss modresources | `https://raw.githubusercontent.com/Fuzss/modresources/main/maven/` | 保留（无害） |
| createmod | `https://maven.createmod.net` | **保留（无害）**：Ponder |
| geckolib cloudsmith | `https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/` | 保留（无害）：neoforge-only GeckoLib |
| forge maven | `https://maven.minecraftforge.net/` | 保留（无害） |
| `flatDir ../mappings-patch` | 本地目录 | **删除**（mappings-patch 映射 jar 无人引用，loom-no-remap 下无效） |

### 6.2 需要新增的仓库

| 仓库 | URL | 需要原因 |
|---|---|---|
| Gradle 插件门户 / fabric maven | `https://maven.fabricmc.net/`、`https://maven.architectury.dev/` | **已在 `settings.gradle:3-6` 的 pluginManagement 中**。`loom-no-remap 1.17-SNAPSHOT` 与 `architectury-plugin 3.5-SNAPSHOT` 均发布在 architectury/fabric maven，需确认 pluginManagement 覆盖 |

> 参照 Uranus26.2：其 `build.gradle` 的 `repositories` 块为空（Loom 自动补充 Minecraft 与库下载源；依赖通过 gradle.properties 引入）。迁移时**保留但可精简**现有仓库列表，删除多余项无害，删除 modrinth/jitpack 则**不可**。

---

## 7. 风险提示与开放决策

1. **Trinkets 重写影响代码**（超出依赖文档范围，供参考）：包名、mod id、`registerTrinket`→`TrinketCallback.setCallback`、`TrinketComponent`→`TrinketAttachment`、CCA→Yumi；`IceAndFireFabric.java:20` 的 `runWhenLoad("trinkets", ...)` 必须改为 `"trinkets_updated"`。详见 `external-compat.md`。
2. **Uranus 26.2 删除了一组本项目用到的 API**：`DynamicItemRenderer`、raycoms 寻路（`AdvancedPathNavigate` 等）、`CustomCollisionsNavigator`、`ICustomCollisions`——需在代码迁移文档中处理（见 `uranus.md`）。
3. **EMI 唯一破坏点**：`EmiRegistry.getRecipeManager()` → `getRecipeMap()`（`@Nullable`，需判空）。详见 `external-compat.md`。
4. **Jade 唯一破坏点**：`HealthElement(float,float)` 构造 → 4 参数（需 `Gui.HeartType`）。详见 `external-compat.md`。
5. **架构决定**：architectury-fabric 是否嵌套进 `META-INF/jars/`（Uranus 嵌套 + `jars` 声明；本项目当前不打包）。
6. **neoforge 模块去留**：Uranus/Jupiter 26.2 均为 fabric-only；若保留 neoforge，需补齐 `neoforge_version` 等定义并解析 neoforge 专属 modrinth hash。
7. **`gradle.properties` 中 `org.gradle.java.home` 固定路径**：需与 CI（`setup-java`）环境一致（Uranus26.2 固定 `C:/Program Files/Zulu/zulu-25`，CI 用 temurin 25）。
