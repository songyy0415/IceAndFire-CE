# External-Compat Migration Research (EMI / Trinkets / Jade / Integration)

Migration scope: MC 1.21.1 Mojmap → MC 26.2 Mojmap for the IceAndFire-CE project.
Reference sources used (all on disk under `D:/aiminecraftdev`):
- EMI: `emi1.21.1` (mod 1.1.24 / MC 1.21.1) vs `emi26.2` (mod 1.1.24 / MC 26.2), source in `xplat/src/main/java/dev/emi/emi/api`.
- Trinkets: `trinkets1.21.1` (`dev.emi.trinkets.api`) vs `trinkets26.2` (`eu.pb4.trinkets.api`, mod id `trinkets_updated`, mod version 4.1.0-beta.3+26.2).
- Jade: `Jade1.21.1` (mod 15.10.6) vs `Jade26.2` (mod 26.2.10).
- Integration: `Integration` (single Loader-based lib, version 0.2 used by the project).

Legend: "identical" = same signature/behaviour across both versions; "改 (changed)" = break or rename requiring an edit.

---

## 1) EMI (`dev.emi.emi.api`)

### 1.1 Used symbols in the project

Files:
- `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/compat/emi/IceAndFireEmiPlugin.java` (lines 5–9 imports, 14–16 texture/stack construction, 19–23 `register`)
- `D:/IceAndFire-CE/common/src/main/java/com/iafenvoy/iceandfire/compat/emi/ForgeRecipeHolder.java` (lines 5–11 imports, 24–37 registration, 39–85 recipe widget impl)

Entrypoint: `fabric.mod.json` line 28–30 declares `"emi": ["com.iafenvoy.iceandfire.compat.emi.IceAndFireEmiPlugin"]` (verified: both EMI versions read the `"emi"` entrypoint key — `EmiAgnosFabric.java` uses `FabricLoader.getInstance().getEntrypointContainers("emi", EmiPlugin.class)` in both). **No change.**

### 1.2 Old → new mapping table (used symbols only)

| Old (EMI 1.21.1) | New (EMI 26.2) | Status |
|---|---|---|
| `dev.emi.emi.api.EmiEntrypoint` (annotation) | same package/class | identical |
| `dev.emi.emi.api.EmiPlugin` (`register(EmiRegistry)`) | same | identical |
| `dev.emi.emi.api.EmiRegistry.addCategory(...)` | same | identical |
| `dev.emi.emi.api.EmiRegistry.addWorkstation(...)` | same | identical |
| `dev.emi.emi.api.EmiRegistry.addRecipe(...)` | same | identical |
| `dev.emi.emi.api.EmiRegistry.getRecipeManager()` returning `net.minecraft.world.item.crafting.RecipeManager` | `EmiRegistry.getRecipeMap()` returning `net.minecraft.world.item.crafting.RecipeMap` | **改 (breaking)** |
| `recipeManager.getAllRecipesFor(RecipeType<T>)` → `List<RecipeHolder<T>>` | `recipeMap.byType(RecipeType<T>)` → `Collection<RecipeHolder<T>>` (also `values()` → `Collection<RecipeHolder<?>>`) | **改** |
| `dev.emi.emi.api.recipe.EmiRecipe` (getCategory/getId/getInputs/getOutputs/getDisplayWidth/getDisplayHeight/addWidgets) | same; `getBackingRecipe()` now typed `RecipeHolder<?>` (default method, not overridden by project) | identical for project |
| `dev.emi.emi.api.recipe.EmiRecipeCategory(Identifier, EmiRenderable, EmiRenderable)` ctor | same 3-arg ctor | identical |
| `dev.emi.emi.api.render.EmiTexture(Identifier, int u, int v, int width, int height)` ctor | same 5-arg ctor (also 7-arg with texture w/h) | identical |
| `dev.emi.emi.api.stack.EmiStack.of(ItemLike)` / `of(ItemStack)` | same factories (`of(ItemLike)`, `of(ItemStack, …)`) | identical |
| `dev.emi.emi.api.stack.EmiIngredient.of(Ingredient)` | same (`of(Ingredient)`, `of(Ingredient, amount)`) | identical |
| `dev.emi.emi.api.widget.WidgetHolder.addTexture(EmiTexture, x, y)` | same | identical |
| `dev.emi.emi.api.widget.WidgetHolder.addSlot(EmiIngredient, x, y)` | same | identical |
| `SlotWidget.large(boolean)` / `SlotWidget.recipeContext(EmiRecipe)` | same | identical |

