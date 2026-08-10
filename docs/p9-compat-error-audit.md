# P9 Compat Migration — Error Audit

Date: 2026-08-10
Baseline: `./gradlew :common:compileJava` → **111 total errors, compat: 28 across 6 files**

---

## Summary

| Module | Errors | Files | Nature |
|---|---|---|---|
| Ponder | 22 | IceAndFirePonderPlugin, DragonForgeStoryBoard | **No Ponder-26.2 artifact exists** (dep commented out); API removed |
| JEI | 4 | IceAndFireJeiPlugin, DragonForgeRecipeCategory | GuiGraphics→GuiGraphicsExtractor; client recipe access removed |
| Jade | 1 | MultipartComponentProvider | Hud.HeartType became package-private in 26.2 |
| EMI | 1 | ForgeRecipeHolder | ResourceKey.location()→identifier() |

---

## Category 1 — Ponder (22) — API FULLY REMOVED

- `common/build.gradle`: `// [P1 待确认] No Ponder-Fabric-26.2 artifact exists yet — disabled until a 26.2 coordinate is available.`
- Only `Ponder-Common-1.21.1` / `Ponder-Fabric-1.21.1` exist in the Gradle cache; `ponder_version=1.0.61` targets MC 1.21.1.
- Errors: `package net.createmod.ponder.api.scene/registration does not exist`, `net.createmod.ponder.foundation does not exist`, plus dependent symbol/override errors.
- **Decision (per task rule "除非确认目标 API 已不存在")**: The target 26.2 Ponder API does not exist. The dependency is commented out and cannot be re-added. The compat sources cannot compile against a non-existent API. **Remove the `compat/ponder/` package and its integration call in `IceAndFireClient`**, documenting that Ponder support is deferred until a 26.2 artifact is published. This is the permitted-removal case, not feature deletion.

## Category 2 — JEI (4) — API changes

Verified against `jei-26.2-common-api-30.7.0.39` / `jei-26.2-fabric-30.7.0.39`:

1. `DragonForgeRecipeCategory:14,39` — `net.minecraft.client.gui.GuiGraphics` no longer exists (26.2):
   - `IDrawable.draw(GuiGraphicsExtractor)` and `draw(GuiGraphicsExtractor, int, int)` (javap-verified)
   - Override `draw(...GuiGraphics...)` → `draw(...GuiGraphicsExtractor...)`
2. `IceAndFireJeiPlugin:61` — `ClientPacketListener.getRecipeManager()` **removed** in 26.2.
   - The client no longer holds a full `RecipeManager`; `ClientPacketListener.recipes()` returns a slimmed `RecipeAccess` (`ClientRecipeContainer` — only `propertySet()` + `stonecutterRecipes()`), and `ClientboundUpdateRecipesPacket` carries only itemSets + stonecutterRecipes. **Custom recipe types are not synced to the client.**
   - JEI 26.2 maintains its own client-side `RecipeMap` exposed via `mezz.jei.common.Internal.getClientSyncedRecipes()` (javap-verified; used by JEI's own `VanillaPlugin.registerRecipes`). `RecipeMap.byType(RecipeType<T>)` returns `Collection<RecipeHolder<T>>` / `values()` returns `Collection<RecipeHolder<?>>`.
   - This class is in the **impl jar** (`jei-26.2-fabric`), not the api jar. → Add `mezz.jei:jei-${minecraft_version}-fabric:${jei_version}` as compileOnly alongside the existing fabric-api compileOnly.
3. `IceAndFireJeiPlugin:63` — generic inference breaks on `recipeManager.getRecipes().stream()...collect(toList())` because `getRecipes()` returns `Collection<RecipeHolder<?>>`. Fix via typed `byType(IafRecipes.DRAGON_FORGE_TYPE.get())` → `Collection<RecipeHolder<DragonForgeRecipe>>`.

## Category 3 — Jade (1) — API change

- `MultipartComponentProvider:37` — `Hud.HeartType has private access in Hud`.
- 1.21.1: `Hud.HeartType` public nested enum. 26.2: `Hud$HeartType` is a **package-private** nested enum (`final class ... Hud$HeartType`, javap-verified); its constants `NORMAL`/`CONTAINER`/etc. are public static fields.
- `HealthElement(Hud$HeartType, float, float, float)` ctor (snownee.jade.impl.ui) still requires it.
- **Fix**: add access-widener entry `accessible class net/minecraft/client/gui/Hud$HeartType` (existing AW pattern). `Hud` itself is public.

## Category 4 — EMI (1) — API change

- `ForgeRecipeHolder:56` — `ResourceKey.location()` **removed** in 26.2; renamed to `identifier()` returning `Identifier` (javap-verified, same as DragonUtils P8 fix).
- `entry.id()` is `ResourceKey<Recipe<?>>` → `this.entry.id().identifier()`.

---

## Migration plan

- **Batch 9a**: Remove Ponder package + integration call (API absent). 
- **Batch 9b**: JEI — add impl-jar compileOnly dep; rewrite `registerRecipes` to `Internal.getClientSyncedRecipes().byType(...)`; `GuiGraphicsExtractor` in category draw. EMI `identifier()`. Jade AW `Hud$HeartType`.
- Verify `./gradlew :common:compileJava`, compat 28 → 0, new error files = 0, one commit `migration: migrate compat api to mc26.2`.

Target after P9: 111 → ~83 (compat 28 → 0).
