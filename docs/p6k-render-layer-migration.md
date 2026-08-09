# P6-K IafRenderLayers Migration Report

## Summary
`IafRenderLayers.java` migrated from the removed 1.21.1 `RenderType` extension model to the
MC26.2 `net.minecraft.client.renderer.rendertype.RenderTypes` factory set. Caller-facing API
(method names + signatures returning `RenderType`) is unchanged.

## Why not keep the custom pipelines
MC26.2 `RenderType` is `final` and its `create(String, RenderSetup)` entry point is
**package-private** (`net.minecraft.client.renderer.rendertype`). A mod cannot turn a custom
`RenderPipeline` (`RenderPipeline.builder(Snippet...)` is public, `RenderSetup.builder(RenderPipeline)`
is public) into a `RenderType` — the final step is unreachable. So every Iaf layer falls back to the
closest stock `RenderTypes` type.

## 1. Old → New mapping table
| Old Iaf layer | Old 1.21.1 pipeline (shader/blend/depth) | New MC26.2 |
|---|---|---|
| `getGhost(tex)` | ENTITY_CUTOUT_NO_CULL shader + ghost blend (ONE, 1-SRC_ALPHA) + NO_CULL + lightmap + overlay | `RenderTypes.entityCutout(tex)` |
| `getGhostDaytime(tex)` | ENTITY_CUTOUT_NO_CULL shader + TRANSLUCENT blend + NO_CULL | `RenderTypes.entityTranslucent(tex)` |
| `getDreadlandsPortal()` | custom shader `RenderVariables.DREAD_PORTAL_PROGRAM` + 2 textures (POSITION_COLOR) | `RenderTypes.entityCutout(DreadPortalBlockEntityRenderer.DREAD_PORTAL)` |
| `getStoneMobRenderType(x,y)` | ENTITY_CUTOUT shader + stone tex + NO_TRANSPARENCY | `RenderTypes.entitySolid(STONE_TEXTURE)` |
| `getIce(tex)` | BEACON_BEAM shader + TRANSLUCENT + CULL + lightmap + overlay | `RenderTypes.beaconBeam(tex, false)` |
| `getStoneCrackRenderType(tex)` | ENTITY_CUTOUT shader + TRANSLUCENT + **EQUAL_DEPTH_TEST** + NO_CULL | `RenderTypes.entityTranslucent(tex)` |

## 2. New pipeline mapping
- `entityCutout` → `RenderPipelines.ENTITY_CUTOUT` (no cull, alpha-test)
- `entityTranslucent` → `RenderPipelines.ENTITY_TRANSLUCENT` (no cull, SRC_ALPHA blend)
- `entitySolid` → `RenderPipelines.ENTITY_SOLID`
- `beaconBeam` → `RenderPipelines.BEACON_BEAM_TRANSLUCENT`

## 3. Modified files
- `common/src/main/java/com/iafenvoy/iceandfire/registry/IafRenderLayers.java` (rewritten; drops
  `extends RenderType`, shard constants, `RenderVariables.DREAD_PORTAL_PROGRAM`, `create(...)` usage)

## 4. Impact scope (callers)
- `particle/GhostAppearanceParticle` — `getGhost(...)` — unchanged errors (its own old-API errors remain)
- `render/block/DreadPortalBlockEntityRenderer` — `getDreadlandsPortal()` — unchanged
- `render/entity/StoneStatueEntityRenderer` — `getStoneMobRenderType`, `getStoneCrackRenderType` — unchanged
- `render/misc/FrozenStateRenderer` — `getIce(...)` — unchanged
All four callers keep the same error counts (they carry their own pre-existing old-API errors);
`getGhostDaytime` has no remaining callers (Ghost renderer already migrated to `RenderTypes`).

## 5. Error change
| metric | before | after |
|---|---|---|
| javac (`:common:compileJava`) | 1,246 | **1,171** (-75) |
| `IafRenderLayers.java` errors | ~150 | **0** |
| new error files | — | **0** |

## Known behavioral deltas (documented, not silent)
- Ghost blend / portal custom shader / stone-crack `EQUAL_DEPTH_TEST` cannot be expressed as a mod
  `RenderType` in 26.2; approximated by stock pipelines. The block-renderer migration (Batch3-C) will
  re-evaluate the dread portal with 26.2 block-pipeline APIs.
