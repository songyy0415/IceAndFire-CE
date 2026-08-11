# Fabric API + Fabric Loader Migration Research (1.21.1 → 26.2)

Project: IceAndFire-CE (Architectury, Mojmap). Author: research agent.
Date: 2026-08-09.
References used:
- 1.21.1 Fabric API source: `D:/aiminecraftdev/fabric-api1.21.1` (github source, **Yarn names**)
- 26.2 Fabric API source: `D:/aiminecraftdev/fabric-api-0.156.0-26.2` (github source, **Mojmap names**; `version=0.156.0`, `minecraft_version=26.2`, `loader_version=0.18.4`)
- Fabric Loader 0.19.3 source: `D:/aiminecraftdev/fabric-loader-0.19.3/fabric-loader-0.19.3`
- MC 26.2 Mojmap: `D:/aiminecraftdev/minecraft26.2`
- MC 1.21.1 Mojmap: `D:/aiminecraftdev/minecraft1.21.1mojmap`

NOTE ON MAPPINGS: the 1.21.1 fabric-api repo source is Yarn-mapped; the published artifact is Mojmap. The 26.2 fabric-api repo source is Mojmap-mapped. All "published-artifact" signatures below are Mojmap.

---

## 0. Cross-cutting MC 26.2 rename that hits Fabric API call sites

**`net.minecraft.resources.ResourceLocation` → `net.minecraft.resources.Identifier` in MC 26.2 mojmap.**
- Verified: `D:/aiminecraftdev/minecraft26.2/net/minecraft/resources/Identifier.java:18` `public final class Identifier implements Comparable<Identifier>`. `grep ResourceLocation` over `net/` = 0 hits; `Identifier` = 880 files.
- Static factories unchanged: `Identifier.fromNamespaceAndPath(String, String)` (Identifier.java:40), `parse` (:44), `withDefaultNamespace` (:48), `bySeparator` (:60).
- Impact: every `ResourceLocation.fromNamespaceAndPath(...)` in the mod (common + fabric) becomes `Identifier.fromNamespaceAndPath(...)`. Directly affects the Fabric-API call sites in §3 (AttachmentRegistry.create, ResourceManagerHelper.registerBuiltinResourcePack).
- This is a Minecraft-API concern → feeds `docs/minecraft-api-migration.md`; listed here because it changes the Fabric API call-site text.

---

## 1. Inventory of `net.fabricmc` usage (all verified by grep)

### common module (`D:/IceAndFire-CE/common/src/main/java`) — loader annotations only
| File | Line | Symbol |
|---|---|---|
| com/iafenvoy/iceandfire/mixin/WorldRendererMixin.java | 8, 9 | `net.fabricmc.api.EnvType`, `net.fabricmc.api.Environment` |
| com/iafenvoy/iceandfire/mixin/InGameHudMixin.java | 4, 5 | EnvType, Environment |
| com/iafenvoy/iceandfire/render/SirenShaderRenderHelper.java | 6, 7 | EnvType, Environment |
| com/iafenvoy/iceandfire/render/PortalRenderHelper.java | 5, 6 | EnvType, Environment |
| com/iafenvoy/iceandfire/impl/ParticleProviderHolder.java | 3, 4 | EnvType, Environment |
| com/iafenvoy/iceandfire/IceAndFireClient.java | 16, 17 | EnvType, Environment |
| com/iafenvoy/iceandfire/event/ClientEvents.java | 15, 16 | EnvType, Environment |
| com/iafenvoy/iceandfire/render/misc/ChainRenderer.java | 7, 8 | EnvType, Environment |
| com/iafenvoy/iceandfire/registry/IafRenderers.java | 28, 29 | EnvType, Environment |

All common usage is `@Environment(EnvType.CLIENT)` class-level annotation. **Unchanged in loader 0.19.3.** The common build.gradle depends on `net.fabricmc:fabric-loader:$fabric_loader_version` solely for these annotations (common/build.gradle comment). That dependency must be bumped to the 26.2 loader too.

