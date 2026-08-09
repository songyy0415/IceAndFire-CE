# Uranus 迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/Uranus1.21.1`（2.4.1-bugfix，MC 1.21.1）vs `D:/aiminecraftdev/Uranus26.2`（2.4.1-bugfix，MC 26.2，Mojmap、fabric-only）。
> 依据：`docs/.research/uranus.md`（完整 API 对照）+ 两版源码。

---

## 一、坐标

| 项 | 1.21.1 | 26.2 |
|---|---|---|
| 坐标 | `maven.modrinth:uranus:FH0tB0dy` | `com.iafenvoy.uranus:uranus-fabric:2.4.1-bugfix`（本地移植构建 → mavenLocal） |

## 二、API 对照（旧 → 新）与判定

### 2.1 ✅ 不变（无需代码改动，编译通过）

| 包/类 | 判定 |
|---|---|
| `event/Event`（`IafEvents`、`CommonEvents` 的自定义事件工厂） | **逐字相同** |
| `event/EntityEvents` / `LivingEntityEvents` / `PlayerEvents` | 仅 Yarn→Mojmap 类名重映射（`PlayerEntity`→`Player` 等），mod 已是 Mojmap → 无改动 |
| `animation/Animation` / `IAnimatedEntity` / `AnimationHandler` | 不变（`AnimationHandler` 内部 `isClient`→`isClientSide()`，对 mod 透明） |
| `object/RegistryHelper` | 返回 `RegistryEntry`→`Holder`、参数 `RegistryKey`→`ResourceKey` —— mod 已传 `ResourceKey` → 编译通过 |
| `object/ServerHelper` | payload `CustomPayload`→`CustomPacketPayload` —— mod 已用后者 → 无改动 |
| `object/{BlockUtil,EntityUtil,VecUtil,FoodUtils}`、`util/{RandomHelper,ShapeBuilder,MemorizeSupplier}` | 不变 |

### 2.2 ⛔ 已删除（Uranus 26.2 无对应 —— 需特性重构，非本阶段实现）

| 旧 API | 影响文件 | 替代策略（待 AI/碰撞迁移阶段） |
|---|---|---|
| `object.entity.pathfinding.raycoms.AdvancedPathNavigate`（+`MovementType`） | `entity/DragonBaseEntity.java`（`createNavigator`，龙飞行/地面寻路）、`entity/HippogryphEntity.java`、`entity/ai/DragonAIAttackMeleeGoal.java` | 改用 vanilla `PathNavigation` / 自定义实现；龙飞行导航逻辑需重写（`MovementType.WALKING/FLYING`、`PathingStuckHandler`） |
| `...raycoms.PathingStuckHandler` | 同上 | 同上 |
| `...raycoms.IPassabilityNavigator` / `pathjobs.ICustomSizeNavigator` | 龙相关 AI | 同上 |
| `object.entity.collision.CustomCollisionsNavigator` / `ICustomCollisions` | `entity/pathfinding/CyclopsNavigation.java`、`entity/DeathWormEntity.java` | 巨型实体碰撞自定义 → vanilla 碰撞或自定义 navigator |
| `client.render.DynamicItemRenderer` | `registry/IafRenderers.java`、`render/item/{DeathwormGauntlet,GorgonHead,MiscItem,TideTridentItem,TrollWeapon}Renderer.java` | **P6 渲染管线**：item renderer 改为 26.2 render-state（`ItemStackRenderState`/`ItemModelResolver`） |

### 2.3 🔴 客户端模型/动画层重写（Uranus 26.2 迁到 render-state）

| 类 | 变化 | 影响 |
|---|---|---|
| `client/model/AdvancedEntityModel` / `BasicEntityModel` | `<T extends Entity>` 无参构造 → `<T extends EntityRenderState>` + `(ModelPart root)` 构造；`render/setupAnim` 覆写移除（`renderToBuffer` final） | `render/model/*` 10+ 模型类需传 `ModelPart` + render-state 驱动 —— **P6** |
| `client/model/AdvancedModelBox` / `BasicModelPart` / `HideableModelRenderer` / `ModelAnimator` | `MatrixStack`→`PoseStack`、`MatrixStack.Entry`→`PoseStack.Pose`、`consumer.vertex`→`addVertex` 等 | 底层 MC 栈类型变化 —— 随 P6 渲染管线 |
| `client/model/util/HideableLayer` | `<T extends Entity, M extends EntityModel<T>, C extends FeatureRenderer<T,M>>` → `<S extends EntityRenderState, M extends EntityModel<? super S>, C extends RenderLayer<S,M>>` | `DreadLichEntityRenderer`/`DreadThrallEntityRenderer` —— **P6** |
| `client/model/util/TabulaModelHandlerHelper` | `<T extends Entity>` → `<T extends LivingEntityRenderState>` | `IafRenderers`/Tabula 模型 —— **P6** |

## 三、本阶段处理

- **依赖坐标**：已在 P1 切换到 `com.iafenvoy.uranus:uranus-fabric:2.4.1-bugfix`（26.2）。
- **未变 API**：验证通过（Event/animation/RegistryHelper/ServerHelper/util 编译无 uranus 错误）。
- **已删除 API**：`DynamicItemRenderer`（P6 渲染）、raycoms 寻路 + collision（AI/碰撞特性重构）—— 按 P3 规则（禁止渲染管线、不修改其它阶段代码）**本阶段不实现**，记录为后续阶段。

## 四、验证

- `./gradlew :common:compileJava`：uranus **未变 API 0 错误**；仅已删除 API 的使用点报错（raycoms 16+8+2+2、collision 2+2、DynamicItemRenderer 22）。
