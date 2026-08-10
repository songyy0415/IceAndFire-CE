# P10 Item Migration — Error Audit

Date: 2026-08-10
Baseline: `./gradlew :common:compileJava` → **83 total errors, item: 30 across 24 files**

---

## Summary

| Category | Errors | Files |
|---|---|---|
| `addDescription(List<Component>)` vs `appendHoverText(Consumer<Component>)` | 6 | ActivePostHit*Item (5) + GhostSwordItem |
| `BuiltInRegistries` missing import | 4 | ChainItem, DragonFleshItem, DamageBonusAbility, SummonLightningAbility |
| Block `updateShape`/override signature changes | ~11 | IceSpikesBlock, PileBlock, DragonForgeCoreBlock, GraveyardSoilBlock, Falling blocks, block entities |
| `Item.getDescriptionId()` final | 1 | DragonScalesItem |
| `getCooldowns().isOnCooldown(Item)` → ItemStack | 1 | SummonGhostSwordAbility |
| `hurtEnemy` void returning value | 1 | TrollWeaponItem |
| `Inventory.selected` private | 2 | PileBlock |
| `List<RecipeHolder>` → `List<DragonForgeRecipe>` | 1 | DragonForgeBlockEntity |

---

## Category 1 — Tooltip: `Ability.addDescription(List)` vs `appendHoverText(Consumer)` (6)

26.2 `Item.appendHoverText(ItemStack, Item$TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` (javap-verified) passes a `Consumer<Component>`. The mod's `Ability.addDescription(List<Component>)` (interface `item/ability/Ability.java:11` + 7 impls) uses `List`. Callers in `appendHoverText` pass the `Consumer`.

Fix: change `Ability.addDescription(List<Component>)` → `Consumer<Component>`, impls `.add(...)` → `.accept(...)`. Files:
- `item/ability/Ability.java` (interface default)
- `item/ability/{DamageBonusAbility, DragonsteelFireToolAbility, DragonsteelIceToolAbility, FireDragonBloodToolAbility, IceDragonBloodToolAbility, LightningDragonBloodToolAbility, SummonGhostSwordAbility}.java`
- Callers `item/tool/{ActivePostHitAxeItem, ActivePostHitHoeItem, ActivePostHitPickaxeItem, ActivePostHitShovelItem, ActivePostHitSwordItem, GhostSwordItem}.java`

## Category 2 — `BuiltInRegistries` missing import (4)

Same as P8/P9: class exists at `net.minecraft.core.registries.BuiltInRegistries`; add import.
- ChainItem, DragonFleshItem, item/ability/DamageBonusAbility, item/ability/SummonLightningAbility

## Category 3 — Block/BlockEntity API changes (~11)

- `FallingBlock.getDustColor(BlockState, BlockGetter, BlockPos)` new abstract (2): FallingGenericBlock, FallingReturningStateBlock → implement.
- `updateShape(...)` signature (IceSpikesBlock, PileBlock) and other "method does not override" → javap `BlockBehaviour`/`Block` 26.2 signatures, adjust.
- `DragonForgeCoreBlock:86`, `DreadSpawnerBlockEntity:71`, `LecternBlockEntity:155/159`, `PodiumBlockEntity:99/103` → per-site override check.
- `GraveyardSoilBlock:27` `!int` bad operand → reorder.
- `PileBlock:100/102` `Inventory.selected` private → 26.2 accessor (javap Inventory).

## Category 4 — Misc item API (4)

- `DragonScalesItem:22` — `Item.getDescriptionId()` is **final** in 26.2; remove override.
- `SummonGhostSwordAbility:30` — `isOnCooldown(Item)` → `isOnCooldown(ItemStack)` (javap ItemCooldowns).
- `TrollWeaponItem:32` — `hurtEnemy` is void; remove invalid `return expr;` (the expression has no effect on behavior).
- `DragonForgeBlockEntity:228` — `List<RecipeHolder<?>>` → `List<DragonForgeRecipe>` (use `byType`/map `.value()`).

---

## 26.2 API verified (javap)

- `Item.appendHoverText(..., Consumer<Component>, ...)` — Consumer, not List
- `Item.getDescriptionId()` — final
- `ItemCooldowns.isOnCooldown(ItemStack)`
- `FallingBlock.getDustColor(BlockState, BlockGetter, BlockPos)` abstract
- `Inventory.selected` — private (needs accessor or field use review)

## Batch plan

- **Batch 10a (tooltip)**: Ability interface + 7 impls List→Consumer, 6 callers unchanged.
- **Batch 10b (imports + misc)**: 4 BuiltInRegistries imports; DragonScalesItem remove getDescriptionId; SummonGhostSwordAbility ItemStack; TrollWeaponItem hurtEnemy; DragonForgeBlockEntity list.
- **Batch 10c (blocks)**: FallingBlock getDustColor; updateShape signatures; PileBlock Inventory; per-site override fixes.

Target after P10: 83 → ~53.
