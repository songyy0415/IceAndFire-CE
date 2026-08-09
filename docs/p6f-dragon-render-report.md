# P6-F Dragon Render 迁移报告

> 迁移：MC 1.21.1 → 26.2，Dragon RenderState + 普通 Feature。
> 基线：commit `89a3c5c`（Hippogryph）。当前编译：javac 1,987 / unique 1,731（迁移前 2,014 / 1,751）。

---

## 本批：DragonRenderState + 普通 Feature（Glow / MaleOverlay / Armor）

### 修改文件

| 文件 | 动作 |
|---|---|
| `render/entity/state/DragonRenderState.java` | **新增**：`LivingEntityRenderState implements IAnimatedEntity` |
| `render/entity/feature/DragonEyesFeatureRenderer.java` | **迁移**：`RenderLayer<DragonRenderState, TabulaModel<DragonRenderState>>` + `submit(…)`；`RenderTypes.eyes(state.eyesTexture)` + `submitModel` |
| `render/entity/feature/DragonMaleOverlayFeatureRenderer.java` | **迁移**：`submit(…)`；`RenderTypes.entityTranslucent(state.maleOverlayTexture)` + `submitModel` |
| `render/entity/feature/DragonArmorFeatureRenderer.java` | **迁移**：`submit(…)` 遍历 `state.armorHead/Chest/Legs/Feet` 4 个纹理；`RenderTypes.entityCutout` + `submitModel`；`getArmorTexture` 静态保留（extract 用） |

### DragonRenderState 字段

- 纹理：`texture / eyesTexture / maleOverlayTexture / armorHead / armorChest / armorLegs / armorFeet`
- 渲染：`renderSize / dragonPitch / prevDragonPitch`
- 状态：`shouldRenderEyes / isMale / isSkeletal / variant / dragonStage`
- 动画：`animation / animationTick / animations`（IAnimatedEntity，供后续 animator）

### submit 迁移

- 旧：`bufferIn.getBuffer(RenderType.xxx) + model.renderToBuffer(matrix, vc, light, overlay, -1)`
- 新：`submitNodeCollector.order(1).submitModel(model, state, poseStack, RenderTypes.xxx(texture), light, NO_OVERLAY, -1, null, state.outlineColor, null)`
- `RenderType.eyes`→`RenderTypes.eyes`、`RenderType.entityTranslucent`→`RenderTypes.entityTranslucent`、`RenderType.entityCutoutNoCull`→`RenderTypes.entityCutout`（26.2 包移入 `rendertype`，`entityNoOutline/entityCutoutNoCull` 删除）

### 错误变化（本批）

- 移除：24（DragonRenderState + 3 个 Feature）。
- 新增：3（`DragonBaseEntityRenderer` 的 `addLayer` 传 `this` 类型不匹配——Feature 构造已改 `RenderLayerParent<DragonRenderState, …>`，渲染器未迁移；**临时性**，渲染器迁移后消除）。
- javac：2,014 → **1,987**；unique：1,751 → **1,731**。

---

## 未处理及原因

| 项 | 原因 |
|---|---|
| **DragonBaseEntityRenderer** | 构造里 `addLayer` 依赖 5 个 Feature；Rider/Banner 未迁移 → 渲染器暂无法编译（本批 3 个临时 addLayer 错误）。 |
| **DragonRiderFeatureRenderer** | 核心用 `Minecraft.getEntityRenderDispatcher().render(Entity, …)` 渲染乘客——**26.2 已删除该方法**（只留 `submit`），乘客渲染需 26.2 实体 submit 管线专项重写。 |
| **DragonBannerFeatureRenderer** | 用 `getItemRenderer().renderStatic(...)` 渲染旗帜——26.2 需 `ItemStackRenderState` 迁移（可参考 PixieItem），留待渲染器批。 |
| **DragonTabulaModelAnimator** | 读取 DragonBaseEntity 大量动画字段，需 state 化（`ITabulaModelAnimator<DragonRenderState>`）——任务指定暂不处理。 |
| **SeaSerpentAncient / Dragon 剩余** | 依赖各实体 RenderState + Tabula animator。 |

---

## 未触碰
- Particle / Screen / Mixin / DynamicItemRenderer。
- 无删除功能 / 注释绕过 / 空实现 / `@SuppressWarnings`。
