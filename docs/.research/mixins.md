# Mixin Analysis & Validation Report (MC 1.21.1 -> MC 26.2)

Research agent output. Project currently targets **MC 1.21.1** (`gradle.properties: minecraft_version=1.21.1`); migration target is **MC 26.2**.
All targets verified against `D:/aiminecraftdev/minecraft1.21.1mojmap` (1.21.1) and `D:/aiminecraftdev/minecraft26.2` (26.2) decompiled sources.

Config file: `D:/IceAndFire-CE/common/src/main/resources/iceandfire.mixins.json`
- `required: true`, `minVersion 0.8`, `compatibilityLevel JAVA_21`, `defaultRequire 1`
- 4 common mixins (Chicken, ChunkRegion, LivingEntity, Mob) + 7 client mixins (GameRenderer, InGameHud, LivingEntityRenderer, PlayerEntityRenderer, RotatingCubeMapRenderer, TitleScreen, WorldRenderer).
- Uses **MixinExtras** (`com.llamalad7.mixinextras.sugar.Local`) in GameRendererMixin, TitleScreenMixin, WorldRendererMixin.

**HEADLINE:** All 11 mixins are valid against 1.21.1. Against 26.2, **7 are broken** (5 fail to apply, 2 need signature/class retargeting + logic rework), 2 apply with minor retargets, 2 apply unchanged. The single biggest problem is the 26.2 render-state/GPU rework (ShaderInstance, PlayerRenderer, PanoramaRenderer, Gui, LivingEntityRenderer, LevelRenderer all replaced).

---

## 1) ChickenMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/ChickenMixin.java`
- Type: classic (`@Mixin(Chicken.class)`, `abstract class ... extends Entity`)
- `@ModifyArg(method = "aiStep", at = @At(value="INVOKE", target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))`
  - Handler: `private ItemLike layRottenEgg(ItemLike egg)` — swaps egg for `IafItems.ROTTEN_EGG` based on config.

### 1.21.1 — VALID
- `Chicken.java` at `net/minecraft/world/entity/animal/Chicken.java`; `aiStep()` line 76; egg laid at line 95 via `this.spawnAtLocation(Items.EGG);`.
- `Entity.spawnAtLocation(ItemLike)` -> `ItemEntity` exists at `net/minecraft/world/entity/Entity.java:1836`. (Note: declared on `Entity`, not `Chicken` — Mixin's inherited-member INVOKE resolution handles the `Chicken` owner.)

### 26.2 — BROKEN (class moved + egg-laying path rewritten)
- Class **moved package**: `net/minecraft/world/entity/animal/Chicken.java` -> `net/minecraft/world/entity/animal/chicken/Chicken.java`.
- `aiStep()` still exists (line 118) but egg-laying no longer calls `spawnAtLocation(ItemLike)`. New logic (line 135-142):
  `if (this.dropFromGiftLootTable(level, BuiltInLootTables.CHICKEN_LAY, this::spawnAtLocation)) { ... }`
  Egg comes from the loot table `CHICKEN_LAY`, spawned through the `dropFromGiftLootTable` (Mob) path.
- `Entity.spawnAtLocation(ItemLike)` single-arg **removed**; new signature requires a level:
  `@Nullable ItemEntity spawnAtLocation(ServerLevel level, ItemLike resource)` (`Entity.java:2227`) and `spawnAtLocation(ServerLevel, ItemStack, ...)`.

**Proposed fix (待确认 - design decision needed):**
1. `@Mixin(net.minecraft.world.entity.animal.chicken.Chicken.class)`.
2. The `@ModifyArg` target `spawnAtLocation(ItemLike)` no longer exists inside aiStep. Options:
   - a) `@ModifyArg` the **loot-table id** argument of `Mob.dropFromGiftLootTable` -> swap `BuiltInLootTables.CHICKEN_LAY` for a modded loot table that rolls rotten eggs (replaces `BuiltInLootTables`... at `@At(value="INVOKE", target="Lnet/minecraft/world/entity/Mob;dropFromGiftLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/BiConsumer;)Z")`).
   - b) `@Inject` at HEAD of `aiStep` after the egg check and do the rotten-egg logic manually.
   - c) `@ModifyArg` on the `BiConsumer` method ref arg (not straightforward).
   Recommended: (a) with a datapack loot table; keeps RNG inside the loot table.

