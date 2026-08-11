# Compatibility Integrations Analysis (EMI / JEI / Jade / Ponder / Delight / Trinkets / Integration)

Project: IceAndFire-CE, MC 1.21.1 Mojmap + Architectury (common/fabric/neoforge).
Branch: `1.21.1-arch-old`. Migration target: MC 26.2.

All file paths below are absolute under `D:/IceAndFire-CE` unless noted.

---

## 1. Module inventory

| Module | Package | Files | External APIs imported | Entry point / registration | MC classes touched |
|---|---|---|---|---|---|
| Delight | `com.iafenvoy.iceandfire.compat.delight` | `DelightFoodItem.java` | `dev.architectury.platform.Platform` only (no Farmer's Delight import!) | None (a plain `Item` subclass) | `net.minecraft.world.item.Item`, `ItemStack`, `TooltipFlag`, `net.minecraft.network.chat.Component` |
| EMI | `com.iafenvoy.iceandfire.compat.emi` | `IceAndFireEmiPlugin.java`, `ForgeRecipeHolder.java` | `dev.emi.emi.api.*` | `@EmiEntrypoint` annotation + fabric.mod.json entrypoint key `"emi"` | `ResourceLocation`, `RecipeHolder`, `IafBlocks`, `IafRecipes` |
| JEI | `com.iafenvoy.iceandfire.compat.jei` | `IceAndFireJeiPlugin.java`, `DragonForgeRecipeCategory.java`, `FireDragonForgeRecipeCategory.java`, `IceDragonForgeRecipeCategory.java`, `LightningDragonForgeRecipeCategory.java` | `mezz.jei.api.*` | `@JeiPlugin` annotation (fabric.mod.json has NO `"jei"` entrypoint key — see §5 note) | `GuiGraphics`, `Screen`, `RecipeHolder`, `RecipeManager`, `Minecraft`, `IafBlocks`, `IafRecipes`, `BestiaryScreen` |
| Jade | `com.iafenvoy.iceandfire.compat.jade` | `IceAndFireJadePlugin.java`, `DragonComponentProvider.java`, `DragonEggBlockComponentProvider.java`, `DragonEggEntityComponentProvider.java`, `MultipartComponentProvider.java` | `snownee.jade.api.*`, `snownee.jade.api.config.IPluginConfig`, `snownee.jade.impl.ui.ArmorElement`, `snownee.jade.impl.ui.HealthElement` | `@WailaPlugin` annotation + fabric.mod.json entrypoint key `"jade"` | `DragonEggBlock`, `Minecraft`, `Level.entityStorage`, `Mob`, `Entity`, `Component` |
| Ponder | `com.iafenvoy.iceandfire.compat.ponder` | `IceAndFirePonderPlugin.java`, `DragonForgeStoryBoard.java` | `net.createmod.ponder.*` (`api.scene.*`, `api.registration.*`, `foundation.PonderIndex`), `com.iafenvoy.uranus.util.RandomHelper` | `PonderIndex.addPlugin(...)` invoked from `IceAndFireClient.init()` via `IntegrationExecutor.runWhenLoad("ponder", ...)` (line 27) | `Minecraft`, `BlockPos`, `Direction`, `ResourceLocation`, `Level`, `Entity`, `DragonForgeBrickBlock`, `DragonBaseEntity` |
| Trinkets (fabric) | `com.iafenvoy.iceandfire.fabric.compat.trinkets` | `TrinketsRegistry.java`, `SimpleTickItemWrapper.java` | `dev.emi.trinkets.api.*` (`Trinket`, `SlotReference`, `TrinketsApi`) | `IntegrationExecutor.runWhenLoad("trinkets", ...)` in `IceAndFireFabric.onInitialize()` (line 20) | `Item`, `ItemStack`, `LivingEntity`, `IafItems` |
| Integration (lib) | `com.iafenvoy.integration` (from `com.github.IAFEnvoy.Integration:integration-common/fabric/neoforge`) | external lib | n/a | `IntegrationExecutor.runWhenLoad` / `getWhenLoad` | n/a |

Reference source dirs used: `D:/aiminecraftdev/emi1.21.1`, `D:/aiminecraftdev/emi26.2`, `D:/aiminecraftdev/Jade1.21.1`, `D:/aiminecraftdev/Jade26.2`, `D:/aiminecraftdev/trinkets1.21.1`, `D:/aiminecraftdev/trinkets26.2`, `D:/aiminecraftdev/Ponder`, `D:/aiminecraftdev/Integration`, `D:/aiminecraftdev/minecraft1.21.1mojmap`, `D:/aiminecraftdev/minecraft26.2`.

---

## 2. Per-module detail

### 2.1 Delight — `common/.../compat/delight/DelightFoodItem.java`
- Pure `Item` subclass; the ONLY optional-compat behavior is a tooltip shown when Farmer's Delight is absent:
  ```java
  if (!Platform.isModLoaded("farmersdelight"))
      tooltip.add(Component.translatable("item.iceandfire.tooltip.require.delight"));
  ```
  (lines 15-20). It does **not** import any `vectorwing.farmersdelight.*` class.
- Used as the item class for 5 registry entries in `common/.../registry/IafItems.java` lines 201-205:
  `COOKED_RICE_WITH_FIRE_DRAGON_MEAT`, `COOKED_RICE_WITH_ICE_DRAGON_MEAT`, `COOKED_RICE_WITH_LIGHTNING_DRAGON_MEAT`, `GHOST_CREAM`, `PIXIE_DUST_MILKY_TEA`.
- **Optionality:** only `dev.architectury.platform.Platform.isModLoaded` at runtime — no compile dep on FD at all. `farmersdelight` is not declared anywhere in `build.gradle` for this module (no compileOnly), so it's purely runtime-detection. **26.2 impact: minimal.** Only `Platform.isModLoaded` (Architectury API) must keep working; MC class surface (`Item.appendHoverText(TooltipContext,...)`) unchanged.

### 2.2 EMI — `common/.../compat/emi/`
- `IceAndFireEmiPlugin.java`: `@EmiEntrypoint public class IceAndFireEmiPlugin implements EmiPlugin`. Builds 3 static `ForgeRecipeHolder` (fire/ice/lightning), each with an `EmiRecipeCategory`, `EmiTexture`, and `EmiStack` workstation (the three `DRAGONFORGE_*_CORE` blocks). `register(EmiRegistry)` just calls `FIRE/ICE/LIGHTNING.register(registry)`.
- `ForgeRecipeHolder.java`: `register(EmiRegistry)` does:
  - `registry.addCategory(category)`
  - `registry.addWorkstation(category, workstation)`
  - **recipe iteration:** `registry.getRecipeManager().getAllRecipesFor(IafRecipes.DRAGON_FORGE_TYPE.get())` (line 34) → filters by `entry.value().getDragonType()` → `registry.addRecipe(new DragonForgeEmiRecipe(...))`.
  - Inner class `DragonForgeEmiRecipe implements EmiRecipe`: `getCategory`, `getId` (returns `entry.id()`), `getInputs` (`EmiIngredient.of(getInput())`, `EmiIngredient.of(getBlood())`), `getOutputs` (`EmiStack.of(getResultItem())`), `getDisplayWidth()=176`, `getDisplayHeight()=1120` (sic — 1120 is a bug-looking value), `addWidgets` (`addTexture(texture,3,4)`, three `addSlot(...)` with `.large(true).recipeContext(this)`).
- **Optionality:** common declares `modCompileOnly "dev.emi:emi-fabric:${emi_version}:api"`; fabric declares `modLocalRuntime "dev.emi:emi-fabric:${emi_version}"`. Not bundled. The `"emi"` entrypoint key + `@EmiEntrypoint` means the class is only touched by Fabric Loader when EMI is installed, so missing `dev.emi` classes never load.
- **EMI API usage vs 1.21.1 reference:** all of `EmiRegistry.addCategory/addWorkstation/addRecipe`, `EmiRecipeCategory` 3-arg ctor, `EmiTexture` 5-arg ctor, `EmiIngredient.of(Ingredient)`, `EmiStack.of(ItemLike)`, `EmiRecipe` methods, `WidgetHolder.addTexture(EmiTexture,int,int)` / `addSlot(...).large(bool).recipeContext(...)`, `SlotWidget` methods all present in `D:/aiminecraftdev/emi1.21.1` unchanged.

### 2.3 JEI — `common/.../compat/jei/`
- `IceAndFireJeiPlugin.java`: `@JeiPlugin public class IceAndFireJeiPlugin implements IModPlugin` (by jdkdigital).
  - `getPluginUid()` = `iceandfire:iceandfire`.
  - 3 static `RecipeType<DragonForgeRecipe>`: `FIRE`/`ICE`/`LIGHTNING` via `RecipeType.create(ResourceLocation.DEFAULT_NAMESPACE, "firedragonforge", DragonForgeRecipe.class)` (note: uses `minecraft:` namespace, not `iceandfire:`).
  - `registerCategories` → `new FireDragonForgeRecipeCategory(guiHelper)` etc.
  - `registerRecipeCatalysts` → `addRecipeCatalyst(IafBlocks.DRAGONFORGE_*_CORE.get(), type)`.
  - `registerRecipes` → `Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(...)`, buckets by `recipe.value().getDragonType()` string switch, `registration.addRecipes(type, list)`.
  - `registerGuiHandlers` → `addGuiScreenHandler(BestiaryScreen.class, ...)` with an anonymous `IGuiProperties` returning `screenClass/guiLeft/guiTop/guiXSize/guiYSize/screenWidth/screenHeight`.
- `DragonForgeRecipeCategory.java`: `abstract ... extends AbstractRecipeCategory<DragonForgeRecipe>` (by jdkdigital). Super ctor `(RecipeType, Component title, IDrawable icon, 170, 79)`. `background` from `guiHelper.drawableBuilder(...).setTextureSize(256,256).build()`; `overlay` from `guiHelper.createAnimatedDrawable(drawable, 50, IDrawableAnimated.StartDirection.LEFT, false)`. `draw(recipe, IRecipeSlotsView, GuiGraphics, mouseX, mouseY)`; `setRecipe(IRecipeLayoutBuilder, recipe, IFocusGroup)` adds INPUT slots (with `.setStandardSlotBackground()`) and OUTPUT slot (`.addItemStack(...)`).
- The three concrete categories (`FireDragonForgeRecipeCategory` etc.) pass `guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(block))` as the icon.
- **Optionality:** common declares `modCompileOnly "mezz.jei:jei-${minecraft_version}-fabric-api:${jei_version}"` (jei 19.21.0.247). No runtime bundling. **fabric.mod.json has NO `"jei"` entrypoint key** (only `main`, `client`, `emi`, `jade`, `modmenu`). JEI's plugin discovery relies on the `"jei"` fabric entrypoint OR annotation scanning; with neither declared on Fabric, the JEI plugin may be silently inert on the Fabric side (verify at runtime). On NeoForge `@JeiPlugin` annotation scanning applies.
- **26.2 impact:** no JEI 26.2 reference source on disk → JEI API surface for 26.2 = 待确认. The JEI 19.21.0.247 API here uses `net.minecraft.resources.ResourceLocation` and `net.minecraft.client.gui.GuiGraphics`, both renamed in 26.2; a 26.2 JEI build must exist and its API ported accordingly.

### 2.4 Jade — `common/.../compat/jade/`
- `IceAndFireJadePlugin.java`: `@WailaPlugin public class IceAndFireJadePlugin implements IWailaPlugin`; `registerClient(IWailaClientRegistration)`:
  - `registerBlockComponent(DragonEggBlockComponentProvider.INSTANCE, DragonEggBlock.class)`
  - `registerEntityComponent(DragonEggEntityComponentProvider.INSTANCE, DragonEggEntity.class)`
  - `registerEntityComponent(DragonComponentProvider.INSTANCE, FireDragonEntity.class)` (+ Ice, Lightning)
  - `registerEntityComponent(MultipartComponentProvider.INSTANCE, MultipartPartEntity.class)`
- `DragonComponentProvider` (enum INSTANCE, `IEntityComponentProvider`): `getUid()` = `iceandfire:dragon`; appends stage/age/gender text for `DragonBaseEntity`.
- `DragonEggBlockComponentProvider` (`IBlockComponentProvider`): `getUid()` = `iceandfire:dragon_egg_block`; hatch-time text via `IafCommonConfig.INSTANCE.dragon.eggBornTime.getValue() - egg.age`.
- `DragonEggEntityComponentProvider` (`IEntityComponentProvider`): `getUid()` = `iceandfire:dragon_egg_entity`; hatch-time text via `... - egg.getDragonAge()`.
- `MultipartComponentProvider` (`IEntityComponentProvider`): `getUid()` = `iceandfire:multipart`; for a `MultipartPartEntity` it resolves the parent through `Minecraft.getInstance().level.entityStorage.getEntityGetter().get(multipart.getParentId())`, then `iTooltip.clear()`, `iTooltip.addAll(mob.getDisplayName().toFlatList(Style.EMPTY.withColor(ChatFormatting.WHITE)))`, `iTooltip.add(new HealthElement(mob.getMaxHealth(), mob.getHealth()))`, `iTooltip.add(new ArmorElement(mob.getArmorValue()))`, plus dragon extras. Imports the internal `snownee.jade.impl.ui.HealthElement` / `ArmorElement`.
- **Optionality:** Jade is declared `modImplementation` (common line 26, fabric line 55, neoforge line 46) — it is a hard (non-optional) dependency in the build, but loading the plugin class is deferred through the `"jade"` fabric entrypoint + `@WailaPlugin`. On NeoForge `@WailaPlugin` class-scan requires the `jade` mod present (mod id check inside Jade).

### 2.5 Ponder — `common/.../compat/ponder/`
- `IceAndFirePonderPlugin.java`: implements `PonderPlugin`; `getModId()` returns `iceandfire`; `registerScenes(PonderSceneRegistrationHelper<ResourceLocation>)` maps the three `DRAGONFORGE_*_CORE_DISABLED` blocks to `DragonForgeStoryBoard`; `registerTags(...)` registers a tag `iceandfire:dragon_forge` with `.item(...).title(...).description(...).register()` and `addToTag(...).add(blockIds...)`; static `init()` calls `PonderIndex.addPlugin(new IceAndFirePonderPlugin())`.
- `DragonForgeStoryBoard.java`: implements `PonderStoryBoard`; `program(SceneBuilder scene, SceneBuildingUtil util)` uses `scene.title`, `scene.configureBasePlate`, `scene.removeShadow`, `scene.world().showSection/modifyBlock/createEntity/modifyEntities`, `scene.overlay().showText(...).attachKeyFrame().placeNearTarget().pointAt(...)`, `util.select().layer(n)`, `util.vector().blockSurface(...)`, `scene.rotateCameraY(-45)`, `scene.idle(...)`. Uses `com.iafenvoy.uranus.util.RandomHelper` for variant pick. Calls into internal dragon API (`setGender`, `setVariant`, `setPosRaw`, `setAgeInDays`, `setOrderedToSit`, `setAnimation`, `dragonType.colors()`).
- **Optionality:** common `modImplementation "net.createmod.ponder:Ponder-Fabric-...:1.0.61"`, fabric `modCompileOnly ...` (line 62), neoforge `modCompileOnly ...` (line 53). Registration is deferred: `IceAndFireClient.init()` line 27:
  ```java
  IntegrationExecutor.runWhenLoad("ponder", () -> IceAndFirePonderPlugin::init);
  ```
  `DragonBaseEntity` line 1647 also uses the lazy-indirection trick to avoid loading ponder classes unless loaded:
  ```java
  if (!IntegrationExecutor.getWhenLoad("ponder", () -> () -> this.level() instanceof SchematicLevel, () -> false)) { ... }
  ```
  (`net.createmod.catnip.levelWrappers.SchematicLevel` imported at line 43 of `DragonBaseEntity.java` — inside the inner supplier so it is resolved only under ponder.)
- **Reference:** the on-disk Ponder source (`D:/aiminecraftdev/Ponder`) is itself **1.21.1** (gradle.properties `minecraft_version = 1.21.1`, `mod_version = 1.0`); `PonderIndex.addPlugin` exists there (line 41). **No 26.2 Ponder/Create reference on disk** → 26.2 Ponder API = 待确认.

### 2.6 Trinkets (fabric) — `fabric/.../fabric/compat/trinkets/`
- `TrinketsRegistry.java`: `registerItems()` registers `IafItems.HYDRA_HEART.get()` via `TrinketsApi.registerTrinket(item, new SimpleTickItemWrapper(item))`.
- `SimpleTickItemWrapper.java`: `implements dev.emi.trinkets.api.Trinket`; `tick(ItemStack stack, SlotReference slot, LivingEntity entity)` calls `this.item.inventoryTick(stack, entity.level(), entity, 0, false)`.
- Data files (dev.emi.trinkets format), all under `fabric/src/main/resources/data/trinkets/`:
  - `entities/iceandfire.json` → assigns slots `["legs/belt"]` to `["player"]`.
  - `slots/legs/belt.json` → `{"amount": 1}`.
  - `tags/item/legs/belt.json` → item tag `iceandfire:hydra_heart`.
- **Optionality:** fabric `modApi "maven.modrinth:trinkets:JagCscwi"` + CCA `dev.onyxstudios.cardinal-components-api:cardinal-components-base/entity:6.1.1` (trinkets' transitive deps). Registration deferred via `IntegrationExecutor.runWhenLoad("trinkets", () -> TrinketsRegistry::registerItems)` in `IceAndFireFabric.onInitialize()` (line 20) — the `dev.emi.trinkets` classes are only linked when trinkets is present.

### 2.7 Integration library (com.iafenvoy.integration)
- Consumed as `modImplementation(include("com.github.IAFEnvoy.Integration:integration-fabric:0.2"))` (fabric), `integration-common:0.2` (common), `integration-neoforge:0.2` (neoforge). `include(...)` = bundled/shadowed into the jar.
- `IntegrationExecutor` (`D:/aiminecraftdev/Integration/common/.../IntegrationExecutor.java`):
  - `runWhenLoad(String modId, Supplier<Runnable>)` → `if (Proxy.isModLoaded(modId)) supplier.get().run();`
  - `getWhenLoad(String modId, Supplier<Supplier<T>>)` → optional; `getWhenLoad(..., defaultValue)` → orElse.
- `Proxy.isModLoaded` / `Proxy.collectEntryPoints` are `@ExpectPlatform` (Architectury). Fabric impl = `FabricLoader.getInstance().isModLoaded` / `getEntrypoints`. NeoForge impl in `IntegrationNeoForge`.
- **26.2 impact:** the Integration lib is a pre-built 0.2 binary. `@ExpectPlatform` requires Architectury annotation processing per platform. Needs a 26.2-compatible Integration build (verify availability) or the pattern should be replaced with plain `Platform.isModLoaded` / Fabric Loader calls.

---

## 3. How optionality works (the patterns to preserve for 26.2)

1. **Entrypoint indirection (EMI, Jade, JEI-on-NeoForge).** The plugin class is referenced from `fabric.mod.json` under a foreign key (`"emi"`, `"jade"`) that only the host mod (EMI/Jade) requests via `FabricLoader.getEntrypoints(...)`. If the host is absent, the class is never loaded → unresolved `dev.emi.*`/`snownee.jade.*` refs never trigger `NoClassDefFoundError`. Compile-time the dep is `modCompileOnly` (EMI, JEI) so it is not bundled.
2. **`IntegrationExecutor.runWhenLoad("modid", ...)` (Ponder, Trinkets, Curios, Ars Nouveau).** Runtime `Proxy.isModLoaded` gate around `Supplier<Runnable>`; the runnable's body references the external API, so it is only linked when the mod is installed. `getWhenLoad("ponder", ...)` (in `DragonBaseEntity`) adds the same laziness for an inline boolean check.
3. **Platform.isModLoaded (Delight).** No external API class at all — only Architectury `Platform.isModLoaded("farmersdelight")` in tooltip logic.
4. **Fabric entrypoint keys declared in `fabric/src/main/resources/fabric.mod.json`:** `main`, `client`, `emi`, `jade`, `modmenu`. **`jei` is absent** (see §5 note).
5. **NeoForge `modCompileOnly` deps** for Ponder/Curios/ProjectE/Ars Nouveau/GeckoLib keep those classes off the runtime classpath; `IntegrationExecutor.runWhenLoad("ars_nouveau"/"curios")` gates them (neoforge `IceAndFireNeoForge.init`, lines 34-35).

For 26.2 the same three mechanisms (entrypoint keys, `IntegrationExecutor`, `modCompileOnly`+`Platform.isModLoaded`) are what must be preserved.

---

## 4. Old → new API mapping tables (verified in reference sources)

### 4.1 EMI (1.21.1 → 26.2) — `dev.emi.emi.api.*`

| Project usage (file:line) | 1.21.1 (`emi1.21.1`) | 26.2 (`emi26.2`) | Status |
|---|---|---|---|
| `@EmiEntrypoint` (`IceAndFireEmiPlugin:12`) | `dev.emi.emi.api.EmiEntrypoint` | `dev.emi.emi.api.EmiEntrypoint` | unchanged |
| `EmiPlugin` interface | `dev.emi.emi.api.EmiPlugin` | `dev.emi.emi.api.EmiPlugin` (+ new default `initialize(EmiInitRegistry)`) | unchanged |
| `EmiRegistry.addCategory/addWorkstation/addRecipe` | present | present | unchanged |
| `EmiRegistry.getRecipeManager()` (`ForgeRecipeHolder:34`) | `RecipeManager getRecipeManager()` | **removed** → `RecipeMap getRecipeMap()` (`EmiRegistry.java:54`; returns `net.minecraft.world.item.crafting.RecipeMap`) | **BREAKING** |
| recipe iteration for `RecipeType<T>` | `getRecipeManager().getAllRecipesFor(type)` | `getRecipeMap().byType(type)` → `Collection<RecipeHolder<T>>` (`RecipeMap.java:36`, MC 26.2) | **BREAKING** — replace line 34 |
| `EmiRecipeCategory(Identifier, EmiRenderable, EmiRenderable)` 3-arg ctor (`ForgeRecipeHolder:28`) | present | present (`EmiRecipeCategory.java:39`) | unchanged (works with `EmiStack` + `EmiTexture`) |
| `EmiTexture(Identifier,u,v,w,h)` 5-arg ctor (`IceAndFireEmiPlugin:14-16`) | present | present (`EmiTexture.java:22`) | unchanged |
| `EmiIngredient.of(Ingredient)` | present | present | unchanged |
| `EmiStack.of(ItemLike)` | `of(ItemConvertible)` | `of(ItemLike)` | unchanged (param-type rename only) |
| `EmiRecipe` interface (category/id/inputs/outputs/width/height/addWidgets) | present | present | unchanged |
| `WidgetHolder.addTexture(EmiTexture,int,int)` | present | present (`WidgetHolder.java:47`) | unchanged |
| `SlotWidget.large(boolean)` / `.recipeContext(EmiRecipe)` (`ForgeRecipeHolder:83`) | present | present (`SlotWidget.java:74,115`) | unchanged |
| `net.minecraft.resources.ResourceLocation` | `ResourceLocation` | **`net.minecraft.resources.Identifier`** | **BREAKING rename (all files)** |
| `RecipeHolder` | `RecipeHolder` | `net.minecraft.world.item.crafting.RecipeHolder` | unchanged |

**EMI port summary:** only 3 changes — `ResourceLocation`→`Identifier`, `getRecipeManager()`→`getRecipeMap().byType(type)` for the one recipe query, and the `getDisplayHeight()` value `1120` is suspect (should likely be a small number; double-check).

### 4.2 Jade (1.21.1 → 26.2) — `snownee.jade.*`

| Project usage (file:line) | 1.21.1 (`Jade1.21.1`) | 26.2 (`Jade26.2`) | Status |
|---|---|---|---|
| `@WailaPlugin` (`IceAndFireJadePlugin:9`) | `snownee.jade.api.WailaPlugin` | `snownee.jade.api.WailaPlugin` (value = required modId, default "") | unchanged |
| `IWailaPlugin` / `registerClient(IWailaClientRegistration)` | present | present | unchanged |
| `IWailaClientRegistration.registerBlockComponent / registerEntityComponent(provider, class)` | present | present (signature now `IComponentProvider<BlockAccessor>/<EntityAccessor>`; `IBlockComponentProvider`/`IEntityComponentProvider` are specializations, so existing enum providers still satisfy it) | unchanged |
| `IEntityComponentProvider.getUid()/appendTooltip(ITooltip, EntityAccessor, IPluginConfig)` | present | present (`IEntityComponentProvider.java:6`) | unchanged |
| `IBlockComponentProvider` | present | present | unchanged |
| `EntityAccessor.getEntity()` | returns `Entity` | returns `Entity` | unchanged |
| `BlockAccessor.getBlockEntity()` | present | `@Nullable BlockEntity` | unchanged (null-safe already via instanceof) |
| `ITooltip.add/addAll/clear` | present | present (`ITooltip.java`) | unchanged |
| `snownee.jade.impl.ui.HealthElement(float maxHealth, float health)` (`MultipartComponentProvider:36`) | **2-arg ctor** (`HealthElement.java:36`) | **4-arg ctor** `HealthElement(Hud.HeartType, float maxHealth, float health, float absorption)` | **BREAKING** |
| `snownee.jade.impl.ui.ArmorElement(float armor)` (`MultipartComponentProvider:37`) | 1-arg ctor | 1-arg ctor (`ArmorElement.java:32`) | unchanged |
| `net.minecraft.resources.ResourceLocation` | `ResourceLocation` | **`net.minecraft.resources.Identifier`** | **BREAKING rename** |
| `Minecraft.getInstance().level.entityStorage.getEntityGetter().get(UUID)` (`MultipartComponentProvider:32`) | works (EntityGetter) | `LevelEntityGetter.get(UUID)` still exists; `entityStorage`/`getEntityGetter()` still present in `ClientLevel` | OK |
| `Mob.getDisplayName().toFlatList(Style)` (`MultipartComponentProvider:35`) | present | `Component.toFlatList(Style)` still present (`Component.java:109`) | unchanged |

**Jade port summary:** update `HealthElement` call to the 4-arg ctor (use `Hud.HeartType.NORMAL` or the same conditional Jade uses, with `0f` absorption) and rename `ResourceLocation`→`Identifier`. Example of new pattern (from `Jade26.2/.../EntityHealthAndArmorProvider.java:74`):
```java
new HealthElement(living.isFullyFrozen() ? Hud.HeartType.FROZEN : Hud.HeartType.NORMAL, maxHealth, health, absorption)
```

### 4.3 Trinkets (dev.emi 1.21.1 → eu.pb4 26.2) — **complete rewrite, different mod**

| Project usage | 1.21.1 `dev.emi.trinkets` | 26.2 `eu.pb4.trinkets` | Status |
|---|---|---|---|
| package root | `dev.emi.trinkets.api` | `eu.pb4.trinkets.api` | **different mod entirely** |
| `Trinket` interface (`SimpleTickItemWrapper`) | `dev.emi.trinkets.api.Trinket` (`tick/onEquip/onUnequip/canEquip/...`) | **does not exist** — behavior is data-component driven | **GONE** |
| `SlotReference(TrinketInventory, int)` record | `dev.emi.trinkets.api.SlotReference` | `eu.pb4.trinkets.api.TrinketSlotReference` / `TrinketSlotAccess` (different shape) | **GONE as-is** |
| `TrinketsApi.registerTrinket(Item, Trinket)` (`TrinketsRegistry:13`) | present (`TrinketsApi.java:49`) | **does not exist** — items become trinkets via the `trinkets:equippable` data component (`TrinketDataComponents.EQUIPMENT` of type `TrinketEquippable`, `TrinketDataComponents.java:7`) or datagen (`TrinketsDataProvider`), not runtime code | **GONE** |
| `TrinketsApi` static methods | `getTrinket`, `getTrinketComponent`, `getEntitySlots`, ... | `getAttachment`, `getDropRule`, `canApplyEffects`, `hurtAndBreakItemStack`, `registerTrinketPredicate`, `withTrinketSlots` | **different API** |
| slot data files `data/trinkets/entities/*.json`, `slots/*.json`, `tags/item/*.json` | dev.emi data format | eu.pb4 reads `data/<ns>/slots/*.json` with **different schema** (`SlotLoader.java`: `group`/slot files, `amount`, `validator_predicates`, `drop_rule`, etc.); entity→slot binding via `trinkets:entity` data / `TrinketEntityDataBuilder` datagen | **rewrite data** |
| CCA dependency (`dev.onyxstudios.cardinal-components-api:*:6.1.1`) | trinkets is CCA-based | eu.pb4 trinkets is **not** CCA-based (uses attachments) | drop CCA for trinkets |

**Trinkets port summary:** the whole module (`TrinketsRegistry` + `SimpleTickItemWrapper` + the three data files) must be rewritten against `eu.pb4.trinkets`. `SimpleTickItemWrapper`'s "tick → `Item.inventoryTick`" behavior has no direct eu.pb4 equivalent (no `Trinket` interface). Options: (a) add the `trinkets:equippable` component with `withSlots("legs/belt")` for `HYDRA_HEART` (via datagen or a component attach), port the slot/entity data files to the eu.pb4 schema; (b) drop trinkets support and rely on Curios (already present on the NeoForge side).

### 4.4 MC renames affecting all compat modules (1.21.1 mojmap → 26.2 mojmap)

| 1.21.1 | 26.2 | Affected modules |
|---|---|---|
| `net.minecraft.resources.ResourceLocation` (`fromNamespaceAndPath`, `.DEFAULT_NAMESPACE`) | `net.minecraft.resources.Identifier` | EMI, JEI, Jade, Ponder |
| `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor` | JEI (`draw` signature), Ponder internal |
| `Item.inventoryTick(ItemStack, Level, Entity, int, boolean)` (1.21.1 `Item.java:254`) | `Item.inventoryTick(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)` (`Item.java:289`) | Trinkets `SimpleTickItemWrapper.tick` callsite |
| `net.minecraft.world.level.entity.EntityGetter` | `net.minecraft.world.level.entity.LevelEntityGetter` | Jade `MultipartComponentProvider` (via `getEntityGetter()`) |
| `RecipeManager.getAllRecipesFor(RecipeType)` | `RecipeMap.byType(RecipeType)` → `Collection<RecipeHolder<T>>` | EMI, JEI recipe enumeration |

### 4.5 JEI (待确认 — no JEI 26.2 source on disk)

API used: `IModPlugin`, `JeiPlugin`, `RecipeType.create`, `AbstractRecipeCategory<DragonForgeRecipe>`, `IRecipeLayoutBuilder.addSlot(...).addIngredients(...).setStandardSlotBackground()`, `.addItemStack(...)`, `IGuiHelper.drawableBuilder/createAnimatedDrawable/createDrawableIngredient`, `VanillaTypes.ITEM_STACK`, `IDrawableAnimated.StartDirection.LEFT`, `IGuiProperties` (anonymous screen-bounds handler for `BestiaryScreen`), registration interfaces `IRecipeCategoryRegistration/IRecipeCatalystRegistration/IRecipeRegistration/IGuiHandlerRegistration`, `IFocusGroup`, `IRecipeSlotsView`, `RecipeIngredientRole`. All present in JEI 19.21.0.247 for 1.21.1. **26.2 counterparts cannot be verified from disk** → 待确认 (migration depends on a JEI build for 26.2 existing).

### 4.6 Ponder (待确认 — no 26.2 Ponder/Create source on disk)

API used: `net.createmod.ponder.api.scene.PonderStoryBoard/SceneBuilder/SceneBuildingUtil`, `net.createmod.ponder.api.registration.PonderPlugin/PonderSceneRegistrationHelper/PonderTagRegistrationHelper`, `net.createmod.ponder.foundation.PonderIndex` (`addPlugin`), plus `net.createmod.catnip.levelWrappers.SchematicLevel` (via `DragonBaseEntity`). The on-disk Ponder reference is 1.21.1 (`minecraft_version=1.21.1`) and confirms all these exist; the 26.2 API is not on disk → 待确认. Note the fabric/neoforge `modCompileOnly` declarations mean Ponder classes are compile-time only and gated at runtime by `IntegrationExecutor`.

---

## 5. Cross-references & surprises

- **Surprise 1 — Trinkets is a different mod in 26.2.** `trinkets26.2` is `eu.pb4.trinkets` ("Trinkets Reborn" by patbox), a full rewrite; the `dev.emi.trinkets` API (`Trinket`, `SlotReference`, `TrinketsApi.registerTrinket`) and its `data/trinkets/*` schema no longer exist. Highest-effort item in this file.
- **Surprise 2 — EMI dropped `EmiRegistry.getRecipeManager()`.** 26.2 replaces it with `getRecipeMap()` returning MC's `net.minecraft.world.item.crafting.RecipeMap`; recipe iteration must switch to `getRecipeMap().byType(IafRecipes.DRAGON_FORGE_TYPE.get())`. Everything else in the EMI plugin is API-stable.
- **Surprise 3 — `fabric.mod.json` declares `"emi"` and `"jade"` entrypoint keys but NOT `"jei"`.** The JEI plugin is registered only via `@JeiPlugin` (which NeoForge scans). On the Fabric side, if JEI's Fabric plugin discovery requires the `"jei"` entrypoint, `IceAndFireJeiPlugin` will never load → recipes/catalysts/bestiary screen handler silently missing. Verify at runtime; if confirmed, add `"jei": ["com.iafenvoy.iceandfire.compat.jei.IceAndFireJeiPlugin"]` to `fabric.mod.json`. (待确认 whether JEI 19.x Fabric auto-scans `@JeiPlugin`.)
- **Surprise 4 — `Item.inventoryTick` signature changed in 26.2.** `(ItemStack, Level, Entity, int, boolean)` → `(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)`. Any port of `SimpleTickItemWrapper` must handle `ServerLevel` and `EquipmentSlot` (also affects any other `inventoryTick` callers in the codebase).
- **Surprise 5 — Jade `HealthElement` ctor grew to 4 args** (`Hud.HeartType, maxHealth, health, absorption`); `ArmorElement` is unchanged.
- **Surprise 6 — `getDisplayHeight()` returns `1120`** in `ForgeRecipeHolder.DragonForgeEmiRecipe` (line 75) while the texture is 170×79 and the overlay workstations suggest a small recipe card — likely a bug to fix while porting.
- **Integration lib is a prebuilt binary (`integration-common/fabric/neoforge:0.2`)** using Architectury `@ExpectPlatform`; needs a 26.2 build or replacement of the pattern with `Platform.isModLoaded`/`FabricLoader.isModLoaded`.

## 6. Fabric/NeoForge entrypoint & dependency facts (for migration)

- `fabric/src/main/resources/fabric.mod.json` entrypoints: `main` (`IceAndFireFabric`), `client`, `emi` (`...compat.emi.IceAndFireEmiPlugin`), `jade` (`...compat.jade.IceAndFireJadePlugin`), `modmenu`. `depends`: `minecraft 1.21.x`, `jupiter >=2.3`, `uranus >=2.3.2`.
- `fabric/build.gradle`: `modImplementation emi? no — modLocalRuntime emi`, `modImplementation jade` (modrinth `pA0xvozk`), `modApi trinkets` (modrinth `JagCscwi`) + CCA base/entity 6.1.1, `modCompileOnly Ponder`, `modImplementation uranus/jupiter`, `include(integration-fabric)`, `modApi modmenu`, `modImplementation fabric-api`, `modImplementation architectury-fabric`.
- `common/build.gradle`: `modCompileOnly emi:api`, `modCompileOnly jei-fabric-api`, `modImplementation jade`, `modImplementation Ponder-Fabric`, `modImplementation uranus/jupiter/integration-common`.
- `neoforge/build.gradle`: `modLocalRuntime emi-neoforge`, `modImplementation jade` (modrinth `JkFFfEao`), `modApi curios`, `modCompileOnly Ponder-NeoForge`, `modCompileOnly projecte/ars_nouveau/geckolib/curios`, `include(integration-neoforge)`.
- `neoforge.mods.toml` declares no compat entrypoints (NeoForge uses annotation scans); dependencies list only neoforge/minecraft/uranus/jupiter.

## 7. Minimal 26.2 action list

1. Global: `ResourceLocation`→`Identifier`; `GuiGraphics`→`GuiGraphicsExtractor` (JEI, Ponder) in all compat files.
2. EMI: replace `getRecipeManager().getAllRecipesFor(...)` with `getRecipeMap().byType(...)`; fix `getDisplayHeight()`.
3. Jade: update `HealthElement` to 4-arg ctor.
4. Trinkets: rewrite module against `eu.pb4.trinkets` (component-based) or drop; port/replace the 3 data files; drop CCA if no longer needed.
5. JEI: confirm 26.2 JEI availability; verify/fix Fabric `"jei"` entrypoint; check JEI recipe/catalyst/bestiary handler APIs.
6. Ponder: obtain 26.2 Ponder/Create API before porting scenes.
7. Delight: no change (only `Platform.isModLoaded`).
8. Integration lib: source or replace with 26.2 build, or inline the `isModLoaded` gate.
