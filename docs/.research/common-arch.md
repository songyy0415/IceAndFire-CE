# IceAndFire-CE Common Module Architecture Research

Analyzed on branch `1.21.1-arch-old`. All paths relative to project root `D:/IceAndFire-CE` unless absolute.
Common code root: `common/src/main/java/com/iafenvoy/iceandfire`.

**Bottom line**: The common module is cleanly Architected around Architectury's `DeferredRegister`/`NetworkManager`/event abstractions plus three IAEnvoy libraries (Uranus, Jupiter, Integration). There is almost NO load-specific leakage in common Java — the only `net.fabricmc.*` imports are the `@Environment(EnvType.CLIENT)` marker annotations (12 files), and `Platform.isNeoForge()`/`isFabric()` gates in 3 files. The single `@ExpectPlatform` bridge is `ComponentManager`. The most important hidden hazard is that the **neoforge module is stale: it still compiles against Yarn mappings** (`net.minecraft.entity.*`, `net.minecraft.potion.Potions`), so it is not part of the current mojmap build and must be rewritten, not ported.

---

## 1. Init / Process pipeline

### 1.1 `IceAndFire` (common, `IceAndFire.java`)

Static constants: `LOGGER`, `MOD_ID="iceandfire"`, `MOD_NAME`, `VERSION` (read via `Platform.getMod(MOD_ID).getVersion()` — Architectury).

`IceAndFire.id(path)` helper (L36-39): parses a `ResourceLocation`, defaulting namespace to `MOD_ID`. TODO comment says it is a temporary compat shim to be removed.

**`IceAndFire.init()`** (L41-80) — called from the platform entrypoints (Fabric `onInitialize`, NeoForge constructor). Order matters:

1. **Config (Jupiter)**: `ConfigManager.getInstance().registerConfigHandler(IafCommonConfig.INSTANCE)`; `ServerConfigManager.registerServerConfig(IafCommonConfig.INSTANCE, ServerConfigManager.PermissionChecker.IS_OPERATOR)`.
2. **Custom-registry data init** (populate the 6 `DefaultedMappedRegistry`s created in `IafRegistries`):
   - `IafBestiaryPages.init()`, `IafDragonColors.init()`, `IafDragonTypes.init()`, `IafHippogryphTypes.init()`, `IafSeaSerpentTypes.init()`, `IafTrollTypes.init()`.
3. **Data-driven item/armor init** (register additional items & armor materials into the DeferredRegisters):
   - `DragonColor.initArmors()`, `SeaSerpentType.initArmors()`, `IafSkullType.initItems()`, `TrollType.initArmors()`.
4. **DeferredRegister `.register()` calls**, in this exact order:
   `IafAttributes`, `IafArmorMaterials`, `IafSounds`, `IafBlocks`, `IafBlockEntities`, `IafDataComponents`, `IafEntities`, `IafItemGroups`, `IafItems`, `IafLoots`, `IafRecipes`, `IafRecipeSerializers`, `IafParticles`, `IafProcessors`, `IafFeatures`, `IafScreenHandlers`, `IafStatusEffects`, `IafStructurePieces`, `IafStructureTypes`.
5. **Trades**: `IafTrades.POI_REGISTRY.register()`, `IafTrades.PROFESSION_REGISTRY.register()`.
6. **Attributes**: `IafEntities.registerAttributes()` → `EntityAttributeRegistry.register(...)` for ~30 mobs.

**`IceAndFire.process()`** (L82-101) — second phase (FMLCommonSetup / after-init). Order:
1. `IafEntities.init()` → `addSpawners()` + `commonSetup()`.
2. `IafTrades.init()` → POI block-state map registration + villager offers.
3. `IafRecipes.init()` → `DispenserBlock.registerProjectileBehavior(...)`.
4. `IafFeatures.init()` → `BiomeModifications.addProperties(...)`.
5. `IafToolMaterials.init()` → repair ingredients.
6. **Event registration** (mixed Architectury + Uranus):
   - `BlockEvent.BREAK` → `ServerEvents::onBreakBlock` (Architectury)
   - `InteractionEvent.INTERACT_ENTITY` → `ServerEvents::onEntityInteract` (Architectury)
   - `InteractionEvent.RIGHT_CLICK_BLOCK` → `ServerEvents::onPlayerRightClick` (Architectury)
   - `EntityEvent.LIVING_DEATH` → `ServerEvents::onEntityDie` (Architectury)
   - `PlayerEvent.ATTACK_ENTITY` → `ServerEvents::onPlayerAttack` (Architectury)
   - `PlayerEvents.LOGGED_OUT` → `ServerEvents::onPlayerLeaveEvent` (Uranus `com.iafenvoy.uranus.event`)
   - `EntityEvents.ON_JOIN_WORLD` → `ServerEvents::onEntityJoinWorld` (Uranus)
   - `EntityEvents.START_TRACKING_TAIL` → `ServerEvents::onLivingSetTarget` (Uranus)
   - `LivingEntityEvents.DAMAGE` → `ServerEvents::onEntityDamage` (Uranus, returns modified float)
   - `EntityEvent.LIVING_HURT` → `ServerEvents::onLivingHurt` (Architectury)
