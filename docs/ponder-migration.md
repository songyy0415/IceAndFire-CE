# Ponder 兼容迁移报告（MC 1.21.1 → MC 26.2）

> 项目：`D:/IceAndFire-CE`（Architectury common/fabric，Mojmap，Fabric 目标）
> 参照：1.21.1 Ponder 源码 `D:/aimoddev/ponder/Ponder1.21.1`；26.2 源码 `D:/aimoddev/createfly/Create-Fly`（分支 26.2）

---

## 1. 背景

1.21.1 分支里龙钢锻炉（Dragon Forge）有 Ponder 教学场景：

- `common/src/main/java/com/iafenvoy/iceandfire/compat/ponder/IceAndFirePonderPlugin.java`
- `common/src/main/java/com/iafenvoy/iceandfire/compat/ponder/DragonForgeStoryBoard.java`
- 场景结构：`common/src/main/resources/assets/iceandfire/ponder/{fire,ice,lightning}_dragon_forge.nbt`
- 语言键：`assets/iceandfire/lang/*.json` 中的 `iceandfire.ponder.dragon_forge.*`

1.21.1 使用独立 Ponder 模组（`net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61`）。
MC 26.2 时代没有独立 Ponder 构件：Ponder 已被并进 Create-Fly（`maven.modrinth:create-fly`，
mod id 为 `create`）。迁移到 26.2 时曾因找不到 `Ponder-Fabric-26.2` 而把上述两个类整体删除
（见 `docs/p9-compat-error-audit.md`，提交 `e2642acc`）。本报告记录补回过程。

---

## 2. 依赖变化

| 项 | 1.21.1 | 26.2 |
|---|---|---|
| 构件 | `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.61` | `maven.modrinth:create-fly:26.2-6.0.9-2` |
| 声明位置 | `common/build.gradle` `modImplementation`；`fabric/build.gradle` `modCompileOnly` | `common/build.gradle` `compileOnly` |
| 运行时 | 可选安装 Ponder | 可选安装 Create-Fly（Ponder 内置） |
| 检测方式 | `IntegrationExecutor.runWhenLoad("ponder", ...)` | `IntegrationExecutor.runWhenLoad("create", ...)` |

`create-fly` 只以 `compileOnly` 参与编译，不会被 shadow/嵌套进本模组产物；玩家安装
Create-Fly 时 Ponder 场景才会注册。

---

## 3. API 差异（1.21.1 Ponder vs 26.2 Create-Fly 内置 Ponder）

| 内容 | 1.21.1 | 26.2 |
|---|---|---|
| 根包 | `net.createmod.ponder` | `com.zurrtum.create.client.ponder` |
| 资源 ID 类型 | `ResourceLocation` | `Identifier` |
| `PonderPlugin` / `PonderIndex` | `api.registration.PonderPlugin`、`foundation.PonderIndex` | 同名接口/类，仅包名变化 |
| 注册辅助器 | `PonderSceneRegistrationHelper` / `PonderTagRegistrationHelper` | 签名一致（`forComponents`、`addStoryBoard`、`registerTag`、`addToTag`） |
| `SceneBuilder` | `overlay()/world()/title/configureBasePlate/removeShadow/idle/rotateCameraY` | 签名一致 |
| `WorldInstructions` | `showSection/modifyBlock/createEntity/modifyEntities` | 签名一致 |
| `TextElementBuilder` / `TagBuilder` | `showText/text/attachKeyFrame/placeNearTarget/pointAt`、`registerTag/.../register` | 签名一致 |
| 场景结构路径 | `assets/<namespace>/ponder/<id>.nbt` | 相同（`PonderSceneRegistry.loadSchematic`） |
| 场景 level | `PonderLevel extends SchematicLevel` | `PonderLevel extends SchematicRenderLevel extends SchematicLevel` |

MC 层面的连带变化（本模组代码需要适配的）：

- `EntityType#create(Level)` 在 26.2 移除，改为 `create(Level, EntitySpawnReason)`；
  本模组用 `DragonType#createEntity(Level)`（其内部即 `create(level, LOAD)`）替代工厂方法。