### 1.3 Registration / API changes

- The only EMI API break that touches the project is `EmiRegistry.getRecipeManager()` → `EmiRegistry.getRecipeMap()`.
  - Project line: `ForgeRecipeHolder.java:34`
    ```java
    List<RecipeHolder<DragonForgeRecipe>> forgeRecipeList =
        registry.getRecipeManager().getAllRecipesFor(IafRecipes.DRAGON_FORGE_TYPE.get());
    ```
  - 26.2 replacement (matches EMI 26.2's own `VanillaPlugin.java:795-805` pattern):
    ```java
    RecipeMap map = registry.getRecipeMap();
    List<RecipeHolder<DragonForgeRecipe>> forgeRecipeList = map == null
        ? List.of() : List.copyOf(map.byType(IafRecipes.DRAGON_FORGE_TYPE.get()));
    ```
  - `byType` signature: `public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type)` (`D:/aiminecraftdev/minecraft26.2/net/minecraft/world/item/crafting/RecipeMap.java:36`).
  - Note: `getRecipeMap()` is `@Nullable` in EMI 26.2 (see `EmiRegistryImpl.java:54-56` / `EmiPort.java:174-175`); guard for null.
- EMI API package layout is byte-for-byte the same set of files in `dev.emi.emi.api` between both versions.
- Rendering types in the reference sources differ only by yarn-vs-mojmap (`DrawContext`→`GuiGraphicsExtractor`, `Text`→`Component`, `TooltipComponent`→`ClientTooltipComponent`); the project never references those, so no action.

### 1.4 Risks
- `getRecipeMap()` nullability must be handled (list may be empty during early reload).
- The `getRecipesFor(...)`/`byKey(...)` methods on `RecipeMap` also exist if more recipe lookups are added later.
- Project uses `modCompileOnly "dev.emi:emi-fabric:${emi_version}:api"` (common) and `modLocalRuntime` (fabric/neoforge). EMI 26.2 jars keep the same `:api` classifier.

---

## 2) Trinkets (`dev.emi.trinkets.api` → `eu.pb4.trinkets.api`)

**This is a complete rewrite** (Patbox "Trinkets Updated"). The old `dev.emi.trinkets.api` package no longer exists in 26.2; no compat shim is shipped. Mod id changed `trinkets` → `trinkets_updated`. CCA dependency removed.

### 2.1 Used symbols in the project

Files:
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/TrinketsRegistry.java` (line 4 import, line 13 `TrinketsApi.registerTrinket`)
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/SimpleTickItemWrapper.java` (lines 3–4 imports, line 9 `implements Trinket`, lines 16–19 `tick`)
- `D:/IceAndFire-CE/fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java:20` (`runWhenLoad("trinkets", …)`)
- Data files: `fabric/src/main/resources/data/trinkets/entities/iceandfire.json`, `.../slots/legs/belt.json`, `.../tags/item/legs/belt.json`
- Deps: `fabric/build.gradle:58-60` (modrinth trinkets + `dev.onyxstudios.cardinal-components-api:cardinal-components-base/entity:6.1.1`), `gradle.properties:17` `cca_version=6.1.1`

### 2.2 Old → new mapping table (used symbols only)

| Old (1.21.1, `dev.emi.trinkets.api`) | New (26.2, `eu.pb4.trinkets.api`) | Status |
|---|---|---|
| `TrinketsApi.registerTrinket(Item, Trinket)` | `TrinketCallback.setCallback(Item, TrinketCallback)` (`eu.pb4.trinkets.api.callback.TrinketCallback:48-50`) | **改** |
| `interface Trinket` (`tick(ItemStack, SlotReference, LivingEntity)`, `onEquip`, `onUnequip`, …) | `interface TrinketCallback` — same method set incl. `tick(ItemStack, TrinketSlotAccess, LivingEntity)` (`TrinketCallback:59-60`) | **改 (rename only)** |
| `SlotReference` (slot/inventory access) | `TrinketSlotAccess(TrinketInventory inventory, int index)` record (`api/TrinketSlotAccess.java`); `TrinketSlotReference` for sync ids | **改** |
| `TrinketsApi.getTrinketComponent(LivingEntity)` → `Optional<TrinketComponent>` (CCA-backed) | `TrinketsApi.getAttachment(LivingEntity)` → `TrinketAttachment` (`api/TrinketsApi.java:36-38`); also `TrinketsLivingEntityExtension.getTrinkets()` | **改** |
| `TrinketComponent` (CCA entity component) | `TrinketAttachment` (interface, non-CCA; `getInventory(slotId)`, `isEquipped`, `equipped`, `forEach`, …) | **改** |
| `TrinketsApi.registerTrinketPredicate(id, Function3<…,TriState>)` | `TrinketsApi.registerTrinketPredicate(id, TrinketPredicate)` (`test(ItemStack, TrinketSlotAccess, LivingEntity)` boolean) | **改** |
| `dev.onyxstudios.cca`/`org.ladysnake` Cardinal-Components (base + entity, 6.1.1) | CCA dropped; replaced by `dev.yumi.mc.core:yumi-mc-foundation:1.1.1+26.2` (`trinkets26.2/build.gradle:88`) | **改 (dependency removed)** |
| mod id `"trinkets"` | mod id `"trinkets_updated"` (`trinkets26.2/src/main/resources/fabric.mod.json:3`, `"name": "Trinkets Updated"`) | **改 (affects `runWhenLoad`)** |

### 2.3 Registration / data-format changes

- **Item registration** (project `TrinketsRegistry.java:13`):
  - Old: `TrinketsApi.registerTrinket(item, new SimpleTickItemWrapper(item));`
  - New: `TrinketCallback.setCallback(item, new SimpleTickItemWrapper(item));` where `SimpleTickItemWrapper implements eu.pb4.trinkets.api.callback.TrinketCallback` (method body unchanged; the tick call `this.item.inventoryTick(stack, entity.level(), entity, 0, false)` is vanilla and untouched).
- **Alternative (recommended in 26.2):** items can be made trinkets purely data-driven via the `trinkets:equipment` data component (`TrinketDataComponents.EQUIPMENT` / `TrinketEquippable.allowedSlots()`). The DEFAULT validator (`TrinketsMain.java:115-121`) accepts an item if it is in tag `trinkets:<group>/<slot>`, in tag `trinkets:all`, OR has a `TrinketEquippable` component listing the slot. The project may keep its tag-based approach, but the callback route is the drop-in for `SimpleTickItemWrapper`.
- **Entity data** `data/trinkets/entities/iceandfire.json` (`{"entities":["player"],"slots":["legs/belt"]}`): format unchanged and still parsed in 26.2 (`EntitySlotLoader.java:54-124`, slots parsed as `"group/slot"`, `#` prefix = entity tag). **No change.**
- **Slot data** `data/trinkets/slots/legs/belt.json` (`{"amount":1}`): `amount` still supported (`SlotLoader.java:165`). 26.2 slot files typically also set `icon`, `order`, `validator_predicates` — an empty `validator_predicates` falls back to the DEFAULT predicate, so the tag still gates items. **Compatible, cosmetic gap only (no icon).**
- **Item tag** `data/trinkets/tags/item/legs/belt.json` (`iceandfire:hydra_heart`): still honoured — DEFAULT/TAG predicates build `TagKey.create(Registries.ITEM, "trinkets", slot.getId())` i.e. `trinkets:legs/belt` (`TrinketsMain.java:117,125`; data namespace constant `TrinketsMain.NAMESPACE = "trinkets"`). **No change.**
- **Entity integration** (was CCA `TrinketComponent` on `LivingEntity`): now `TrinketsApi.getAttachment(entity)` or `((TrinketsLivingEntityExtension) entity).getTrinkets()` (`api/ext/TrinketsLivingEntityExtension.java:12-14`); equipped-slot queries via `TrinketAttachment.isEquipped/equipped/forEach`. No CCA usage in the project today, so no project code change beyond the registration.

### 2.4 Risks
- **`Item.inventoryTick` signature changed in 26.2 — breaks `SimpleTickItemWrapper.java:18`** (cross-cutting MC-API change landing inside the Trinkets wrapper):
  - 1.21.1 mojmap: `Item.inventoryTick(ItemStack, Level, Entity, int slotId, boolean isSelected)` (`minecraft1.21.1mojmap/.../Item.java:254`)
  - 26.2 mojmap: `Item.inventoryTick(ItemStack, ServerLevel, Entity owner, @Nullable EquipmentSlot slot)` (`minecraft26.2/.../Item.java:289`)
  - Project call `this.item.inventoryTick(stack, entity.level(), entity, 0, false);` no longer compiles. Since the new param is `ServerLevel` and `TrinketCallback.tick` fires on both sides, the replacement needs a client/server guard, e.g. `if (entity.level() instanceof ServerLevel serverLevel) this.item.inventoryTick(stack, serverLevel, entity, null);` — the client-side behaviour (and the correct `EquipmentSlot`, e.g. `EquipmentSlot.BODY`/`null`) is **待确认 (to be confirmed)** at target build time.
- Mod id change `"trinkets"` → `"trinkets_updated"` affects `IceAndFireFabric.java:20` — `IntegrationExecutor.runWhenLoad("trinkets", …)` will silently do nothing unless the id is updated.
- CCA lines in `fabric/build.gradle:59-60` (`dev.onyxstudios.cardinal-components-api` 6.1.1) become dead deps for Trinkets. Note the project's groupId (`dev.onyxstudios.cardinal-components-api`) differs from what Trinkets 1.21.1 declares (`org.ladysnake.cardinal-components-api`, `trinkets1.21.1/build.gradle:48-49`) — the project never imports CCA classes directly (grep of `common/`, `fabric/`, `neoforge/` sources finds no `dev.onyxstudios.cca`/`ladysnake` imports), so the CCA entries were purely transitive for Trinkets and can be dropped. (Verify against the 26.2 build's exact Yumi/loader coordinates before removing.)
- 26.2 Trinkets requires MC 26.2 + newer Fabric loader (0.19.3); the old `maven.modrinth:trinkets:JagCscwi` coordinate targets 1.21.1 and must be re-resolved for 26.2.
- If the project ever wants per-slot model rendering in 26.2, that is the new `eu.pb4.trinkets.api.client.renderer` package (`ClientTrinket`, `TrinketRendererRegistry`, elements) — not used today.

