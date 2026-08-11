# Minecraft API Migration: 1.21.1 mojmap -> 26.2 (IceAndFire-CE)

Scope: every Minecraft API used by `common/` and `fabric/` that changes between
`D:/aiminecraftdev/minecraft1.21.1mojmap` and `D:/aiminecraftdev/minecraft26.2`.
All claims verified against the two reference trees on disk. Line numbers are
from those trees / the mod source at time of writing.

Conventions:
- `[STABLE]` = verified present and identical in both trees.
- `[MOVED]` = same API, different package/class.
- `[RENAMED]` = different name.
- `[SIGNATURE]` = same class/method, changed signature.
- `[REMOVED]` = gone in 26.2; replacement listed.
- `[待确认]` = could not be fully verified; must be confirmed.

The single most important structural fact: 26.2 replaces the **immediate-mode
render pipeline** (RenderSystem.global-state + GuiGraphics + EntityRenderer.render)
with a **render-state extraction model** (`EntityRenderState`, `GuiRenderState`,
`extractRenderState` + `submit`, `SubmitNodeCollector`). Most client-side breakage
in IceAndFire-CE traces back to this one architectural change.

---

## 1. ResourceLocation -> Identifier (renamed, API-identical)

- 1.21.1: `net.minecraft.resources.ResourceLocation`
- 26.2: `net.minecraft.resources.Identifier` (same static factories)
- Verified: `Identifier.withDefaultNamespace(...)`, `Identifier.fromNamespaceAndPath(...)` in 26.2.
- `ResourceLocation.withDefaultNamespace` -> `Identifier.withDefaultNamespace` (same signature)
- `ResourceLocation.fromNamespaceAndPath` -> `Identifier.fromNamespaceAndPath`
- `ResourceLocation.DEFAULT_NAMESPACE` -> `Identifier.DEFAULT_NAMESPACE`
- Mod impact: bulk mechanical rename across all `common/` files (hundreds of imports). No behavior change. `net.minecraft.resources.ResourceLocation` no longer exists.
- 26.2 path verified: `D:/aiminecraftdev/minecraft26.2/net/minecraft/resources/Identifier.java`

---

## 2. Registry / Registries core (mostly stable)

| member | 1.21.1 | 26.2 | verdict |
|---|---|---|---|
| `Registries` class | `net.minecraft.core.registries.Registries` | same path | [STABLE] |
| `Registry.wrapAsHolder(T)` | `net.minecraft.core.Registry` line 148 | `net.minecraft.core.Registry` line 137 | [STABLE] |
| `Registry` package | `net.minecraft.core.Registry` | `net.minecraft.core.Registry` | [STABLE] |
| `BuiltInRegistries` | `net.minecraft.core.registries.BuiltInRegistries` | same | [STABLE] |
| `ResourceKey.create(RegistryKey, ResourceLocation)` | present | present | [STABLE] |