---

## 2) ChunkRegionMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/ChunkRegionMixin.java`
- Type: classic (`@Mixin(WorldGenRegion.class)`)
- `@Shadow private Supplier<String> currentlyGenerating;`
- `@Inject(method = "ensureCanWrite", at = @At(value="INVOKE", target = "Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V"), cancellable = true)` — suppresses log spam for dragon caves.

### 1.21.1 — VALID
- `WorldGenRegion.java` (unchanged package). `currentlyGenerating` field line 77; `ensureCanWrite(BlockPos)` -> `boolean` line 244; `Util.logAndPauseIfInIde(String)` called line 260; `net.minecraft.Util` class.

### 26.2 — PARTIALLY BROKEN (one descriptor change; logic otherwise intact)
- Class, field, method all still exist. `currentlyGenerating` now `private @Nullable Supplier<String>` (line 75) — fine for `@Shadow`. `ensureCanWrite(BlockPos)` line 272; `Util.logAndPauseIfInIde(warning)` called inside line 276.
- **`Util` moved package**: `net.minecraft.Util` -> `net.minecraft.util.Util` (`D:/aiminecraftdev/minecraft26.2/net/minecraft/util/Util.java`). The INVOKE target descriptor `Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V` will NOT match.
- Also note the mod must not import `net.minecraft.Util` for any of its own logic (rename to `net.minecraft.util.Util`).

**Proposed fix:** change target string to `Lnet/minecraft/util/Util;logAndPauseIfInIde(Ljava/lang/String;)V`. Everything else unchanged.

---

## 3) LivingEntityMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/LivingEntityMixin.java`
- Type: classic (`@Mixin(LivingEntity.class)`)
- `@Shadow public abstract ItemStack getItemInHand(InteractionHand hand);`
- `@Inject(method = "tick", at = @At("RETURN"))` -> `CommonEvents.LIVING_TICK` event.
- `@Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"))` -> summon-ghost-sword ability.
- `@Inject(method = "onEffectRemoved", at = @At(value="INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;refreshDirtyAttributes()V"))` -> `FrozenStatusEffect.onRemoved(entity)`.

### 1.21.1 — VALID
- `tick()` `LivingEntity.java:2305`; `swing(InteractionHand, boolean)` line 1737; `getItemInHand(InteractionHand)` line 1913.
- `protected void onEffectRemoved(MobEffectInstance)` line 1016; calls `this.refreshDirtyAttributes()` line 1020 (target matches).

### 26.2 — PARTIALLY BROKEN (onEffectRemoved replaced)
- `tick()` line 2755, `swing(InteractionHand, boolean)` line 2026, `getItemInHand(InteractionHand)` line 2252 — all VALID, unchanged.
- **`onEffectRemoved(MobEffectInstance)` REMOVED.** Replaced by `protected void onEffectsRemoved(Collection<MobEffectInstance> effects)` at line 1105, which still calls `this.refreshDirtyAttributes()` at line 1119 (so the INVOKE target `LivingEntity.refreshDirtyAttributes()V` still exists inside the new method). `refreshDirtyAttributes` is now `private` (line 1123) — descriptor unchanged `()V`, still matches.
- The `@Inject(method = "onEffectRemoved"...)` will fail: `method="onEffectRemoved"` matches nothing.

**Proposed fix:**
- Change `method = "onEffectRemoved"` -> `method = "onEffectsRemoved"`.
- Handler signature: param is now `Collection<MobEffectInstance>` instead of a single `MobEffectInstance`. Rewrite body: iterate the collection, and when an effect whose `effect.getEffect().value() instanceof FrozenStatusEffect` is found, call `onRemoved((LivingEntity)(Object)this)`.

---

## 4) MobEntityMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/MobEntityMixin.java`
- Type: classic (`@Mixin(Mob.class)`, `abstract class ... extends Entity`)
- `@Unique private static boolean iceandfire$isSkeleton(Entity)`.
- `@Inject(method = "dropFromLootTable", at = @At("HEAD"))` handler `public void dropHandler(DamageSource, boolean, CallbackInfo)` — drops `WITHERBONE` from wither-skeletons.