---

## 3) Jade (`snownee.jade.api`)

### 3.1 Used symbols in the project

Files (all in `common/.../compat/jade/`):
- `IceAndFireJadePlugin.java` (lines 5–7 imports, line 9 `@WailaPlugin`, lines 12–20 `registerClient`)
- `DragonComponentProvider.java` (lines 8–11 imports, `getUid` 17–19, `appendTooltip` 22–28)
- `DragonEggBlockComponentProvider.java` (uses `IBlockComponentProvider`, `BlockAccessor.getBlockEntity`)
- `DragonEggEntityComponentProvider.java` (uses `IEntityComponentProvider`, `EntityAccessor.getEntity`)
- `MultipartComponentProvider.java` (lines 13–18 imports incl. `snownee.jade.impl.ui.ArmorElement`/`HealthElement`; lines 34–37 `clear`/`addAll`/`add(HealthElement)`/`add(ArmorElement)`)

Entrypoint: `fabric.mod.json:31-33` `"jade": ["…IceAndFireJadePlugin"]` (Jade reads the `"jade"` entrypoint key in both versions; `fabric.mod.json` in both Jade refs lists it). **No change.**

### 3.2 Old → new mapping table (used symbols only)

| Old (Jade 15.10.6 / MC 1.21.1) | New (Jade 26.2.10 / MC 26.2) | Status |
|---|---|---|
| `snownee.jade.api.WailaPlugin` (annotation) | same | identical |
| `IWailaPlugin.registerClient(IWailaClientRegistration)` | same | identical |
| `IWailaClientRegistration.registerBlockComponent(provider, Class<? extends Block>)` | same | identical |
| `IWailaClientRegistration.registerEntityComponent(provider, Class<? extends Entity>)` | same | identical |
| `IEntityComponentProvider extends IComponentProvider<EntityAccessor>` | same | identical |
| `IBlockComponentProvider extends IComponentProvider<BlockAccessor>` | same | identical |
| `IComponentProvider.getUid()` → `ResourceLocation`; `appendTooltip(ITooltip, T accessor, IPluginConfig)` | same | identical |
| `EntityAccessor.getEntity()` / `BlockAccessor.getBlockEntity()` | same | identical |
| `ITooltip.add(Component)` / `addAll(List<Component>)` / `clear()` | same | identical |
| `IPluginConfig` (passed through, `get(ResourceLocation)`) | same (+ default `get(IToggleableProvider)`) | identical |
| `snownee.jade.impl.ui.ArmorElement(float armor)` | same ctor | identical |
| `snownee.jade.impl.ui.HealthElement(float maxHealth, float health)` | `HealthElement(Hud.HeartType heartType, float maxHealth, float health, float absorption)` | **改 (breaking ctor)** |
| `Element` base → `iTooltip.add(IElement)` | `Element` (now `implements Renderable, LayoutElement, …`); `ITooltip.add(LayoutElement)` | identical for project (HealthElement/ArmorElement extend `Element`) |