### fabric module (`D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric`)
| File | Line | Symbol | Used |
|---|---|---|---|
| IafAttachments.java | 9, 10, 11 | `fabric.api.attachment.v1.AttachmentRegistry` / `AttachmentSyncPredicate` / `AttachmentType` | yes |
| ModMenu.java | 8, 9 | `net.fabricmc.api.EnvType`, `Environment` | yes |
| IceAndFireFabric.java | 7 | `net.fabricmc.api.ModInitializer` | yes |
| IceAndFireFabric.java | 8 | `fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry` | **NO (dead import)** |
| IceAndFireFabric.java | 9 | `fabric.api.registry.FabricBrewingRecipeRegistryBuilder` | yes |
| IceAndFireFabric.java | 10 | `net.fabricmc.loader.api.FabricLoader` | yes |
| IceAndFireFabricClient.java | 7 | `net.fabricmc.api.ClientModInitializer` | yes |
| IceAndFireFabricClient.java | 8 | `fabric.api.client.particle.v1.ParticleFactoryRegistry` | yes |
| IceAndFireFabricClient.java | 9 | `fabric.api.resource.ResourceManagerHelper` | yes |
| IceAndFireFabricClient.java | 10 | `fabric.api.resource.ResourcePackActivationType` | yes |
| IceAndFireFabricClient.java | 11 | `net.fabricmc.loader.api.FabricLoader` | yes |

No `fabric.api.event.*`, `fabric.api.networking.*`, `fabric.api.client.rendering.*`, `fabric.api.renderer.*`, `fabric.api.registry.Registry` usage anywhere in the project (grep over common+src + fabric/src = 0 hits). The mod routes events/networking/registry through **Architectury**, so the Fabric events/networking/rendering families listed in the task are NOT touched and need no migration.

---

## 2. Fabric Loader migration table (0.16.7 → 0.19.3)

`gradle.properties`: `fabric_loader_version=0.16.7` (1.21.1). Target reference: **0.19.3** (`D:/aiminecraftdev/fabric-loader-0.19.3`). 26.2 fabric-api modules declare `fabricloader >= 0.18.4` (e.g. fabric-resource-loader-v1/src/main/resources/fabric.mod.json:19).

| Symbol (loader api) | Signature | 1.21.1 (0.16.7) | 26.2 (0.19.3) | Usage site |
|---|---|---|---|---|
| `net.fabricmc.loader.api.FabricLoader` | — | present | present | IceAndFireFabric.java:10, IceAndFireFabricClient.java:11 |
| `FabricLoader.getInstance()` | `static FabricLoader getInstance()` | present | present (FabricLoader.java:41) | IceAndFireFabricClient.java:22 |
| `FabricLoader.getModContainer(String)` | `Optional<ModContainer> getModContainer(String id)` | present | present (FabricLoader.java:154) | IceAndFireFabricClient.java:22 |
| `FabricLoader.isDevelopmentEnvironment()` | `boolean isDevelopmentEnvironment()` | present | present (FabricLoader.java:181) | IceAndFireFabricClient.java:21 (note: mod actually uses Architectury `Platform.isDevelopmentEnvironment()` here) |
| `net.fabricmc.api.EnvType` | `enum {CLIENT, SERVER}` | present | present (EnvType.java:28) | all common/fabric `@Environment` |
| `net.fabricmc.api.Environment` | `@interface Environment { EnvType value(); }` | present | present (Environment.java:43) | all common/fabric |
| `net.fabricmc.api.ModInitializer` | `void onInitialize()` | present | present (ModInitializer.java:29-33) | IceAndFireFabric.java:13 |
| `net.fabricmc.api.ClientModInitializer` | `void onInitializeClient()` | present | present (ClientModInitializer.java:32-36) | IceAndFireFabricClient.java:15 |

