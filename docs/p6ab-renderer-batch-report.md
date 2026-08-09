# P6-A+B Renderer Batch Report

> 迁移：MC 1.21.1 → 26.2 RenderState 架构。
> 基线：commit `24ac811`（DragonEgg 模板）、`880c7db`（Cyclops/DeathWorm 批量）。
> 当前编译：javac 2,070 / unique 1,797（本批前 2,076 / 1,803）。

---

## 本批 Renderer：SirenEntityRenderer ✅

| 文件 | 变更 |
|---|---|
| `render/entity/state/SirenRenderState.java` | **新增**：`extends LivingEntityRenderState implements IAnimatedEntity`，字段 `texture / swimProgress / swimming / singing / singingPose / singProgress / onGround / tailBuffer(ChainBuffer)` + 动画快照（animation/animationTick/animations） |
| `render/model/SirenModel.java` | 泛型 `LivingEntityRenderState`→`SirenRenderState`；`setupAnim(state)` 单参 + 本地变量映射（limbAngle←walkAnimationPos 等）；`animate(state,…)`；实体读取全部改 state（swimProgress/swimming/singing/singingPose/singProgress/onGround/tailBuffer） |
| `render/entity/SirenEntityRenderer.java` | `MobRenderer<SirenEntity, SirenRenderState, SirenModel>`（3 参）；`createRenderState()` + `extractRenderState(entity,state,ticks)`（快照 texture/swim/sing/动画/tailBuffer）；`scale(state)`；`getTextureLocation(state)` → `state.texture` |

### 本批 API 变化
- `MobRenderer<T, M>`（2 参）→ `MobRenderer<T, S extends LivingEntityRenderState, M>`（3 参）。
- `render(entity, yaw, partialTicks, PoseStack, MultiBufferSource, light)` → `createRenderState()` / `extractRenderState(entity, state, partialTicks)` / `getTextureLocation(state)` / `scale(state, PoseStack)`。
- 模型 `setupAnim(entity, 6 参)` → `setupAnim(state)`（动画输入由 `LivingEntityRenderState` 携带：`walkAnimationPos/Speed`、`ageInTicks`、`yRot/xRot`）。
- 动画：state 实现 `IAnimatedEntity` → `animator.startAnimate(state)` 复用（`getAnimation()` 读快照）。
- `ChainBuffer`（`entity.util.ChainBuffer`）经 `tailBuffer` 引用快照。

### 本批错误变化
- 移除：6（SirenRenderState/SirenModel/SirenEntityRenderer + IafRenderers 注册行）。
- 新增：0。
- javac：2,076 → **2,070**；unique：1,803 → **1,797**。

---

## 未处理 Renderer 及原因（后续批次）

| Renderer | 未处理原因 |
|---|---|
| **PixieEntityRenderer** | 依赖 2 个 feature renderer：`PixieItemFeatureRenderer`（26.2 物品渲染管线 `ItemStackRenderState`）、`PixieGlowFeatureRenderer`（eyes 层）。均需 `RenderLayer.submit(PoseStack, SubmitNodeCollector, int, S, float, float)` 迁移。 |
| **HippogryphEntityRenderer** | 文件内 2 个 `LayerHippogriffSaddle`（鞍/甲/胸）等内嵌 `RenderLayer`，用旧 `render(…, MultiBufferSource, …)` → 需改 26.2 submit 管线（`submitNodeCollector.submitModel`）。 |
| **DragonBaseEntityRenderer** | 最复杂：Tabula 模型 + 5 个 feature renderer（MaleOverlay/Eyes/Rider/Banner/Armor）+ 3 个 `DragonTabulaModelAnimator`。feature 与 animator 均需 state 化。 |
| **SeaSerpentEntityRenderer** | Tabula 模型 + `SeaSerpentTabulaModelAnimator`（`ITabulaModelAnimator<State>`）+ `SeaSerpentAncientFeatureRenderer`。 |

**共同阻塞点**：26.2 `RenderLayer<S extends EntityRenderState, M>` 的抽象方法从 `render(…)` 改为 `submit(PoseStack, SubmitNodeCollector, int lightCoords, S state, float yRot, float xRot)`；且 `MultiBufferSource` 已删除，图层内的物品/发光/装备渲染需改用 `SubmitNodeCollector.submitModel(...)` 与 26.2 物品渲染管线。这是独立的子系统（feature renderer 迁移），需专项批次。

---

## 未触碰
- Particle / Screen / Mixin / DynamicItemRenderer / AdvancedModelBox 深层渲染（Uranus 26.2 标注 out-of-scope）。
- 无删除功能 / 无注释绕过 / 无空实现 / 无 `@SuppressWarnings`。