7. `ServerNetworkHelper.registerReceivers()`.

### 1.2 `IceAndFireClient` (common, `IceAndFireClient.java`)

`@Environment(EnvType.CLIENT)`. Called by Fabric `ClientModInitializer`; NeoForge calls `init()` from its mod constructor guarded by `Platform.getEnv()==Dist.CLIENT` (but NeoForge client `process()` is NOT wired — see §5.4).

**`init()`**: register client config (`IafClientConfig.INSTANCE`); `IafRenderers.registerEntityRenderers()` (Architectury `EntityRendererRegistry`); `IafKeybindings.init()` (Architectury `KeyMappingRegistry` + `ClientTickEvent.CLIENT_POST`); `IntegrationExecutor.runWhenLoad("ponder", () -> IceAndFirePonderPlugin::init)` (Integration lib).

**`process()`**: `IafScreenHandlers.registerGui()` (vanilla `MenuScreens.register`); `IafRenderers.registerRenderLayers()/registerModelPredicates()/registerBlockEntityRenderers()/registerArmorRenderers()/registerItemRenderers()`; `PortalRenderHelper.init()`; registers `CommonEvents.LIVING_TICK` → `ClientEvents::onLivingUpdate` (custom Uranus-style Event); `ClientTickEvent.CLIENT_POST` → `SirenShaderRenderHelper::tick`; `ClientNetworkHelper.registerReceivers()`.

### 1.3 Fabric wiring (`fabric/src/main/java/.../fabric/IceAndFireFabric.java`)

`onInitialize()`: `IceAndFire.init()` + `IceAndFire.process()` + `IafAttachments.init()` + Fabric-specific **brewing recipe** via `FabricBrewingRecipeRegistryBuilder` (Fabric API `net.fabricmc.fabric.api.registry`) + `IntegrationExecutor.runWhenLoad("trinkets", ...)`.
`IceAndFireFabricClient.onInitializeClient()`: client init/process + `IafRenderers.registerParticleRenderers(...)` bridge + registers a **built-in resource pack** `iaf_legacy` via Fabric API `ResourceManagerHelper.registerBuiltinResourcePack` (guarded by `Platform.isDevelopmentEnvironment()`).

---

## 2. Directory analysis

### 2.1 `event/`
| File | Role |
|---|---|
| `ServerEvents.java` (350 lines) | The core gameplay-logic event handlers: `onEntityDamage` (Troll/Dragon armor damage reduction), `onLivingSetTarget` (chicken/amphithere alarm), `onPlayerAttack` (sheep→cyclops, stone statue cracking, multipart hit → `NetworkManager.sendToServer(PlayerHitMultipartC2SPayload)`), `onEntityDie` (chain drop, Alex easter egg, ghost spawn on player death), `onEntityInteract` (chain removal, dragon horn on multipart), `onPlayerRightClick` (chest→untamed dragon agro, wall→`ChainItem.attachToFence`), `onBreakBlock` (gold pile agro), `onPlayerLeaveEvent` (dismount), `onEntityJoinWorld` (add AI goals to vanilla mobs), `onLivingHurt` (lightning-dragonsteel armor lightning immunity). Uses Architectury `EventResult` and `dev.architectury.utils.value.IntValue` (the `xp` param in `onBreakBlock`). |
| `ClientEvents.java` | `@Environment(CLIENT)`. `onCameraSetup` (dragon third-person zoom), `onLivingUpdate` (dragon riding control → `NetworkManager.sendToServer(DragonControlC2SPayload)`), `onPostRenderLiving` (cockatrice beam / frozen / chain renderers). Holds `currentView` and static `LIGHTNINGS` list. |
| `IafEvents.java` | 3 custom cancelable `Event<...>` objects (`ON_GRIEF_BREAK_BLOCK`, `ON_DRAGON_DAMAGE_BLOCK`, `ON_DRAGON_FIRE_BLOCK`) using the Uranus `Event` helper. These are the "old" internal event API — different mechanism from Architectury events. |
| `CommonEvents.java` | One custom `Event<Consumer<LivingEntity>> LIVING_TICK` (Uranus `Event`), consumed by fabric's `IafAttachments.init()` and by `IceAndFireClient.process()`. |

