# Integration 迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/Integration`（0.2，loader 级、**不依赖 MC 版本 API**）。

---

## 一、坐标

| 项 | 值 |
|---|---|
| 坐标 | `com.github.IAFEnvoy.Integration:integration-common/-fabric:0.2`（jitpack，P1 未变） |

## 二、API 对照（旧 → 新）

| API | 判定 |
|---|---|
| `IntegrationExecutor.runWhenLoad(String modId, Supplier<Runnable>)` | 无变化（`IntegrationExecutor.java` 仍存在，loader 级 API 与 MC 版本无关） |
| `IntegrationExecutor.getWhenLoad(String modId, Supplier<T>, Supplier<T>)` | 无变化（`DragonBaseEntity.java:1650` 用于 ponder） |
| `integration-fabric` 嵌套进 `META-INF/jars`（P1 已配置） | 无变化 |

## 三、修改文件

- **无**。`IntegrationExecutor` 的调用点（`IceAndFireClient.java:27` ponder、`IceAndFireFabric.java` trinkets_updated、`DragonBaseEntity.java:1650` ponder 检查）在 26.2 下编译通过。

## 四、行为变化

- **无**。Integration 是 loader 级库（基于 Fabric Loader 的 ModContainer 检查），不触及 MC 版本 API；`runWhenLoad("trinkets_updated", ...)` 的 mod id 已随 Trinkets 迁移更新（P3-C）。

## 五、验证

- `./gradlew :common:compileJava`：Integration 相关 0 错误。
- 运行时验证（`IntegrationExecutor.getModContainer` 在 Fabric Loader 0.19.3 下）留待最终启动测试。
