# Fabric Module Analysis — IceAndFire-CE

Target of this file: the `fabric` module of IceAndFire-CE (MC 1.21.1 Mojmap, Architectury).
Reference sources used to verify 26.2 API survival:
- 26.2 Fabric API: `D:/aiminecraftdev/fabric-api-0.156.0-26.2`
- 26.2 Fabric Loader: `D:/aiminecraftdev/fabric-loader-0.19.3/fabric-loader-0.19.3`
- 1.21.1 Fabric API: `D:/aiminecraftdev/fabric-api1.21.1`
- MC 1.21.1 mojmap: `D:/aiminecraftdev/minecraft1.21.1mojmap`
- MC 26.2 mojmap: `D:/aiminecraftdev/minecraft26.2`
- Trinkets 1.21.1 / 26.2, Architectury 26.2, Jupiter new, Integration
- Project root: `D:/IceAndFire-CE`

Legend: **[OK]** = exists unchanged in 26.2 · **[CHANGED]** = moved / renamed / signature changed · **[REMOVED]** = gone in 26.2 · **[N/A]** = not applicable / not verified.

---

## 1. Inventory of `fabric/src` (all files)

Root: `D:/IceAndFire-CE/fabric`

### Java sources (7 files)

| File | Role |
|---|---|
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java` | Fabric main entrypoint (`ModInitializer`). Boots common (`IceAndFire.init/process`), initializes attachments, registers a brewing mix, defers Trinkets registration. |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java` | Fabric client entrypoint (`ClientModInitializer`). Boots common client, wires particle providers into Fabric's particle registry, registers a built-in resource pack (`iaf_legacy`). |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java` | Defines Fabric `AttachmentType`s for the three entity components (ChainData / MiscData / PortalData) and a `LIVING_TICK` hook that ticks + syncs them. |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java` | ModMenu integration (`ModMenuApi`) — returns a Jupiter `ConfigSelectScreen` for both common and client config. |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/TrinketsRegistry.java` | Registers `IafItems.HYDRA_HEART` as a Trinket via `TrinketsApi.registerTrinket`. |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/SimpleTickItemWrapper.java` | `Trinket` impl that forwards `tick` to `Item.inventoryTick`. |
| `fabric/src/main/java/com/iafenvoy/iceandfire/impl/fabric/ComponentManagerImpl.java` | Fabric-side impl of the common `ComponentManager` API (platform split) — returns the attachments from `IafAttachments`. |

### Resources (7 files)