### 3.3 Registration / tooltip changes

- Server/common registration (`IWailaCommonRegistration`) unchanged in signature (`registerBlockDataProvider`, `registerEntityDataProvider`); only new members added (`blockOperations()`, `entityTypeOperations()`). Project does not use server registration — no change.
- **The single break: `HealthElement` constructor.** Project `MultipartComponentProvider.java:36`:
  ```java
  iTooltip.add(new HealthElement(mob.getMaxHealth(), mob.getHealth()));
  ```
  Must become (4-arg):
  ```java
  iTooltip.add(new HealthElement(Gui.HeartType.CONTAINER, mob.getMaxHealth(), mob.getHealth(), mob.getAbsorptionAmount()));
  ```
  - Reference source (yarn) shows `Hud.HeartType` (`Jade26.2/.../impl/ui/HealthElement.java:36`); mojmap name of the heart-type enum is **待确认 (to be confirmed)** — in 1.21.1 mojmap it is `net.minecraft.client.gui.Gui.HeartType`; the 26.2 mojmap equivalent (`Gui.HeartType` or a relocated type) must be checked at compile time.
  - `ArmorElement(float)` is unchanged; `iTooltip.add(...)` accepts these `Element` subclasses in both versions because `ITooltip.add(LayoutElement)` (26.2) is satisfied by `Element implements LayoutElement`.