**Conclusion: FabricLoader + annotation surface is 100% unchanged.** Only the version string in gradle.properties (and common/build.gradle dependency) needs bumping. No `getModContainer`/`isDevelopmentEnvironment` behavior change relevant here.

---

## 3. Fabric API migration table (0.116.5+1.21.1 → 0.156.0+26.2)

### 3.1 Attachment API — module `fabric-data-attachment-api-v1` (unchanged module, API compatible)

| Symbol | 1.21.1 | 26.2 | Compat? |
|---|---|---|---|
| `AttachmentRegistry.create(RL/Id, Consumer<Builder<A>>)` | Yarn: `create(Identifier, Consumer)` (AttachmentRegistry.java:52) | `create(Identifier, Consumer<Builder<A>>)` (26.2 AttachmentRegistry.java:49-52) | ✅ same, only `ResourceLocation`→`Identifier` arg type |
| `AttachmentType<A>` | interface | interface (26.2 AttachmentType.java) | ✅ |
| `AttachmentSyncPredicate` | `@FunctionalInterface interface extends BiPredicate<AttachmentTarget, ServerPlayer>` | same (26.2 AttachmentSyncPredicate.java:32-33) | ✅ |
| `Builder.initializer(Supplier<A>)` | present (1.21.1 Builder:151) | present (26.2 Builder, via `Builder<A> initializer(Supplier<A>)`) | ✅ |
| `Builder.persistent(Codec<A>)` | present (1.21.1 Builder:127) | present (26.2 Builder:127-ish `Builder<A> persistent(Codec<A>)`) | ✅ |
| `Builder.copyOnDeath()` | present (1.21.1 Builder:134) | present (26.2 Builder:144-ish) | ✅ |
| `Builder.syncWith(StreamCodec<? super RegistryFriendlyByteBuf,A>, AttachmentSyncPredicate)` | Yarn: `syncWith(PacketCodec<? super RegistryByteBuf,A>, pred)` (1.21.1 Builder:160); Mojmap artifact = `StreamCodec<RegistryFriendlyByteBuf,...>` | `syncWith(StreamCodec<? super RegistryFriendlyByteBuf,A>, pred)` (26.2 AttachmentRegistry.java:142) | ✅ same Mojmap signature |
| `Builder.syncWith(StreamCodec, pred, int maxSyncSize)` | — | **NEW in 26.2** (AttachmentRegistry.java:153-154) | optional |
| `AttachmentTarget.getAttachedOrCreate(AttachmentType<A>)` | present | present (26.2 AttachmentTarget.java:146; requires initializer) | ✅ |
| `AttachmentTarget.setAttached / getAttached / getAttachedOrThrow / getAttachedOrSet` | present | present (26.2 AttachmentTarget.java:81-174) | ✅ |

Usage site: `fabric/.../IafAttachments.java:17-19` (`CHAIN_DATA`, `MISC_DATA`, `PORTAL_DATA`) and `fabric/.../impl/fabric/ComponentManagerImpl.java:13,16,21` (`getAttachedOrCreate`).
The mod's `PACKET_CODEC` fields are already `StreamCodec<RegistryFriendlyByteBuf, T>` (ChainData.java:24, MiscData.java:27, PortalData.java:31) — matches 26.2 `syncWith` exactly. **No code change required** beyond the `ResourceLocation`→`Identifier` rename in `AttachmentRegistry.create(...)`.
`ByteBufCodecs.fromCodecWithRegistries(Codec)` still exists in 26.2 (ByteBufCodecs.java:352).

### 3.2 Brewing — **RENAMED class** (breaking)

| Symbol | 1.21.1 | 26.2 |
|---|---|---|
| Class | `net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder` (fabric-content-registries-v0) | **`net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder`** (same module/package) |
| Event | `Event<FabricBrewingRecipeRegistryBuilder.BuildCallback> BUILD` | `Event<FabricPotionBrewingBuilder.BuildCallback> BUILD` (same shape, `EventFactory.createArrayBacked`) |
| Callback | `BuildCallback.build(BrewingRecipeRegistry.Builder builder)` | `BuildCallback.build(PotionBrewing.Builder builder)` |

