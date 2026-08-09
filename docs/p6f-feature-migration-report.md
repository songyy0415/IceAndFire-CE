# P6-F FeatureRenderer / RenderLayer 迁移报告

> 迁移：MC 1.21.1 → 26.2，RenderLayer `render(…, MultiBufferSource, …)` → `submit(PoseStack, SubmitNodeCollector, int light, S state, float yRot, float xRot)`。
> 基线：commit `7004134`（Siren）。当前编译：javac 2,046 / unique 1,775（迁移前 2,070 / 1,797）。

---

## 本批：Pixie 全量（渲染器 + Glow/Item Feature + RenderState）

目标：以最小渲染器（Pixie）打通 feature 子系统迁移，为 Dragon/SeaSerpent/Hippogryph 建立模式。

### 修改文件

| 文件 | 动作 |
|---|---|
| `render/entity/state/PixieRenderState.java` | **新增**：`extends LivingEntityRenderState`，字段 `texture / sitting / hasItemInHand` |
| `render/entity/feature/PixieGlowFeatureRenderer.java` | **迁移**：`RenderLayer<PixieRenderState, PixieModel>`；`submit(…)`；`RenderTypes.eyes(state.texture)` + `submitNodeCollector.order(1).submitModel(…)` |
| `render/entity/feature/PixieItemFeatureRenderer.java` | **迁移**：`RenderLayer<PixieRenderState, PixieModel>`；`submit(…)`；`state.headItem.submit(poseStack, collector, light, overlay, outlineColor)` |
| `render/entity/PixieEntityRenderer.java` | **迁移**：`MobRenderer<PixieEntity, PixieRenderState, PixieModel>`；extract 快照 texture/sitting/hasItemInHand + `itemModelResolver.updateForLiving(state.headItem, …)`；`scale(state)`；`getTextureLocation(state)` |
| `render/model/PixieModel.java` | 泛型→`PixieRenderState`；`setupAnim(state)` 单参 + 本地映射；实体读取改 state（hasItemInHand/sitting） |

### RenderState 字段说明

- `texture`：主/发光纹理（`getColor()` 计算，extract 快照）。
- `sitting`：`isPixieSitting()`（模型坐姿 + 渲染器 scale 位移）。
- `hasItemInHand`：`!getItemInHand(MAIN_HAND).isEmpty()`（模型手持姿势）。
- `headItem`（继承自 `LivingEntityRenderState`）：手持物品的 `ItemStackRenderState`，extract 用 `ItemModelResolver.updateForLiving` 填充，feature 用 `.submit(…)` 渲染。

### submit 迁移（API 变化）

旧：
```java
render(PoseStack, MultiBufferSource buffer, int light, Entity e, float…){
    RenderType eyes = RenderType.eyes(texture);          // 26.2 RenderType 移入 rendertype 包
    VertexConsumer vc = buffer.getBuffer(eyes);
    model.renderToBuffer(matrix, vc, light, Overlay, -1);
}
```
新：
```java
submit(PoseStack, SubmitNodeCollector c, int light, S state, float yRot, float xRot){
    RenderType eyes = RenderTypes.eyes(state.texture);
    c.order(1).submitModel(this.getParentModel(), state, matrix, eyes, light, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
}
```
- 物品图层：`state.headItem.submit(matrix, c, light, OverlayTexture.NO_OVERLAY, state.outlineColor)`（`ItemStackRenderState`）。
- 关键点：`MultiBufferSource` 删除；`RenderType.eyes`→`RenderTypes.eyes`；`RenderType` 移入 `net.minecraft.client.renderer.rendertype`；渲染经 `SubmitNodeCollector.submitModel`。

### 错误变化

- 移除：22（PixieRenderState/PixieModel/PixieEntityRenderer/PixieGlow/PixieItem + IafRenderers 注册行）。
- 新增：0（唯一 1 处 `PixieModel:222 getTimer` 为**既有** jar-render helper 错误，因 import 插入行号 +1，属 P6-C 方块渲染器范畴）。
- javac：2,070 → **2,046**；unique：1,797 → **1,775**。

---

## 未处理 Feature（后续批次）

| Feature | 未处理原因 |
|---|---|
| **DragonEyes / DragonMaleOverlay / DragonRider / DragonBanner / DragonArmor** | 依赖 `DragonRenderState`（随 Dragon 渲染器迁移）；DragonArmor 属装备图层（26.2 `EquipmentLayer`）。 |
| **SeaSerpentAncient** | 依赖 `SeaSerpentRenderState` + `SeaSerpentTabulaModelAnimator`（`ITabulaModelAnimator<State>`）。 |
| **Hippogryph 内嵌 RenderLayer（鞍/甲/胸）** | 装备图层，需 26.2 `EquipmentLayer` 模式。 |
| **BipedArmor / BipedArmorFeatureRendererMultiple** | 装备图层（26.2 `EquipmentClientInfo` + `submitModel`）。 |

**共同点**：equipment 图层需 26.2 `EquipmentLayer`（基于 `EquipmentClientInfo` 的模型/纹理解析）；glow 图层本批已示范（`RenderTypes.eyes` + `submitModel`）。

---

## 未触碰
- Particle / Screen / Mixin / DynamicItemRenderer。
- 无删除功能 / 注释绕过 / 空实现 / `@SuppressWarnings`。
