# P2-A 迁移报告：Minecraft 26.2 Common 基础 API

> 日期：2026-08-09
> 范围：仅 `common` 模块的 Minecraft 1.21.1 → 26.2 基础 API 迁移（不涉及 fabric / neoforge / 渲染管线 / 外部依赖代码）。
> 依据：`docs/minecraft-api-migration.md` + 1.21.1 / 26.2 Mojmap 源码逐项核对（每个改动均对照源码验证，无猜测、无删功能、无空实现）。

---

## 一、错误数量

| 指标 | 阶段开始 | 当前 |
|---|---|---|
| javac 报错数（`-Xmaxerrs 5000`） | **>5000**（触顶；默认上限 100 时显示 100） | **3,280** |
| 独立错误位置（日志去重） | **4,074** | **2,869** |
| 涉及文件 | 434 / 598 | — |

下降：**4,074 → 2,869（约 30%）**。`cannot find symbol` 从 4,826 → 3,480。

> 说明：阶段开始 javac 报 100 / 5,000 均为其报告上限，并非真实总数；真实独立错误位置经日志去重统计。

---

## 二、已完成的 API 家族（7 个迁移 commit）

| commit | 家族 | 涉及 |
|---|---|---|
| `0539325` | **核心标识符**：`ResourceLocation` → `Identifier` | 146 文件 / 789 处，全部用法（`fromNamespaceAndPath` 293、`DEFAULT_NAMESPACE` 17、`withDefaultNamespace` 9、`CODEC` 4、`tryParse` 3、类型引用）逐一对照 26.2 `Identifier`（同名同签名、包未变），纯 1:1 改名 |
| `0f9b27a` | **Level API**：`isClientSide` 字段 → `isClientSide()` 方法 | 26.2 字段变 private、新增 public 方法；255 处字段访问改方法调用 |
| `dfaa538` | **实体 API**：`getBaseExperienceReward()` → `(ServerLevel)` | 14 文件（13 覆写 + 2 调用），26.2 加 `ServerLevel` 参数 |
| `62c979c` | **Item API**：`appendHoverText` → `(…TooltipDisplay, Consumer<Component>…)` | 46 文件，`List.add` → `Consumer.accept`，super 调用加 `display` 参数 |
| `b51703b` | **Item API**：`inventoryTick` → `(ServerLevel, EquipmentSlot)` | 4 非装甲物品；`slot==MAINHAND` 映射旧 `selected` |
| `406b3a1` | **实体 API + 包迁移**：`MobSpawnType`→`EntitySpawnReason`、`AbstractArrow`→`projectile/arrow/`、`Fireball`→`projectile/hurtingprojectile/` | 53 文件；`SPAWN_EGG`→`SPAWN_ITEM_USE` |
| `5952845` | **杂项 util/实体**：`Tuple`→`commons-lang3 Pair`、`Blocks.WHITE_WOOL`→`Blocks.WOOL.get(DyeColor.WHITE)`、`displayClientMessage`→`sendSystemMessage` | 16 文件 |

另含 P1 commit `b0db41b`（构建系统 loom-no-remap 升级）。

共改动 **~300 个 common Java 文件**（8 个 commit 合计 334 个文件含构建配置）。

---

## 三、关键 API 的源码证据

每个改动均对照 26.2 源码核实，示例：

- `Identifier.tryParse`（`@Nullable`，26.2 `Identifier.java:52`）、`fromNamespaceAndPath`（:40）、`CODEC`（:19）、`DEFAULT_NAMESPACE`、`withDefaultNamespace`（:48）
- `Level.isClientSide()` public（26.2 `Level.java:163`）；字段 `private final`（:127）
- `LivingEntity.getBaseExperienceReward(final ServerLevel)`（26.2 `LivingEntity.java:608`）
- `Item.appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`（26.2 `Item.java:324`）；`TooltipDisplay` 在 `net.minecraft.world.item.component`
- `Item.inventoryTick(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)`（26.2 `Item.java:289`）；调用点 `Inventory.java:246`：`i == selected ? MAINHAND : null`
- `EntitySpawnReason` 枚举（26.2 `EntitySpawnReason.java`）
- `Item.Properties.sword(ToolMaterial, float, float)`（26.2 `Item.java:482`）
- `InteractionResult` sealed interface：`SUCCESS/PASS/FAIL/CONSUME` + `Success.heldItemTransformedTo(ItemStack)`（26.2 `InteractionResult.java`）