- 1.21.1 file: `fabric-content-registries-v0/src/main/java/net/fabricmc/fabric/api/registry/FabricBrewingRecipeRegistryBuilder.java` (BUILD + `registerItemRecipe`, `registerPotionRecipe`, `registerRecipes`, `getEnabledFeatures`).
- 26.2 file: `fabric-content-registries-v0/src/main/java/net/fabricmc/fabric/api/registry/FabricPotionBrewingBuilder.java` (identical method set; `registerPotionRecipe(Holder<Potion>, Ingredient, Holder<Potion>)`).
- Usage site: `IceAndFireFabric.java:19`:
  `FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(Potions.WATER, IafItems.SHINY_SCALES.get(), Potions.WATER_BREATHING));`
  The lambda body calls vanilla `PotionBrewing.Builder.addMix(Holder<Potion>, Item, Holder<Potion>)`, which is **unchanged** in 26.2 (verified PotionBrewing.java:141-152). `Potions.WATER` / `Potions.WATER_BREATHING` are `Holder<Potion>` in both.
- Migration: rename import + the `BUILD` field reference to `FabricPotionBrewingBuilder.BUILD`. Nothing else changes.
- This is the **only hard API-class rename in the mod's used surface**.

### 3.3 Particles — **RENAMED class** (breaking, but trivial for this mod)

| Symbol | 1.21.1 | 26.2 |
|---|---|---|
| Class | `net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry` (fabric-particles-v1) | **`net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry`** (same module/package) |
| `getInstance()` | present | present (ParticleProviderRegistry.java:41) |
| `register(ParticleType<T>, ParticleProvider<T>)` | Yarn: `register(ParticleType<T>, ParticleFactory<T>)` (Mojmap artifact: ParticleProvider) | `register(ParticleType<T>, ParticleProvider<T>)` (ParticleProviderRegistry.java:52) — same Mojmap sig |
| Pending overload | `register(ParticleType<T>, PendingParticleFactory<T>)`; `PendingParticleFactory<T>.create(FabricSpriteProvider)` (1.21.1 `FabricSpriteProvider.java`) | `register(ParticleType<T>, PendingParticleProvider<T>)`; `PendingParticleProvider<T>.create(FabricSpriteSet)` (ParticleProviderRegistry.java:62, FabricSpriteSet.java:39) |

Usage site: `IceAndFireFabricClient.java:20`:
```java
IafRenderers.registerParticleRenderers(holder -> holder.applyRegister(
    ParticleFactoryRegistry.getInstance()::register,
    (t, f) -> ParticleFactoryRegistry.getInstance().register(t, f::create)));
```
- Common path (`getInstance()::register` → `register(ParticleType, ParticleProvider)`) works identically with `ParticleProviderRegistry`.
- **Important mitigation:** every particle in `IafRenderers.registerParticleRenderers` (IafRenderers.java:99-109) uses the 1-arg `ParticleProviderHolder<ParticleType, ParticleProvider>` constructor (the common `ParticleProvider` path). **No particle uses the extended `SpriteParticleRegistration` path.** So the second lambda (extended factory) is effectively dead code today; the migration can simply drop it.
- The extended path additionally breaks at the **vanilla** level: `ParticleEngine.SpriteParticleRegistration` no longer exists in MC 26.2 — `ParticleEngine` in 26.2 (ParticleEngine.java:24) has no nested `SpriteParticleRegistration`/`register(...)` method; providers are now resolved from `resourceManager.getProviders()` keyed by `BuiltInRegistries.PARTICLE_TYPE.getId(...)` (ParticleEngine.java:64). This also invalidates the access-widener entry `accessible class net/minecraft/client/particle/ParticleEngine$SpriteParticleRegistration` (see §5).
- `ParticleProviderHolder.java` (common) itself uses vanilla Mojmap types `ParticleProvider`, `ParticleOptions`, `ParticleType`, `ParticleEngine.SpriteParticleRegistration` — only the last one breaks; it is unused by any registered particle.