- `Direction#getNormal()` 在 26.2 移除（改为 `getStepX/getStepY/getStepZ`）；
  用 `BlockPos#relative(Direction)` 代替 `BlockPos#offset(direction.getNormal())`。

---

## 4. 代码改动

### 4.1 恢复 `compat/ponder` 包

- `IceAndFirePonderPlugin`：按新包名恢复，注册 3 个故事板（fire/ice/lightning）与
  `iceandfire:dragon_forge` 标签；`init()` 调用 `PonderIndex.addPlugin(...)`。
  实体工厂改为 `IafDragonTypes.FIRE/ICE/LIGHTNING::createEntity`。
- `DragonForgeStoryBoard`：场景脚本逐行保留（标题、底座、分层显示、炉砖 `GRILL` 状态、
  龙实体生成与摆位、文本提示）。仅改了包名与 `center.relative(...)`。

### 4.2 客户端入口

`IceAndFireClient.init()` 结尾恢复：

```java
IntegrationExecutor.runWhenLoad("create", () -> IceAndFirePonderPlugin::init);
```

`runWhenLoad` 只在该 mod 已加载时执行 lambda，未安装 Create-Fly 时不会加载
`IceAndFirePonderPlugin`（避免 `NoClassDefFoundError`）。

### 4.3 龙实体 tick 的 Ponder 场景判断

`DragonBaseEntity#tick` 原判断：

```java
IntegrationExecutor.getWhenLoad("ponder",
        () -> () -> this.level().getClass().getSimpleName().equals("SchematicLevel"), () -> false)
```

26.2 改为检测 `create`，并同时接受 `PonderLevel`（26.2 Create-Fly 的场景 level 类名）与
`SchematicLevel`（1.21.1 Ponder 的判断名），仍然使用类名字符串判断以避免对 Ponder 的硬依赖。
命中时跳过 `refreshDimensions()/updateParts()`。

### 4.4 构建配置

- `gradle.properties`：新增 `create_fly_version=26.2-6.0.9-2`；
  `ponder_version=1.0.61` 仅保留给（未参与构建的）neoforge 模块引用。
- `common/build.gradle`：`compileOnly "maven.modrinth:create-fly:${create_fly_version}"`。
- `fabric/build.gradle`：删除失效的 Ponder-26.2 注释（Ponder 类只在 `:common` 中被引用，
  且 Create-Fly 是可选运行时模组，不打包）。

---

## 5. 资源

`assets/iceandfire/ponder/{fire,ice,lightning}_dragon_forge.nbt` 与语言键在迁移时未被删除，
本次无需改动。26.2 的 `StructureTemplate` 仍从 `assets/<ns>/ponder/<path>.nbt` 读取。

---

## 6. 验证

- `./gradlew :common:compileJava`：通过（仅 JEI 既有的 deprecation 警告）。
- `./gradlew build`：通过；产物 `fabric/build/libs/IceAndFireCE-fabric-2.2.jar` 含
  `com/iafenvoy/iceandfire/compat/ponder/*`，且不含任何 `com/zurrtum/create` 类（compileOnly 未泄漏）。
- dev 客户端（`./gradlew :fabric:runClient`）：
  - 不装 Create-Fly：`runWhenLoad("create", ...)` 不执行，Ponder 类不加载，启动正常。
  - 把 `create-fly-26.2-6.0.9-2.jar` 放入 `fabric/run/mods/` 后启动：Create 正常加载，
    调试日志确认 `IceAndFirePonderPlugin.init()`（`PonderIndex.addPlugin`）在
    Create 的 `Minecraft.run()` 注入点调用 `PonderIndex.registerAll()` 之前执行，
    随后 `Registering Ponder Scenes/Tags` 正常完成，无异常、无崩溃。
  - 插件以 `getModId() = iceandfire` 注册，场景键为
    `iceandfire:fire_dragon_forge`、`iceandfire:ice_dragon_forge`、`iceandfire:lightning_dragon_forge`，
    与 `assets/iceandfire/ponder/*.nbt` 一一对应。