---

## 四、尚未处理（剩余主要错误）

**P2-A 范围内（基础 API）：**

| 项 | 错误量 | 原因 / 需做的事 |
|---|---|---|
| `ArmorItem` / `ArmorMaterial` | ~148 | 26.2 移除 `ArmorItem` 类与 `Registries.ARMOR_MATERIAL`；`ArmorMaterial` 变为 record（`Map<ArmorType,Integer>` 防御、`TagKey` 修复、`ResourceKey<EquipmentAsset>` 资产）。6 个装甲物品 → `extends Item` + `Item.Properties().humanoidArmor(material, ArmorType)`；`IafArmorMaterials` 需重写（含 EquipmentAsset 纹理资产注册）。**架构级** |
| `InteractionResultHolder` | ~76 | `Item.use` 返回类型改为非泛型 `InteractionResult`；`new InteractionResultHolder<>(…, itemStack)` / `sidedSuccess(itemStack)` 需按语义改写（`Success.heldItemTransformedTo` 等） |
| `BannerPatternItem` | ~78 | 26.2 移除（旗帜图案数据驱动化），`IafItems` 的注册需重构 |
| `SwordItem` / `IafToolMaterials` | ~26+ | 26.2 移除 `SwordItem` 类与 `Tier` 接口 → `Item.Properties().sword(ToolMaterial,…)` + `IafToolMaterials implements Tier` 改为提供 `ToolMaterial`。牵连整个工具系统 |
| `EntityType.is(TagKey)` | ~50 | 需核对 26.2 签名 |
| `isControlledByLocalInstance` | ~34 | 26.2 移除，需找替代（`getControllingPassenger` 相关） |
| Optional<Integer>/<Boolean> 返回 | ~224 | 若干 getter 在 26.2 改返回 Optional，需逐处适配 |
| `IafRegistries`（`DefaultedMappedRegistry`） | — | 自定义注册表在 26.2 的构造变化待核 |
| `EntityRendererRegistry.register`（Architectury） | ~80 | Architectury 21.0.7 渲染注册 API 变化（属 P6 客户端，但阻塞 common 编译） |

**P6 渲染管线（非 P2-A 范围，但占剩余错误大头）：**

- `MultiBufferSource`（~206，26.2 彻底移除）、`RenderType`（~198）、`GuiGraphics`（~102）→ 需迁移到 26.2 render-state 架构（`extractRenderState`/`submit`/`GuiGraphicsExtractor`）
- 实体/方块实体渲染器、模型、Screen、RenderSystem 立即模式 → 见 `docs/minecraft-api-migration.md` §7-10

---

## 五、下一阶段建议

1. **P2-A 收尾**：先做 `InteractionResult`（Item.use，13 文件，语义明确）、`BannerPatternItem`、`SwordItem`/`ToolMaterial`（工具系统）；再处理 `ArmorItem`（架构级，建议独立阶段，含 EquipmentAsset 数据包 + 纹理）。
2. **Registry**：核对 `IafRegistries`（`DefaultedMappedRegistry`）在 26.2 的构造与 `RegistryKeys`。
3. **P6 渲染管线**：`MultiBufferSource`/`RenderType`/`GuiGraphics`/实体渲染器是剩余最大错误源（500+），需在实体/渲染 API 迁移后单独推进（render-state 架构重写）。

---

## 六、合规确认

- ✅ 未删除任何功能
- ✅ 未注释代码绕过错误
- ✅ 未添加空实现
- ✅ 未猜测 API（每个改动均有 1.21.1/26.2 源码证据）
- ✅ 每批修改后运行 `./gradlew :common:compileJava` 记录错误数
- ✅ 按 API 家族独立 commit（7 个迁移 commit）