- Jade 26.2 uses Classtweaker (`jade.classtweaker` in its fabric.mod.json) instead of AccessWidener — internal; no impact on the project's compile-time API usage.

### 3.4 Risks
- `HealthElement` is an `impl` class (`snownee.jade.impl.ui`) — using impl classes is unsupported API and could change again; consider replacing the health bar with plain `Component` text (e.g. `iTooltip.add(Component.literal((int) mob.getHealth() + "/" + (int) mob.getMaxHealth()))`) to avoid the dependency on the unstable ctor. If keeping the icon, verify the heart-type enum's mojmap name for 26.2.
- Mod version: Jade 1.21.1 = 15.10.6; Jade 26.2 = 26.2.10. Mod id `jade` unchanged. Coordinate in `common/build.gradle:26` (`maven.modrinth:jade:pA0xvozk`) is 1.21.1-specific and must be re-resolved; neoforge uses `JkFFfEao` (`neoforge/build.gradle:46`).

---

## 4) Integration library (`com.iafenvoy.integration`)

### 4.1 What the library is

Loader-based, MC-version-independent helper for optional-mod integration. Source: `D:/aiminecraftdev/Integration` (single version). It depends only on Architectury (`dev.architectury.injectables.annotations.ExpectPlatform`) and loader APIs (`FabricLoader`, `ModList`) — the only non-JDK import in common is `com.mojang.logging.LogUtils` (log4j, MC-version independent). No Minecraft gameplay classes. **Compiles identically against MC 1.21.1 and 26.2.**