### 2.2 `network/` (+ `network/payload/`)
| File | Role |
|---|---|
| `ClientNetworkHelper.java` | `registerReceivers()`: 6 `NetworkManager.registerReceiver(Side.S2C, ...)` for `DragonSetBurnBlockS2CPayload`, `LightningBoltS2CPayload` (queues into `ClientEvents.LIGHTNINGS`), `StartRidingMobS2CPayload`, `UpdatePixieHouseS2CPayload`, `UpdatePixieJarS2CPayload`, `UpdatePodiumS2CPayload`. |
| `ServerNetworkHelper.java` | `registerReceivers()`: registers S2C payload **types** via `NetworkManager.registerS2CPayloadType(...)` guarded by `Platform.getEnvironment() == Env.SERVER`; then 5 `NetworkManager.registerReceiver(Side.C2S, ...)` for `DragonControlC2SPayload`, `MultipartInteractC2SPayload` (uses `ctx.queue`), `PlayerHitMultipartC2SPayload`, `StartRidingMobC2SPayload`. |
| `payload/*.java` (10 files) | All plain vanilla MC `CustomPacketPayload` records: `Type` + `StreamCodec` built from `RecordCodecBuilder` via `ByteBufCodecs.fromCodec`. **No Architectury payload abstraction is used** — the codecs are already cross-loader. IDs: `dragon_control`, `dragon_set_burn_block`, `lightning_bolt_s2c`, `multipart_interact`, `player_hit_multipart`, `start_riding_mob_c2s`, `start_riding_mob_s2c`, `update_pixie_house`, `update_pixie_jar`, `update_podium`. |

> **Note (cosmetic bug)**: `MultipartInteractC2SPayload` codec uses `UUIDUtil.AUTHLIB_CODEC.fieldOf("blockPos")` for `creatureID` and `Codec.FLOAT.fieldOf("isProducing")` for `dmg`; `PlayerHitMultipartC2SPayload` uses `fieldOf("blockPos")`/`fieldOf("isProducing")` for `entityId`/`index`. These field names are only used for error messages/stream identity, so functionally harmless, but clearly copy-paste leftovers.

