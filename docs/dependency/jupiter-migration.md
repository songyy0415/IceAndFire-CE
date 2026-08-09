# Jupiter 迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/Jupiter old`（2.3.x，MC 1.21.1）vs `D:/aiminecraftdev/Jupiter new`（2.4.2，MC 26.2）。
> 仅迁移 Jupiter 集成代码与坐标；配置 API 因 P2-A 已处理 `ResourceLocation`→`Identifier`，无需额外改动。

---

## 一、版本与坐标

| 项 | 旧 | 新 |
|---|---|---|
| 版本 | 2.3.x（MC 1.21.1） | **2.4.2**（MC 26.2，`jupiter-2.4.2-26.2-universal.jar`） |
| 坐标 | `maven.modrinth:jupiter:5pNXzmee` | `com.iafenvoy:jupiter:2.4.2-26.2-universal`（本地移植构建 → mavenLocal） |
| 来源 | Modrinth 1.21.1 文件 | `D:/aiminecraftdev/Jupiter new`（Stonecutter 26.2 构建） |

## 二、API 对照（旧 → 新）

**结论：Jupiter API 包根与签名基本不变**（`com.iafenvoy.jupiter`），唯一真正的代码变化来自 MC 层（`ResourceLocation`→`Identifier`，P2-A 已完成）。

| 旧 API | 新 API | 判定 |
|---|---|---|
| `ConfigManager.getInstance().registerConfigHandler(...)` | 同 | 无变化 |
| `ServerConfigManager.registerServerConfig(..., PermissionChecker.IS_OPERATOR)` | 同 | 无变化 |
| `AutoInitConfigContainer#<init>(ResourceLocation, String, String)` | `#<init>(Identifier, String, String)` | MC 层改名（P2-A 已处理） |
| `FileConfigContainer#<init>(ResourceLocation, String, String)` | `#<init>(Identifier, String, String)` | 同上 |
| `BooleanEntry/DoubleEntry/IntegerEntry/SeparatorEntry.builder(...)` | 同 | 无变化 |
| `ConfigSelectScreen.builder(Component, Screen).server(...).client(...).build()` | 同 | 无变化 |

（新版删除的旧弃用 API —— 直接构造器、`SeparatorEntry.text/tooltip` 实例方法等 —— 项目全部使用 builder，不受影响。）

## 三、修改文件

- `common/build.gradle`：`maven.modrinth:jupiter:5pNXzmee` → `com.iafenvoy:jupiter:2.4.2-26.2-universal`
- `fabric/build.gradle`：同上
- `common/.../config/IafCommonConfig.java` / `IafClientConfig.java`：**无改动**（P2-A 已把容器构造的 `ResourceLocation`→`Identifier`）
- `common/.../IceAndFire.java` / `IceAndFireClient.java`：无改动
- `fabric/.../ModMenu.java`（`ConfigSelectScreen.builder`）：无改动

## 四、行为变化

- **无**。配置注册、加载、同步、ModMenu 集成行为均不变（API 未变，仅编译目标从 1.21.1 Jupiter 换成 26.2 Jupiter）。

## 五、验证

- `./gradlew :common:compileJava`：错误数 2,183 → 2,179（jupiter 相关 0 错误）；配置类 0 错误。
