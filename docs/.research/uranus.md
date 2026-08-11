# Uranus API Migration Table (1.21.1 → 26.2)

Research file for the IceAndFire-CE migration project.
Compiled 2026-08-09 against the on-disk sources:

- Old: `D:/aiminecraftdev/Uranus1.21.1` (mod_version **2.4.1-bugfix**, MC 1.21.1, Yarn "named" workspace, Architectury 13.0.8, fabric 0.16.7, fabric-api 0.107.0+1.21.1, neoforge 21.1.76)
- New: `D:/aiminecraftdev/Uranus26.2` (mod_version **2.4.1-bugfix**, MC 26.2, **Mojmap/Official workspace**, Architectury 21.0.7, fabric-loader 0.19.3, fabric-api 0.156.0+26.2, **fabric only**)

> Summary of the headline finding: the Uranus package tree is *almost unchanged* (`com.iafenvoy.uranus.*`),
> but (a) a set of whole packages were **deleted** (raycoms pathfinding, collision, DynamicItemRenderer),
> (b) the **entire client model/render layer was rewritten to the 26.2 render-state architecture**
> (`Entity`→`EntityRenderState`, `MatrixStack`→`PoseStack`, `MultiBufferSource`→`SubmitNodeCollector`,
> `VertexConsumer.vertex`→`addVertex`, `setupAnim`-style model callbacks), and (c) the artifact is now
> **officially (Mojmap) mapped** and **fabric-only**.

---

## 1. Dependency coordinates

Project declaration (IceAndFire-CE) — three spots:

| File | Coordinate | Notes |
|---|---|---|
| `D:/IceAndFire-CE/common/build.gradle:20` | `modImplementation "maven.modrinth:uranus:FH0tB0dy"` | 1.21.1 common |
| `D:/IceAndFire-CE/fabric/build.gradle:43` | `modImplementation "maven.modrinth:uranus:FH0tB0dy"` | 1.21.1 fabric |
| `D:/IceAndFire-CE/neoforge/build.gradle:41` | `modImplementation "maven.modrinth:uranus:BBb3HOQ5"` | 1.21.1 neoforge (different hash) |

Gradle coordinates produced by Uranus itself (unchanged group/artifact):
- group: `com.iafenvoy.uranus`, artifact: `uranus`, version: `2.4.1-bugfix`
  (`D:/aiminecraftdev/Uranus1.21.1/gradle.properties` and `D:/aiminecraftdev/Uranus26.2/gradle.properties` — identical values).
- Jar naming: 1.21.1 `uranus-fabric-2.4.1-bugfix-1.21.1-fabric.jar` / `uranus-neoforge-2.4.1-bugfix-1.21.1-neoforge.jar`;
  26.2 `uranus-fabric-2.4.1-bugfix-26.2-fabric.jar` (see `build.gradle` `archivesName`, `include` lines).

**Changed build config (26.2):**
- Plugin `dev.architectury.loom` → `dev.architectury.loom-no-remap` (1.17-SNAPSHOT). The 26.2 jar is **Mojmap/official mapped**, no remapping — the mod (Mojmap) can consume it directly. The 1.21.1 build used `net.minecraft:minecraft` + Yarn mappings (`loom`); 26.2 uses `com.mojang:minecraft`.
- `enabled_platforms=fabric` — **neoforge is dropped** in 26.2 (gradle.properties; build.gradle only packages `uranus-fabric`). The 26.2 `neoforge/` tree still exists on disk but is not built.
- `fabric.mod.json` 26.2: adds `"jars": [{"file": "META-INF/jars/architectury-fabric.jar"}]` (bundles Architectury nested jar); depends on `fabric-api:*` + `minecraft:26.x` (1.21.1 depended on `fabric:*` + `minecraft:1.21.x`).
- `accesswidener`: header changed from `accessWidener v2 named` → `accessWidener v2 official`; `PathNodeMaker entityBlockX/Y/ZSize` → `NodeEvaluator entityWidth/Height/Depth`; `DamageSources registry` field widened by `ServerChunkLoadingManager getChunkHolder` → `ChunkMap getVisibleChunkIfPresent`; `Transformation/ModelTransformation$Deserializer` → `ItemTransform/ItemTransforms$Deserializer`.

**For 26.2 the mod must switch to the 26.2 Modrinth version hash (not yet known / 待确认).** The group:artifact:version coordinates themselves are unchanged.

---

## 2. Usage inventory — every `com.iafenvoy.uranus` reference in the project

No matches in `D:/IceAndFire-CE/fabric/src`. All usage is in `common/src`.

### 2.1 Unique symbols used (imports)