### 1.21.1 — VALID
- `Mob.dropFromLootTable(DamageSource, boolean)` `Mob.java:482`.

### 26.2 — BROKEN (method signature changed)
- `protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled)` `Mob.java:412` — **first parameter `ServerLevel level` added**.
- `@Inject(method = "dropFromLootTable")` (no descriptor) with handler `(DamageSource, boolean, CallbackInfo)` will not resolve — Mixin matches the handler param types against the target method. In 26.2 the handler must be `(ServerLevel, DamageSource, boolean, CallbackInfo)`.
- Also `this.spawnAtLocation(new ItemStack(WITHERBONE, ...))` inside the handler must become `this.spawnAtLocation(level, new ItemStack(WITHERBONE, ...))` because `spawnAtLocation` now requires a `ServerLevel` (see ChickenMixin section; `Entity.java:2227`).

**Proposed fix:** update handler to `dropHandler(ServerLevel level, DamageSource source, boolean causedByPlayer, CallbackInfo ci)` and call `this.spawnAtLocation(level, stack, ...)`. `this.random` still exists. (Alternatively inject into `LootTable` / `dropFromLootTable` args.) No `ServerLevel` import issue — it is already the same class used in ChunkRegionMixin.

---

## 5) GameRendererMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/GameRendererMixin.java`
- Type: classic (`@Mixin(GameRenderer.class)`), uses MixinExtras `@Local`.
- `@Shadow @Final private Camera mainCamera;`
- `@Inject(method = "renderLevel", at = @At(value="INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER))` -> `ClientEvents.onCameraSetup(this.mainCamera)`.
- `@Inject(method = "reloadShaders", at = @At(value="INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", shift = At.Shift.BEFORE))` with `@Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list2` — registers custom `rendertype_dread_portal` shader into `RenderVariables.DREAD_PORTAL_PROGRAM`.

### 1.21.1 — VALID
- `renderLevel(DeltaTracker)` `GameRenderer.java:1201`; calls `camera.setup(level, entity, !isFirstPerson, isMirrored, g)` line 1215 (matches the INVOKE target; `Camera.setup(BlockGetter, Entity, boolean, boolean, float)` `Camera.java:50`).
- `reloadShaders(ResourceProvider)` line 430; builds `List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list2` and constructs `new ShaderInstance(factory, "rendertype_dread_portal", DefaultVertexFormat.POSITION_COLOR)` — matches the `<init>` target. `ShaderInstance` constructor `(ResourceProvider, String, VertexFormat)` exists.
- `mainCamera` field line 130.

### 26.2 — BROKEN (both hooks gone; shader system reworked)
- `renderLevel(DeltaTracker)` still exists (`GameRenderer.java:522`) but **`Camera.setup(...)` no longer exists** in `Camera.java` (26.2 `Camera` only has private `setupPerspective`/`setupOrtho`, line 323/327). Camera flow in 26.2 renderLevel uses `this.mainCamera.update(deltaTracker)` (line 372), `tick()` (line 261), and `extractRenderState` (line 627). The INVOKE target won't match -> `onCameraSetup` injection FAILS.
- **`reloadShaders` removed entirely** (no match anywhere in 26.2 GameRenderer). **`ShaderInstance` class removed** (no `class ShaderInstance` anywhere in `net`). New shader system: `net.minecraft.client.renderer.ShaderManager` (extends `SimplePreparableReloadListener`), `com.mojang.blaze3d.shaders.ShaderSource`, shaders loaded from resource-pack `shaders/` folder; post-process `PostChain` via `minecraft.getShaderManager().getPostChain(...)`. There is no program list `list2` to append to, and `DefaultVertexFormat`-based program construction is gone.
- `mainCamera` field still exists (line 122) — `@Shadow` OK.

