# P6-J Uranus Model Pipeline / AdvancedModelBox Migration — Status Report

## Phase goal (from prompt)
- 修复 compile 后的模型运行时风险 (fix the post-compile runtime risk)
- 将 Uranus 旧模型系统接入 26.2 submit pipeline (integrate the Uranus AdvancedModelBox system into the 26.2 submit pipeline)
- 不修改动画逻辑 / 不删除模型

## 1. Root-cause of the runtime risk (verified against 26.2 sources)
26.2 `Model.renderToBuffer` is **final** and draws only the single `ModelPart root`
(`net/minecraft/client/model/Model.java:31`). `LivingEntityRenderer.submit` renders the
model via `SubmitNodeCollector.submitModel(model, ...)` →
`ModelFeatureRenderer` → `model.renderToBuffer(...)`. `ModelPart` is `final` and its
geometry (`cubes`/`children`) is private-final; `Model.root` is `protected final`.

The mod's hand-built models (`DragonBaseModel` passes `new ModelPart(List.of(), Map.of())`;
`BipedBaseModel` has no valid ctor) carry all geometry in Uranus `AdvancedModelBox`
`cubeList`s, **not** in a `ModelPart`. → `renderToBuffer` draws an empty root → **these mobs
rendered invisible at runtime.** Tabula models were already fixed in Uranus via
`TabulaModel.buildRenderRoot` (ModelPart mirror + `syncRenderTree`).

## 2. Fix: wire the AdvancedModelBox tree into the 26.2 deferred graph

### Uranus (re-published to mavenLocal as `2.4.1-bugfix`)
`AdvancedEntityModel.renderPartsToBuffer(PoseStack, VertexConsumer, int light, int overlay, int color)`:
walks `getAllParts()`, renders each top-level box (`getParent() == null`) through
`AdvancedModelBox.render(...)` — the legacy geometry path (translateAndRotate + cube quads +
recursion). No animation math touched.

### Mod: `render/entity/AdvancedEntityRendererBase.java` (new)
`AdvancedEntityRendererBase<E extends Mob, S extends LivingEntityRenderState, M extends AdvancedEntityModel<S>>
extends MobRenderer<E, S, M>` replicates the 26.2 `LivingEntityRenderer.submit` flow but
replaces `submitModel(model, ...)` with:
```java
submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
    PoseStack fresh = new PoseStack();
    fresh.last().pose().set(pose.pose());
    fresh.last().normal().set(pose.normal());
    this.model.renderPartsToBuffer(fresh, buffer, state.lightCoords, overlayCoords, tintedColor);
});
```
(`setupAnim(state)` runs before the deferred draw, so the animated pose is captured.)

### Template (proven): Troll
- `render/entity/state/TrollRenderState.java` (new): `LivingEntityRenderState` + `IAnimatedEntity`
  (animation/animationTick/animations) + `trollType`/`weaponType`/`isStone`/`stoneProgress`.
- `TrollModel`: `setupAnim(TrollEntity,...)` → `setupAnim(TrollRenderState state)`, animation
  inputs from `state.walkAnimationPos/Speed/ageInTicks/yRot/xRot` + `state.stoneProgress`; same math.
- `TrollEntityRenderer`: `AdvancedEntityRendererBase<TrollEntity, TrollRenderState, TrollModel>`
  + `createRenderState()` + `extractRenderState(...)` + `getTextureLocation(state)`.
- `TrollEyesFeatureRenderer` / `TrollWeaponFeatureRenderer`: `RenderLayer<TrollRenderState, TrollModel>`
  with `submit(...)` drawing `getParentModel().renderPartsToBuffer(...)` via `submitCustomGeometry`.

## 3. Verified error change
| metric | before | after | delta |
|---|---|---|---|
| javac error lines | 3,186 | 3,092 | **-94** |
| unique error messages | 1,423 | 1,379 | **-44** |

Fixed files: `TrollModel`, `TrollEntityRenderer`, `TrollEyesFeatureRenderer`,
`TrollWeaponFeatureRenderer`, `IafScreenHandlers` (cascade). **0 new error files.**

## 4. Repeatable recipe for the remaining batch
For each hand-built model + renderer pair:
1. New `XxxRenderState extends LivingEntityRenderState implements IAnimatedEntity` — add the
   entity fields the model's `setupAnim` reads (e.g. `stoneProgress`, `isPassenger`, arm/attack).
2. Model: `extends DragonBaseModel<XxxRenderState>` (or `BipedBaseModel<XxxRenderState>`);
   `setupAnim(XxxEntity,...)` → `setupAnim(XxxRenderState state)` with
   `limbAngle=state.walkAnimationPos; limbDistance=state.walkAnimationSpeed;
   animationProgress=state.ageInTicks; headYaw=state.yRot; headPitch=state.xRot;` then the SAME body.
3. Renderer: `extends AdvancedEntityRendererBase<XxxEntity, XxxRenderState, XxxModel>` +
   `createRenderState/extractRenderState/getTextureLocation(state)`.
4. Feature layers: `RenderLayer<XxxRenderState, XxxModel>` + `submit(...)` via `submitCustomGeometry`.

## 5. Remaining (next batches)
- Models (28): Amphithere, BipedBaseModel, ChainTie, Cockatrice(+Chick), DeathWormGauntlet,
  DreadBeast/Ghoul/Knight/Lich/LichSkull/Scuttler/Thrall, Ghost, Gorgon(+Head/HeadActive),
  Hippocampus, HydraBody/Head, IFChainBuffer, PixieHouse, StonePlayer, StymphalianBird,
  TideTrident, TrollWeapon (PixieModel already state-based).
- Renderers: the above entities' renderers + features (GorgonEyes, HydraHead, GenericGlowing,
  BipedArmor/Multiple), item renderers (DeathwormGauntlet, GorgonHead, MiscItem, TideTridentItem,
  TrollWeapon), armor renderers (Basic/ScaleArmor), misc (Chain, CockatriceBeam, FrozenState),
  block (DreadPortal/Spawner/EggInIce/Jar/Lectern/PixieHouse/Podium).
- Note: `TrollEntity.java`/`TrollType.java` etc. carry **gameplay** (non-model) errors
  (`Explosion` abstract, registry `Optional`) that belong to a gameplay phase, not P6-J.