| File | Role |
|---|---|
| `fabric/src/main/resources/fabric.mod.json` | Fabric mod metadata (transcribed in §3). |
| `fabric/src/main/resources/iceandfire.accesswidener` | AccessWidener (identical copy of common's; transcribed in §4). |
| `fabric/src/main/resources/logo.png` | Mod icon (referenced by `fabric.mod.json` "icon"). |
| `fabric/src/main/resources/data/trinkets/entities/iceandfire.json` | Trinkets: player→slot mapping (`{entities:[player], slots:[legs/belt]}`). |
| `fabric/src/main/resources/data/trinkets/slots/legs/belt.json` | Trinkets: belt slot def (`{amount:1}`). |
| `fabric/src/main/resources/data/trinkets/tags/item/legs/belt.json` | Trinkets: item tag putting `iceandfire:hydra_heart` into the belt slot. |

---

## 2. Per-file deep analysis + external imports

### 2.1 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java`

Summarized (23 lines):
- `onInitialize()`: `IceAndFire.init()` → `IceAndFire.process()` → `IafAttachments.init()` → registers a brewing mix `Potions.WATER + IafItems.SHINY_SCALES → Potions.WATER_BREATHING` via `FabricBrewingRecipeRegistryBuilder.BUILD` → defers `TrinketsRegistry::registerItems` until Trinkets loads (via `IntegrationExecutor.runWhenLoad`).

Imports:
- `com.iafenvoy.iceandfire.IceAndFire` (common)
- `com.iafenvoy.iceandfire.fabric.compat.trinkets.TrinketsRegistry` (this module)
- `com.iafenvoy.iceandfire.registry.IafItems` (common)
- `com.iafenvoy.integration.IntegrationExecutor` (Integration lib) — `runWhenLoad(String modId, Supplier<Runnable>)`
- `net.fabricmc.api.ModInitializer` (loader) **[OK]** (loader 26.2 still has `net/fabricmc/api/ModInitializer.java`)
- `net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry` (fabric-api) **[OK]** — **DEAD IMPORT** (not used in body)
- `net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder` (fabric-api, content-registries) **[REMOVED in 26.2]** — grep of the entire 26.2 fabric-api tree for `FabricBrewingRecipeRegistryBuilder` returns nothing. This is the module's biggest Fabric-API break.
- `net.fabricmc.loader.api.FabricLoader` (loader) **[OK]** — **DEAD IMPORT** (not used in body)
- `net.minecraft.world.item.alchemy.Potions` (MC) **[OK]** — `net/minecraft/world/item/alchemy/Potions.java` still exists in 26.2, `WATER`, `WATER_BREATHING` still defined.

Notes: `builder.addMix(...)` binds to the vanilla 1.21.1 mojmap `PotionBrewing.Builder` (mojmap name; Yarn calls it `BrewingRecipeRegistry.Builder`), which fabric-api 1.21.1 extends via interface injection (`FabricBrewingRecipeRegistryBuilder`, see `fabric-content-registries-v0`). In 26.2 `PotionBrewing` is built entirely in code by `PotionBrewing.bootstrap(FeatureFlagSet)` (only `addVanillaMixes` is called; there is no datapack `potion_brewing` resource type and no Fabric event) — see §6.9.

### 2.2 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java`

Summarized (24 lines):
- `onInitializeClient()`: `IceAndFireClient.init()` → `IceAndFireClient.process()` → `IafRenderers.registerParticleRenderers(holder -> holder.applyRegister(...))` wiring Fabric's particle registry → registers builtin resource pack `iceandfire:iaf_legacy` (only when not a dev environment) via `ResourceManagerHelper.registerBuiltinResourcePack(...)`.

Imports:
- `com.iafenvoy.iceandfire.IceAndFire` (common)
- `com.iafenvoy.iceandfire.IceAndFireClient` (common)
- `com.iafenvoy.iceandfire.registry.IafRenderers` (common) — `registerParticleRenderers(Consumer<ParticleProviderHolder<?>>)`
- `dev.architectury.platform.Platform` (Architectury) — `isDevelopmentEnvironment()` **[OK]** — confirmed present in `architectury-api26.2/common/.../dev/architectury/platform/Platform.java:183`
- `net.fabricmc.api.ClientModInitializer` (loader) **[OK]**
- `net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry` (fabric-api, particles-v1) **[CHANGED → `ParticleProviderRegistry`]** — in 26.2 the class is `net/fabricmc/fabric/api/client/particle/v1/ParticleProviderRegistry.java` (`ParticleFactoryRegistry` is gone). Methods: `static getInstance()`, `register(ParticleType<T>, ParticleProvider<T>)`, `register(ParticleType<T>, PendingParticleProvider<T>)`. The pending factory inner type is now `PendingParticleProvider` with `ParticleProvider<T> create(FabricSpriteSet)` (was `PendingParticleFactory.create(FabricSpriteProvider)`). Both `register` overloads survive, so the `holder.applyRegister(ParticleFactoryRegistry.getInstance()::register, (t, f) -> ParticleFactoryRegistry.getInstance().register(t, f::create))` wiring works with a straight rename — `f::create` (a `ParticleProvider.Sprite` factory) now yields a `ParticleProvider`.
- `net.fabricmc.fabric.api.resource.ResourceManagerHelper` (fabric-api) **[REMOVED from active modules; only `deprecated/fabric-resource-loader-v0`]** — in 26.2 the class exists only under `deprecated/` and is `@Deprecated`; replacement is `net.fabricmc.fabric.api.resource.v1.ResourceLoader.registerBuiltinPack(Identifier, ModContainer, Component, PackActivationType)` (see §6.7).
- `net.fabricmc.fabric.api.resource.ResourcePackActivationType` (fabric-api) **[REMOVED from active modules; only `deprecated/fabric-resource-loader-v0`]** — `@Deprecated`; replacement is `net.fabricmc.fabric.api.resource.v1.pack.PackActivationType` (constants `NORMAL`/`DEFAULT_ENABLED`/`ALWAYS_ENABLED` are mirrored).
- `net.fabricmc.loader.api.FabricLoader` (loader) **[OK]** — `getInstance().getModContainer(String)` returns `Optional<ModContainer>` (confirmed in loader 26.2 `FabricLoader.java`).
- `net.minecraft.network.chat.Component` (MC) — `Component.translatable` **[OK]** (26.2 `Component.java:139`).
- `net.minecraft.resources.ResourceLocation` (MC) **[REMOVED → `net.minecraft.resources.Identifier`]** — 26.2 has no `ResourceLocation`; `Identifier.fromNamespaceAndPath(String, String)` at `Identifier.java:40`.

### 2.3 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java`

Summarized (34 lines):
- Creates three `AttachmentType`s: `CHAIN_DATA`, `MISC_DATA`, `PORTAL_DATA`, each `AttachmentRegistry.create(id, builder -> builder.initializer(...).persistent(CODEC).syncWith(PACKET_CODEC, AttachmentSyncPredicate.all()).copyOnDeath())`.
- `init()` subscribes `CommonEvents.LIVING_TICK`; generic `tickAndSync(type, entity)` calls `entity.getAttachedOrCreate(type).tick(entity)` and `entity.setAttached(type, attachment)` if dirty.

Imports:
- `com.iafenvoy.iceandfire.IceAndFire` (common)
- `com.iafenvoy.iceandfire.data.component.ChainData` / `MiscData` / `PortalData` (common) — each exposes `static Codec<...> CODEC` and `static StreamCodec<RegistryFriendlyByteBuf, ...> PACKET_CODEC` (verified for ChainData: `StreamCodec<RegistryFriendlyByteBuf, ChainData> PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC)`).
- `com.iafenvoy.iceandfire.event.CommonEvents` (common) — `LIVING_TICK` event.
- `com.iafenvoy.iceandfire.util.attachment.IafEntityAttachment<T>` (common) — `tick(T)`, `isDirty()`.
- `net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry` **[OK]** — 26.2 still has `create(Identifier, Consumer<Builder<A>>)`, `createDefaulted`, `createPersistent` (`fabric-data-attachment-api-v1`).
- `net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate` **[OK]** — `all()` exists in 26.2.
- `net.fabricmc.fabric.api.attachment.v1.AttachmentType` **[OK]** — 26.2 `AttachmentType.java` has `initializer()`, `isPersistent()`, `copyOnDeath()`; the `Builder` (in `AttachmentRegistry`) keeps `persistent(Codec)`, `copyOnDeath()`, `initializer(Supplier)`, `syncWith(StreamCodec, AttachmentSyncPredicate)`.
- `net.minecraft.resources.ResourceLocation` **[REMOVED → `Identifier`]**
- `net.minecraft.world.entity.Entity` (MC) **[OK]** — `getAttachedOrCreate(AttachmentType)`, `setAttached(AttachmentType, A)` come from Fabric's `AttachmentTarget` mixin on Entity (present in both 1.21.1 and 26.2 fabric-api).

Notes: All `AttachmentRegistry.Builder` methods used are still present in 26.2; the only edits needed are `ResourceLocation` → `Identifier` and (nothing else). The `@SuppressWarnings("UnstableApiUsage")` stays valid (attachment API is still unstable-marked).

### 2.4 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java`

Summarized (18 lines):
- `@Environment(EnvType.CLIENT)` `ModMenuApi`; `getModConfigScreenFactory()` returns `parent -> ConfigSelectScreen.builder(Component.translatable("config.iceandfire.title"), parent).server(IafCommonConfig.INSTANCE).client(IafClientConfig.INSTANCE).build()`.

Imports:
- `com.iafenvoy.iceandfire.config.IafClientConfig` / `IafCommonConfig` (common) — both extend `com.iafenvoy.jupiter.config.container.AutoInitConfigContainer` (verified for `IafCommonConfig`), which is a subtype of Jupiter's `AbstractConfigContainer` (the type required by `ConfigSelectScreen.Builder.server/client`).
- `com.iafenvoy.jupiter.render.screen.ConfigSelectScreen` (Jupiter) **[OK in Jupiter new/26.2]** — `builder(Component, Screen)` at `Jupiter new/.../render/screen/ConfigSelectScreen.java:126`, `Builder.server(AbstractConfigContainer)` :155, `Builder.client(AbstractConfigContainer)` :160, `build()` :165. Same call shape as the project uses.
- `com.terraformersmc.modmenu.api.ConfigScreenFactory` (ModMenu) **[待确认]** — ModMenu 26.2 is not in the provided reference tree; API is stable across 11.x/12.x, `getModConfigScreenFactory()` signature unchanged.
- `com.terraformersmc.modmenu.api.ModMenuApi` (ModMenu) **[待确认]**
- `net.fabricmc.api.EnvType` (loader) **[OK]** (`EnvType.java` present in loader 26.2)
- `net.fabricmc.api.Environment` (loader) **[OK]** (`Environment.java` present in loader 26.2)
- `net.minecraft.network.chat.Component` **[OK]**

### 2.5 `fabric/src/main/java/com/iafenvoy/iceandfire/impl/fabric/ComponentManagerImpl.java`

Summarized (23 lines):
- Static impl of the common `ComponentManager` platform split. `getChainData(LivingEntity)` / `getMiscData(LivingEntity)` / `getPortalData(Player)` return `living.getAttachedOrCreate(IafAttachments.CHAIN_DATA / MISC_DATA / PORTAL_DATA)`.

Imports:
- `com.iafenvoy.iceandfire.data.component.ChainData` / `MiscData` / `PortalData` (common)
- `com.iafenvoy.iceandfire.fabric.IafAttachments` (this module)
- `net.minecraft.world.entity.LivingEntity` (MC) **[OK]**
- `net.minecraft.world.entity.player.Player` (MC) **[OK]**

Notes: No external platform imports. All `getAttachedOrCreate` come from Fabric `AttachmentTarget`. No 26.2 work needed here beyond the Entity attachment API being stable.

### 2.6 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/TrinketsRegistry.java`

Summarized (15 lines):
- `registerItems()` registers `IafItems.HYDRA_HEART.get()` via `TrinketsApi.registerTrinket(item, new SimpleTickItemWrapper(item))`.

Imports:
- `com.iafenvoy.iceandfire.registry.IafItems` (common)
- `dev.emi.trinkets.api.TrinketsApi` (Trinkets) **[REMOVED → `eu.pb4.trinkets.api.TrinketsApi`]** — 26.2 Trinkets is a full rewrite by PB4. Package is now `eu.pb4.trinkets.api`. `registerTrinket(Item, Trinket)` **no longer exists**; replacement is `TrinketCallback.setCallback(Item, TrinketCallback)` or having the Item itself implement `TrinketCallback` (`eu.pb4.trinkets.api.callback.TrinketCallback`). `TrinketsApi` still exposes `getAttachment`, `getDropRule`, `canApplyEffects`, `hurtAndBreakItemStack`, `registerTrinketPredicate`, `withTrinketSlots`, `getPlayerSlots`/`getEntitySlots`.
- `net.minecraft.world.item.Item` (MC) **[OK]**

### 2.7 `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/SimpleTickItemWrapper.java`

Summarized (20 lines):
- `implements Trinket`; `tick(ItemStack, SlotReference, LivingEntity)` forwards to `this.item.inventoryTick(stack, entity.level(), entity, 0, false)`.

Imports:
- `dev.emi.trinkets.api.SlotReference` (Trinkets) **[REMOVED → `eu.pb4.trinkets.api.TrinketSlotAccess`]** (26.2 also has `TrinketSlotReference`). The `Trinket` interface itself became `eu.pb4.trinkets.api.callback.TrinketCallback` with `default void tick(ItemStack, TrinketSlotAccess, LivingEntity)` (confirmed in `TrinketCallback.java:59`).
- `dev.emi.trinkets.api.Trinket` (Trinkets) **[REMOVED → `eu.pb4.trinkets.api.callback.TrinketCallback`]**
- `net.minecraft.world.entity.LivingEntity` **[OK]**
- `net.minecraft.world.item.Item` **[OK]**
- `net.minecraft.world.item.ItemStack` **[OK]**

Notes (double break): the inner call `Item.inventoryTick(ItemStack, Level, Entity, int, boolean)` is 1.21.1's signature. 26.2 `Item.inventoryTick` is `(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)` — `Item.java:289` (1.21.1 was `(ItemStack, Level, Entity, int slotId, boolean isSelected)` at `Item.java:254`). So the wrapper needs an `EquipmentSlot` derived from the trinket `TrinketSlotAccess` and a `ServerLevel` cast; this is where the migration must be careful.

### 2.8 Resources (trinkets data JSON)

- `data/trinkets/entities/iceandfire.json` — `{ "entities": ["player"], "slots": ["legs/belt"] }`.
- `data/trinkets/slots/legs/belt.json` — `{ "amount": 1 }`.
- `data/trinkets/tags/item/legs/belt.json` — `{ "replace": false, "values": ["iceandfire:hydra_heart"] }`.

26.2 trinkets still reads JSON data from `data/<ns>/trinkets/...` (`SlotLoader`/`EntitySlotLoader` in `eu.pb4.trinkets.impl.data`; `TrinketsMain.NAMESPACE = "trinkets"`). EntitySlotLoader reads `replace`, `slots`, `entities` (same shape). SlotLoader's `SlotData.read` still reads `amount`, `order`, `icon`, `max_stack_size`, `drop_rule`, `is_vanity`, `is_hidden` (so `{amount:1}` parses). `legs/belt` still exists as `DefaultTrinketSlots.LEGS_BELT` and the item tag `DefaultTrinketSlotTags.LEGS_BELT = "trinkets:legs/belt"`. So the three JSON files remain structurally valid; the *Java* API around them is what broke. **[CHANGED — files OK, API broken]**

---

## 3. `fabric/src/main/resources/fabric.mod.json` (full transcription)

```json
{
  "schemaVersion": 1,
  "id": "iceandfire",
  "version": "${version}",
  "name": "Ice And Fire Community Edition",
  "description": "An unofficial fork of Ice And Fire. Dragons and other mythological creatures in minecraft.",
  "authors": [ "Alexthe666 (Origin Author)", "IAFEnvoy" ],
  "contributors": [ "xiaowu", "uoay" ],
  "contact": { "sources": "https://github.com/IAFEnvoy/IceAndFire-CE" },
  "license": "LGPL-3.0",
  "icon": "logo.png",
  "environment": "*",
  "entrypoints": {
    "main":   [ "com.iafenvoy.iceandfire.fabric.IceAndFireFabric" ],
    "client": [ "com.iafenvoy.iceandfire.fabric.IceAndFireFabricClient" ],
    "emi":    [ "com.iafenvoy.iceandfire.compat.emi.IceAndFireEmiPlugin" ],
    "jade":   [ "com.iafenvoy.iceandfire.compat.jade.IceAndFireJadePlugin" ],
    "modmenu":[ "com.iafenvoy.iceandfire.fabric.ModMenu" ]
  },
  "mixins": [ "iceandfire.mixins.json" ],
  "accessWidener": "iceandfire.accesswidener",
  "depends": {
    "minecraft": "1.21.x",
    "jupiter":   ">=2.3",
    "uranus":    ">=2.3.2"
  }
}
```

Notes:
- `iceandfire.mixins.json` lives in **common** resources (`common/src/main/resources/iceandfire.mixins.json`), bundled into the fabric jar via the shadowed `:common` configuration.
- `emi`/`jade` entrypoints point at **common** classes (`com.iafenvoy.iceandfire.compat.emi.IceAndFireEmiPlugin`, `com.iafenvoy.iceandfire.compat.jade.IceAndFireJadePlugin`), not fabric code. EMI/Jade survival in 26.2 is covered in other research files.
- `accessWidener` is the copy under `fabric/src/main/resources` (identical to common's).
- `${version}` is expanded at processResources time (`fabric/build.gradle` `filesMatching('fabric.mod.json')`).
- `depends` declares `jupiter >=2.3` and `uranus >=2.3.2`; 26.2 will need those bumped to the 26.2-era Jupiter/Uranus versions.

---

## 4. `iceandfire.accesswidener` (full transcription, v2 named) + 26.2 target status

File (identical in `common/src/main/resources/` and `fabric/src/main/resources/` — verified via `diff`):
`fabric/src/main/resources/iceandfire.accesswidener`

```
accessWidener v2 named
```

| # | Line | 26.2 status |
|---|---|---|
| 1 | `accessible class net/minecraft/client/particle/ParticleEngine$SpriteParticleRegistration` | **[REMOVED]** — 26.2 has no `SpriteParticleRegistration` inner class in `ParticleEngine.java`. The vanilla sprite factory is now the inner interface `net.minecraft.client.particle.ParticleProvider$Sprite`. (1.21.1 mojmap used `SpriteParticleRegistration`.) |
| 2 | `accessible field net/minecraft/client/renderer/entity/EntityRenderDispatcher renderers Ljava/util/Map;` | **[OK]** — field exists (`EntityRenderDispatcher.java:44`, `Map<EntityType<?>, EntityRenderer<?, ?>>`, now private). Erased descriptor `Ljava/util/Map;` still matches. |
| 3 | `accessible field net/minecraft/world/entity/Mob goalSelector Lnet/minecraft/world/entity/ai/goal/GoalSelector;` | **[OK]** — `Mob.java:134` (`protected final GoalSelector goalSelector`). |
| 4 | `accessible field net/minecraft/world/entity/Mob targetSelector Lnet/minecraft/world/entity/ai/goal/GoalSelector;` | **[OK]** — `Mob.java:135`. |
| 5 | `accessible field net/minecraft/world/entity/projectile/ThrownTrident ID_LOYALTY Lnet/minecraft/network/syncher/EntityDataAccessor;` | **[CHANGED]** — class **moved** to `net/minecraft/world/entity/projectile/arrow/ThrownTrident.java`; `ID_LOYALTY` still exists (`ThrownTrident.java:32`). AW class path must be updated. |
| 6 | `accessible field net/minecraft/world/entity/projectile/ThrownTrident ID_FOIL Lnet/minecraft/network/syncher/EntityDataAccessor;` | **[CHANGED]** — same move; `ID_FOIL` at `arrow/ThrownTrident.java:33`. |
| 7 | `accessible field net/minecraft/world/entity/projectile/ThrownTrident dealtDamage Z` | **[CHANGED]** — same move; `dealtDamage` at `arrow/ThrownTrident.java:36` (`boolean`, private). |
| 8 | `accessible field net/minecraft/world/entity/projectile/AbstractArrow pickupItemStack Lnet/minecraft/world/item/ItemStack;` | **[CHANGED]** — `AbstractArrow` moved to `net/minecraft/world/entity/projectile/arrow/AbstractArrow.java`; `pickupItemStack` still exists (`AbstractArrow.java:80`, `ItemStack`, private). |
| 9 | `accessible method net/minecraft/world/entity/projectile/AbstractArrow setPierceLevel (B)V` | **[CHANGED]** — same move; `private void setPierceLevel(byte)` at `arrow/AbstractArrow.java:694`. |
| 10 | `accessible field net/minecraft/world/level/pathfinder/NodeEvaluator mob Lnet/minecraft/world/entity/Mob;` | **[OK]** — `NodeEvaluator.java:16` (`protected Mob mob`). |
| 11 | `accessible field net/minecraft/client/multiplayer/ClientLevel entityStorage Lnet/minecraft/world/level/entity/TransientEntitySectionManager;` | **[OK]** — `ClientLevel.java:147` (`private final TransientEntitySectionManager<Entity>`; erased descriptor matches). |
| 12 | `accessible method net/minecraft/world/entity/LivingEntity getBaseExperienceReward ()I` | **[CHANGED — signature]** — 26.2 method is `protected int getBaseExperienceReward(ServerLevel level)` (`LivingEntity.java:608`). AW descriptor must become `(Lnet/minecraft/server/level/ServerLevel;)I`. |
| 13 | `accessible method net/minecraft/world/damagesource/CombatTracker getMostSignificantFall ()Lnet/minecraft/world/damagesource/CombatEntry;` | **[OK]** — `CombatTracker.java:108` (`private @Nullable CombatEntry getMostSignificantFall()`). |
| 14 | `accessible method net/minecraft/client/Camera getMaxZoom (F)F` | **[OK]** — `Camera.java:294` (`private float getMaxZoom(float)`). |
| 15 | `accessible method net/minecraft/client/Camera move (FFF)V` | **[OK]** — `Camera.java:331` (`protected void move(float, float, float)`). |
| 16 | `accessible method net/minecraft/client/renderer/GameRenderer loadEffect (Lnet/minecraft/resources/ResourceLocation;)V` | **[REMOVED]** — no `loadEffect` in 26.2 `GameRenderer.java`. Post-effect loading now goes through `setPostEffect(Identifier)` (private, `GameRenderer.java:223`) + shader-manager `PostChain` (`getPostChain(Identifier, LevelTargetBundle.MAIN_TARGETS)`); also `ResourceLocation` → `Identifier`. |
| 17 | `accessible field net/minecraft/client/gui/screens/Screen renderables Ljava/util/List;` | **[OK]** — `Screen.java:67` (`private final List<Renderable>`; erased descriptor matches). |
| 18 | `accessible field net/minecraft/world/entity/ai/village/poi/PoiTypes TYPE_BY_STATE Ljava/util/Map;` | **[CHANGED — value type]** — still exists (`PoiTypes.java:59`) but value type is now `Map<BlockState, Holder<PoiType>>` (1.21.1 was `Map<BlockState, PoiType>`). Erased descriptor still matches, but any common code reading it must handle `Holder<PoiType>`. |
| 19 | `accessible method net/minecraft/core/particles/SimpleParticleType <init> (Z)V` | **[OK]** — `SimpleParticleType.java:11` (`protected SimpleParticleType(boolean overrideLimiter)`). |

**AW summary for 26.2:** 9 lines OK as-is; 4 lines need class-path updates (arrow/trident move to `world/entity/projectile/arrow/`); 2 lines removed (particle registration inner class, `GameRenderer.loadEffect`); 1 line signature change (`getBaseExperienceReward` gains `ServerLevel`); 1 line semantic type change (`PoiTypes.TYPE_BY_STATE` value becomes `Holder<PoiType>`).

---

## 5. Common-module wiring (`project(':common')`) and Architectury plumbing

- `fabric/build.gradle`:
  - `architectury { platformSetupLoomIde(); fabric() }` — Architectury fabric platform.
  - `loom { accessWidenerPath = project(":common").loom.accessWidenerPath }` — the AW is declared once in `common/build.gradle` (`loom { accessWidenerPath = file("src/main/resources/iceandfire.accesswidener") }`) and inherited here. The AW is a **single shared file**; any 26.2 fix must be applied to `common/src/main/resources/iceandfire.accesswidener` (the fabric copy is redundant but identical).
  - `common(project(path: ':common', configuration: 'namedElements')) { transitive false }` → added to `compileClasspath`, `runtimeClasspath`, `developmentFabric`.
  - `shadowBundle project(path: ':common', configuration: 'transformProductionFabric')` → `shadowJar` (classifier `dev-shadow`) → `remapJar { input.set shadowJar.archiveFile }`. The common jar's resources (incl. `iceandfire.mixins.json`, `iceandfire.accesswidener`) land in the final fabric jar this way.
  - `processResources` expands `${version}` in `fabric.mod.json`.
- Version pins (root `gradle.properties`): `minecraft_version=1.21.1`, `fabric_loader_version=0.16.7`, `fabric_api_version=0.116.5+1.21.1`, `architectury_api_version=13.0.8`, `cca_version=6.1.1`, `emi_version=1.1.19+1.21.1`, `modmenu_version=11.0.3`, `ponder_version=1.0.61`, `integration_version=0.2`. (26.2 will need all bumped.)
- The three `net.fabricmc` interface-entrypoints (`ModInitializer`/`ClientModInitializer`/`ModMenuApi`) are the Architectury-standard fabric bootstrap; `DevArchitectury Platform` is used only for `isDevelopmentEnvironment()`.

---

## 6. API-usage table (old → 26.2)

| Usage (file:line) | 1.21.1 API | 26.2 status | 26.2 replacement / note |
|---|---|---|---|
| `IceAndFireFabric:19` | `net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder.BUILD` | **[REMOVED]** | no equivalent in 26.2 fabric-api. 26.2 vanilla builds `PotionBrewing` in `PotionBrewing.bootstrap(FeatureFlagSet)` (`PotionBrewing.java:129`); a custom mix needs a Mixin into `bootstrap`/`addVanillaMixes` (builder is mutable there), or another 26.2-native hook. Exact mechanism 待确认. |
| `IceAndFireFabric:19` | `builder.addMix(Potions.WATER, item, Potions.WATER_BREATHING)` (vanilla `PotionBrewing.Builder`) | **[CHANGED]** | `PotionBrewing.Builder.addMix(Holder<Potion>, Item, Holder<Potion>)` still exists in 26.2 (`PotionBrewing.java:226`), but the entry point to obtain the builder is different. |
| `IceAndFireFabric:20` | `com.iafenvoy.integration.IntegrationExecutor.runWhenLoad` | **[OK]** | present in `Integration` common source (`IntegrationExecutor.java:7`). |
| `IceAndFireFabricClient:20` | `net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry` | **[CHANGED]** | `ParticleProviderRegistry` (same package). `getInstance()`, `register(type, ParticleProvider)`, `register(type, PendingParticleProvider)` preserved; `PendingParticleFactory` → `PendingParticleProvider`, `FabricSpriteProvider` → `FabricSpriteSet`. Rename-only for this wiring. |
| `IceAndFireFabricClient:22` | `net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(ResourceLocation, ModContainer, Component, ResourcePackActivationType)` | **[REMOVED from active API; deprecated]** | `net.fabricmc.fabric.api.resource.v1.ResourceLoader.registerBuiltinPack(Identifier, ModContainer, Component, PackActivationType)`. `ResourcePackActivationType` → `PackActivationType` (`fabric-resource-loader-v1/.../v1/pack/PackActivationType.java`). |
| `IceAndFireFabricClient:22` | `net.minecraft.resources.ResourceLocation` | **[REMOVED]** | `net.minecraft.resources.Identifier` (`Identifier.fromNamespaceAndPath`). |
| `IceAndFireFabricClient:22` | `FabricLoader.getInstance().getModContainer(id)` | **[OK]** | returns `Optional<ModContainer>` in loader 26.2. |
| `IafAttachments:17-19` | `AttachmentRegistry.create(ResourceLocation, Consumer<Builder>)` | **[CHANGED]** | `AttachmentRegistry.create(Identifier, Consumer<Builder>)`; param type rename only. |
| `IafAttachments:17-19` | `Builder.initializer(Supplier)`, `.persistent(Codec)`, `.syncWith(StreamCodec, AttachmentSyncPredicate)`, `.copyOnDeath()` | **[OK]** | all four builder methods present in 26.2 `AttachmentRegistry.Builder`. |
| `IafAttachments:17-19` | `AttachmentSyncPredicate.all()` | **[OK]** | present in 26.2. |
| `IafAttachments:30-32` | `Entity.getAttachedOrCreate(AttachmentType)` / `setAttached(...)` | **[OK]** | from fabric `AttachmentTarget` mixin, stable in 26.2. |
| `ModMenu:16` | `com.iafenvoy.jupiter.render.screen.ConfigSelectScreen.builder(Component, Screen).server(...).client(...).build()` | **[OK]** | identical builder chain in Jupiter new (`ConfigSelectScreen.java:126/155/160/165`). |
| `ModMenu:6-7` | `com.terraformersmc.modmenu.api.{ModMenuApi,ConfigScreenFactory}` | **[待确认]** | ModMenu 26.2 not in reference tree; API is stable across versions. |
| `TrinketsRegistry:13` | `dev.emi.trinkets.api.TrinketsApi.registerTrinket(Item, Trinket)` | **[REMOVED]** | 26.2 rewrite: `eu.pb4.trinkets.api.TrinketsApi`; use `eu.pb4.trinkets.api.callback.TrinketCallback.setCallback(Item, TrinketCallback)` or implement `TrinketCallback` on the Item. |
| `SimpleTickItemWrapper:9` | `dev.emi.trinkets.api.Trinket` | **[REMOVED]** | `eu.pb4.trinkets.api.callback.TrinketCallback`. |
| `SimpleTickItemWrapper:10` | `dev.emi.trinkets.api.SlotReference` | **[REMOVED]** | `eu.pb4.trinkets.api.TrinketSlotAccess` (also `TrinketSlotReference`). |
| `SimpleTickItemWrapper:17` | `Trinket.tick(ItemStack, SlotReference, LivingEntity)` | **[CHANGED]** | `TrinketCallback.tick(ItemStack, TrinketSlotAccess, LivingEntity)`. |
| `SimpleTickItemWrapper:18` | `Item.inventoryTick(ItemStack, Level, Entity, int, boolean)` | **[CHANGED]** | 26.2: `Item.inventoryTick(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)` (`Item.java:289`). Needs `ServerLevel` + `EquipmentSlot` derived from trinket slot. |
| trinkets JSON data (3 files) | `data/trinkets/...` json format | **[OK structurally]** | 26.2 loaders still read `data/<ns>/trinkets/slots` & `entities` (`SlotLoader`, `EntitySlotLoader`); `legs/belt` = `DefaultTrinketSlots.LEGS_BELT`, tag `DefaultTrinketSlotTags.LEGS_BELT` = `trinkets:legs/belt`. |
| `fabric.mod.json` entrypoints `emi`/`jade` | common `IceAndFireEmiPlugin` / `IceAndFireJadePlugin` | — | out of fabric-module scope (analyzed separately). |
| `accessWidener` 20 lines | mojmap targets | mixed | see §4 table. |

---

## 7. "Risky for 26.2" list (fabric module)

1. **Brewing recipe registration is gone.** `FabricBrewingRecipeRegistryBuilder.BUILD` (and the whole fabric brewing API) is removed from 26.2 fabric-api; vanilla 26.2 has no datapack potion-brewing type and builds `PotionBrewing` purely in `PotionBrewing.bootstrap` (`MinecraftServer.<init>` line 353). The `Shiny Scales → Water Breathing` mix needs a Mixin into `PotionBrewing.bootstrap`/`addVanillaMixes` (or equivalent). HIGH RISK — a core game feature with no direct API replacement.
2. **Trinkets is a full rewrite (PB4).** Package `dev.emi.trinkets.api` → `eu.pb4.trinkets.api`; `Trinket` → `TrinketCallback`; `SlotReference` → `TrinketSlotAccess`; `registerTrinket` removed. `TrinketsRegistry` and `SimpleTickItemWrapper` must be rewritten. The dependency coordinates change from `dev.emi:trinkets` to PB4's artifact. HIGH RISK.
3. **`Item.inventoryTick` signature change** breaks `SimpleTickItemWrapper` even after the Trinkets rewrite: `(ItemStack, Level, Entity, int, boolean)` → `(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)`. Must derive `EquipmentSlot` and obtain `ServerLevel` from the tick context. HIGH RISK.
4. **Built-in resource pack API moved to deprecated + relocated.** `ResourceManagerHelper`/`ResourcePackActivationType` only exist under `deprecated/fabric-resource-loader-v0` in 26.2 and are `@Deprecated`; migrate to `ResourceLoader.registerBuiltinPack(Identifier, ModContainer, Component, PackActivationType)` (and `ResourceLocation` → `Identifier`). MEDIUM RISK (deprecated path would still compile if the deprecated module ships, but should not be used).
5. **`ParticleFactoryRegistry` renamed to `ParticleProviderRegistry`.** Low effort (mechanical rename), but the accompanying vanilla `ParticleEngine.SpriteParticleRegistration` AW target is removed and replaced by `ParticleProvider$Sprite` — the common `ParticleProviderHolder` also references the old inner class. MEDIUM RISK for the particle subsystem.
6. **AW entries that break on 26.2:** (a) `ThrownTrident`/`AbstractArrow` moved to `world/entity/projectile/arrow/` — 4 entries (ID_LOYALTY, ID_FOIL, dealtDamage, pickupItemStack, setPierceLevel = 5 lines) must be re-pointed; (b) `LivingEntity.getBaseExperienceReward ()I` → `(Lnet/minecraft/server/level/ServerLevel;)I`; (c) `ParticleEngine$SpriteParticleRegistration` and `GameRenderer.loadEffect` removed; (d) `PoiTypes.TYPE_BY_STATE` value becomes `Holder<PoiType>` (semantic). Any common code relying on these AW grants will fail to compile at runtime or throw linkage errors. MEDIUM/HIGH RISK.
7. **`ResourceLocation` → `Identifier`** rename affects `IceAndFireFabricClient.java` and `IafAttachments.java` directly (plus every common file using it, out of scope here). Mechanical but broad. LOW risk individually.
8. **Dead imports** in `IceAndFireFabric.java` (`FabricDefaultAttributeRegistry`, `FabricLoader`) are harmless but will need re-checking once the file is edited for the brewing fix.

---

## 8. Key file paths (all absolute)

- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java`
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java`
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java`
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java`
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/TrinketsRegistry.java`
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/SimpleTickItemWrapper.java`
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/impl/fabric/ComponentManagerImpl.java`
- `D:/IceAndFire-CE/fabric/src/main/resources/fabric.mod.json`
- `D:/IceAndFire-CE/fabric/src/main/resources/iceandfire.accesswidener` (duplicate of common's `D:/IceAndFire-CE/common/src/main/resources/iceandfire.accesswidener`)
- `D:/IceAndFire-CE/fabric/build.gradle` (wiring; common via `namedElements` + `transformProductionFabric`)
- Trinkets data: `D:/IceAndFire-CE/fabric/src/main/resources/data/trinkets/{entities,slots,tags}/...`