**Proposed fix (待确认 - significant rework):**
- `onCameraSetup`: re-target to a 26.2 camera-update site, e.g. inject at `@At("RETURN")` of `GameRenderer.renderLevel`, or hook `Camera` itself (e.g. after `mainCamera.update(deltaTracker)` at `GameRenderer.java:372`). `ClientEvents.onCameraSetup(Camera)` may still be reused if a valid post-setup point exists; otherwise move the logic into the extract/render-state pipeline (e.g. `LevelRenderState.cameraRenderState`).
- Dread-portal shader: must be moved to the datapack/resource-pack shader system. Provide `assets/<mod>/shaders/rendertype_dread_portal.json` + GLSL and look it up via `ShaderManager` at render time instead of constructing a `ShaderInstance` and caching in `RenderVariables.DREAD_PORTAL_PROGRAM`. All `RenderVariables.DREAD_PORTAL_PROGRAM` consumers and the `DefaultVertexFormat.POSITION_COLOR` render pipeline need review.

---

## 6) InGameHudMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/InGameHudMixin.java`
- Type: classic (`@Mixin(Gui.class)`), `@Environment(CLIENT)`.
- `@Shadow protected abstract void renderTextureOverlay(GuiGraphics, ResourceLocation, float);`
- `@Shadow @Final private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;`
- `@Shadow @Final private Minecraft minecraft;`
- `@Inject(method = "renderCameraOverlays", at = @At(value="INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))` — draws dread-portal overlay using `POWDER_SNOW_OUTLINE_LOCATION` with alpha `min(renderTick, i)/i` where `i = player.getTicksRequiredToFreeze()`.

### 1.21.1 — VALID
- `Gui.java` (`net/minecraft/client/gui/Gui.java`): `renderCameraOverlays(GuiGraphics, DeltaTracker)` line 207; `renderTextureOverlay(GuiGraphics, ResourceLocation, float)` line 996; `POWDER_SNOW_OUTLINE_LOCATION` line 115; `minecraft` field line 130; powder-snow block lines 226-227.
- `getTicksFrozen()` is declared on `Entity` (`Entity.java:2331`), not `LocalPlayer`; Mixin inherited-member INVOKE resolution matches the `LocalPlayer` owner in practice (also `getTicksRequiredToFreeze` on `Entity` 1.21.1:2331ff).

### 26.2 — BROKEN (HUD reworked into `Hud` class + render-state extraction)
- **`Gui` is no longer the HUD renderer.** The overlay/HUD logic moved to new `net.minecraft.client.gui.Hud` (line 85). `Gui` keeps only `minecraft` field (line 68) and text/debug helpers; `renderCameraOverlays`, `renderTextureOverlay`, `POWDER_SNOW_OUTLINE_LOCATION` all gone from `Gui`.
- `Hud` has `POWDER_SNOW_OUTLINE_LOCATION` (`Identifier`, line 116), `minecraft` (line 145), and the overlay method is now `extractCameraOverlays(GuiGraphicsExtractor, DeltaTracker)` (starts line ~262); powder-snow block lines 293-295: `if (player.getTicksFrozen() > 0) this.extractTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, player.getPercentFrozen());`.
- Overlay helper renamed `renderTextureOverlay` -> `extractTextureOverlay(GuiGraphicsExtractor, Identifier, float)` (`Hud.java:1026`). `ResourceLocation` renamed `Identifier` (same runtime type in 1.21.1 vs 26.2, name changed).
- `getTicksFrozen()` still on `Entity` (line 2821); call inside `extractCameraOverlays` at line 293 -> INVOKE owner is `Entity` (declaring class); the descriptor in the mixin (`LocalPlayer.getTicksFrozen()I`) is the pre-existing quirk.

**Proposed fix:**
- `@Mixin(Hud.class)`.
- `@Inject(method = "extractCameraOverlays", at = @At(value="INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTicksFrozen()I"))` (recommend `Entity` owner for robustness; `LocalPlayer` may still resolve via hierarchy).
- `@Shadow protected abstract void extractTextureOverlay(GuiGraphicsExtractor, Identifier, float);`
- `@Shadow @Final private static Identifier POWDER_SNOW_OUTLINE_LOCATION;`
- `@Shadow @Final private Minecraft minecraft;`
- Handler: `(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci)`; compute alpha from `PortalRenderHelper.getTick()` and `player.getTicksRequiredToFreeze()` as before.