### 2.3 `registry/` (35 files)
| File | What it registers / does |
|---|---|
| `IafRegistries.java` | Creates 6 **vanilla `DefaultedMappedRegistry`** instances (not DeferredRegister) for custom types: `BESTIARY_PAGE`, `DRAGON_COLOR`, `DRAGON_TYPE`, `HIPPOGRYPH_TYPE`, `SEA_SERPENT_TYPE`, `TROLL_TYPE`. |
| `IafRegistryKeys.java` | `ResourceKey<Registry<...>>` for those 6 custom registries. |
| `IafAttributes.java` | `DeferredRegister<Attribute>`; one attribute `DRAGON_FORGE_SPEED`. Has a `//FIXME::Fix this f**king thing after port to NeoForge only` comment. |
| `IafArmorMaterials.java` | `DeferredRegister<ArmorMaterial>`; 11+ materials incl. config-driven dragonsteel. |
| `IafBannerPatterns.java` | `ResourceKey<BannerPattern>` only (`PATTERN_DREAD`). |
| `IafBestiaryPages.java` | Populates `IafRegistries.BESTIARY_PAGE` via `Registry.register(...)`. |
| `IafBlockEntities.java` | `DeferredRegister<BlockEntityType<?>>`; 9 BE types. |
| `IafBlocks.java` (19 KB) | `DeferredRegister<Block>`; ~100+ blocks. |
| `IafDamageTypes.java` | 5 `ResourceKey<DamageType>` + custom `DamageSource` subclasses (`CustomEntityDamageSource`, `CustomIndirectEntityDamageSource`). |
| `IafDataComponents.java` | `DeferredRegister<DataComponentType<?>>`; 10 data components (tick_counter, user_id, active, hippogryph_egg, nbt_compound, crystal_dragon_data, bestiary_pages, dragon_horn, dragon_skull, stone_status). |
| `IafDragonColors.java` / `IafDragonTypes.java` | Populate `IafRegistries.DRAGON_COLOR` / `DRAGON_TYPE` via `Registry.register(...)`. |
| `IafEntities.java` | `DeferredRegister<EntityType<?>>`; ~50 entity types (build helper `EntityType.Builder`). `registerAttributes()` → `EntityAttributeRegistry.register`. `commonSetup()` → `SpawnPlacements.register`. `addSpawners()` → `BiomeModifications.addProperties` but **early-returns `if (Platform.isNeoForge())`** (NeoForge uses datapack biome modifiers instead). |
| `IafFeatures.java` | `DeferredRegister<Feature<?>>`; 8 features + `ResourceKey<ConfiguredFeature>`/`ResourceKey<PlacedFeature>` constants. `init()` runs `BiomeModifications.addProperties`, **early-returns `if (Platform.isNeoForge())`**. |
| `IafHippogryphTypes.java` / `IafSeaSerpentTypes.java` / `IafTrollTypes.java` | Populate the corresponding custom registries. |
| `IafItemGroups.java` | `DeferredRegister<CreativeModeTab>`; 4 tabs via `CreativeTabRegistry.create(...)`. |
| `IafItems.java` (58 KB) | `DeferredRegister<Item>`; huge item list. Helper methods `registerBlock/registerItem/registerToolOrWeapon/registerArmor` call `RegistrySupplier.listen(o -> CreativeTabRegistry.append(tab, o))`. Spawn eggs use Architectury's `ArchitecturySpawnEggItem` (`dev.architectury.core.item`). |
| `IafKeybindings.java` | Client: `KeyMappingRegistry.register` (Architectury) + `ClientTickEvent.CLIENT_POST` to cycle `ClientEvents.currentView`. |
| `IafLoots.java` | `DeferredRegister<LootItemFunctionType<?>>`; 2 loot functions. |
| `IafParticles.java` | `DeferredRegister<ParticleType<?>>`; 10 particle types. |
| `IafProcessors.java` | `DeferredRegister<StructureProcessorType<?>>` via **`DeferredSupplier`** (not RegistrySupplier) — 4 structure processors. |
| `IafRecipeSerializers.java` | `DeferredRegister<RecipeSerializer<?>>`; dragonforge serializer. |
| `IafRecipes.java` | `DeferredRegister<RecipeType<?>>`; `DRAGON_FORGE_TYPE`. `init()` registers dispenser behaviors. |
| `IafRenderLayers.java` | Client-only custom `RenderType` subclass (ghost/frozen/stone/dread-portal render types). Not in init pipeline. |
| `IafRenderers.java` | Client: `EntityRendererRegistry`, `BlockEntityRendererRegistry`, `RenderTypeRegistry`, `ItemPropertiesRegistry` (Architectury client APIs) + `registerParticleRenderers(Consumer<ParticleProviderHolder<?>>)` callback bridge + Uranus `IArmorRendererBase`/`DynamicItemRenderer`. |
| `IafScreenHandlers.java` | `DeferredRegister<MenuType<?>>`; 5 extended menus via **`MenuRegistry.ofExtended(...)`** (Architectury) + 2 vanilla `new MenuType<>(...)`. `registerGui()` → vanilla `MenuScreens.register`. |
| `IafSounds.java` | `DeferredRegister<SoundEvent>`; ~dozens of sounds. |
| `IafStatusEffects.java` | `DeferredRegister<MobEffect>`; `FROZEN`, `SIREN_CHARM`. |
| `IafStructurePieces.java` / `IafStructureTypes.java` | `DeferredRegister<StructurePieceType>` / `DeferredRegister<StructureType<?>>`. |
| `IafToolMaterials.java` | `enum` implementing `Tier` (vanilla); `init()` sets repair ingredients. |
| `IafTrades.java` | 2 `DeferredRegister`s (`PoiType`, `VillagerProfession`). `init()` does `PoiTypes.TYPE_BY_STATE.put(state, ...)` (vanilla mutable-map hack) then registers villager offers via `util/trade/TradeOfferHelper`. |
| `IafWorld.java` | `ResourceKey<Level> DREAD_LAND` (custom dimension). |

