# P2-E Common Gate 报告（P2 收尾）

> 目标：清理剩余的、明确属于 Common Gameplay/API 的问题，结束 P2。
> 每个修改均对照 1.21.1 / 26.2 Mojmap 源码；禁止处理 client/render/EntityRenderer/RenderState/RenderType/MultiBufferSource/GuiGraphics/Shader/PlayerRenderer/WorldRenderer/Mixin/Fabric API/第三方依赖；无法明确归属的错误不修改、记录后续。

---

## 一、本阶段修改

### 1.1 完成项（commit `0bfaf6b`，17 文件）

| 项 | 26.2 变化 | 处理 |
|---|---|---|
| **VillagerProfession** | 移到 `net.minecraft.world.entity.npc.villager`；由 class 变 **record**（`name` 变 `Component`、新增 `Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel`） | `IafTrades`：import 移包、`SCRIBE`→`Component.translatable`、补 `tradeSetsByLevel` |
| **NearestAttackableTargetGoal** 6 参 | 最后一个参数 `Predicate<LivingEntity>` → **`TargetingConditions.Selector`**（`boolean test(LivingEntity, ServerLevel)`） | 8 个 AI 目标类 ctor 参数改 `Selector`；调用方 lambda 转 2 参 `(entity, level) ->`（含多行 lambda、方法引用 `X::m`→`(e,l)->X.m(e)`） |
| **Item.hurtEnemy** | 26.2 返回 **void**（`Item.java:252`） | 10 个工具物品覆写 `boolean`→`void`，`return super.hurtEnemy`→`super.hurtEnemy` |

### 1.2 验证

- `./gradlew :common:compileJava`：**javac 2,523 errors**；**去重 2,183 个独立错误位置**（P2-E 开始 2,222 → 2,183，降 ~40 处；本阶段聚焦明确 Common 项，未追求全清）。

---

## 二、剩余错误分类

| 类别 | 占比（按错误行） | 说明 |
|---|---|---|
| **Rendering/Client** | ~53% | `render/`、`mixin/`、`screen/`、`compat/`、`particle/`、`registry/IafRenderers`/`IafRenderLayers` —— 渲染管线（`MultiBufferSource`/`RenderType`/`GuiGraphics`/`EntityRenderer`/粒子）、Screen（`MenuScreens.register` 私有）、Jade/JEI/EMI 兼容 —— **P6** |
| **Common 剩余** | ~34% | 实体/物品/方块/数据/配方/世界 API：`spawnAtLocation`（加 `ServerLevel`+`Vec3`）、`knockback`、`EntityType.create`、`StructureProcessorType` 泛型、`CompoundTag`→`ValueOutput` 残留、`void→boolean` 残余 —— **P4/P5 续** |
| **无法确定归属** | ~13% | `AdvancedPathNavigate`（**Uranus 第三方** raycoms，26.2 已删）、`MenuScreens`（client 屏幕注册）、trade 系统（`TradeSet` 数据驱动）、`IafScreenHandlers`、部分 entity 文件 |

---

## 三、记录后续阶段（未修改）

### Common API 深层迁移（P4/P5 续）
- **`Entity.spawnAtLocation`**：26.2 加 `ServerLevel` 首参 + `Vec3` 偏移（`Entity.java:2227-2235`）；mod 约 34 处 `spawnAtLocation(item, yOffset)` 需逐处传 level + 转 Vec3。
- **`Entity.knockback`**：26.2 签名变化（`knockback(float,double,double)` 需核对），14 处。
- **`EntityType.create(Level)` → `create(ServerLevel, EntitySpawnReason)`**：约 40 处。
- **`StructureProcessorType`** 泛型（22 处）。
- **NBT 写入残留**（`CompoundTag`→`ValueOutput`，20 处，部分在 block entity）。
- **交易系统**：`TradeOfferHelper`/`TradeOfferInternals` 依赖旧 `VillagerTrades.ItemListing` + profession 直连；26.2 改 `TradeSet` 数据驱动（`tradeSetsByLevel`），需数据迁移（P5）。

### 第三方 / Client（不在 Common 范围）
- **`AdvancedPathNavigate`**（Uranus raycoms 寻路）—— Uranus 26.2 已删除该包（见 `uranus.md`），属第三方依赖适配（P3/P4），本次未处理。
- **`MenuScreens.register` 私有** + `IafScreenHandlers` —— client 屏幕注册（P6）。

---

## 四、合规确认

- ✅ 未处理 render/mixin/client/screen/compat/Fabric API/第三方
- ✅ 无法明确归属的错误（Uranus 寻路、TradeSet 系统）记录未改
- ✅ 每个修改均有 1.21.1/26.2 源码依据；未删功能、未注释绕过、未空实现

---

## 五、P2 阶段总结

| 阶段 | 主要工作 | 错误（去重） |
|---|---|---|
| P2-A 基础 | ResourceLocation→Identifier、isClientSide、appendHoverText、inventoryTick、MobSpawnType、包迁移等 | 4074 → 2745 |
| P2-B 收尾 | InteractionResult、Tool 系统（Tier→ToolMaterial） | 2745 → 2585 |
| P2-C Armor | IafArmorMaterials、6 装甲物品、equipment_asset | 2585 → 2428 |
| P2-D 清理 | CompoundTag Optional、hurtServer/ValueIO/UUID、EntityType.is、isControlledByLocalInstance、SoundEvents、VillagerTrades | 2428 → 2222 |
| P2-E Gate | VillagerProfession、NearestAttackableTargetGoal Selector、hurtEnemy void | 2222 → **2183** |

**P2 全阶段去重错误：4,074 → 2,183（下降 ~46%）**。剩余错误中约 53% 属渲染/客户端（P6），约 34% 属 Common 深层 API（P4/P5 续），约 13% 为第三方/交易系统（P3/P5）。