---

## 7) LivingEntityRendererMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/LivingEntityRendererMixin.java`
- Type: classic (`@Mixin(LivingEntityRenderer.class)`)
- `@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))` -> `ClientEvents.onPostRenderLiving(...)`.

### 1.21.1 — VALID
- `LivingEntityRenderer.render(T, float, float, PoseStack, MultiBufferSource, int)` `LivingEntityRenderer.java:53`.

### 26.2 — BROKEN (render-state extraction architecture)
- `LivingEntityRenderer` is now generic `<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>` (line 38). **No `render(...)` with PoseStack/MultiBufferSource** — the per-frame hook is `public void extractRenderState(T entity, S state, float partialTicks)` (line 242).
- Rendering now goes: `EntityRenderer.extractRenderState(entity, state, partialTicks)` (line 161) -> GPU submit later; `MultiBufferSource` replaced by `GpuBufferSlice`/`RenderPipelines`; there is no post-PoseStack per-entity render callback of the old shape.

**Proposed fix (待确认 - behavior rework):** retarget to `extractRenderState(T entity, S state, float partialTicks)` at `@At("RETURN")`. `ClientEvents.onPostRenderLiving(LivingEntity, float, PoseStack, MultiBufferSource, int)` cannot be called unchanged — the new signature has no PoseStack/MultiBufferSource/light. The post-render work (feature renders / overlays) must be re-ported onto the RenderState + `FeatureRenderer`-style pipeline (see also PlayerEntityRendererMixin / DragonRiderFeatureRenderer note below).

---

## 8) PlayerEntityRendererMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/PlayerEntityRendererMixin.java`
- Type: classic (`@Mixin(PlayerRenderer.class)`)
- `@Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)` — suppresses player rendering when riding a dragon (first person or not in `DragonRiderFeatureRenderer.RENDERING_RIDERS`).

### 1.21.1 — VALID
- `PlayerRenderer.render(AbstractClientPlayer, float, float, PoseStack, MultiBufferSource, int)` `PlayerRenderer.java:69`.

### 26.2 — BROKEN (PlayerRenderer class removed)
- **`PlayerRenderer` does not exist** anywhere in 26.2. Replaced by `net.minecraft.client.renderer.entity.player.AvatarRenderer` (`AvatarRenderer<AvatarlikeEntity extends Avatar & ClientAvatarEntity, AvatarRenderState, PlayerModel>`, line 48). It extends `LivingEntityRenderer` and has `renderRightHand`/`renderLeftHand` (237/243), `extractRenderState(AvatarlikeEntity, AvatarRenderState, float)` (166), but no `render(AbstractClientPlayer, ...)` override.
- `EntityRenderer` in 26.2 has `extractRenderState(T, S, float)` (line 161) as the per-entity hook; the actual draw is issued later from the extracted state.
- `LocalPlayer` and `RemotePlayer` still exist in 26.2 (`client/player/LocalPlayer.java`, `client/player/RemotePlayer.java`).

**Proposed fix (待确认 - logic rework):**
- `@Mixin(AvatarRenderer.class)`.
- `@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)` with handler `(AvatarlikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci)`.
- `entity.getVehicle() instanceof DragonBaseEntity` still works (entity is the avatar/player). `Minecraft.getInstance().options.getCameraType().isFirstPerson()` still exists in 26.2.
- The `DragonRiderFeatureRenderer.RENDERING_RIDERS` mechanism is a client feature renderer — it must be re-ported to the 26.2 feature-renderer/RenderState pipeline (see LivingEntityRendererMixin). Whether cancellation-at-extraction is the right seam (vs cancelling at submit time) needs verification (待确认).

---

## 9) RotatingCubeMapRendererMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/RotatingCubeMapRendererMixin.java`
- Type: classic (`@Mixin(value = PanoramaRenderer.class, priority = 900)`)
- `@Unique private int iceandfire$slowTick;`
- `@Inject(method = "render", at = @At(value="HEAD"), cancellable = true)` handler `(GuiGraphics, int width, int height, float alpha, float tickDelta, CallbackInfo)` -> calls `TitleScreenRenderManager.tick()`/`renderBackground(...)` and cancels the vanilla panorama, when `customMainMenu` enabled.