### 2.4 `registry/tag/`
All are plain vanilla `TagKey` constants:
- `CommonBlockTags.java` / `CommonItemTags.java` — namespace **`c`** (common/Convention tags): cobblestones, gravels, stones, strings, ingots/silver.
- `IafBannerPatternTags`, `IafBiomeTags` (30+), `IafBlockTags` (9), `IafEntityTags` (18), `IafItemTags` (19) — namespace `iceandfire`. Mirrored as JSON in `common/src/main/resources/data/{iceandfire,c}/tags/...`.

### 2.5 `impl/`
| File | Role |
|---|---|
| `ComponentManager.java` | **The only `@ExpectPlatform` in common.** 3 static methods: `getChainData(LivingEntity)`, `getMiscData(LivingEntity)`, `getPortalData(Player)`, each `throw new AssertionError("This method should be replaced by Architectury.")`. Implemented by `impl.fabric.ComponentManagerImpl` and `impl.neoforge.ComponentManagerImpl` (§5). |
| `ParticleProviderHolder.java` | `@Environment(CLIENT)` bridge class holding either a `ParticleProvider<T>` or `ParticleEngine.SpriteParticleRegistration<T>`; `applyRegister(...)` lets the loader plug in its own registry consumer (Fabric passes `ParticleFactoryRegistry.getInstance()::register`). This is the particle-extension pattern. |

### 2.6 `util/attachment/`
- `IafEntityAttachment<T extends Entity>` — interface: `tick(T)`, `isDirty()`, `markDirty()`.
- `NeedUpdateData<T extends Entity>` — abstract base implementing the dirty flag; the three attachment data classes extend it: `ChainData`, `MiscData`, `PortalData` (all in `data/component/`, each with `CODEC` + `PACKET_CODEC` and a static `get()` delegating to `ComponentManager`).

### 2.7 `util/trade/`
| File | Role |
|---|---|
| `TradeOfferHelper.java` | Public registration API (`registerVillagerOffers`, `registerWanderingTraderOffers`, deprecated `refreshOffers`). Header comment: **"From object builder api v1"** — this is a **copy of Fabric API's villager-api-v1** `TradeOfferHelper` ported into common. It works cross-loader because it operates on the vanilla `VillagerTrades.TRADES` / `WANDERING_TRADER_TRADES` maps. |
| `TradeOfferInternals.java` | The copied internals. Mutates `VillagerTrades.TRADES` and `WANDERING_TRADER_TRADES` directly with a synchronized register helper. Its `LoggerFactory.getLogger("fabric-villager-api-v1")` logger name is a Fabric-API leftover. |
| `factory/BuyWithPrice.java` | `VillagerTrades.ItemListing` impl producing `MerchantOffer` from `ItemCost` pairs (1.20.6+ `MerchantOffer`/`ItemCost` API — vanilla). |

### 2.8 Other notable common packages (context)
- `config/` — Jupiter `AutoInitConfigContainer`/`AutoInitConfigCategoryBase` with typed entries (`BooleanEntry`, `IntegerEntry`, `DoubleEntry`, `SeparatorEntry`). `IafCommonConfig.INSTANCE` + `IafClientConfig.INSTANCE` singletons.
- `compat/` — `delight` (Farmers Delight, guarded by `Platform.isModLoaded("farmersdelight")`), `emi`, `jade`, `jei`, `ponder` (uses `IntegrationExecutor.runWhenLoad`).
- `data/` — `BestiaryPage`, `DragonArmorMaterial`, `DragonArmorPart`, `DragonColor`, `DragonType`, `HippogryphType`, `IafSkullType` (enum `SkullBlock.Type`), `SeaSerpentType`, `TrollType`; `data/component/` = the 3 attachment data classes.
- `mixin/` — 11 mixins shared across loaders via `iceandfire.mixins.json` (4 common + 7 client). Client mixins use `@Environment(CLIENT)`.
- `world/` — `DangerousGeneration`, `DragonPosWorldData`, `feature/`, `processor/`, `structure/`.
- `util/` — `Color4i`, `DragonTypeProvider`, `IafMath`, `ItemRandomizer`, `RestrictWorldAccess` (a `ServerLevelAccessor` decorator that blocks world writes outside a predicate — no Architectury/loader deps).

