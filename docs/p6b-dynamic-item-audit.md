# P6-B DynamicItemRenderer Replacement — Audit

Status: **Step 1 — API audit only, no code written yet.**

## 1. What the old DynamicItemRenderer did

`com.iafenvoy.uranus.client.render.DynamicItemRenderer` (deleted in Uranus 26.2) was a client-side
map `RENDERERS: Map<Item, DynamicItemRenderer>` that completely replaced the vanilla item render for
registered items. Its single method:

```java
void render(ItemStack stack, ItemDisplayContext mode, PoseStack pose, MultiBufferSource buffer, int light, int overlay);
```

Responsibilities: receive the ItemStack, pick a model/pose, and submit arbitrary geometry via
`MultiBufferSource.getBuffer(RenderType)` + `model.renderToBuffer(...)`.

## 2. The 5 mod item renderers being replaced

| Renderer | Model | Reads from stack | Custom behaviour |
|---|---|---|---|
| `TideTridentItemRenderer` | `TideTridentModel` (AdvancedModelBox, rigid) | `stack.hasFoil()`, enchantments | `ItemRenderer.getFoilBufferDirect` + `RenderType.entityCutoutNoCull(TRIDENT)` |
| `TrollWeaponRenderer` | `TrollWeaponModel` (AdvancedModelBox) | `TrollWeaponItem.weapon` (variant → texture) | per-variant texture |
| `DeathwormGauntletRenderer` | `DeathWormGauntletModel` (AdvancedModelBox) | item variant (`RED/WHITE/YELLOW`) | `MODEL.animate(stack, partialTick)` item animation |
| `GorgonHeadRenderer` | `GorgonHeadActiveModel`/`GorgonHeadModel` | `stack.has(ACTIVE)` data component | active/inactive model swap |
| `MiscItemRenderer` | block entity renderers (chest/portal/pixie house) | BlockItem type | delegates to `BlockEntityRenderDispatcher.renderItem(...)` |

## 3. MC26.2 replacement pipeline

26.2 item rendering is **data-driven** and fully state-based. Key types
(`net.minecraft.client.renderer.*`):

- `ItemModel` (interface): `update(ItemStackRenderState, ItemStack, ItemModelResolver, ItemDisplayContext, ClientLevel, ItemOwner, int)`.
- `ItemStackRenderState` (final-ish vanilla class): holds `LayerRenderState[]`, `submit(PoseStack, SubmitNodeCollector, int light, int overlay, int color)`.
- `LayerRenderState.setupSpecialModel(SpecialModelRenderer<T>, T argument)` — the custom-geometry hook.
- `SpecialModelRenderer<T>` (interface): `submit(T arg, PoseStack, SubmitNodeCollector, int light, int overlay, boolean outline, int color)`, `getExtents(Consumer<Vector3fc>)`, `extractArgument(ItemStack)`.
- `NoDataSpecialModelRenderer` extends `SpecialModelRenderer<Void>` — convenience for stack-data-free renderers.
- `SpecialModelRenderer$Unbaked<T>`: `bake(BakingContext)`, `type() → MapCodec<? extends Unbaked<T>>`.
- `SpecialModelWrapper<T>` (implements `ItemModel`): wraps a baked `SpecialModelRenderer` + transform; its `update` calls `state.newLayer().setupSpecialModel(renderer, extractArgument(stack))`.
- `SpecialModelRenderers.bootstrap()`: `ID_MAPPER.put(Identifier, MapCodec)` per special type (`ExtraCodecs$LateBoundIdMapper`).
- Model JSON (new `assets/<ns>/items/<item>.json` format):
  ```json
  { "model": { "type": "minecraft:special", "base": "minecraft:item/decorated_pot",
               "model": { "type": "minecraft:decorated_pot" } } }
  ```

**Chain**: JSON `type` → `ID_MAPPER` codec → `Unbaked.bake()` → `SpecialModelRenderer` →
`SpecialModelWrapper.update()` → `ItemStackRenderState.submit()` → `specialRenderer.submit(...)`
with a `SubmitNodeCollector` (so `submitCustomGeometry` + Uranus `renderPartsToBuffer` are reachable).

## 4. Mapping old → 26.2

| Old (1.21.1 / Uranus) | 26.2 |
|---|---|
| `DynamicItemRenderer.RENDERERS.put(item, r)` | JSON `items/<item>.json` `"minecraft:special"` + codec registration |
| `render(stack, mode, pose, buffer, light, overlay)` | `SpecialModelRenderer.submit(arg, pose, collector, light, overlay, outline, color)` |
| `MultiBufferSource.getBuffer(renderType)` | `SubmitNodeCollector.submitCustomGeometry(pose, renderType, (pose, buffer) -> …)` |
| `model.renderToBuffer(...)` (AdvancedModelBox) | `model.renderPartsToBuffer(...)` (Uranus) |
| `ItemRenderer.getFoilBuffer/Direct` | second pass with `RenderTypes.entityGlint()` (see TideTrident entity renderer) |
| `ItemDisplayContext mode` | passed into `update(...)`; store on `ItemStackRenderState.displayContext` |
| stack-driven pose/variant | `extractArgument(stack)` → T (variant/active/weapon enum) |