### 1.21.1 — VALID
- `PanoramaRenderer.render(GuiGraphics, int, int, float, float)` `PanoramaRenderer.java:23`.

### 26.2 — BROKEN (class renamed)
- `PanoramaRenderer` **removed**; replaced by `net.minecraft.client.renderer.Panorama` (`Panorama.java:9`). `TitleScreen` now imports `net.minecraft.client.renderer.Panorama` (26.2 `TitleScreen.java:31`).
- Old `render(GuiGraphics, int, int, float, float)` replaced by `public void extractRenderState(GuiGraphicsExtractor graphics, int width, int height)` (`Panorama.java:35`) — only 2 ints, no alpha/tickDelta params. Also now writes `PanoramaRenderState` into `gameRenderState.guiRenderState.panoramaRenderState` and uses `RenderPipelines.GUI_TEXTURED`.

**Proposed fix:**
- `@Mixin(Panorama.class)` (keep priority 900).
- `@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)` handler `(GuiGraphicsExtractor context, int width, int height, CallbackInfo ci)`.
- Call `TitleScreenRenderManager.tick()`/`renderBackground(context, width, height)` then `ci.cancel()`. `TitleScreenRenderManager.renderBackground` must be updated to accept `GuiGraphicsExtractor` (and any `GuiGraphics`->`GuiGraphicsExtractor` / `Identifier` renames inside).

---

## 10) TitleScreenMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/TitleScreenMixin.java`
- Type: classic (`@Mixin(value = TitleScreen.class, priority = 900)`, `abstract class ... extends Screen`)
- `@Shadow @Nullable private SplashRenderer splash;`
- `@Inject(method = "init", at = @At("RETURN"))` -> sets custom `splash` when custom main menu enabled.
- `@Inject(method = "render", at = @At(value="INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V"))` with `@Local(ordinal = 2) int i` -> `TitleScreenRenderManager.drawModName(context, this.width, this.height, 16777215 | i)`.

### 1.21.1 — VALID
- `TitleScreen` unchanged package. `splash` field line 56; `protected void init()` line 110; `render(GuiGraphics, int, int, float)` line 294; logo rendered at line 312 `this.logoRenderer.renderLogo(guiGraphics, this.width, g)`; `LogoRenderer.renderLogo(GuiGraphics, int, float)` at `LogoRenderer.java:32` (matches INVOKE target).
- `@Local(ordinal=2) int i` captures the alpha int (`k = Mth.ceil(g*255)<<24`, line ~306) used for the splash/branding draw.

### 26.2 — PARTIALLY BROKEN (render renamed + LogoRenderer API changed)
- Class + `splash` field OK (`TitleScreen.java:51`, still `@Nullable SplashRenderer`). `init()` still exists (line 105) -> `init` @Inject VALID.
- **`render(GuiGraphics, int, int, float)` renamed** to `public void extractRenderState(GuiGraphicsExtractor, int, int, float)` (line 289).
- Inside, logo drawn at line 309: `this.logoRenderer.extractRenderState(graphics, this.width, this.logoRenderer.keepLogoThroughFade() ? 1.0F : widgetFade);`. `LogoRenderer.renderLogo` **removed**; new API `LogoRenderer.extractRenderState(GuiGraphicsExtractor, int, float)` (and 4-arg overload) (`LogoRenderer.java:30/34`). `keepLogoThroughFade()` is a new LogoRenderer method.
- **`@Local(ordinal = 2) int i` is gone** — the int alpha local no longer exists in 26.2 (`extractRenderState` uses float `widgetFade`). The `16777215 | i` alpha-computation must be rebuilt (e.g. compute alpha from `widgetFade` float, or use `ARGB.white(widgetFade)`).

**Proposed fix:**
- `@Inject(method = "extractRenderState", at = @At(value="INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V"))`.
- Handler `(GuiGraphicsExtractor context, int mouseX, int mouseY, float a, CallbackInfo ci)`; capture the logo alpha (float `widgetFade`) via a new `@Local` (e.g. `@Local float widgetFade`) instead of `int i`, and pass a computed int/ARGB to `TitleScreenRenderManager.drawModName`.
- `TitleScreenRenderManager.drawModName` must be updated to `GuiGraphicsExtractor`.
- `init` injection unchanged.