---

## 3. `dev.architectury.*` API usage table (all in common)

| Architectury API | Files using it | Notes |
|---|---|---|
| `event.events.common.BlockEvent.BREAK` | `IceAndFire.java:89` | |
| `event.events.common.InteractionEvent.INTERACT_ENTITY` | `IceAndFire.java:90` | |
| `event.events.common.InteractionEvent.RIGHT_CLICK_BLOCK` | `IceAndFire.java:91` | |
| `event.events.common.EntityEvent.LIVING_DEATH` | `IceAndFire.java:92` | |
| `event.events.common.PlayerEvent.ATTACK_ENTITY` | `IceAndFire.java:93` | |
| `event.events.common.EntityEvent.LIVING_HURT` | `IceAndFire.java:98` | |
| `event.events.client.ClientTickEvent.CLIENT_POST` | `IceAndFireClient.java:40`, `IafKeybindings.java:20`, `PortalRenderHelper.java:4` | |
| `event.EventResult` | `ServerEvents.java:23` (+ used throughout) | |
| `networking.NetworkManager` | `ClientEvents.java:14,59,72`; `ServerEvents.java:24,207`; `ServerNetworkHelper.java:7,21-113`; `ClientNetworkHelper.java:11,25-88`; `entity/DragonBaseEntity.java:40`; `entity/MultipartPartEntity.java:4`; `item/ability/LightningMultihitAbility.java:5` | `registerReceiver`, `registerS2CPayloadType`, `sendToServer` |
| `platform.Platform` | `IceAndFire.java:20,32` (getMod→version); `DelightFoodItem.java:3,18` (isModLoaded); `TitleScreenRenderManager.java:6,121` (isFabric); `ServerNetworkHelper.java:8,20` (getEnvironment==Env.SERVER); `IafFeatures.java:6,67` (isNeoForge); `IafEntities.java:7,139` (isNeoForge) | |
| `registry.registries.DeferredRegister` | ~20 registry classes: Attributes, ArmorMaterials, Sounds, Blocks, BlockEntities, DataComponents, Entities, Features, ItemGroups, Items, Loots, Recipes, RecipeSerializers, Particles, Processors, ScreenHandlers, StatusEffects, StructurePieces, StructureTypes, Trades(×2) | `DeferredRegister.create(MOD_ID, Registries.X)` |
| `registry.registries.RegistrySupplier` | same classes + `data/DragonColor.java`, `data/SeaSerpentType.java`, `data/TrollType.java` | `.get()`, `.getId()`, `.listen(...)` |
| `registry.registries.DeferredSupplier` | `IafProcessors.java:9`, `IafTrades.java:8` | |
| `registry.CreativeTabRegistry` | `IafItemGroups.java:4`, `IafItems.java:15` | `create(...)`, `append(...)` |
| `registry.level.biome.BiomeModifications` | `IafFeatures.java:7,87`, `IafEntities.java:8,142-150` | `addProperties(context.hasTag(...), ...)` |
| `registry.level.entity.EntityAttributeRegistry` | `IafEntities.java:9,100-127` | `register(RegistrySupplier, bakeAttributes)` |
| `registry.menu.MenuRegistry` | `IafScreenHandlers.java:7,20-26`; `entity/HippogryphEntity.java:24`, `entity/HippocampusEntity.java:20`, `entity/DragonBaseEntity.java:42`; `item/BestiaryItem.java:10`; `item/block/DragonForgeInputBlock.java:11`, `DragonForgeBrickBlock.java:11`, `DragonForgeCoreBlock.java:10` | `ofExtended(...)` (cross-loader extended menus) |
| `registry.menu.ExtendedMenuProvider` | `entity/DragonBaseEntity.java:41`, `entity/HippogryphEntity.java:23`, `entity/HippocampusEntity.java:19`, `item/block/entity/DragonForgeBlockEntity.java:10` | |
| `registry.client.level.entity.EntityRendererRegistry` | `IafRenderers.java:24,44-95` | |
| `registry.client.rendering.BlockEntityRendererRegistry` | `IafRenderers.java:25,112-119` | |
| `registry.client.rendering.RenderTypeRegistry` | `IafRenderers.java:26,158-170` | |
| `registry.item.ItemPropertiesRegistry` | `IafRenderers.java:27,174-181` | |
| `registry.client.keymappings.KeyMappingRegistry` | `IafKeybindings.java:5,16-19` | |
| `core.item.ArchitecturySpawnEggItem` | `IafItems.java:14` | |
| `injectables.annotations.ExpectPlatform` | `impl/ComponentManager.java:6,11,16,21` | |
| `utils.Env` | `ServerNetworkHelper.java:9` | |
| `utils.value.IntValue` | `ServerEvents.java:25` | `onBreakBlock` xp param |