### 3.4 Resource packs — module deprecated, API kept

| Symbol | 1.21.1 | 26.2 |
|---|---|---|
| Module | `fabric-resource-loader-v0` (root dir) | moved to **`deprecated/fabric-resource-loader-v0`** (still built+published; settings.gradle:72) |
| `ResourceManagerHelper` | `fabric.api.resource.ResourceManagerHelper` | same FQN, in deprecated module (26.2 v0 file:41) |
| `registerBuiltinResourcePack(Identifier, ModContainer, Component, ResourcePackActivationType)` | present | present, same signature (26.2 v0 file:126) |
| `registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)` | present | present (26.2 v0 file:103) |
| `ResourcePackActivationType` enum `{NORMAL, ALWAYS_ENABLED, DEFAULT_ENABLED}` | present | present (26.2 v0 ResourcePackActivationType.java:25-29); `NORMAL` wraps v1 `PackActivationType.NORMAL` |

Usage site: `IceAndFireFabricClient.java:22`:
```java
ResourceManagerHelper.registerBuiltinResourcePack(
    ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "iaf_legacy"),
    container, Component.translatable("resourcePack.iceandfire.legacy.name"),
    ResourcePackActivationType.NORMAL)
```
- Still **compiles in 26.2** (deprecated v0 ships in the fabric-api BOM) → `ResourceLocation`→`Identifier` arg rename is the only required change. Compile-time deprecation warning expected.
- New module `fabric-resource-loader-v1` (settings.gradle:64) is the forward direction: every mod's resource dirs are auto-added as packs (`ModPackResourcesUtil.getModResourcePacks`, `PackActivationType.ALWAYS_ENABLED`); overlay/ordering via fabric.mod.json custom value `fabric:resource_load_order` (ModPackResourcesUtil.java:79). **No direct public `registerBuiltinResourcePack` equivalent in v1** → for now keep the deprecated v0 call (works), restructure to v1 later if the built-in `iaf_legacy` pack needs v1 features.
- Note: 26.2 v1 source imports `net.minecraft.server.packs.repository.Pack` (Mojmap) — consistent Mojmap.

### 3.5 Object builder

| Symbol | 1.21.1 | 26.2 | Compat |
|---|---|---|---|
| `FabricDefaultAttributeRegistry.register(EntityType<? extends LivingEntity>, AttributeSupplier.Builder)` | present (1.21.1 :58) | present (26.2 :78) | ✅ |
| `register(EntityType, AttributeSupplier)` | present (1.21.1 :80) | present (26.2 :99) | ✅ |
| `Event<ModifyDefaultAttribute> MODIFY` | — | **NEW in 26.2** (26.2 :62) | optional |

Usage: **unused.** `IceAndFireFabric.java:8` imports `FabricDefaultAttributeRegistry` but never calls it. Safe to delete the import; if kept, still valid in 26.2.

---

## 4. Fabric module reorganization (26.2) relevant to this mod

| 1.21.1 module | 26.2 module | Notes |
|---|---|---|
| `fabric-data-attachment-api-v1` | `fabric-data-attachment-api-v1` | unchanged |
| `fabric-object-builder-api-v1` | `fabric-object-builder-api-v1` | unchanged |
| `fabric-content-registries-v0` | `fabric-content-registries-v0` | brewing class renamed inside |
| `fabric-particles-v1` | `fabric-particles-v1` | factory→provider rename inside |
| `fabric-resource-loader-v0` | `deprecated/fabric-resource-loader-v0` + new `fabric-resource-loader-v1` | v0 deprecated but shipped |
| `fabric-recipe-api-v1` | `fabric-recipe-api-v1` | unchanged (not used by mod) |
| `fabric-networking-api-v1` | `fabric-networking-api-v1` | unchanged (not used by mod) |
| (keybinding) `fabric-key-binding-api-v1` | `fabric-key-mapping-api-v1` | renamed module; not used by mod |

