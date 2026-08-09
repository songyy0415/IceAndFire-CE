# P6-J AdvancedModelBox Batch Migration — Status Report

## Completed sub-batches (committed)

### A. Pipeline core (earlier)
Uranus `renderPartsToBuffer` + mod `AdvancedEntityRendererBase` (submitCustomGeometry) + Troll template.

### B. Dread biped family (shared-base migration)
- **States**: `BipedRenderState` (isPassenger/mainArm/swingingArm/attackTime/isSneak),
  `AnimatedBipedRenderState` (+IAnimatedEntity), `DreadGhoul/Knight/Lich/ThrallRenderState`.
- **Bases**: `BipedBaseModel<T extends BipedRenderState>` (26.2 state-based; `attackTime`/`isSneak`
  restored from state; obsolete `setModelAttributes`/`copyPropertiesTo` removed), `DreadBaseModel`.
- **Models**: DreadGhoul/Knight/Lich/Thrall `setupAnim(XxxRenderState)`; animation inputs from
  `state.walkAnimationPos/Speed/ageInTicks/yRot/xRot`.
- **Renderers**: `AdvancedEntityRendererBase` + extract/scale/texture(state); `GenericGlowingFeatureRenderer`
  → `RenderLayer` submit via `submitCustomGeometry`+`renderPartsToBuffer`.

### C. Dread beast + scuttler
`DreadBeastRenderState`/`DreadScuttlerRenderState` (IAnimatedEntity + size/variant), models + renderers.

### D. Batch1 (18c4220): Amphithere + Cockatrice (+Chick)
- `AmphithereRenderState` (progress fields + 3×IFChainBuffer refs + onGround), `CockatriceRenderState`
  (beam precompute: start/target pos, gameTime, attackScale).
- Amphithere model+renderer (variant/blink textures); Cockatrice model+renderer with **adult/chick model
  swap** via `AdvancedEntityRendererBase.getModel(state)`; beam drawn via `submitExtra` +
  `submitCustomGeometry`; `CockatriceBeamRenderer` → VertexConsumer-based (`RenderTypes.entityCutout`).

### E. Batch2 (34633f1/fcee9a9/8e5502c): Ghost/Gorgon/StymphalianBird + Hippocampus + Hydra
- **Ghost**: custom translucent RenderType + alpha fade via `getModelTint` + haunted-shopping-list
  billboard via `submitExtra`; `getFlipDegrees()=0`. `GhostModel`/`Dread*Model` residuals fixed
  (armPose moved to state, dead `prepareMobModel` removed).
- **BipedBaseModel**: `ArmedModel<BipedRenderState>` + 26.2 `translateToHand(state, arm, pose)` +
  `renderStatue` via `renderPartsToBuffer` (clears all Dread biped subclass errors).
- **Gorgon** (+`GorgonEyesFeatureRenderer` submit), **StymphalianBird** (flyProgress).
- **Hippocampus**: saddle/bridle/chest/armor 4 layers + rainbow wool tint
  (`DyeColor.getTextureDiffuseColor` replaces removed `Sheep.getColor`).
- **Hydra**: `HydraHeadFeatureRenderer` loops remaining heads, animates shared head models with
  per-head precomputed progress arrays; `HydraRenderState` carries headCount/severedHead/arrays.

### F. Batch3-A-1 (dcdc6c1): TideTrident / ChainTie / DreadLichSkull
- Non-Mob entity renderers → `EntityRenderer<E,S>` + `submit()` (not MobRenderer):
  `submitCustomGeometry` + `renderPartsToBuffer`; TideTrident glint pass via `RenderTypes.entityGlint()`;
  ChainTie renders its `BasicEntityModel` via `parts()` + `BasicModelPart.render()`.
- Models state-ized (`setupAnim(XxxRenderState)`, ctor `super(new ModelPart(List.of(), Map.of()))`).
- Fixed `TideTridentEntity` `ThrownTrident` import → 26.2 `projectile.arrow` package.
- Model-level cleanups: `PixieHouseModel` → `AdvancedEntityModel<EntityRenderState>`; `PixieModel` +
  `IFChainBuffer` partial-tick access → `Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)`.

## Verified error change (javac `:common:compileJava`)
| stage | errors |
|---|---|
| P6-J baseline | 1,489 |
| after Batch1 | 1,462 |
| after Batch2 | 1,334 |
| after Batch3-A-1 | 1,252 |
| **current** | **1,252** |
**0 new error files** across all batches. Animations/models unchanged.

## Migration recipe (repeatable, proven)
1. `XxxRenderState extends LivingEntityRenderState [implements IAnimatedEntity]` — add every field the
   model's `setupAnim`/renderer reads (progress/blink/beam/severedHead...).
2. Model: `extends DragonBaseModel<XxxRenderState>` (or `BipedBaseModel<XxxRenderState>`);
   `setupAnim(XxxRenderState state)` with `limbAngle=state.walkAnimationPos; ... headPitch=state.xRot;`
   — same body.
3. Renderer: `AdvancedEntityRendererBase<XxxEntity, XxxRenderState, XxxModel>` + extract/scale/
   texture(state). Features: `RenderLayer<XxxRenderState, XxxModel>` + `submit(...)`.
4. Non-Mob entities (trident/knot/projectile): `EntityRenderer<E,S>` + `submit()` +
   `submitCustomGeometry` + `renderPartsToBuffer`.
5. Glint: second `submitCustomGeometry` with `RenderTypes.entityGlint()` (vanilla trident pattern).

## Remaining (next batches)
- **Batch3-A rest**: `StonePlayerModel` + `StoneStatueEntityRenderer` (statue: depends on broken
  `IafRenderLayers` registry + removed vanilla model fields young/riding/attackTime; full rewrite).
- **Batch3-B (DynamicItemRenderer)**: TrollWeapon / DeathWormGauntlet / TideTridentItem /
  GorgonHead(+Active)+GorgonHeadRenderer — audit `IafRenderers` + Uranus `DynamicItemRenderer` deletion;
  needs a 26.2 item-pipeline replacement (`ItemStackRenderState`/`ItemModelResolver`).
- **Batch3-C**: block renderers (PixieHouse/Jar/DreadPortal/DreadSpawner/EggInIce/Lectern/Podium —
  `BlockEntityRenderer<T,S>`), armor (BipedArmor/BasicArmor/ScaleArmor — `EquipmentLayer`),
  misc (FrozenState/SirenShader/Chain/CockatriceBeam done), `RenderVariables`, `IafRenderLayers`.
- Known deferred: DreadKnight/Lich/Thrall ItemInHandLayer + DreadThrall armor layer.
- `entity/Dread*Entity.java` + `TideTridentEntity` gameplay errors belong to a gameplay phase, not P6-J.
