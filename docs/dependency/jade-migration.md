# Jade 迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/Jade1.21.1` vs `D:/aiminecraftdev/Jade26.2`（26.2.10）。

---

## 一、坐标

| 项 | 1.21.1 | 26.2 |
|---|---|---|
| 坐标 | `maven.modrinth:jade:pA0xvozk` | `maven.modrinth:jade:JB4B8a9g`（26.2.10，P1 已设置） |

## 二、API 对照（旧 → 新）

| 旧 API | 新 API | 判定 |
|---|---|---|
| `snownee.jade.api.IWailaPlugin` / `@WailaPlugin` / `registerClient(IWailaClientRegistration)` | 同 | 无变化 |
| `IEntityComponentProvider` / `IBlockComponentProvider` / `appendTooltip(ITooltip, EntityAccessor, IPluginConfig)` / `getUid()` | 同 | 无变化 |
| `snownee.jade.impl.ui.HealthElement(float maxHealth, float health)` | `HealthElement(Hud.HeartType, float maxHealth, float health, float absorption)`（`HealthElement.java:36`） | 构造加 `HeartType` + `absorption`；26.2 MC `Gui`→`Hud`（`net.minecraft.client.gui.Hud.HeartType`，Jade 自身也用 `Hud.HeartType.NORMAL`） |
| `snownee.jade.impl.ui.ArmorElement(float)` | 同 | 无变化 |

## 三、修改文件

- `common/.../compat/jade/MultipartComponentProvider.java`：
  - `new HealthElement(mob.getMaxHealth(), mob.getHealth())` → `new HealthElement(Hud.HeartType.NORMAL, mob.getMaxHealth(), mob.getHealth(), 0)`
  - 新增 `import net.minecraft.client.gui.Hud;`
- `common/.../compat/jade/{IceAndFireJadePlugin, DragonComponentProvider, DragonEggBlockComponentProvider, DragonEggEntityComponentProvider}.java`：**无改动**（插件/组件接口 26.2 不变）

## 四、行为变化

- **无**。Multipart 实体的血量/护甲/阶段信息显示不变（`HeartType.NORMAL` = 正常心，`absorption=0` = 无吸收）。

## 五、验证

- `./gradlew :common:compileJava`：Jade 相关 0 错误。