| # | Class | # import sites | Files (representative) |
|---|---|---|---|
| 1 | `com.iafenvoy.uranus.ServerHelper` | 7 | world/DangerousGeneration.java:4, entity/PixieEntity.java, entity/DragonBaseEntity.java:29, item/block/entity/{Podium,PixieHouse,Jar}BlockEntity.java |
| 2 | `com.iafenvoy.uranus.util.RandomHelper` | 12 | world/structure/*, world/feature/*, particle/*, entity/*, event/ServerEvents.java:22, screen/TitleScreenRenderManager.java:4, compat/ponder/DragonForgeStoryBoard.java:7 |
| 3 | `com.iafenvoy.uranus.util.ShapeBuilder` | 1 | world/structure/DragonCaveStructure.java:10 |
| 4 | `com.iafenvoy.uranus.event.Event` | 3 | event/IafEvents.java:4, event/CommonEvents.java:3, render/entity/DragonSkullEntityRenderer.java:18 |
| 5 | `com.iafenvoy.uranus.event.EntityEvents` | 1 | IceAndFire.java:13 |
| 6 | `com.iafenvoy.uranus.event.LivingEntityEvents` | 1 | IceAndFire.java:14 |
| 7 | `com.iafenvoy.uranus.event.PlayerEvents` | 1 | IceAndFire.java:15 |
| 8 | `com.iafenvoy.uranus.object.RegistryHelper` | 9 | item/tool/{TideTrident,DragonBow,HippogryphSword}Item.java, entity/{DreadKnight,Hippocampus,TideTrident}Entity.java, event/ServerEvents.java:21, item/PixieWandItem.java |
| 9 | `com.iafenvoy.uranus.object.VecUtil` | 2 | particle/DragonFlameParticle.java:3, particle/DragonFrostParticle.java:3 |
| 10 | `com.iafenvoy.uranus.object.BlockUtil` | 1 | entity/DragonEggEntity.java:16 |
| 11 | `com.iafenvoy.uranus.object.EntityUtil` | 1 | entity/DragonBaseEntity.java:33 |
| 12 | `com.iafenvoy.uranus.object.item.FoodUtils` | 1 | entity/DragonBaseEntity.java:38 |
| 13 | `com.iafenvoy.uranus.object.entity.collision.CustomCollisionsNavigator` | 1 | entity/pathfinding/CyclopsNavigation.java:4 |
| 14 | `com.iafenvoy.uranus.object.entity.collision.ICustomCollisions` | 1 | entity/DeathWormEntity.java:15 |
| 15 | `com.iafenvoy.uranus.object.entity.pathfinding.raycoms.AdvancedPathNavigate` | 5 | entity/DragonBaseEntity.java:34, entity/HippogryphEntity.java:22, entity/ai/DragonAIAttackMeleeGoal.java:4 |
| 16 | `com.iafenvoy.uranus.object.entity.pathfinding.raycoms.IPassabilityNavigator` | 1 | entity/DragonBaseEntity.java:35 |
| 17 | `com.iafenvoy.uranus.object.entity.pathfinding.raycoms.PathingStuckHandler` | 1 | entity/DragonBaseEntity.java:36 |
| 18 | `com.iafenvoy.uranus.object.entity.pathfinding.raycoms.pathjobs.ICustomSizeNavigator` | 1 | entity/DragonBaseEntity.java:37 |
| 19 | `com.iafenvoy.uranus.animation.Animation` | 20 | entity/* (all animated), render/model/* |
| 20 | `com.iafenvoy.uranus.animation.AnimationHandler` | 16 | entity/{Troll,StymphalianBird,Siren,SeaSerpent,Hippogryph,Hippocampus,Gorgon,Ghost,Dread*,DeathWorm,Cyclops,Cockatrice,Amphithere}Entity.java |
| 21 | `com.iafenvoy.uranus.animation.IAnimatedEntity` | 27 | entity/* + render/model/* |
| 22 | `com.iafenvoy.uranus.client.model.AdvancedEntityModel` | 12 | render/model/* |
| 23 | `com.iafenvoy.uranus.client.model.AdvancedModelBox` | 28 | render/model/* |
| 24 | `com.iafenvoy.uranus.client.model.basic.BasicModelPart` | 24 | render/model/*, entity/util/{ReversedBuffer,ChainBuffer}.java |
| 25 | `com.iafenvoy.uranus.client.model.basic.BasicEntityModel` | 1 | render/model/ChainTieModel.java:5 |
| 26 | `com.iafenvoy.uranus.client.model.ModelAnimator` | 17 | render/model/* |
| 27 | `com.iafenvoy.uranus.client.model.util.HideableModelRenderer` | 5 | render/model/{BipedBase,Ghost,DreadThrall,DreadLich,DreadKnight,DreadGhoul,DreadLichSkull}Model.java |
| 28 | `com.iafenvoy.uranus.client.render.armor.ArmorModelBase` | 10 | render/model/armor/* |
| 29 | `com.iafenvoy.uranus.client.model.ITabulaModelAnimator` | 4 | render/model/animator/{Dragon,SeaSerpent}TabulaModelAnimator.java, render/entity/DragonSkullEntityRenderer.java:14 |
| 30 | `com.iafenvoy.uranus.client.model.TabulaModel` | 13 | render/entity/*, render/model/animator/*, render/entity/feature/* |
| 31 | `com.iafenvoy.uranus.client.model.util.TabulaModelHandlerHelper` | 6 | registry/IafRenderers.java, render/entity/{DragonSkull,MobSkull,SeaSerpent}EntityRenderer.java, render/model/animator/* |
| 32 | `com.iafenvoy.uranus.client.render.DynamicItemRenderer` | 7 | registry/IafRenderers.java:21, render/item/* |
| 33 | `com.iafenvoy.uranus.client.render.armor.IArmorRendererBase` | 2 | render/item/armor/{Scale,Basic}ArmorRenderer.java |
| 34 | `com.iafenvoy.uranus.client.model.util.HideableLayer` | 2 | render/entity/{DreadLich,DreadThrall}EntityRenderer.java |
| 35 | `com.iafenvoy.uranus.util.function.MemorizeSupplier` | 5 | data/{DragonColor,SeaSerpentType}.java, registry/IafArmorMaterials.java, render/entity/DragonSkullEntityRenderer.java |

### 2.2 Key call sites (for impact notes)

**ServerHelper** — `ServerHelper.server` (public field, unchanged) used at `world/DangerousGeneration.java:15`; `ServerHelper.sendToAll(payload)` used at `entity/DragonBaseEntity.java:500`, `entity/PixieEntity.java:310`, `item/block/entity/{Jar,PixieHouse,Podium}BlockEntity.java`. 26.2 `sendToAll` takes `CustomPacketPayload` (Mojmap) — the mod's own payloads already implement it, so **no mod change**.

**RandomHelper** — `randomOne`, `nextDouble(1,7)`, `nextInt(0,n)`, `randomize(v,0.5)` across entity/particle/structure code. Class is **byte-identical** in 26.2 → no change.

**ShapeBuilder** — `ShapeBuilder.start().getAllInCutOffSphereMutable(r,r,center)` and `.getAllInRandomlyDistributedRangeYCutOffSphereMutable(...)` in `world/structure/DragonCaveStructure.java:135-144`. Method signatures unchanged (Random→RandomSource is a mojmap rename only); internals updated to Mojmap. **No mod change.**

**Event / EntityEvents / LivingEntityEvents / PlayerEvents** — `Event.java` is **identical**; the three events only re-mapped `PlayerEntity`→`Player`, `ServerPlayerEntity`→`ServerPlayer`, `World`→`Level`. Registration in `IceAndFire.java:94-97` and the custom `Event<...>` factories in `event/IafEvents.java`, `event/CommonEvents.java`, `render/entity/DragonSkullEntityRenderer.java:36` are **unchanged**.

**RegistryHelper** — methods used by the mod: `getEntry(registryAccess, Registries.BANNER_PATTERN, ...)` (`entity/DreadKnightEntity.java:73-74`), `getEnchantment(registryAccess, Enchantments.X)` (many). Return types changed `RegistryEntry`→`Holder` and params `RegistryKey`→`ResourceKey`. Because the mod is already Mojmap and passes `this.registryAccess()` / `Registries.X`, these call sites compile as-is against 26.2 (the call passes `ResourceKey` already).

**VecUtil / BlockUtil / EntityUtil / FoodUtils** — method *signatures used by the mod are unchanged* (e.g. `VecUtil.createBlockPos(x,y,z)` `particle/DragonFlameParticle.java:38`, `BlockUtil.isBurning(state)` `entity/DragonEggEntity.java:243`, `EntityUtil.item(serverWorld,x,y,z,stack,delay)` `entity/DragonBaseEntity.java:1106`, `FoodUtils.getFoodPoints(entity)` and `getFoodPoints(stack, meatOnly, includeFish)` in `entity/ai/DragonAITarget*.java` and `entity/DragonBaseEntity.java:2331`). Only internals were re-mapped to Mojmap.

**MemorizeSupplier** — used as `new MemorizeSupplier<>(() -> Ingredient.of(...))` for `ArmorMaterial` ingredient suppliers (`data/DragonColor.java:48`, `data/SeaSerpentType.java:53`, `registry/IafArmorMaterials.java:27-31`). **Identical** in 26.2.

---

## 3. API-surface inventory (old vs new)

### 3.1 Exact file diff — `common/src/main/java/com/iafenvoy/uranus`

All files below were **deleted in 26.2** (verified via `diff` of the two file trees):

| Deleted file (1.21.1) | Notes |
|---|---|
| `client/model/tools/BasicModelBase.java` | not used by the mod |
| `client/model/tools/BasicModelRenderer.java` | not used by the mod |
| `client/render/DynamicItemRenderer.java` | **USED by the mod** — render/item/* + registry/IafRenderers.java |
| `client/render/armor/IArmorTextureProvider.java` | not used by the mod |
| `mixin/BuiltinModelItemRendererMixin.java` | removed from common mixin config too |
| `object/PathUtil.java` | not used by the mod |
| `object/entity/collision/CustomCollisionsBlockCollisions.java` | collision engine — **USED indirectly** |
| `object/entity/collision/CustomCollisionsNavigator.java` | **USED** — CyclopsNavigation extends it |
| `object/entity/collision/CustomCollisionsNodeProcessor.java` | |
| `object/entity/collision/ICustomCollisions.java` | **USED** — DeathWormEntity implements it |
| `object/entity/pathfinding/raycoms/AbstractAdvancedPathNavigate.java` | **USED indirectly** |
| `object/entity/pathfinding/raycoms/AdvancedPathNavigate.java` | **USED** — dragon/hippogryph navigation |
| `object/entity/pathfinding/raycoms/ChunkCache.java` | |
| `object/entity/pathfinding/raycoms/IAdvancedPathingMob.java` | |
| `object/entity/pathfinding/raycoms/IPassabilityNavigator.java` | **USED** — DragonBaseEntity implements |
| `object/entity/pathfinding/raycoms/IStuckHandler.java` | |
| `object/entity/pathfinding/raycoms/ITallWalker.java` | |
| `object/entity/pathfinding/raycoms/MNode.java` | |
| `object/entity/pathfinding/raycoms/PathFindingStatus.java` | |
| `object/entity/pathfinding/raycoms/PathPointExtended.java` | |
| `object/entity/pathfinding/raycoms/PathResult.java` | |
| `object/entity/pathfinding/raycoms/Pathfinding.java` | |
| `object/entity/pathfinding/raycoms/PathfindingConstants.java` | |
| `object/entity/pathfinding/raycoms/PathingOptions.java` | |
| `object/entity/pathfinding/raycoms/PathingStuckHandler.java` | **USED** — DragonBaseEntity |
| `object/entity/pathfinding/raycoms/SurfaceType.java` | |
| `object/entity/pathfinding/raycoms/pathjobs/AbstractPathJob.java` | |
| `object/entity/pathfinding/raycoms/pathjobs/ICustomSizeNavigator.java` | **USED** — DragonBaseEntity implements |
| `object/entity/pathfinding/raycoms/pathjobs/PathJobMoveAwayFromLocation.java` | |
| `object/entity/pathfinding/raycoms/pathjobs/PathJobMoveToLocation.java` | |
| `object/entity/pathfinding/raycoms/pathjobs/PathJobRandomPos.java` | |

Every other common class has a same-named 26.2 counterpart. Fabric-side: 1.21.1 had
`fabric/.../fabric/mixin/ArmorFeatureRendererMixin.java` + `uranus-fabric.mixins.json`; **26.2 has no fabric mixin dir** —
`ArmorFeatureRendererMixin` now lives only in common (common mixin "client" list: `ArmorFeatureRendererMixin, ClientWorldMixin, ModelLoaderMixin`; `BuiltinModelItemRendererMixin` removed). Neoforge `uranus-neoforge.mixins.json` still declares `neoforge/mixin/ArmorFeatureRendererMixin`, but neoforge is not built in 26.2.

### 3.2 Mixin config diff (common)

- 1.21.1 `uranus.mixins.json`: 9 server mixins + 4 client (`ArmorFeatureRendererMixin, BuiltinModelItemRendererMixin, ClientWorldMixin, ModelLoaderMixin`).
- 26.2: 9 server mixins + 3 client — `BuiltinModelItemRendererMixin` gone.
- 26.2 fabric bundles the common mixin via `"mixins": ["uranus.mixins.json"]` (same as 1.21.1).

---

## 4. Master old→new API table (for every symbol the mod uses)

`OLD` = Uranus 1.21.1 (Yarn names, but shown in 1.21.1-src form); `NEW` = Uranus 26.2 (Mojmap). "Mod impact" = work required in IceAndFire-CE.

### 4.1 Unchanged classes (no mod work)

| Class | Old → New |
|---|---|
| `util/RandomHelper` | identical |
| `event/Event` | identical |
| `animation/Animation` | identical |
| `animation/IAnimatedEntity` | identical |
| `util/function/MemorizeSupplier` | identical |
| `ServerHelper` | `sendToAll(CustomPayload)` → `sendToAll(CustomPacketPayload)`; `server` field unchanged. Mod payloads already Mojmap → no change |
| `object/VecUtil` | only `createBottomCenter` re-mapped (`Vec3d.ofBottomCenter`→`Vec3.atBottomCenterOf`); `createBlockPos` unchanged |
| `object/BlockUtil` | internals only (`isIn`→`is`, `getDefaultState`→`defaultBlockState`); `isBurning` unchanged |
| `object/item/FoodUtils` | internals only (`PassiveEntity`→`AgeableMob`, `DataComponentTypes`→`DataComponents`, `isIn`→`is`); `getFoodPoints` overloads unchanged |
| `event/EntityEvents` | `onJoinWorld(Entity, World)`→`onJoinWorld(Entity, Level)`; `onTrackingStart(Entity, ServerPlayerEntity)`→`(Entity, ServerPlayer)` |
| `event/LivingEntityEvents` | import re-map only |
| `event/PlayerEvents` | `handleConnection(PlayerEntity)`→`handleConnection(Player)` |
| `object/EntityUtil` | `summon/lightening/item` re-mapped internally (`ServerWorld`→`ServerLevel`, `MobEntity`→`Mob`, `refreshPositionAndAngles`→`snapTo`, `initialize`→`finalizeSpawn`, `spawnEntity`→`addFreshEntity`, `SpawnReason`→`EntitySpawnReason`, `EntityType.LIGHTNING_BOLT`→`EntityTypes.LIGHTNING_BOLT`). Signatures used by mod unchanged |

### 4.2 RegistryHelper — return/param types renamed (Mojmap re-map only)

| Old (1.21.1) | New (26.2) |
|---|---|
| `T get(DynamicRegistryManager, RegistryKey<Registry<T>>, RegistryKey<T>)` | `T get(RegistryAccess, ResourceKey<Registry<T>>, ResourceKey<T>)` |
| `RegistryEntry<T> entry(RegistryAccess, ResourceKey<Registry<T>>, T)` | `Holder<T> entry(RegistryAccess, ResourceKey<Registry<T>>, T)` (impl `lookupOrThrow(registry).wrapAsHolder(obj)`) |
| `RegistryEntry<T> getEntry(DynamicRegistryManager, RegistryKey, RegistryKey)` | `Holder<T> getEntry(RegistryAccess, ResourceKey, ResourceKey)` |
| `RegistryEntry<Enchantment> getEnchantment(RegistryManager, RegistryKey<Enchantment>)` | `Holder<Enchantment> getEnchantment(RegistryAccess, ResourceKey<Enchantment>)` |
| `RegistryEntry<ArmorMaterial> getArmorMaterial(...)` | **removed** from 26.2 (not used by mod) |
| `RegistryEntry<DamageType> getDamageSource(...)` | `Holder<DamageType> getDamageSource(RegistryAccess, ResourceKey<DamageType>)` |

Mod impact: none required — call sites already pass Mojmap types and consume `Holder`/`RegistryEntry` as generic params. (Note: `getArmorMaterial` overload was dropped; only a concern if re-added.)

### 4.3 Animation system

| Class | Old → New | Mod impact |
|---|---|---|
| `animation/Animation` | identical | none |
| `animation/AnimationHandler` | `entity.getWorld().isClient` → `entity.level().isClientSide()` | none (internal) |
| `animation/IAnimatedEntity` | identical | none |
| `animation/AnimationAI` | not used by mod | — |

### 4.4 Render-state model rewrite — **the big breaking area** (client)

All client model/render classes now follow the 26.2 render-state architecture.
`Entity` generic → `EntityRenderState` / `HumanoidRenderState` / `LivingEntityRenderState`;
`MatrixStack` → `PoseStack`; `VertexConsumer.vertex(...)` → `addVertex(...)`;
`MinecraftClient` → `Minecraft`; `MathHelper` → `Mth`; `RotationAxis` → `Axis`.

| Class | Old → New (signature) | Mod impact |
|---|---|---|
| `client/model/basic/BasicEntityModel` | `abstract BasicEntityModel<T extends Entity> extends EntityModel<T>` with no-arg ctor + `render(...)` override iterating `parts()` → `abstract BasicEntityModel<T extends EntityRenderState> extends EntityModel<T>`, ctor takes `ModelPart root` (or `ModelPart root, Function<Identifier,RenderType>`), **`render`/`setAngles`/`animateModel` overrides removed** (26.2 `renderToBuffer` is final). `parts()` retained. | **HIGH.** Every model subclass must pass a `ModelPart` root to `super()` and stop overriding `render`; render-state wiring must be added. `render/model/ChainTieModel.java` extends this. |
| `client/model/AdvancedEntityModel` | `abstract ...<T extends Entity>` no-arg ctor → `<T extends EntityRenderState>` ctor `(ModelPart root)`; anim helpers `MathHelper.cos/sin`→`Mth.cos/sin`; **all animation float-helpers preserved unchanged**. | **HIGH** — the 10+ `AdvancedEntityModel` subclasses in `render/model/*` must supply a `ModelPart` root and provide render-state-driven `setupAnim`. |
| `client/model/AdvancedModelBox` | `translateAndRotate(MatrixStack)`→`(PoseStack)`; `render(MatrixStack, VertexConsumer,...)`→`(PoseStack,...)`; `matrixStack.push/peek/pop`→`pushPose/last/popPose`; `RotationAxis.POSITIVE_Z/Y/X`→`Axis.ZP/YP/XP`; `entry.getPositionMatrix()/getNormalMatrix()`→`entry.pose()/normal()`; `consumer.vertex(...)`→`consumer.addVertex(...)`. Box math (`rotateAngleX/Y/Z`, `setRotationAngle`, `walk`, `bob`, `setScale`, etc.) unchanged. | None at the mod API level (fields/methods used by models keep names); only the underlying MC stack types differ. |
| `client/model/ModelAnimator` | `MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)` → `Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)`; `MathHelper`→`Mth`. Public animator API (`startKeyframe`, `rotate/rotateX/Y/Z`, `move`, `staticKeyframe`, `resetKeyframe`, ...) unchanged. | none |
| `client/model/basic/BasicModelPart` | `render(MatrixStack,VertexConsumer,int,int[,int])`→`(PoseStack,...)`; `translateRotate(MatrixStack)`→`(PoseStack)`; `Direction.getUnitVector()`→`Direction.step()`; internal push/pop/vertex re-map. `cubeList`, `getParent`, `addChild` etc. unchanged. | none (used by `ReversedBuffer`, `ChainBuffer`, models) |
| `client/model/util/HideableModelRenderer` | `render/invisibleRender(MatrixStack,...)`→`(PoseStack,...)`; push/pop→pushPose/popPose. Public API (`visible`, `invisibleRender`) unchanged. | none |
| `client/model/util/HideableLayer` | `<T extends Entity, M extends EntityModel<T>, C extends FeatureRenderer<T,M>>` + `render(MatrixStack, VertexConsumerProvider, int, T, float*6)` → `<S extends EntityRenderState, M extends EntityModel<? super S>, C extends RenderLayer<S,M>>` + `submit(PoseStack, SubmitNodeCollector, int, S, float yRot, float xRot)`. Ctor `(C, FeatureRendererContext<T,M>)`→`(C, RenderLayerParent<S,M>)`. | **HIGH.** `render/entity/DreadLichEntityRenderer.java:21,26` and `DreadThrallEntityRenderer.java:29,34` instantiate `HideableLayer<DreadLichEntity, DreadLichModel, ItemInHandLayer<DreadLichEntity, DreadLichModel>>`. Must switch generics to render states and the wrapped layer to `ItemInHandLayer<S, M>`; renderers must extend `RenderLayerParent<S,M>`. |
| `client/model/util/TabulaModelHandlerHelper` | `getModel(Identifier)` / `getModel(Identifier, Supplier<ITabulaModelAnimator<T>>)` / `getModel(Identifier, MemorizeSupplier<...>)` with `<T extends Entity>` → same methods with `<T extends LivingEntityRenderState>`; `ResourceManager.findResources`→`listResources`, `Resource.getInputStream`→`open`, `MinecraftClient`→`Minecraft`. | **HIGH.** All `TabulaModelHandlerHelper.getModel(...)` calls in `registry/IafRenderers.java:44-46`, `render/entity/*`, `render/model/animator/*` return `TabulaModel<T extends LivingEntityRenderState>`. |
| `client/model/ITabulaModelAnimator` | `void setRotationAngles(TabulaModel<T> model, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale)` with `<T extends Entity>` → same signature with `<T extends LivingEntityRenderState>` (param renamed `state`). | **HIGH.** `DragonTabulaModelAnimator<T extends DragonBaseEntity>` (`render/model/animator/DragonTabulaModelAnimator.java:19`) and `SeaSerpentTabulaModelAnimator extends IceAndFireTabulaModelAnimator<SeaSerpentEntity>` implement this; they read Entity state heavily → must be re-typed to a render-state and the entity data moved into the state. |
| `client/model/TabulaModel` | `<T extends Entity> extends AdvancedEntityModel<T>`; `setAngles(T entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch)` → `<T extends LivingEntityRenderState> extends AdvancedEntityModel<T>`; `setupAnim(T state)` deriving inputs from `state.walkAnimationPos/walkAnimationSpeed/ageInTicks/yRot/xRot`; **new private ModelPart render tree** (`buildRenderRoot`, `buildRenderPart`, `syncRenderTree`) so `renderToBuffer` draws real geometry; calls `tabulaModelAnimator.setRotationAngles(...)` then `syncRenderTree()`. `rootBoxes`, `getCube`, `cubeList`, `scaleChildren` API retained. | **HIGH.** Every place that holds a `TabulaModel<T>` typed against an Entity (renderers, feature renderers, skull renderers) must re-type to `TabulaModel<XxxRenderState>`. |
| `client/render/armor/ArmorModelBase` | `extends BipedEntityModel<LivingEntity>`; `setAngles(LivingEntity, floats...)` (reads ArmorStandEntity) → `extends HumanoidModel<HumanoidRenderState>`; `setupAnim(HumanoidRenderState)` reading `ArmorStandRenderState` rotation `Rotations` via `x()/y()/z()`; **`hat.copyFrom(head)` removed** (hat is now child of head); `render(EquipmentSlot, PoseStack, MultiBufferSource, int, ItemStack, Identifier)` → `render(EquipmentSlot, PoseStack, SubmitNodeCollector, int, ItemStack, RenderType, HumanoidRenderState)` and `renderHelmet/Chestplate/Leggings/Boots` similarly (no more `VertexConsumer`/`ItemRenderer.getArmorGlintConsumer`; render type passed explicitly). | **HIGH.** All 10 armor models in `render/model/armor/*` extend it → ctor must pass `ModelPart`; overridden `setAngles` replaced by `setupAnim(RenderState)`; all render* helpers rewritten. |
| `client/render/armor/IArmorRendererBase` | `<T extends LivingEntity>`; `getHumanoidArmorModel(LivingEntity, ItemStack, EquipmentSlot, BipedEntityModel<T>)`; `getArmorTexture(ItemStack, Entity, EquipmentSlot)`; `render(PoseStack, MultiBufferSource, LivingEntity, EquipmentSlot, int, ItemStack, BipedEntityModel<T>)`; static `RENDERERS` map + `register(renderer, ItemConvertible...)`. | **HIGH.** `render/item/armor/{Scale,Basic}ArmorRenderer.java` implement this → must switch to `HumanoidRenderState`, `RenderLayerParent`, `SubmitNodeCollector`, `RenderType`; `IArmorTextureProvider` (the texture side) was deleted → texture supply must move into the renderer or the state. |

### 4.5 Deleted APIs used by the mod (no Uranus 26.2 counterpart — must be reimplemented in-mod or reworked to vanilla)

| Old class | Old surface used by the mod | Mod usage | 26.2 status |
|---|---|---|---|
| `client/render/DynamicItemRenderer` | static `RENDERERS` map (`ItemConvertible → DynamicItemRenderer`), abstract item-render hooks | `registry/IafRenderers.java:140-148` (`RENDERERS.put(item, renderer)` for DeathwormGauntlet, GorgonHead, TideTrident, PixieHouse block items); `render/item/{TrollWeapon,TideTridentItem,MiscItem,GorgonHead,DeathwormGauntlet}Renderer.java` extend it | **Deleted, no replacement in Uranus 26.2 or Jupiter 26.2** (checked Jupiter new — no BEWLR/item-renderer hook). Must be reimplemented (custom `ItemModel`/`BEWLR`-style hook or mixin). |
| `object/entity/collision/CustomCollisionsNavigator` | ctor `(Mob, Level)`; overrides `createPathFinder` | `entity/pathfinding/CyclopsNavigation.java:9` `extends CustomCollisionsNavigator` | **Deleted.** Rewrite to extend vanilla `PathNavigation` / use vanilla `PathFinder` (already re-mapped to Mojmap in the mod file). |
| `object/entity/collision/ICustomCollisions` | `canPassThrough(BlockPos, BlockState, VoxelShape)` + static `getAllowedMovementForEntity` (overrides `Entity.getAllowedMovement`) | `entity/DeathWormEntity.java:78` implements it | **Deleted.** Drop interface; burrowing-through-blocks behaviour must be reimplemented via `Entity` collision overrides. |
| `object/entity/pathfinding/raycoms/AdvancedPathNavigate` | ctor `(Mob, Level, MovementType, float width, float height)`; `enum MovementType { WALKING, FLYING, CLIMBING }`; `moveToLivingEntity(Entity, double)`; `setCanFloat`; `getNodeEvaluator` | `entity/DragonBaseEntity.java:513-548` (`createNavigator` overloads, `switchNavigator`), `entity/HippogryphEntity.java:945-964`, `entity/ai/DragonAIAttackMeleeGoal.java:31,37,44,76` | **Deleted.** Dragon/hippogryph navigation must fall back to vanilla `PathNavigation` or a new in-mod implementation (flying/size-aware pathing is lost). |
| `object/entity/pathfinding/raycoms/IPassabilityNavigator` | `maxSearchNodes()`, `isBlockExplicitlyPassable(...)`, `isBlockExplicitlyNotPassable(...)` | `entity/DragonBaseEntity.java:134` implements; methods implemented at `DragonBaseEntity.java:2828,2867,2872` | **Deleted.** Remove `implements` + the three methods (or keep as dead code). |
| `object/entity/pathfinding/raycoms/PathingStuckHandler` | `PathingStuckHandler.createStuckHandler()`, `.withTeleportSteps(5)` | `entity/DragonBaseEntity.java:508-509,536` | **Deleted.** Remove the stuck-handler plumbing. |
| `object/entity/pathfinding/raycoms/pathjobs/ICustomSizeNavigator` | `isSmallerThanBlock()`, `getXZNavSize()`, `getYNavSize()` | `entity/DragonBaseEntity.java:134` implements; methods at `DragonBaseEntity.java:2833-2845`; `getYNavSize()` also read by `entity/util/dragon/DragonUtils.java:56` | **Deleted.** Remove interface; keep the helper methods or inline the size math. |

---

## 5. Broader changes (beyond per-symbol)

1. **Render-state architecture (client).** The single biggest change. 26.2 `EntityModel`/`RenderLayer`/`Model` are driven by `EntityRenderState` extracted by the renderer, never the live entity; `MultiBufferSource` is deleted, geometry is deferred through `SubmitNodeCollector`; `Model.renderToBuffer` is final and draws a single `ModelPart` root. Uranus 26.2 solves Tabula rendering by mirroring the `AdvancedModelBox` tree into a real `ModelPart` tree (`TabulaModel.buildRenderRoot`/`syncRenderTree`). Every model, animator, layer and armor renderer in the mod must be re-typed and re-wired accordingly (see §4.4).
2. **Deleted packages** (no replacement, must reimplement or rework to vanilla): raycoms pathfinding, custom collisions, `DynamicItemRenderer`, `PathUtil`, `BasicModelBase`/`BasicModelRenderer`, `IArmorTextureProvider`, `BuiltinModelItemRendererMixin` (§3.1, §4.5).
3. **Mapping change.** 1.21.1 workspace was Yarn ("named"); 26.2 is built with `loom-no-remap` against `com.mojang:minecraft` → the artifact is **official/Mojmap mapped** (`accessWidener v2 official`). The mod is Mojmap, so this is a *simplification* (direct consumption; the whole 1.21.1 "which mapping is the Uranus jar in" question disappears). Requires JDK 25/Gradle 9.5.1 (26.2 pins `org.gradle.java.home` to Zulu 25).
4. **Platform coverage.** Uranus 26.2 ships **fabric only** (`enabled_platforms=fabric`; no neoforge jar built). The mod's `neoforge` module currently pins its own Uranus hash (`BBb3HOQ5`); for 26.2 a neoforge Uranus (or dropping neoforge) must be decided.
5. **Architectury bundling.** 26.2 fabric.mod.json bundles `META-INF/jars/architectury-fabric.jar` and depends on `fabric-api:*` + `minecraft:26.x`. The 1.21.1 version depended on `fabric:*` + `minecraft:1.21.x`.
6. **Uranus.init() / UranusClient.process()** entrypoints unchanged (`Uranus.java` identical; client only re-mapped `MinecraftClient`→`Minecraft`, `getEntityById`→`getEntity`).
7. **Mixins.** Common `BuiltinModelItemRendererMixin` removed; fabric-specific `uranus-fabric.mixins.json`/`fabric/mixin/ArmorFeatureRendererMixin` removed (ArmorFeatureRendererMixin now common-only). 26.2 mixin `minVersion 0.8`, `compatibilityLevel JAVA_17` unchanged.

---

## 6. Mod action-items (ordered by impact)

1. **Render-state migration (blocking, largest):** `TabulaModel`, `AdvancedEntityModel`, `BasicEntityModel`, `ITabulaModelAnimator`, `HideableLayer`, `ArmorModelBase`, `IArmorRendererBase` — re-type all `Entity` generics to render states, pass `ModelPart` roots, replace `render`/`setupAnim` overrides, rewrite armor/item renderers for `SubmitNodeCollector`/`RenderType`.
2. **Reimplement deleted pathfinding** for dragons/hippogryphs and `CyclopsNavigation` (or accept vanilla `PathNavigation` degradation), and **collision** for DeathWorm.
3. **Reimplement custom item rendering** (`DynamicItemRenderer` replacement).
4. **Dependency:** point fabric/common to the 26.2 Modrinth hash (待确认); resolve neoforge fate.
5. Low-risk: none for `RandomHelper`, `Event`, events, `VecUtil`, `BlockUtil`, `EntityUtil`, `FoodUtils`, `MemorizeSupplier`, `ServerHelper`, `ShapeBuilder`, animation classes.
