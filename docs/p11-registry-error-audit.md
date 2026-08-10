# P11 Registry API Migration — Error Audit

Date: 2026-08-10
Baseline: `./gradlew :common:compileJava` → **53 total errors, registry: 17 across 5 files**

---

## Summary

| File | Errors | Old API | New API (26.2) |
| ---- | ------ | ------- | -------------- |
| IafRenderers | 9 | `ParticleProviderHolder<>(type, XxxParticle::factory)` with `ParticleProvider<T>` field | `ParticleProviderHolder` stores `ParticleProviderRegistry.PendingParticleProvider<T>` (the `SpriteSet -> ParticleProvider` factory form); fabric `register(ParticleType, PendingParticleProvider)` overload |
| IafKeybindings | 4 | `new KeyMapping(name, key, "key.categories.gameplay")` | `new KeyMapping(name, key, KeyMapping.Category.GAMEPLAY)` |
| IafItems | 2 | `MobEffects.JUMP_BOOST_BOOST` | `MobEffects.JUMP_BOOST` |
| IafToolMaterials | 1 | `ItemTags.AIR` (removed) | `TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("iceandfire","empty"))` |
| IafRecipeSerializers | 1 | `DragonForgeRecipe.Serializer::new` (class impl of interface) | `RecipeSerializer` is a **final record** in 26.2; register `DragonForgeRecipe.SERIALIZER` |

---

## Category 1 — Particle renderer registration (IafRenderers, 9)

- `ParticleProviderHolder` held a `ParticleProvider<T>`; the mod's `XxxParticle.factory(SpriteSet)` methods return `ParticleProvider`, so the method refs are `SpriteSet -> ParticleProvider`, i.e. a **`PendingParticleProvider`** (`create(FabricSpriteSet)` SAM; `FabricSpriteSet extends SpriteSet`).
- Fix: `ParticleProviderHolder` field/ctor/applyRegister switched to `PendingParticleProvider<T>`; fabric `register(ParticleType, PendingParticleProvider)` overload used. `GhostAppearanceParticle.factory()` (no SpriteSet) wrapped as `sprites -> GhostAppearanceParticle.factory()`.

## Category 2 — KeyMapping Category (IafKeybindings, 4)

- 26.2 `KeyMapping(String, int, KeyMapping$Category)` — 3rd arg is a `Category` record, not a lang-key String. `KeyMapping.Category.GAMEPLAY` exists. `"key.categories.gameplay"` → `Category.GAMEPLAY` (the category id still drives the `key.categories.*` lang lookup).

## Category 3 — MobEffects rename (IafItems, 2)

- `MobEffects.JUMP_BOOST_BOOST` → `MobEffects.JUMP_BOOST` (26.2 rename; javap-verified).

## Category 4 — ItemTags.AIR removed (IafToolMaterials, 1)

- `ItemTags.AIR` no longer exists. Default `repairItems` replaced with an empty mod tag `TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("iceandfire","empty"))` (only used as the default before `setRepairItems` overwrites it).

## Category 5 — RecipeSerializer final record (IafRecipeSerializers, 1) — required unblock

- `RecipeSerializer` in 26.2 is a **final record** `(MapCodec<T>, StreamCodec<...>)`, not an interface. `DragonForgeRecipe.Serializer implements RecipeSerializer` is illegal ("interface expected here").
- Migrated `DragonForgeRecipe` to the 26.2 `Recipe<T>` contract (12 errors, recipe module): added `showNotification()`, `group()`, `placementInfo()`, `recipeBookCategory()`; removed `canCraftInDimensions`/`getResultItem(HolderLookup)`/`getToastSymbol`; `assemble(T)` 1-arg; `getType()`/`getSerializer()` return `RecipeType<? extends Recipe<T>>`/`RecipeSerializer<? extends Recipe<T>>`; `Serializer` class → static `MAP_CODEC`/`STREAM_CODEC`/`SERIALIZER` record fields.
- `IafRecipeSerializers` now registers `() -> DragonForgeRecipe.SERIALIZER`.

---

## 26.2 API verified (javap)

- `KeyMapping(String, int, Category)`; `KeyMapping$Category.GAMEPLAY`
- `MobEffects.JUMP_BOOST`
- `ParticleProviderRegistry.register(ParticleType, PendingParticleProvider)`; `PendingParticleProvider.create(FabricSpriteSet)`; `FabricSpriteSet extends SpriteSet`
- `ItemTags.AIR` absent
- `Recipe` interface (26.2): `matches/assemble/showNotification/group/getSerializer/getType/placementInfo/recipeBookCategory/display/isSpecial`
- `RecipeSerializer` final record `(MapCodec, StreamCodec)`
- `PlacementInfo.create(List<Ingredient>)`

## Validation

`./gradlew :common:compileJava`: **53 → 24** (registry 17 → 0, recipe 12 → 0 bonus), new error files: 0.

Remaining (out of scope): util 6 / data 8 / network 6 / effect 1 / IceAndFire 1.
