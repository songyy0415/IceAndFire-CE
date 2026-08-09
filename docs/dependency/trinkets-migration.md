# Trinkets 迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/trinkets1.21.1`（3.x，`dev.emi.trinkets`）vs `D:/aiminecraftdev/trinkets26.2`（4.1.0-beta.3+26.2，`eu.pb4.trinkets`，Patbox「Trinkets Updated」重写）。
> Trinkets 是 **fabric 模块**兼容（`fabric/src/main/java/.../compat/trinkets/`），不参与 `:common:compileJava`。

---

## 一、坐标与 mod id

| 项 | 1.21.1 | 26.2 |
|---|---|---|
| 坐标 | `maven.modrinth:trinkets:JagCscwi`（dev.emi.trinkets） | `maven.modrinth:trinkets-updated:to3SIE4i`（eu.pb4.trinkets，P1 已设置） |
| mod id | `trinkets` | **`trinkets_updated`** |
| 底层组件 | Cardinal-Components（CCA） | **不再依赖 CCA**（改用 PB4 自己的 attachment 系统） |

## 二、API 对照（旧 → 新）

| 旧 API（dev.emi.trinkets） | 新 API（eu.pb4.trinkets） |
|---|---|
| `TrinketsApi.registerTrinket(Item, Trinket)` | `TrinketCallback.setCallback(Item, TrinketCallback)`（`callback/TrinketCallback.java:48`） |
| `interface Trinket`（`tick(ItemStack, SlotReference, LivingEntity)` 等） | `interface TrinketCallback`（`tick(ItemStack, TrinketSlotAccess, LivingEntity)` 默认方法等，`TrinketCallback.java:59`） |
| `SlotReference` | `TrinketSlotAccess`（`slotType()/get()/set()` 等） |
| `TrinketsApi.getTrinketComponent(...)` / CCA | `TrinketsApi.getAttachment(LivingEntity)` → `TrinketAttachment` |

## 三、修改文件

| 文件 | 修改 |
|---|---|
| `fabric/.../compat/trinkets/TrinketsRegistry.java` | `TrinketsApi.registerTrinket` → `TrinketCallback.setCallback`；import `dev.emi.trinkets.api` → `eu.pb4.trinkets.api.callback` |
| `fabric/.../compat/trinkets/SimpleTickItemWrapper.java` | `implements Trinket` → `implements TrinketCallback`；`tick(ItemStack, SlotReference, LivingEntity)` → `(ItemStack, TrinketSlotAccess, LivingEntity)`；内部转发改为 `item.inventoryTick(stack, (ServerLevel) entity.level(), entity, null)` |
| `fabric/.../fabric/IceAndFireFabric.java` | `IntegrationExecutor.runWhenLoad("trinkets", ...)` → `"trinkets_updated"` |
| `fabric/build.gradle` | 坐标已改（P1） |
| `fabric/src/main/resources/data/trinkets/...`（entities/slots/tags JSON） | 数据格式兼容（26.2 仍读 `data/<ns>/trinkets`），**无需改** |

## 四、行为变化

- **`Item.inventoryTick` 转发改为仅服务器端**：26.2 `inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot)` 需要 `ServerLevel`；`TrinketCallback.tick` 在双端调用，故包装器仅当 `entity.level() instanceof ServerLevel` 时转发（1.21.1 传 `Level` 双端皆可）。物品逻辑（如 HydraHeart 回血）本就是服务器逻辑，行为等价。
- Trinket 槽位行为（`legs/belt` 等）不变。

## 五、验证

- `:common:compileJava` 不覆盖 fabric 模块（fabric 编译被 common 剩余错误阻塞）。改动按 26.2 `eu.pb4.trinkets` 源码核对（`TrinketCallback.setCallback`/`tick` 签名、`TrinketSlotAccess` 存在）。