Mod usages verified:
- `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/registry/IafTrades.java:36`
  `PoiTypes.TYPE_BY_STATE.put(state, BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(SCRIBE_POI.get()))` — `PoiTypes.TYPE_BY_STATE` still exists in 26.2
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/world/entity/ai/village/poi/PoiTypes.java:59` private static Map, still referenced from the mod via access widener).
- `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/entity/DragonBaseEntity.java:1276`
  `this.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ...))` — `ReloadableServerRegistries.Holder.getLootTable` exists in 26.2. [STABLE]

---

## 3. DataComponents / ItemStack (stable)

| member | verdict |
|---|---|
| `ItemStack.set(DataComponentType, T)` | [STABLE] |
| `ItemStack.get(DataComponentType)` | [STABLE] |
| `ItemStack.getOrDefault(DataComponentType, T)` | [STABLE] |
| `ItemStack.has(DataComponentType)` | [STABLE] |
| `ItemStack.is(TagKey)` | [STABLE] |
| `ItemStack.copy()` / `isEmpty()` / `getItem()` | [STABLE] |
| `DataComponents.FOOD` / `ENCHANTMENTS` | [STABLE] |
| `ItemStack.getUseDuration(LivingEntity)` / `getUseItemRemainingTicks()` | [STABLE] (both verified in 26.2 LivingEntity/ItemStack) |
| `MobEffectInstance(Holder<MobEffect>, int, ...)` | [STABLE] (26.2 ctor at `.../MobEffectInstance.java:52`) |
| `MobEffectInstance.getEffect()` -> `Holder<MobEffect>` | [STABLE] (26.2 line 199) |

Mod examples: `TideTridentItemRenderer.java` uses `stack.set(DataComponents.ENCHANTMENTS, ...)`,
`HippogryphEntity.java:317` uses `itemstack.has(DataComponents.FOOD)`.

---

## 4. Entity / Mob / LivingEntity members (mostly stable, 2 breaks)

### Stable
| member | verdict | 26.2 location |
|---|---|---|
| `Mob.goalSelector` / `Mob.targetSelector` | [STABLE] | `.../world/entity/Mob.java:134-135` (still `protected final GoalSelector`) |
| `NodeEvaluator.mob` (field) | [STABLE] | `.../world/level/pathfinder/NodeEvaluator.java:16` `protected Mob mob` |
| `ClientLevel.entityStorage` | [STABLE] | `.../client/multiplayer/ClientLevel.java:147` |
| `ClientLevel.entityStorage.getEntityGetter()` | [STABLE] | `.../ClientLevel.java:1069` |
| `ClientLevel.entitiesForRendering()` | [STABLE] | `.../ClientLevel.java:444` |
| `CombatTracker.getMostSignificantFall()` | [STABLE] | `.../world/damagesource/CombatTracker.java:108` (private, widener) |
| `Camera.getMaxZoom(float)` / `Camera.move(float,float,float)` | [STABLE] | `.../client/Camera.java:294` private / `move` present |
| `LivingEntity.tick()` | [STABLE] | `.../world/entity/LivingEntity.java:2755` |
| `LivingEntity.swing(InteractionHand, boolean)` | [STABLE] | `.../LivingEntity.java:2026` |
| `LivingEntity.refreshDirtyAttributes()` | [STABLE] | `.../LivingEntity.java:1123` |
| `LivingEntity.getItemInHand(InteractionHand)` | [STABLE] | `.../LivingEntity.java:2214` getMainHandItem / getItemInHand present |
| `EntityType.Builder.of(EntityFactory, MobCategory)` | [STABLE] | `.../world/entity/EntityType.java:493` |
| `Entity.spawnAtLocation(ItemLike)` | [STABLE] | `.../world/entity/Entity.java` (used by ChickenMixin) |
| `Chicken.aiStep()` | [STABLE] | `.../world/entity/animal/chicken/Chicken.java:118` — NOTE **package moved** (see §6) |
| `Mob.dropFromLootTable(...)` | [SIGNATURE] | see below |
| `LivingEntity.getBaseExperienceReward()` | [SIGNATURE] | see below |
| `LivingEntity.onEffectRemoved(MobEffectInstance)` | [REMOVED] | see below |

### 4.1 `LivingEntity.getBaseExperienceReward` — SIGNATURE CHANGE
- 1.21.1: `protected int getBaseExperienceReward()` (`.../LivingEntity.java:575`)
- 26.2: `protected int getBaseExperienceReward(ServerLevel level)` (`.../LivingEntity.java:608`)
- Mod overrides it: `TrollEntity.java:241`, `StymphalianBirdEntity.java:119`, `SirenEntity.java:131`, `SeaSerpentEntity.java:166`.
- Mod calls it: `StoneStatueEntity.java:194` `livingEntity.getBaseExperienceReward()`.
- Migration: add `ServerLevel` parameter to all 4 overrides + the caller.
- NOTE: 26.2 signature is **package-private or protected?** — 26.2 line 608 declares `protected int getBaseExperienceReward(final ServerLevel level)`. Verified protected.

### 4.2 `Mob.dropFromLootTable` — SIGNATURE CHANGE
- 1.21.1: `protected void dropFromLootTable(DamageSource, boolean)` (`.../Mob.java:482`)
- 26.2: `protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled)` (`.../Mob.java:412`)
- Mod mixin `MobEntityMixin.java:29` injects at `dropFromLootTable` HEAD with
  `(DamageSource, boolean)` params — must change to `(ServerLevel, DamageSource, boolean)`.

### 4.3 `LivingEntity.onEffectRemoved` — REMOVED
- 1.21.1: `protected void onEffectRemoved(MobEffectInstance)` (`.../LivingEntity.java:1016`)
- 26.2: method gone. Replaced by:
  - `protected void onEffectUpdated(MobEffectInstance, boolean doRefreshAttributes, @Nullable Entity source)` (`.../LivingEntity.java:1091`)
  - `protected void onEffectsRemoved(Collection<MobEffectInstance> effects)` (`.../LivingEntity.java:1105`)
  - `protected void onEffectAdded(MobEffectInstance, @Nullable Entity source)` (`.../LivingEntity.java:1075`)
- Mod mixin `LivingEntityMixin.java:33` targets `onEffectRemoved` INVOKE + injects
  `refreshDirtyAttributes` INVOKE — must be retargeted (likely `onEffectUpdated` or `onEffectsRemoved`).

---

## 5. Projectiles — package reorganization + ThrownTrident fields

- 1.21.1: `net.minecraft.world.entity.projectile.ThrownTrident`, `net.minecraft.world.entity.projectile.AbstractArrow`
- 26.2: **moved** to `net.minecraft.world.entity.projectile.arrow.ThrownTrident` / `...arrow.AbstractArrow` [MOVED]
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/world/entity/projectile/arrow/ThrownTrident.java`, `AbstractArrow.java`)
- Members verified present in 26.2 (unchanged):
  - `ThrownTrident.ID_LOYALTY` (`EntityDataAccessor<Byte>`) — 26.2 line 32
  - `ThrownTrident.ID_FOIL` (`EntityDataAccessor<Boolean>`) — 26.2 line 33
  - `ThrownTrident.dealtDamage` (boolean) — 26.2 line 36
  - `AbstractArrow.pickupItemStack` — 26.2 line 80
  - `AbstractArrow.setPierceLevel(byte)` — 26.2 (present; used at line 551)
- Mod usage: `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/entity/TideTridentEntity.java:30,37,41`
  extends `ThrownTrident`, sets `this.pickupItemStack`, calls `this.setPierceLevel((byte) piercingLevel)`.
  Only the **import package** changes.
- Access widener (`iceandfire.accesswidener` lines 6-10) already lists these; the AW
  class-name strings must be updated to the `arrow/` package for 26.2.

---

## 6. Miscellaneous entity-class moves

- `Chicken`: `net.minecraft.world.entity.animal.Chicken` -> `net.minecraft.world.entity.animal.chicken.Chicken` [MOVED]
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/world/entity/animal/chicken/Chicken.java`)
  ChickenMixin `@Mixin(Chicken.class)` — import update only.
- `Util`: `net.minecraft.Util` -> `net.minecraft.util.Util` [MOVED]
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/util/Util.java`)
  `Util.logAndPauseIfInIde(String)` still exists (line 779).
  ChunkRegionMixin targets `Lnet/minecraft/Util;logAndPauseIfInIde(...)` — mixin target string must be updated to `net/minecraft/util/Util`.
- `WorldGenRegion`: still `net.minecraft.server.level.WorldGenRegion`; `ensureCanWrite(BlockPos)`
  still exists (26.2 line 272), `currentlyGenerating` field still exists (line 75). [STABLE]