## 5. Design: `AdvancedItemRendererBase`

Mod-side base mirroring `AdvancedEntityRendererBase`, **not touching Uranus**:

```java
public abstract class AdvancedItemRendererBase<T> implements SpecialModelRenderer<T> {
    // shared helpers
    protected abstract T extractArgument(ItemStack stack);
    protected abstract void updateArgument(T arg, ItemStack stack, ItemDisplayContext ctx); // optional pre-bake
    @Override public abstract void submit(T arg, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean outline, int color);
    @Override public void getExtents(Consumer<Vector3fc> extents) {} // no culling hints → conservative
    // common submit path:
    protected void submitModel(PoseStack pose, SubmitNodeCollector collector, RenderType type, int light, int overlay, AdvancedEntityModel<?> model) {
        collector.submitCustomGeometry(pose, type, (p, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(p.pose());
            fresh.last().normal().set(p.normal());
            model.renderPartsToBuffer(fresh, buffer, light, overlay, -1);
        });
    }
}
```

Per-item `Unbaked` subclasses expose `static final MapCodec<…Unbaked> MAP_CODEC = MapCodec.unit(new …Unbaked())`
and `bake(BakingContext) → new AdvancedItemRendererBase<?>(…)`. Registration is a **mixin into
`SpecialModelRenderers.bootstrap()`** (or `@Redirect` on `ID_MAPPER.put`) adding
`ID_MAPPER.put(IceAndFire.id("troll_weapon"), TrollWeaponRenderer.Unbaked.MAP_CODEC)` for each type.
Items that render a fixed model with no stack data implement `NoDataSpecialModelRenderer` (T extends Void).

## 6. Per-item migration notes

- **TideTridentItem**: rigid AdvancedModelBox → `NoDataSpecialModelRenderer`; base texture
  `RenderTypes.entitySolid(TideTridentEntityRenderer.TRIDENT)`; foil via a second
  `submitCustomGeometry` with `RenderTypes.entityGlint()` (as done in the entity renderer). Drop
  `ItemRenderer.getFoilBufferDirect` + `DataComponents.ENCHANTMENTS` copy.
- **TrollWeapon**: T = weapon enum; `extractArgument` reads `TrollWeaponItem.weapon`; submit uses
  `weapon.getTextureLocation()` with `RenderTypes.entityCutout`.
- **DeathwormGauntlet**: T = variant enum; keep `MODEL.animate(stack, partialTick)` but replace
  `Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true)` with
  `getDeltaTracker().getGameTimeDeltaPartialTick(true)`; submit via `renderPartsToBuffer`.
- **GorgonHead**: T = boolean active; `extractArgument` reads `stack.has(IafDataComponents.ACTIVE)`;
  swap `GorgonHeadActiveModel`/`GorgonHeadModel` in submit (both already AdvancedEntityModel-based).
- **MiscItem**: delegates to block-entity rendering (chest/portal/pixie house). **Deferred to
  Batch3-C.** 26.2 `BlockEntityRenderDispatcher` exposes `getRenderer(E/S)` + `tryExtractRenderState(...)`
  + `submit(S, PoseStack, SubmitNodeCollector, CameraRenderState)` — the submit path **requires a
  `CameraRenderState`**, which `SpecialModelRenderer.submit(...)` does not receive, and the
  `PixieHouseBlockEntityRenderer` is not yet migrated to `BlockEntityRenderer<T,S>`. So MiscItem
  cannot be converted to a SpecialModelRenderer until Batch3-C lands; the old `MiscItemRenderer` and
  its `DynamicItemRenderer.RENDERERS.put(...)` rows stay (erroring) until then.

## 7. Risks / behaviour deltas

- 26.2 has **no MultiBufferSource**; every path goes through `submitCustomGeometry`.
- `ItemRenderer.getFoilBuffer*` is gone; glint = separate `entityGlint()` submit.
- The `getTimer()` partial-tick call in DeathwormGauntlet must move to `getDeltaTracker()`.
- Special-model registration requires a mixin into `SpecialModelRenderers.bootstrap` (private
  `ID_MAPPER`). Alternatively register via the same `LateBoundIdMapper.put` from a client-init hook if
  the field is accessible; otherwise mixin.
- `getExtents` returning nothing may disable some item-entity culling; acceptable for these large tools.
- MiscItem's block-entity delegation is blocked on the block-renderer migration.

## 8. Deliverables (next step — Step 2, not yet done)
1. `AdvancedItemRendererBase<T>` (new file).
2. 5 `Unbaked` + renderer classes.
3. Registration mixin (or init hook) + `items/*.json` model files.
4. Compile + per-commit verification.