The 26.2 build renames `fabric-key-binding-api-v1` → `fabric-key-mapping-api-v1` (see 26.2 settings.gradle / dir listing). Not relevant to this mod (no keybinding usage).

---

## 5. fabric.mod.json + access widener notes

### fabric.mod.json (`D:/IceAndFire-CE/fabric/src/main/resources/fabric.mod.json`)
- `schemaVersion: 1` — loader 0.19.3 still reads schemaVersion 1 (ModMetadataParser.java:65-99; absent → treated as 0). **No change needed.**
- `entrypoints`: `main`, `client`, `emi`, `jade`, `modmenu` — all **custom string keys**; the loader treats entrypoint names as data, no registry of fixed names. No new mandatory entrypoint in 0.19.3.
- `accessWidener: "iceandfire.accesswidener"` — field unchanged; file lives in `fabric/src/main/resources/` and is byte-identical to `common/src/main/resources/iceandfire.accesswidener` (verified diff = identical). Header `accessWidener v2 named` unchanged in 0.19.3.
- `mixins: ["iceandfire.mixins.json"]` — file lives in **common** (`common/src/main/resources/iceandfire.mixins.json`); referenced from the fabric jar. Concept unchanged.
- `depends`: `"minecraft": "1.21.x"` → must become `"26.x"`. Consider adding `"fabricloader": ">=0.18.4"` and `"fabric-api": "*"` (currently absent — the fabric API dependency is only declared in build.gradle).
- `environment: "*"` unchanged.

### access widener (`iceandfire.accesswidener`, 20 entries) — status against 26.2 mojmap
| AW line | 26.2 status |
|---|---|
| `accessible class ParticleEngine$SpriteParticleRegistration` | **BREAKS** — class removed in 26.2 (see §3.3). Delete entry. |
| `accessible method LivingEntity getBaseExperienceReward ()I` | **BREAKS** — 26.2 sig is `getBaseExperienceReward(ServerLevel)` (LivingEntity.java:608). Update descriptor. |
| `accessible method GameRenderer loadEffect (Lnet/minecraft/resources/ResourceLocation;)V` | **BREAKS** — method renamed in 26.2 (now `setPostEffect`/`clearPostEffect`/`togglePostEffect`/`checkEntityPostEffect`, field `postEffectId`; GameRenderer.java:120,197-209). Also `ResourceLocation`→`Identifier`. Update. |
| `accessible field EntityRenderDispatcher renderers Ljava/util/Map;` | exists but descriptor changed → 26.2 `private Map<EntityType<?>, EntityRenderer<?, ?>>` (EntityRenderDispatcher.java:44). Re-verify exact descriptor. |
| `ThrownTrident ID_LOYALTY/ID_FOIL/dealtDamage`, `AbstractArrow pickupItemStack/setPierceLevel` | **BREAKS (package)** — both moved from `net.minecraft.world.entity.projectile` → `net.minecraft.world.entity.projectile.arrow` (26.2: `.../projectile/arrow/ThrownTrident.java`, `.../arrow/AbstractArrow.java`). Update package in AW. Field/method names otherwise present. |
| `accessible method Camera getMaxZoom (F)F` | now `private float getMaxZoom(float)` (Camera.java:294) — still exists; `accessible` still applies. OK. |
| `accessible method Camera move (FFF)V` | present (Camera.java:286). OK. |
| `accessible field Screen renderables Ljava/util/List;` | present, `private final List<Renderable>` (Screen.java:67). OK (may need descriptor re-check). |
| `accessible field PoiTypes TYPE_BY_STATE Ljava/util/Map;` | present (PoiTypes.java:59). OK. |
| `accessible field NodeEvaluator mob` | present `protected Mob mob` (NodeEvaluator.java:16). OK. |
| `accessible field ClientLevel entityStorage` | present (ClientLevel.java:147). OK. |
| `accessible field Mob goalSelector/targetSelector` | present, now `protected final` (Mob.java:134-135). OK. |
| `accessible method CombatTracker getMostSignificantFall ()L...CombatEntry;` | present but now `private` (CombatTracker.java:108). OK (accessible applies). |
| `accessible class ParticleEngine$SpriteParticleRegistration` | covered above (remove). |
| `accessible method SimpleParticleType <init> (Z)V` | ctor now `protected` (SimpleParticleType.java:11) — still exists; `accessible` applies. OK. |