- `CameraType`: `net.minecraft.client.CameraType` unchanged. `isFirstPerson()` present. [STABLE]

---

## 7. Particles — MAJOR rework

### 7.1 `ParticleEngine.SpriteParticleRegistration` — REMOVED
- 1.21.1: nested interface `ParticleEngine.SpriteParticleRegistration<T>` (`.../particle/ParticleEngine.java:572`)
- 26.2: gone. Replacement is nested interface `ParticleProvider.Sprite<T extends ParticleOptions>`
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/client/particle/ParticleProvider.java`).
- Mod usage: `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/impl/ParticleProviderHolder.java`
  wraps `ParticleEngine.SpriteParticleRegistration<T>` and `ParticleProvider<T>`.
  `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java:20`
  passes `(t, f) -> ParticleFactoryRegistry.getInstance().register(t, f::create)` — that lambda is a
  `SpriteParticleRegistration`; must be rewritten to the 26.2 `ParticleProvider.Sprite` contract.
- Access widener line 2: `accessible class net/minecraft/client/particle/ParticleEngine$SpriteParticleRegistration`
  — the AW entry is now invalid (class gone).

### 7.2 `ParticleProvider.createParticle` — SIGNATURE CHANGE (added RandomSource)
- 1.21.1: `Particle createParticle(T options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux)`
- 26.2: `@Nullable Particle createParticle(T options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random)`
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/client/particle/ParticleProvider.java:9`)
- Every mod particle factory lambda (`BloodParticle::factory`, `DragonFlameParticle::factory`, etc.
  — all in `IafRenderers.registerParticleRenderers`, `.../registry/IafRenderers.java:98-108`)
  must add the `RandomSource` param.

### 7.3 `TextureSheetParticle` -> `SingleQuadParticle` — RENAMED
- 1.21.1: `net.minecraft.client.particle.TextureSheetParticle`
- 26.2: `net.minecraft.client.particle.SingleQuadParticle`
- Mod particles extending `TextureSheetParticle`: `BloodParticle`, `DragonFlameParticle`,
  `DragonFrostParticle`, `DreadPortalParticle`, `DreadTorchParticle`, `HydraBreathParticle` (all in
  `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/particle/`). Import + ctor changes.

### 7.4 `ParticleRenderType.CUSTOM` — REMOVED
- 1.21.1: `ParticleRenderType CUSTOM` is an anonymous instance (interface).
- 26.2: `ParticleRenderType` is a **record** `(String name, String shorthand)` with constants
  `SINGLE_QUADS`, `ITEM_PICKUP`, `ELDER_GUARDIANS`, `NO_RENDER` — no `CUSTOM`.
- Mod: `GhostAppearanceParticle.getRenderType()` returns `ParticleRenderType.CUSTOM` —
  must be rewritten to a 26.2-valid type (待确认 how a fully-custom quad particle is represented
  in 26.2; likely via `SingleQuadParticle` + `ParticleRenderType` selection).

### 7.5 `SimpleParticleType(boolean)` ctor — STABLE
- 26.2: `protected SimpleParticleType(final boolean overrideLimiter)` (`.../core/particles/SimpleParticleType.java:11`). [STABLE]

---

## 8. Entity rendering pipeline — MAJOR rework (EntityRenderState)

### 8.1 `EntityRenderer` type parameters + render method
- 1.21.1: `public abstract class EntityRenderer<T extends Entity>`; instance method
  `void render(T entity, float yaw, float tickDelta, PoseStack, MultiBufferSource, int light)`.
- 26.2: `public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState>`; the
  instance `render(...)` is replaced by:
  - `public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)` (26.2 line 102)
  - `public void extractRenderState(T entity, S state, float partialTicks)` (26.2 line 161)
- `MultiBufferSource` class is **gone** in 26.2 (no file found). Replaced by `SubmitNodeCollector` + render states.
- `EntityRendererProvider` still exists (`.../entity/EntityRendererProvider.java`), but
  `EntityRendererProvider.Context` fields changed: `getItemInHandRenderer()` replaced by `getItemModelResolver()`
  (26.2 `.../EntityRendererProvider.java`). `ItemInHandRenderer` moved to `net.minecraft.client.renderer.ItemInHandRenderer`
  (1.21.1 was `.../client/renderer/entity/ItemInHandRenderer.java`).
- `LivingEntityRenderer` 26.2: `LivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S>` (line 38).
- `MobRenderer` 26.2: `MobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends LivingEntityRenderer<T, S, M>`.
- `EntityModel` 26.2: `EntityModel<T extends EntityRenderState>` — no longer has
  `setupAnim(entity, limbSwing, ...)` / `renderToBuffer(...)` abstract contract with entity;
  rendering now goes through `Model.submit(...)` / render states. All mod model classes
  (TabulaModel-based dragon models etc.) must be reworked to the state model. [待确认 exact per-model steps]