---

## 11) WorldRendererMixin
File: `common/src/main/java/com/iafenvoy/iceandfire/mixin/WorldRendererMixin.java`
- Type: classic (`@Mixin(LevelRenderer.class)`), `@Environment(CLIENT)`, uses MixinExtras `@Local`.
- `@Shadow @Final private RenderBuffers renderBuffers;`
- `@Unique private final LightningRenderer iceandfire$lightningRenderer = new LightningRenderer();`
- `@Inject(method = "renderLevel", at = @At(value="INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))` handler `(CallbackInfo, @Local(argsOnly=true) DeltaTracker tickCounter, @Local(argsOnly=true) Camera camera, @Local PoseStack matrices)` — renders `ClientEvents.LIGHTNINGS` bolts via `LightningRenderer` into `renderBuffers.bufferSource()`.

### 1.21.1 — VALID
- `LevelRenderer.renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)` `LevelRenderer.java:878`; iterates `this.level.entitiesForRendering()` line 962. `ClientLevel.entitiesForRendering()` `ClientLevel.java:263`.
- `RenderBuffers.renderBuffers` field line 171; `RenderBuffers.bufferSource()` -> `MultiBufferSource.BufferSource` (`RenderBuffers.java:59`).

### 26.2 — BROKEN (render method reworked; bufferSource removed)
- `renderLevel(...)` **renamed/signature-rewritten** to `public void render(GraphicsResourceAllocator, DeltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky)` (`LevelRenderer.java:154`). No `Camera` param (now `CameraRenderState`), no `PoseStack` (now `Matrix4fc modelViewMatrix`).
- **`ClientLevel.entitiesForRendering()` is no longer called in LevelRenderer.** It still exists (`ClientLevel.java:444`) but is now consumed by `LevelExtractor` (`net/minecraft/client/renderer/extract/LevelExtractor.java:231`) during render-state extraction. The INVOKE target inside `renderLevel` will NOT match.
- `renderBuffers` field still exists (line 103) but **`RenderBuffers.bufferSource()` is gone** (26.2 `RenderBuffers.java` has no `bufferSource()`; class is now `implements AutoCloseable`, built around the GPU/frame-graph pipeline). Custom geometry can no longer be drawn into `bufferSource()`.
- `camera.getPosition()` -> `CameraRenderState.position()` (`Vec3`), and `@Local PoseStack matrices` -> no PoseStack; matrix pushes must go to `RenderSystem.getModelViewStack()` or the provided `Matrix4fc modelViewMatrix`.

**Proposed fix (待确认 - significant rework):** custom lightning must move to the new extraction/GPU pipeline. Candidates:
- Inject into `LevelExtractor.extractRenderState`/entity loop and store bolt geometry in a render-state (e.g. a custom `RenderState`), drawn via a registered `RenderPipelines` shader, OR
- Use a custom `FeatureRenderer`/`RenderSystem`-level hook at the level submit stage (`LevelRenderer.submitFeatures`, `FeatureRenderDispatcher`).
- `LightningBoltData`/`LightningRenderer` (project code) currently draw via `MultiBufferSource` (`renderBuffers.bufferSource()`) — that call path must be replaced; `LightningRenderer.update/render` signatures and the `BoltRenderInfo`/`SpawnFunction` helpers likely need a GPU/buffer rework.

---