Public API (`common/src/main/java/com/iafenvoy/integration/IntegrationExecutor.java`):
- `static void runWhenLoad(String modId, Supplier<Runnable> supplier)` — runs `supplier.get().run()` iff `Proxy.isModLoaded(modId)`.
- `static <T> Optional<T> getWhenLoad(String modId, Supplier<Supplier<T>> supplier)` — returns value iff mod loaded.
- `static <T> T getWhenLoad(String modId, Supplier<Supplier<T>> supplier, Supplier<T> defaultValue)` — as above with a fallback.

Supporting types (unused by project, documented for completeness):
- `Proxy` (`@ExpectPlatform isModLoaded`/`collectEntryPoints`), `ProxyImpl` on fabric (FabricLoader) and neoforge (ModList + ASM annotation scan).
- `entrypoint.IntegrationEntryPoint` (marker), `EntryPointProvider` (annotation with `slug`), `EntryPointManager`.
- `Integration` (mod id `"integration"`, logger), `util.ReflectUtil`.

### 4.2 Project usage (verified compiles as-is)

| File:line | Call | Verdict on 26.2 |
|---|---|---|
| `fabric/.../IceAndFireFabric.java:20` | `IntegrationExecutor.runWhenLoad("trinkets", () -> TrinketsRegistry::registerItems)` | Compiles as-is; **runtime no-op unless mod id updated to `"trinkets_updated"`** (Trinkets-side change, not Integration) |
| `common/.../IceAndFireClient.java:27` | `IntegrationExecutor.runWhenLoad("ponder", () -> IceAndFirePonderPlugin::init)` | Compiles as-is |
| `neoforge/.../IceAndFireNeoForge.java:34` | `IntegrationExecutor.runWhenLoad("ars_nouveau", () -> IceAndFireArsNouveauCompat::init)` | Compiles as-is |
| `neoforge/.../IceAndFireNeoForge.java:35` | `IntegrationExecutor.runWhenLoad("curios", () -> CuriosRegistry::registerItems)` | Compiles as-is |
| `common/.../entity/DragonBaseEntity.java:1647` | `IntegrationExecutor.getWhenLoad("ponder", () -> () -> this.level() instanceof SchematicLevel, () -> false)` | Compiles as-is |

Dependency coordinates (unchanged shape; versions re-resolve as-is): `integration_version=0.2` (`gradle.properties:22`); `com.github.IAFEnvoy.Integration:integration-common` (common), `-fabric` (fabric, `include(...)`), `-neoforge` (neoforge, `include(...)`).

### 4.3 Risks
- None from the Integration library itself. The only migration-relevant item is the *string* mod-id passed to `runWhenLoad`/`getWhenLoad` for Trinkets (`"trinkets"` → `"trinkets_updated"`). Verify the Curios mod id (26.2 `curios`) and Ponder id stay `"curios"`/`"ponder"` at target build time.

---

## Cross-cutting notes

- All four libraries use the mojmap API names the project already targets (`ResourceLocation`, `RecipeHolder`, `Component`, `Ingredient`, `ItemStack`); the reference sources at `D:/aiminecraftdev` mix yarn (`emi26.2`, `trinkets26.2`, `Jade26.2` use yarn `Identifier`/`Hud`/`GuiGraphicsExtractor`) and mojmap (`emi1.21.1`, `Jade1.21.1`), so only *signatures*, not import names, should be compared when reading those sources.
- Compile-classpath coordinates in the project that are MC-version-pinned and must be re-resolved for 26.2: `emi_version` (common build.gradle:23, fabric build.gradle:52, neoforge build.gradle:44), Jade `maven.modrinth:jade` (common:26, neoforge:46), Trinkets `maven.modrinth:trinkets` (fabric:58). `integration_version=0.2` is not MC-pinned.