(The access-widener target validation is cross-cutting with `docs/mixin-migration.md` / `docs/minecraft-api-migration.md`; listed here because AW is a Fabric loader concept.)

---

## 6. Summary of required changes (Fabric-side)

1. **gradle.properties**: `fabric_loader_version=0.16.7` → `0.19.3`; `fabric_api_version=0.116.5+1.21.1` → `0.156.0+26.2` (reference versions). Bump `minecraft_version` → `26.2`.
2. **common/build.gradle** loader dependency: same bump (used for `@Environment`).
3. `IceAndFireFabric.java:9,19`: `FabricBrewingRecipeRegistryBuilder` → `FabricPotionBrewingBuilder` (import + `BUILD` ref). Lambda `builder.addMix(...)` unchanged. Dead import at `:8` (`FabricDefaultAttributeRegistry`) can be removed.
4. `IceAndFireFabricClient.java:8,20`: `ParticleFactoryRegistry` → `ParticleProviderRegistry`. The extended `SpriteParticleRegistration` branch is dead (no particle uses it) — safe to drop; keep only the common `getInstance()::register` path.
5. `IceAndFireFabricClient.java:22` + `IafAttachments.java:17-19`: rename `ResourceLocation.fromNamespaceAndPath` → `Identifier.fromNamespaceAndPath` (global MC rename).
6. `ResourceManagerHelper`/`ResourcePackActivationType`: no change (deprecated v0 still shipped); expect deprecation warnings. Long-term migrate to `fabric-resource-loader-v1`.
7. `fabric.mod.json`: `depends.minecraft` → `"26.x"`; optionally add `"fabricloader": ">=0.18.4"`, `"fabric-api": "*"`.
8. **Access widener** (fabric/src/main/resources/iceandfire.accesswidener + common copy): delete `ParticleEngine$SpriteParticleRegistration`; update `getBaseExperienceReward`, `loadEffect` (renamed), `EntityRenderDispatcher.renderers` descriptor, and `ThrownTrident`/`AbstractArrow` package (`...projectile.arrow`).

## 7. 待确认 (to be confirmed)

- **Deprecated v0 compilation**: the 26.2 `deprecated/fabric-resource-loader-v0` repo source is Mojmap but mixes `net.minecraft.resources.Identifier` (v0 ResourceManagerHelper.java:25 import) — expected fine when compiling a Mojmap mod against the published Mojmap artifact, but should be smoke-tested in the actual build (the repo's own source layout is unusual).
- **`EntityRenderDispatcher.renderers` exact AW descriptor** in 26.2 (`Map<EntityType<?>, EntityRenderer<?, ?>>`) — needs the actual field descriptor string.
- **Final version strings** to use in gradle.properties for 26.2 (0.19.3 loader / 0.156.0 fabric-api are the available reference versions; the real latest may be higher).
- Whether `fabric-data-attachment-api-v1`'s `@ApiStatus.Experimental` still requires the existing `@SuppressWarnings("UnstableApiUsage")` (IafAttachments.java:15, ComponentManagerImpl.java:10) — expected yes, harmless.
- `fabric-resource-loader-v1` has no public builtin-pack registration API yet — confirm no other public entrypoint before deciding to drop the deprecated v0 call.