### 8.2 `PlayerRenderer` — REMOVED (replaced by AvatarRenderer)
- 1.21.1: `net.minecraft.client.renderer.entity.player.PlayerRenderer` (class with `render(...)`).
- 26.2: no `PlayerRenderer` class. Player rendering is `AvatarRenderer`
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/client/renderer/entity/player/AvatarRenderer.java`).
- Mod `PlayerEntityRendererMixin.java:17` `@Mixin(PlayerRenderer.class)` + inject `render(...)` —
  must be retargeted to `AvatarRenderer.submit(...)` / the new pipeline.

### 8.3 `LevelRenderer` (WorldRenderer) — renderLevel REMOVED
- 1.21.1: `LevelRenderer.renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)`.
- 26.2: `LevelRenderer.render(...)` new signature:
  `render(GraphicsResourceAllocator, DeltaTracker, boolean, CameraRenderState, Matrix4fc, GpuBufferSlice, Vector4f, boolean)`
  (`.../client/renderer/LevelRenderer.java:154`). Entity iteration moved to `LevelExtractor`
  (`.../client/renderer/extract/LevelExtractor.java:231` iterates `level.entitiesForRendering()`).
- Mod `WorldRendererMixin.java:33` injects `renderLevel` at `ClientLevel;entitiesForRendering()` —
  target no longer exists; must move to `LevelExtractor` or the new `LevelRenderer.render(...)`.
  `@Shadow RenderBuffers renderBuffers` still valid (26.2 LevelRenderer field at line 103) but the
  `bufferSource()` usage at `WorldRendererMixin.java:48` (`this.renderBuffers.bufferSource()`) —
  `RenderBuffers.bufferSource()` returns `MultiBufferSource.BufferSource` which no longer exists. [REMOVED]
- `ClientEvents.onPostRenderLiving(LivingEntity, float, PoseStack, MultiBufferSource, int)`
  (used by LivingEntityRendererMixin) — `MultiBufferSource` type gone; the whole "render living after
  entity" hook must move to the new submit pipeline.

### 8.4 `ThrownItemRenderer` — STABLE class, ctor changed
- 1.21.1: `net.minecraft.client.renderer.entity.ThrownItemRenderer` ctor `(EntityRendererProvider.Context)`.
- 26.2: still `.../entity/ThrownItemRenderer.java`; ctors:
  `(EntityRendererProvider.Context, float scale, boolean fullBright)` and `(Context)` (lines 19, 26).
  `ThrownItemRenderer::new` usages in `IafRenderers.java:53,62,64` still compile if the renderer's
  type params are updated. [MOVED-signature-待确认] The class is now `ThrownItemRenderer<T extends ThrownItem>` style; constructor ref works.

### 8.5 `BlockEntityRenderer` / `BlockEntityRenderDispatcher` — REWORK
- 1.21.1: `BlockEntityRenderer<T>` interface `render(T, float, PoseStack, MultiBufferSource, int, int)`.
- 26.2: `BlockEntityRenderer<T, S extends BlockEntityRenderState>` with
  `S createRenderState()`, `extractRenderState(...)`, `void submit(S state, PoseStack, SubmitNodeCollector, CameraRenderState)`.
- 1.21.1: `BlockEntityRenderDispatcher.renderItem(BlockEntity, PoseStack, MultiBufferSource, int, int)`.
- 26.2: `renderItem(...)` gone; `submit(S, PoseStack, SubmitNodeCollector, CameraRenderState)` (26.2 `.../blockentity/BlockEntityRenderDispatcher.java:95`).
- Mod block-entity renderers (Jar, Lectern, Podium, EggInIce, DreadPortal, DreadSpawner, PixieHouse —
  `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/render/block/*.java`) all implement
  the old `render(...)` and use `MultiBufferSource` — must be rewritten to createRenderState/extractRenderState/submit.
- `MiscItemRenderer.java:29-33` uses `Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(...)`
  — `renderItem` gone; 待确认 replacement (likely ItemModelResolver/ItemStackRenderState-based).

---

## 9. GUI pipeline — MAJOR rework (GuiGraphicsExtractor)

### 9.1 `GuiGraphics` -> `GuiGraphicsExtractor` (extract model)
- 1.21.1: `net.minecraft.client.gui.GuiGraphics`; `Screen.render(GuiGraphics, int, int, float)`.
- 26.2: class is `net.minecraft.client.gui.GuiGraphicsExtractor`
  (`D:/aiminecraftdev/minecraft26.2/net/minecraft/client/gui/GuiGraphicsExtractor.java`); state object is
  `net.minecraft.client.renderer.state.gui.GuiRenderState`.
- `Screen.render(GuiGraphics,int,int,float)` -> `Screen.extractRenderState(GuiGraphicsExtractor, int, int, float)`
  (26.2 `.../screens/Screen.java:113`).
- Every mod screen that overrides `render(GuiGraphics, int, int, float)` must switch to
  `extractRenderState(GuiGraphicsExtractor, int, int, float)`:
  BestiaryScreen.java:121, DragonForgeScreen.java:42, DragonScreen.java:34, HippocampusScreen.java:29,
  HippogryphScreen.java:29, LecternScreen.java:180, PodiumScreen.java:32.

### 9.2 `GuiGraphics` members — mostly RENAMED/REMOVED
| 1.21.1 `GuiGraphics` | 26.2 `GuiGraphicsExtractor` | verdict |
|---|---|---|
| `drawString(Font, String, int, int, int, boolean)` | `text(Font, String, int, int, int, boolean)` (line 243) | [RENAMED] |
| `blit(ResourceLocation, ...)` 8-10 arg variants | `blit(RenderPipeline, Identifier, ...)` — **first arg is now `RenderPipeline`** | [SIGNATURE] |
| `blitSprite(...)` | `blitSprite(RenderPipeline, Identifier, ...)` | [SIGNATURE] |
| `renderItem(...)` | `item(ItemStack, int, int, int)` (lines 886-894) | [RENAMED] |
| `bufferSource()` / `flush()` | gone | [REMOVED] |
| `pose()` | `pose()` returns `Matrix3x2fStack` (line 133) | [SIGNATURE] — 2D pose matrix, not PoseStack |

- 26.2 blit example signature (line 301):
  `blit(RenderPipeline renderPipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight)`.
- All `TitleScreenRenderManager.java` blits (lines 95-110), all BestiaryScreen blits, all screen `render(...)`
  methods must be rewritten to pass a `RenderPipeline` (e.g. `RenderPipelines.GUI_TEXTURED` or similar — 待确认 exact pipeline constants).

### 9.3 In-game HUD (`Gui`)
- 1.21.1 `Gui`: `renderCameraOverlays(GuiGraphics, DeltaTracker)` (private-ish, line ~200s),
  `renderTextureOverlay(GuiGraphics, ResourceLocation, float)` (line 996),
  `POWDER_SNOW_OUTLINE_LOCATION` field (line 115).
- 26.2 `Gui`: `renderCameraOverlays` / `renderTextureOverlay` **gone** (grep finds nothing);
  `Gui` now has `extractRenderState(DeltaTracker, boolean, boolean)` (line 145). HUD moved to a
  separate `Hud` class (`.../client/gui/Hud.java`).
- Mod `InGameHudMixin.java` (`@Mixin(Gui.class)`) injects `renderCameraOverlays` + `@Shadow renderTextureOverlay`
  and `POWDER_SNOW_OUTLINE_LOCATION` — all three targets removed. Must be fully rewritten (likely against `Hud`).

### 9.4 TitleScreen / logo / panorama
- 1.21.1 `TitleScreen.render(GuiGraphics, int, int, float)`; `LogoRenderer.renderLogo(GuiGraphics, int, float)`;
  `PanoramaRenderer.render(GuiGraphics, int, int, float, float)`; `SplashRenderer.render(GuiGraphics, int, Font, int)`.
- 26.2:
  - `TitleScreen.extractRenderState(GuiGraphicsExtractor, int, int, float)` (line 289)
  - `LogoRenderer.extractRenderState(GuiGraphicsExtractor, int, float)` (used in TitleScreen:309) [SIGNATURE]
  - `PanoramaRenderer` -> **`Panorama`** [RENAMED] (`.../client/renderer/Panorama.java`), `extractRenderState(GuiGraphicsExtractor, int, int)` (line 22)
  - `SplashRenderer` ctor changed `SplashRenderer(String)` -> `SplashRenderer(Component)` (line 22);
    `render(...)` -> `extractRenderState(GuiGraphicsExtractor, int, Font, float)` (line 26)
- Mod mixins that break:
  - `TitleScreenMixin.java:37` injects `TitleScreen.render` at `LogoRenderer.renderLogo(GuiGraphics,IF)` INVOKE
  - `RotatingCubeMapRendererMixin.java:13,18` `@Mixin(PanoramaRenderer.class)` injects `render(GuiGraphics, int,int,float,float)`
  - `TitleScreenRenderManager.getSplash()` returns `new SplashRenderer(String)` — ctor now takes Component (line 56)
  - `TitleScreenRenderManager.renderBackground/drawModName` use `GuiGraphics` + `RenderSystem.setShaderColor` + `blit` — all gone (see §9.5, §10).

### 9.5 `RenderSystem` immediate-mode state — REMOVED
1.21.1 `com.mojang.blaze3d.systems.RenderSystem` had `setShader`, `setShaderColor`, `setShaderTexture`,
`enableBlend`, `disableBlend`, `blendFuncSeparate`, `defaultBlendFunc`, `enableDepthTest`, `disableDepthTest`,
`viewport`. In 26.2 `RenderSystem` moved to `com.mojang.blaze3d.systems.RenderSystem` (still same package!) but
**all of the above are gone** — grep for `setShader|setShaderColor|setShaderTexture|enableBlend|disableBlend|
blendFuncSeparate|defaultBlendFunc|enableDepthTest|disableDepthTest|viewport` in 26.2 returns nothing.
Remaining API is GpuDevice/RenderPass oriented (`getDevice()`, `getModelViewStack()`, `setShaderFog`, `setShaderLights`, etc.).
- Mod usages that break (34 `RenderSystem.` call sites):
  - `TitleScreenRenderManager.java:93-113,119-120` — setShaderColor / enableBlend / disableBlend
  - `LecternScreen.java:95-101,128,130,136` — setShader(GameRenderer::getPositionTexShader), setShaderColor, viewport
  - `HippogryphScreen.java:37-38`, `DragonScreen.java:42` — setShader(GameRenderer::getPositionTexShader), setShaderColor
  - `BestiaryScreen.java:136,146,697-721,808` — enableDepthTest/disableDepthTest/setShaderTexture
  - `IndexPageButton.java:24-25` — enableBlend/enableDepthTest
  - `IafRenderLayers.java:13-18` — enableBlend/blendFuncSeparate/disableBlend/defaultBlendFunc
- `GameRenderer.getPositionTexShader()` / `RenderSystem.setShader(...)` — REMOVED in 26.2 (see §10).

---

## 10. GameRenderer / shaders / post-processing

### 10.1 `GameRenderer.loadEffect` / `currentEffect` / `shutdownEffect` — REMOVED
- 1.21.1: `loadEffect(ResourceLocation)` (private-ish, `.../GameRenderer.java:318`), `currentEffect()` (line 789),
  `shutdownEffect()` (line 291).
- 26.2: all three gone. Replacement: `Minecraft.getInstance().getShaderManager()` returns
  `net.minecraft.client.renderer.ShaderManager` (`Minecraft.java:2561`), with
  `@Nullable PostChain getPostChain(Identifier id, Set<Identifier> allowedTargets)` (`ShaderManager.java:186`).
  Post-chains now load from `post_effect/` JSON converter (`POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect")`).
- Mod: `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/render/SirenShaderRenderHelper.java:29,35,40`
  uses `renderer.currentEffect()`, `renderer.loadEffect(SIREN_SHADER)`, `renderer.shutdownEffect()`.
  SIREN_SHADER path is `shaders/post/siren.json` — 26.2 loads `post_effect/siren.json`; resource must move.
  Full rewrite of this helper required. `net.minecraft.client.renderer.PostChain` class still exists in 26.2.

### 10.2 `ShaderInstance` — REMOVED
- 1.21.1: `net.minecraft.client.renderer.ShaderInstance` (used by GameRendererMixin ctor target and SirenShaderRenderHelper path import).
- 26.2: no `ShaderInstance` class anywhere in the tree. Shaders now managed by
  `ShaderManager` / `ShaderDefines` (`.../client/renderer/ShaderDefines.java`) / `RenderPipeline`.
- Mod: `GameRendererMixin.java:35-41` `@Inject(method="reloadShaders", ...)` targets
  `Lnet/minecraft/client/renderer/ShaderInstance;<init>(ResourceProvider;String;VertexFormat;)V`
  and constructs a `ShaderInstance` for the "rendertype_dread_portal" program. `reloadShaders(ResourceProvider)`
  is gone from 26.2 GameRenderer (26.2 has `render(DeltaTracker, boolean)` at line 393, `renderLevel(DeltaTracker)` at 522 — no reloadShaders).
  The whole custom-shader registration (`RenderVariables.DREAD_PORTAL_PROGRAM`, `IafRenderLayers.DREAD_PORTAL_PROGRAM`)
  must be reworked to the 26.2 ShaderManager/ShaderDefines model. [待确认 exact mechanism]

### 10.3 `GameRenderer.renderLevel` + `Camera.setup` — SIGNATURE / REMOVED
- 1.21.1: `GameRenderer.renderLevel(DeltaTracker)` exists (line 1201); inside it `Camera.setup(BlockGetter, Entity, boolean, boolean, float)` is called.
- 26.2: `GameRenderer.renderLevel(DeltaTracker)` still exists (line 522) but `Camera.setup(...)` is gone
  (26.2 Camera has `setupPerspective`/`setupOrtho` private + render-state flow). Camera operations now happen
  in `CameraRenderState`.
- Mod `GameRendererMixin.java:30` injects `renderLevel` at `Camera.setup(...)` INVOKE AFTER —
  target gone. Must be retargeted to the new camera-extraction call.

---

## 11. Item rendering — `ItemRenderer` REMOVED

- 1.21.1: `net.minecraft.client.renderer.entity.ItemRenderer` with `renderStatic(...)` and `getFoilBufferDirect(...)`.
- 26.2: **no `ItemRenderer` class** (find returns nothing). Replaced by:
  - `net.minecraft.client.renderer.item.ItemModelResolver` (`Minecraft.getItemModelResolver()` at `Minecraft.java:2895`)
  - `net.minecraft.client.renderer.item.ItemStackRenderState` with `submit(PoseStack, SubmitNodeCollector, int light, int overlay, int color)` (line 109)
  - `ItemInHandRenderer.renderItem(LivingEntity, ItemStack, ItemDisplayContext, PoseStack, SubmitNodeCollector, int)` (`.../client/renderer/ItemInHandRenderer.java:130`)
  - GUI item rendering: `GuiGraphicsExtractor.item(ItemStack, int, int, int)`
- Mod usages that break (all `Minecraft.getInstance().getItemRenderer().renderStatic(...)`):
  - `render/block/PodiumBlockEntityRenderer.java:49`
  - `render/entity/feature/DragonBannerFeatureRenderer.java:42`
  - `render/entity/feature/PixieItemFeatureRenderer.java:34`
  - `render/entity/GhostSwordEntityRenderer.java:39`
  - `render/item/TideTridentItemRenderer.java:28` (also uses `ItemRenderer.getFoilBufferDirect(...)` at line 51)
- `ItemDisplayContext` enum unchanged (`NONE/THIRD_PERSON/FIRST_PERSON/GUI/GROUND/FIXED` all present, `FIXED`=8). [STABLE]

---

## 12. Options / KeyMapping / Input

### 12.1 `KeyMapping` ctor — SIGNATURE CHANGE
- 1.21.1: `KeyMapping(String name, int key, String category)` (`.../client/KeyMapping.java:87`)
- 26.2: `KeyMapping(String name, int key, KeyMapping.Category category)` (`.../client/KeyMapping.java:87`);
  `KeyMapping.Category` is a class with constants `MOVEMENT`, `GAMEPLAY`, ... (`.../KeyMapping.java:204,207`).
- Mod: `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/registry/IafKeybindings.java:10-13`
  uses `new KeyMapping("key.dragon_fireAttack", GLFW.GLFW_KEY_R, "key.categories.gameplay")` —
  third arg is a String; must become `KeyMapping.Category.GAMEPLAY`. (Architectury `KeyMappingRegistry.register` unchanged.)

### 12.2 `Options`
- `Options.getCameraType()` / `setCameraType(CameraType)` — [STABLE] (26.2 `Options.java:1921,1925`)
- `Options.keyShift` / `Options.keyJump` — [STABLE] (26.2 lines 659-660, fields are `KeyMapping`)
- `Options.renderDistance()` — [STABLE] (26.2 line 1007)
- `Options.languageCode` — [STABLE] (BestiaryScreen uses it)
- `KeyMapping.isDown()` — [STABLE] (26.2 line 105)
- `InputConstants.isKeyDown(Window, int)` — [STABLE] (26.2 `com/mojang/blaze3d/platform/InputConstants.java:192`)
- `Minecraft.getInstance().getWindow()` — [STABLE] (`Minecraft.java:2804`); `Window` moved to
  `com.mojang.blaze3d.platform.Window` [MOVED] (getGuiScale/getWidth/getHeight present, lines 494-534)

---

## 13. Networking (Architectury — NOT a direct-MC migration concern)

- All mod networking goes through `dev.architectury.networking.NetworkManager` with
  `CustomPacketPayload` records (`ServerNetworkHelper.java`, `ClientNetworkHelper.java`, `.../network/payload/*`).
- `FriendlyByteBuf` still exists in 26.2 (`.../network/FriendlyByteBuf.java`) with `readResourceKey(...)` (line 590).
- Payload C2S/S2C registration is via Architectury — treat as unchanged unless Architectury's 26.2 version
  changes its API. [待确认 Architectury 26.2]

---

## 14. Screen / Menu registration

- `MenuType` — [STABLE] class at `net.minecraft.world.inventory.MenuType`. 1.21.1 ctor was
  `public MenuType(MenuSupplier, FeatureFlagSet)` (line 48); 26.2 ctor is **private**
  `private MenuType(MenuSupplier, FeatureFlagSet)` (line 50) — mod's `IafScreenHandlers.java:24-25`
  `new MenuType<>(... , FeatureFlags.VANILLA_SET)` would not compile; must use a registry/Builder or
  Architectury `MenuRegistry` (Architectury `MenuRegistry.ofExtended` already used for most screens).
- `MenuScreens.register(MenuType, ScreenConstructor)` — 1.21.1 `public static` (line 57);
  26.2 **private static** (line 53) — `IafScreenHandlers.registerGui()` uses
  `MenuScreens.register(...)` for all 7 screens; must switch to Architectury `ScreenRegistry.register(...)`
  or a 26.2-provided public registration path. [待确认 Architectury ScreenRegistry]

---

## 15. Creative tabs — STABLE

- Mod uses `dev.architectury.registry.CreativeTabRegistry.create(...)` + `DeferredRegister<CreativeModeTab>`
  (`IafItemGroups.java`). `CreativeModeTab` still exists in 26.2. [STABLE — but Architectury 26.2 待确认]

---

## 16. Tags — STABLE

- `EntityTypeTags` (`net.minecraft.tags.EntityTypeTags`) still exists; `UNDEAD` present (`26.2 .../tags/EntityTypeTags.java:11`).
  Used by `BuiltinAbilities.java:11`.
- `ItemTags` still exists; `MEAT` present (`26.2 .../tags/ItemTags.java:89`). Used by `HippogryphEntity.java:317`.
- `BlockTags.SAND` present. `TagKey` class present. `Ingredient.of(...)` present (26.2 `.../crafting/Ingredient.java:81`).
- Mod's own tags in `com.iafenvoy.iceandfire.registry.tag.*` are internal. [STABLE]

---

## 17. Other verified stable APIs

- `Component` / `ChatFormatting` — [STABLE]
- `net.minecraft.network.chat.Component.translatable` — [STABLE]
- `ExperienceOrb.award(ServerLevel, Vec3, int)` — [STABLE]
- `MoverType` — [STABLE]
- `TemptGoal` class — [STABLE] (26.2 present)
- `PoseStack`, `DeltaTracker`, `VertexConsumer`, `OverlayTexture` — [STABLE] (OverlayTexture at `.../renderer/texture/OverlayTexture.java`)
- `Lighting.setupFor3DItems()` — 1.21.1 `com.mojang.blaze3d.platform.Lighting` static (line 30);
  26.2 `Lighting` class exists (`com/mojang/blaze3d/platform/Lighting.java`) but is now an instance
  `AutoCloseable` with no `setupFor3DItems`/`setupForFlatItems` statics — LecternScreen.java:132 `Lighting.setupFor3DItems()` breaks. [SIGNATURE/REMOVED]
- `Minecraft.getInstance().level` (ClientLevel), `.player`, `.options`, `.gameRenderer`, `.font` — [STABLE]
- `Minecraft.getInstance().getResourceManager()` — [STABLE] (26.2 line 2565)
- `Entity.getTicksFrozen()` / `getTicksRequiredToFreeze()` — [STABLE] but moved from `Player`/`LocalPlayer`
  to `Entity` (26.2 `.../world/entity/Entity.java:2821,2838`). InGameHudMixin targets
  `LocalPlayer;getTicksFrozen()I` INVOKE — still resolves (inherited), but target string references LocalPlayer; fine.
- `EntityRenderDispatcher.renderers` field (access widener) — still exists in 26.2 but type is now
  `Map<EntityType<?>, EntityRenderer<?, ?>>` (two type params) vs 1.21.1 `Map<EntityType<?>, EntityRenderer<?>>`.
  Widener entry must be updated (type is generic-erased, so AW entry text may still match; 待确认).
  `EntityRenderDispatcher.getRenderer(T)` still exists (line 91). **`EntityRenderDispatcher.render(...)` instance
  method is gone** — DragonRiderFeatureRenderer.java:54,116 and DreadSpawnerBlockEntityRenderer.java:34 and
  StoneStatueEntityRenderer.java:74 call `getEntityRenderDispatcher().render(...)` / `.renderers.get(...)` — the
  `render(...)` calls break (see §8.1 state model). 待确认 exact replacement for "render an entity now".

---

## 18. Mixin target inventory (what breaks, by file)

| mod file | mixin target (1.21.1) | 26.2 status |
|---|---|---|
| `mixin/GameRendererMixin.java:30` | `GameRenderer.renderLevel` @ `Camera.setup(...)` INVOKE | `Camera.setup` gone; renderLevel still exists but camera extraction changed |
| `mixin/GameRendererMixin.java:35` | `GameRenderer.reloadShaders` @ `ShaderInstance.<init>(ResourceProvider,String,VertexFormat)` | `reloadShaders` and `ShaderInstance` both gone |
| `mixin/WorldRendererMixin.java:33` | `LevelRenderer.renderLevel` @ `ClientLevel.entitiesForRendering()` | `renderLevel` gone -> new `render(...)`; entity iteration in LevelExtractor |
| `mixin/WorldRendererMixin.java:29,48` | `@Shadow RenderBuffers renderBuffers` + `renderBuffers.bufferSource()` | `RenderBuffers` field exists but `bufferSource()` type (MultiBufferSource) gone |
| `mixin/InGameHudMixin.java:22,26,32` | `Gui.renderCameraOverlays` + `renderTextureOverlay` + `POWDER_SNOW_OUTLINE_LOCATION` | all removed; Gui now extractRenderState; HUD in `Hud` |
| `mixin/LivingEntityRendererMixin.java:15` | `LivingEntityRenderer.render(LivingEntity,FF,PoseStack,MultiBufferSource,I)` | render signature gone; now extractRenderState/submit |
| `mixin/PlayerEntityRendererMixin.java:19` | `PlayerRenderer.render(AbstractClientPlayer,FF,PoseStack,MultiBufferSource,I)` | `PlayerRenderer` gone -> `AvatarRenderer` |
| `mixin/TitleScreenMixin.java:37` | `TitleScreen.render` @ `LogoRenderer.renderLogo(GuiGraphics,IF)` | render->extractRenderState; renderLogo signature changed |
| `mixin/RotatingCubeMapRendererMixin.java:13,18` | `@Mixin(PanoramaRenderer.class)` render(GuiGraphics,int,int,float,float) | `PanoramaRenderer`->`Panorama`; render->extractRenderState |
| `mixin/ChickenMixin.java:20` | `Chicken.aiStep` @ `Chicken.spawnAtLocation(ItemLike)` | class moved to `animal/chicken`; aiStep+spawnAtLocation still exist |
| `mixin/ChunkRegionMixin.java:22` | `WorldGenRegion.ensureCanWrite` @ `Util.logAndPauseIfInIde(String)` | method+target exist; `Util` moved to `net.minecraft.util.Util` |
| `mixin/LivingEntityMixin.java:27,33` | `LivingEntity.swing(InteractionHand,Z)` + `onEffectRemoved` @ `refreshDirtyAttributes` | swing OK; `onEffectRemoved` gone -> onEffectUpdated/onEffectsRemoved |
| `mixin/MobEntityMixin.java:29` | `Mob.dropFromLootTable` HEAD | signature now `(ServerLevel, DamageSource, boolean)` |

---

## 19. Access widener updates needed (`common/src/main/resources/iceandfire.accesswidener`)

| line | current entry | 26.2 action |
|---|---|---|
| 2 | `ParticleEngine$SpriteParticleRegistration` | class gone -> remove or retarget to `ParticleProvider$Sprite` |
| 3 | `EntityRenderDispatcher renderers` | field type now `Map<EntityType<?>, EntityRenderer<?,?>>` (generic-erased); verify |
| 17 | `GameRenderer loadEffect` | gone -> remove |
| 6-10 | `ThrownTrident/AbstractArrow` class names | package moved to `projectile/arrow/` -> update class strings |
| 13 | `LivingEntity getBaseExperienceReward ()I` | signature now `(Lnet/minecraft/server/level/ServerLevel;)I` -> update |
| 2-21 rest | `Mob goalSelector/targetSelector`, `NodeEvaluator.mob`, `ClientLevel.entityStorage`, `CombatTracker.getMostSignificantFall`, `Camera getMaxZoom/move`, `Screen renderables`, `PoiTypes TYPE_BY_STATE`, `SimpleParticleType <init>(Z)` | all still present in 26.2; keep |

---

## 20. Summary of the biggest migrations (ordered by blast radius)

1. **Render-state pipeline** (EntityRenderer / LivingEntityRenderer / BlockEntityRenderer / EntityModel /
   PlayerRenderer / LevelRenderer.renderLevel / EntityRenderDispatcher.render): touches ~40+ mod files in
   `render/`, `mixin/`, and all screens. This is the architectural keystone.
2. **GuiGraphics -> GuiGraphicsExtractor + RenderPipeline blit + Screen.extractRenderState**: all 7 screens
   plus TitleScreenRenderManager, mixins. `RenderSystem` immediate-mode calls (setShaderColor, blend, depth,
   viewport) are gone.
3. **RenderSystem.setShader / ShaderInstance / GameRenderer.getPositionTexShader / loadEffect/currentEffect /
   shutdownEffect**: SirenShaderRenderHelper, Lectern/Hippogryph/Dragon screens, GameRendererMixin,
   IafRenderLayers (custom RenderType + CompositeState builders).
4. **Particles**: SpriteParticleRegistration -> ParticleProvider.Sprite, createParticle +RandomSource,
   TextureSheetParticle -> SingleQuadParticle, ParticleRenderType.CUSTOM gone.
5. **ItemRenderer -> ItemModelResolver / ItemStackRenderState**: 5 files use `getItemRenderer().renderStatic`.
6. **MultiBufferSource / RenderBuffers.bufferSource()**: type removed from the tree; replaced by
   SubmitNodeCollector + render states.
7. **KeyMapping category String -> KeyMapping.Category**: IafKeybindings.
8. **MenuType private ctor + MenuScreens.register private**: IafScreenHandlers (use Architectury registries).
9. **LivingEntity.getBaseExperienceReward(ServerLevel)**, **Mob.dropFromLootTable(ServerLevel, ...)**,
   **LivingEntity.onEffectRemoved** -> onEffectUpdated/onEffectsRemoved.
10. **Entity/Util/Chicken package moves** (imports + mixin target strings).
11. **ResourceLocation -> Identifier** (mechanical rename).

Surprises worth flagging:
- `MultiBufferSource` and `ItemRenderer` are **gone entirely** in 26.2, not just moved.
- `RenderSystem` still exists at the same package but lost all immediate-mode state APIs.
- `ParticleRenderType` changed from interface to record, removing `CUSTOM`.
- `Lighting.setupFor3DItems()` static is gone (now an instance AutoCloseable).
- `SplashRenderer(String)` -> `SplashRenderer(Component)`.
- `Mob.dropFromLootTable` gained a `ServerLevel` first parameter — many mods miss this one.