## Cross-cutting 26.2 API renames relevant to these mixins
- `net.minecraft.Util` -> `net.minecraft.util.Util` (ChunkRegionMixin).
- `net.minecraft.world.entity.animal.Chicken` -> `net.minecraft.world.entity.animal.chicken.Chicken`.
- `net.minecraft.resources.ResourceLocation` -> `net.minecraft.resources.Identifier`.
- `GuiGraphics` -> `GuiGraphicsExtractor` in GUI/extract paths (Hud, Panorama, TitleScreen, LogoRenderer).
- `PlayerRenderer` -> `AvatarRenderer` (with `Avatar`, `ClientAvatarEntity`, `AvatarRenderState`).
- `PanoramaRenderer` -> `Panorama`.
- `ShaderInstance`/`reloadShaders`/`DefaultVertexFormat` program registration -> `ShaderManager` resource-pack shader system.
- `RenderBuffers.bufferSource()` (MultiBufferSource) -> gone; GPU `GpuBufferSlice`/`RenderPipelines`/frame-graph.
- `onEffectRemoved(MobEffectInstance)` -> `onEffectsRemoved(Collection<MobEffectInstance>)`.
- `Mob.dropFromLootTable(DamageSource, boolean)` -> `dropFromLootTable(ServerLevel, DamageSource, boolean)`.
- `Entity.spawnAtLocation(ItemLike)` -> `spawnAtLocation(ServerLevel, ItemLike)` (+ `ItemStack`/`Vec3` overloads).
- `EntityRenderer.render(...)`/`LivingEntityRenderer.render(...)` -> `extractRenderState(entity, state, partialTicks)` render-state pipeline.

## Summary table

| Mixin | Target class 1.21.1 | Target class 26.2 | Members changed in 26.2 | 1.21.1 OK? | 26.2 OK? |
|---|---|---|---|---|---|
| ChickenMixin | `animal.Chicken` | `animal.chicken.Chicken` | egg path: `spawnAtLocation(ItemLike)` gone; `dropFromGiftLootTable`+loot table `CHICKEN_LAY`; `spawnAtLocation(ServerLevel,ItemLike)` | Yes | **No** |
| ChunkRegionMixin | `server.level.WorldGenRegion` | same | `Util` -> `util.Util` in INVOKE target only | Yes | No (minor: descriptor) |
| LivingEntityMixin | `world.entity.LivingEntity` | same | `onEffectRemoved` -> `onEffectsRemoved(Collection)`; rest same | Yes | **No** (one inject) |
| MobEntityMixin | `world.entity.Mob` | same | `dropFromLootTable(ServerLevel,DamageSource,boolean)`; `spawnAtLocation` level param | Yes | **No** |
| GameRendererMixin | `client.renderer.GameRenderer` | same | `Camera.setup` gone; `reloadShaders` gone; `ShaderInstance` gone -> ShaderManager | Yes | **No** |
| InGameHudMixin | `client.gui.Gui` | `client.gui.Hud` | `renderCameraOverlays`->`extractCameraOverlays`; `renderTextureOverlay`->`extractTextureOverlay`; `GuiGraphics`->`GuiGraphicsExtractor`; `ResourceLocation`->`Identifier` | Yes | **No** |
| LivingEntityRendererMixin | `client.renderer.entity.LivingEntityRenderer` | same | `render(...)` gone -> `extractRenderState(T,S,float)` | Yes | **No** |
| PlayerEntityRendererMixin | `...entity.player.PlayerRenderer` | `...entity.player.AvatarRenderer` | class replaced; `render(AbstractClientPlayer,...)` gone -> `extractRenderState(AvatarlikeEntity,...)` | Yes | **No** |
| RotatingCubeMapRendererMixin | `client.renderer.PanoramaRenderer` | `client.renderer.Panorama` | `render(GuiGraphics,int,int,float,float)` -> `extractRenderState(GuiGraphicsExtractor,int,int)` | Yes | **No** |
| TitleScreenMixin | `client.gui.screens.TitleScreen` | same | `render`->`extractRenderState`; `LogoRenderer.renderLogo`->`extractRenderState`; `@Local int i` alpha gone | Yes | **No** (init part OK) |
| WorldRendererMixin | `client.renderer.LevelRenderer` | same | `renderLevel`->`render`; `entitiesForRendering` call gone (moved to LevelExtractor); `renderBuffers.bufferSource()` gone | Yes | **No** |

**Bottom line:** 0 mixins are 100% transferable to 26.2 as-is; 2 need only a one-line retarget (ChunkRegionMixin, plus `LivingEntityMixin`'s `tick`/`swing` injections carry over while `onEffectRemoved` needs the new name+collection). 7 mixins are substantially broken by the 26.2 render-state/GPU rework and require design rework, not just signature edits.