---

## 4. Load-specific leakage into common (migration hazards)

1. **`net.fabricmc.api.EnvType` / `@Environment(EnvType.CLIENT)`** in 12 common files:
   - `IceAndFireClient.java`, `event/ClientEvents.java`, `impl/ParticleProviderHolder.java`, `mixin/WorldRendererMixin.java`, `mixin/InGameHudMixin.java`, `render/SirenShaderRenderHelper.java`, `render/misc/ChainRenderer.java`, `render/PortalRenderHelper.java`, `registry/IafRenderers.java`.
   - This is by design (see `common/build.gradle` comment: Fabric-Loader `@Environment` gets remapped to the platform's annotation; "Do NOT use other classes from Fabric Loader"). It is only the annotation, so the migration risk is low, but it is the *only* Fabric import that exists in common.
   - **No `net.fabricmc.fabric.api.*` in common.** Verified by grep.
2. **`Platform.isNeoForge()`** gates — `IafFeatures.java:67` and `IafEntities.java:139` skip the Architectury `BiomeModifications` code on NeoForge because NeoForge instead loads datapack biome modifiers from `common/src/main/resources/data/iceandfire/neoforge/biome_modifier/*.json` (18 files). **This couples common code to "NeoForge is the alternate loader"** and to NeoForge's datapack `biome_modifier` format. If the target architecture drops NeoForge, both the gates and the JSONs must be handled.
3. **`Platform.isFabric()`** — `screen/TitleScreenRenderManager.java:121` makes a per-loader UI decision.
4. **`data/iceandfire/neoforge/` resources** — NeoForge-format `biome_modifier` datapack files ship in *common* resources; they only take effect on NeoForge. Load-specific resource leakage.
5. **`util/trade/TradeOfferInternals.java`** — a **copy of Fabric API villager-api-v1** code (logger name `"fabric-villager-api-v1"`, comment "From object builder api v1"). Functionally load-agnostic (mutates vanilla `VillagerTrades` maps) but the origin is Fabric API code living in common.
6. **NeoForge module is stale / Yarn-mapped** (the big one): `neoforge/src/main/java/com/iafenvoy/iceandfire/impl/neoforge/ComponentManagerImpl.java` imports `net.minecraft.entity.LivingEntity` and `net.minecraft.entity.player.PlayerEntity` (Yarn); `neoforge/.../IceAndFireNeoForge.java` imports `net.minecraft.entity.EntityType`, `net.minecraft.potion.Potions`. These are **Yarn mappings, not Mojmap** — the module cannot compile in this mojmap repo and must be rewritten from scratch rather than migrated.

---

## 5. Extension points / how loaders implement common contracts

There are **no `ServiceLoader`** usages. Only one `@ExpectPlatform`:

### 5.1 `ComponentManager` (`impl/ComponentManager.java`)
Common declares 3 `@ExpectPlatform static` methods. Architectury's transformer replaces them at compile time with the platform's `<impl-package>.<Class>Impl`:
- Fabric impl: `fabric/src/main/java/com/iafenvoy/iceandfire/impl/fabric/ComponentManagerImpl.java` → `entity.getAttachedOrCreate(IafAttachments.CHAIN_DATA)` etc.
- NeoForge impl: `neoforge/src/main/java/com/iafenvoy/iceandfire/impl/neoforge/ComponentManagerImpl.java` → `entity.getData(IafAttachments.CHAIN_DATA.get())` etc.

### 5.2 Attachment storage (loaders register the SAME common data classes)
- Data classes (`data/component/ChainData|MiscData|PortalData`) are fully in common (fields + `CODEC` + `PACKET_CODEC` + `tick()` logic + `get()` static via `ComponentManager`).
- **Fabric** registers them with Fabric API `AttachmentRegistry.create(...)` (`fabric/.../fabric/IafAttachments.java`) with `persistent(CODEC).syncWith(PACKET_CODEC, AttachmentSyncPredicate.all()).copyOnDeath()`, and ticks them from the common `CommonEvents.LIVING_TICK` custom event.
- **NeoForge** registers them with NeoForge `DeferredRegister`/`AttachmentType` (`neoforge/.../neoforge/IafAttachments.java`) and ticks from `EntityTickEvent.Post`.

### 5.3 Particle registration (callback pattern, not SPI)
Common `IafRenderers.registerParticleRenderers(Consumer<ParticleProviderHolder<?>>)` + `impl/ParticleProviderHolder.java`. The Fabric client supplies the consumer wiring it to Fabric API `ParticleFactoryRegistry`. A future loader supplies its own consumer.

### 5.4 Optional-mod integration via Integration lib
`IntegrationExecutor.runWhenLoad("modid", () -> Class::init)` is used in common for Ponder, in fabric for Trinkets, in neoforge for Curios + Ars Nouveau. This is the IAEnvoy `Integration` library abstraction (declared as `integration-common` in common, `include(integration-fabric)` in fabric).

### 5.5 Entrypoints / per-loader wiring
- **Fabric**: `fabric.mod.json` `entrypoints.main` → `IceAndFireFabric`, `client` → `IceAndFireFabricClient`, plus `emi`, `jade`, `modmenu` entrypoints. Mixins + accessWidener referenced from common (`iceandfire.mixins.json`, `iceandfire.accesswidener`).
- **NeoForge**: `@Mod` + `@EventBusSubscriber` in `IceAndFireNeoForge`. Note NeoForge calls `IceAndFireClient.init()` in the mod constructor but **never calls `IceAndFireClient.process()`** — client-only `process()` steps (screen gui registration, render layers, client network receivers) are missing on NeoForge (stale/broken).

---

## 6. `common/src/main/resources` — data/assets that do NOT need migration

Everything below is loader-neutral vanilla resource-pack content (JSON/data format identical across loaders) and requires no code migration:

- `assets/iceandfire/`: `blockstates/` (116), `models/` (866: block/item/tabula), `textures/` (1239), `lang/` (16 files: `en_us`, `de_de`, `es_ar`, `es_cl`, `es_ec`, `es_es`, `es_mx`, `fr_fr`, `ja_jp`, `ko_kr`, `pl_pl`, `pt_br`, `ru_ru`, `uk_ua`, `zh_cn` + `bestiary/` subdir), `particles/`, `sounds/` + `sounds.json` (41 KB), `splashes.txt`, `patchouli_books/`, `ponder/`, `shaders/`, `tinkers/` (TConstruct book), `textures/`.
- `data/iceandfire/`: `advancement/` (2), `banner_pattern/`, `damage_type/`, `dimension/` + `dimension_type/`, `loot_table/` (3), `pe_custom_conversions/` (ProjectE compat), `recipe/` (345), `structure/` (26), `tags/` (125), `worldgen/` (68).
- `data/minecraft/` — vanilla tag overrides (tool/mineable/entity_type/damage_type etc.).
- `data/c/` — common/Convention tags.
- `data/iceandfire/neoforge/` — **NeoForge-only** `biome_modifier/*.json` (18 files). This is the one resource area that is load-specific and is entangled with the `Platform.isNeoForge()` gates in §4.

Also in common resources: `iceandfire.mixins.json` (shared mixin config, 4 common + 7 client mixins) and `iceandfire.accesswidener` — both shared by fabric & neoforge modules (fabric copies the AW path via `loom.accessWidenerPath = project(":common").loom.accessWidenerPath`).

---

## Key risks for the migration
1. NeoForge module is Yarn-mapped and stale (biggest rewrite).
2. `Platform.isNeoForge()` gates + `data/iceandfire/neoforge/biome_modifier` JSONs hard-wire common behavior to the NeoForge loader's datapack world-gen system.
3. `@ExpectPlatform` (`ComponentManager`) and the particle `ParticleProviderHolder` callback are the only platform bridges — any new architecture must reproduce these two patterns.
4. Copied Fabric-API `util/trade/*` code in common (functional, but Fabric-derived).
5. Missing NeoForge client `process()` wiring.
