# P6-J AdvancedModelBox Batch Migration — Status Report

## Completed sub-batches (committed)

### A. Pipeline core (earlier)
Uranus `renderPartsToBuffer` + mod `AdvancedEntityRendererBase` (submitCustomGeometry) + Troll template.

### B. Dread biped family (shared-base migration)
- **States**: `BipedRenderState` (isPassenger/mainArm/swingingArm/attackTime/isSneak),
  `AnimatedBipedRenderState` (+IAnimatedEntity), `DreadGhoul/Knight/Lich/ThrallRenderState`.
- **Bases**: `BipedBaseModel<T extends BipedRenderState>` (26.2 state-based; `attackTime`/`isSneak`
  restored from state; obsolete `setModelAttributes`/`copyPropertiesTo` removed — 26.2 deleted
  `EntityModel.copyPropertiesTo` and HumanoidModel arm-pose fields live on the render state),
  `DreadBaseModel<T extends AnimatedBipedRenderState>`.
- **Models**: DreadGhoul/Knight/Lich/Thrall `setupAnim(XxxRenderState)`, animation inputs from
  `state.walkAnimationPos/Speed/ageInTicks/yRot/xRot`.
- **Renderers**: `AdvancedEntityRendererBase` + `createRenderState/extractRenderState/
  getTextureLocation(state)/scale(state)`; `GenericGlowingFeatureRenderer` → `RenderLayer` submit
  via `submitCustomGeometry`+`renderPartsToBuffer`.

### C. Dread beast + scuttler
`DreadBeastRenderState`/`DreadScuttlerRenderState` (IAnimatedEntity + size/variant), models +
renderers migrated to the same pattern.

## Verified error change
| metric | baseline | current | delta |
|---|---|---|---|
| javac error lines | 3,186 | 2,978 | **-208** |
| unique error messages | 1,423 | 1,323 | **-100** |
Fixed files (model pipeline): Troll, DreadGhoul/Knight/Lich/Thrall/Beast/Scuttler renderers +
models + base classes. **0 new error files** from these batches.

## Migration recipe (repeatable, proven)
1. `XxxRenderState extends LivingEntityRenderState [implements IAnimatedEntity]` — add every
   field the model's `setupAnim`/renderer reads (size/variant/screamStage/animation fields).
2. Model: `extends DragonBaseModel<XxxRenderState>` (or `BipedBaseModel<XxxRenderState>`);
   `setupAnim(XxxEntity,...)` → `setupAnim(XxxRenderState state)` with
   `limbAngle=state.walkAnimationPos; limbDistance=state.walkAnimationSpeed;
   animationProgress=state.ageInTicks; headYaw=state.yRot; headPitch=state.xRot;` — same body.
3. Renderer: `AdvancedEntityRendererBase<XxxEntity, XxxRenderState, XxxModel>` + extract/scale/
   texture(state). Feature layers: `RenderLayer<XxxRenderState, XxxModel>` + `submit(...)`.

## Remaining (next batches — same recipe)
- Models: Ghost, Amphithere, Cockatrice(+Chick), Gorgon, HydraBody/Head, Hippocampus,
  StymphalianBird, PixieHouse, ChainTie, TideTrident, TrollWeapon, DeathWormGauntlet, StonePlayer,
  DreadLichSkull, GorgonHead(+Active), IFChainBuffer, PixieModel(cleanup).
- Renderers: their entity renderers + features (BipedArmor/Multiple, GorgonEyes, HydraHead) +
  item (DeathwormGauntlet/GorgonHead/MiscItem/TideTrident/TrollWeapon) + armor (Basic/Scale) +
  misc (Chain/CockatriceBeam/FrozenState) + block renderers.
- Known deferred: DreadKnight/Lich/Thrall ItemInHandLayer + DreadThrall armor layer (26.2 requires
  `ArmedEntityRenderState`; armor feature migration in a later sub-batch).
- Note: `entity/Dread*Entity.java` carry **gameplay** errors (NearestAttackableTargetGoal/
  Predicate→Selector/horse package/Explosion abstract) belonging to a gameplay phase, not P6-J.
