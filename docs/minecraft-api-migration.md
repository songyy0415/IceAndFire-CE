# Minecraft API 迁移文档：1.21.1 (Mojmap) → 26.2

> 适用对象：IceAndFire-CE 迁移项目（Architectury，Fabric 目标）。
> 本文件仅覆盖 **该模组实际使用到** 的 Minecraft API 变化，不含全部改动。
> 所有结论基于 `docs/.research/minecraft.md`、`docs/.research/common-arch.md`、`docs/.research/fabric-module.md` 三份研究材料，
> 并已对照 `D:/IceAndFire-CE/common/src` 与 `D:/IceAndFire-CE/fabric/src` 实际源码核实。
> 凡无法完全核实处均标注 **待确认**。

## 0. 适用范围与总览

- 基线：Mojmap 命名（1.21.1 参考树 `D:/aiminecraftdev/minecraft1.21.1mojmap`，26.2 参考树 `D:/aiminecraftdev/minecraft26.2`）。
- **最重要的结构性事实**：26.2 用「渲染状态提取模型」（`EntityRenderState` / `GuiRenderState` / `extractRenderState` + `submit` / `SubmitNodeCollector`）替代了 1.21.1 的 **立即模式渲染管线**（`RenderSystem` 全局状态 + `GuiGraphics` + `EntityRenderer.render`）。
  模组绝大多数客户端破坏可追溯到这一处架构改动。其次是 `MultiBufferSource` 与 `ItemRenderer` 在 26.2 中 **被彻底移除**（不是移动）。
- 网络与注册大量走 Architectury 抽象（`NetworkManager`、`DeferredRegister`），本文件只记录直接依赖 MC 类的情况。

---

## 1. ResourceLocation → Identifier（更名，API 一致）

| 项 | 旧 API (1.21.1) | 新 API (26.2) | 原因 |
|---|---|---|---|
| 类名 | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | Mojang 更名，`ResourceLocation` 在 26.2 中已不存在 |
| 静态工厂 | `ResourceLocation.withDefaultNamespace(...)` | `Identifier.withDefaultNamespace(...)` | 同签名 |
| 静态工厂 | `ResourceLocation.fromNamespaceAndPath(...)` | `Identifier.fromNamespaceAndPath(...)` | 同签名 |
| 常量 | `ResourceLocation.DEFAULT_NAMESPACE` | `Identifier.DEFAULT_NAMESPACE` | 同签名 |

**修改位置**：全工程数百处 `import net.minecraft.resources.ResourceLocation` 的机械替换。fabric 模块直接受影响：`fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java:22`（`registerBuiltinResourcePack` 参数）、`fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java:17-19`（`AttachmentRegistry.create` 参数）。common 模块全部文件。

**原因**：纯机械更名，无行为变化。

---

## 2. Component / Text（稳定）

| 项 | 判定 | 26.2 依据 |
|---|---|---|
| `net.minecraft.network.chat.Component` | [稳定] | `Component.translatable` 26.2 `Component.java:139` 仍在 |
| `Component.translatable(...)` | [稳定] | 同上 |
| `ChatFormatting` | [稳定] | 26.2 保留 |

**修改位置**：无。fabric `ModMenu.java`、common 各处 `Component.translatable` 不受影响。

---

## 3. Registries / ResourceKey / Holder / HolderSet（核心稳定）

| 项 | 判定 | 说明 |
|---|---|---|
| `net.minecraft.core.registries.Registries` | [稳定] | 路径不变 |
| `net.minecraft.core.Registry`（含 `wrapAsHolder`） | [稳定] | 26.2 `Registry.java:137` |
| `net.minecraft.core.registries.BuiltInRegistries` | [稳定] | 路径不变 |
| `ResourceKey.create(RegistryKey, ResourceLocation)` | [稳定] | 仍在（注意第二参数 26.2 变 `Identifier`，见 §1） |
| `Holder<...>` / `HolderSet` 使用方式 | [稳定] | 见 §4 `MobEffectInstance(Holder<MobEffect>)` |
| `ReloadableServerRegistries.Holder.getLootTable(ResourceKey<...>)` | [稳定] | `DragonBaseEntity.java:1276` 的 `this.getServer().reloadableRegistries().getLootTable(...)` 在 26.2 仍在 |

**修改位置**：无（仅 `ResourceKey.create` 第二实参因 §1 变为 `Identifier`）。

---

## 4. Level / ServerLevel / ClientLevel

| 项 | 判定 | 说明 |
|---|---|---|
| `ClientLevel.entityStorage` | [稳定] | 26.2 `ClientLevel.java:147`；AW 已开放 |
| `ClientLevel.entityStorage.getEntityGetter()` | [稳定] | 26.2 `ClientLevel.java:1069`；`ClientEvents.java:80,82` 使用 |
| `ClientLevel.entitiesForRendering()` | [稳定] | 26.2 `ClientLevel.java:444`；但 `LevelRenderer.renderLevel` 已不存在（见 §7.3） |
| `ServerLevel`（含 `WorldGenRegion`） | [稳定] | `WorldGenRegion.ensureCanWrite(BlockPos)` 26.2 `WorldGenRegion.java:272` 仍在，`currentlyGenerating` 字段仍在（line 75） |
| `Level`/`ServerLevel` 继承体系 | [稳定] | `Mob.dropFromLootTable`、`getBaseExperienceReward` 的新增 `ServerLevel` 参数见 §6 |

**修改位置**：无直接类更名；但注意 `WorldRendererMixin`（渲染期实体遍历）依赖 `entitiesForRendering()` 的调用点，该调用点本身已因 `LevelRenderer.renderLevel` 移除而迁移到 `LevelExtractor`（见 §7.3）。

---

## 5. ItemStack / DataComponents（稳定）

| 项 | 判定 |
|---|---|
| `ItemStack.set(DataComponentType, T)` / `get(...)` / `getOrDefault(...)` / `has(...)` / `is(TagKey)` / `copy()` / `isEmpty()` / `getItem()` | [稳定] |
| `DataComponents.FOOD` / `ENCHANTMENTS` | [稳定]（`HippogryphEntity.java:317` 用 `itemstack.has(DataComponents.FOOD)`；`TideTridentItemRenderer` 用 `stack.set(DataComponents.ENCHANTMENTS, ...)`） |
| `ItemStack.getUseDuration(LivingEntity)` / `getUseItemRemainingTicks()` | [稳定]（26.2 均在） |
| `MobEffectInstance(Holder<MobEffect>, int, ...)` | [稳定]（26.2 构造在 `MobEffectInstance.java:52`） |
| `MobEffectInstance.getEffect()` → `Holder<MobEffect>` | [稳定]（26.2 `MobEffectInstance.java:199`） |

**修改位置**：无。`ClientEvents.java:85`、`LivingEntityMixin.java:35` 中 `effect.getEffect().value() instanceof FrozenStatusEffect` 的 `Holder.value()` 用法在 26.2 有效。

**待确认**：`ItemStack` 内部 `getItemInHand` / `getMainHandItem`（26.2 `LivingEntity.java:2214`）已核对存在。

---

## 6. Entity / LivingEntity / Mob（2 处签名变化 + 1 处方法移除）

### 6.1 稳定成员

`Mob.goalSelector` / `targetSelector`（26.2 `Mob.java:134-135`）、`NodeEvaluator.mob` 字段（`NodeEvaluator.java:16`）、`CombatTracker.getMostSignificantFall()`（`CombatTracker.java:108`，私有，经 AW）、`LivingEntity.tick()`、`swing(InteractionHand, boolean)`（`LivingEntity.java:2026`）、`refreshDirtyAttributes()`（`LivingEntity.java:1123`）、`EntityType.Builder.of(...)`、`Entity.spawnAtLocation(ItemLike)`、`Chicken.aiStep()`（见 §13 包移动）、`MoverType`、`TemptGoal`、`ExperienceOrb.award(ServerLevel, Vec3, int)` 均 [稳定]。

`Entity.getTicksFrozen()` / `getTicksRequiredToFreeze()` — 从 `Player`/`LocalPlayer` 上移到 `Entity`（26.2 `Entity.java:2821,2838`）。`InGameHudMixin.java:32` 的目标串 `LocalPlayer;getTicksFrozen()I` 是继承方法，仍能解析，无需改动。

### 6.2 `LivingEntity.getBaseExperienceReward` — 签名变化

| 项 | 旧 API | 新 API |
|---|---|---|
| 方法 | `protected int getBaseExperienceReward()` | `protected int getBaseExperienceReward(ServerLevel level)`（26.2 `LivingEntity.java:608`） |

**修改位置**（模组中共 **14 处覆写 + 1 处调用**，比研究材料列出的 4 处更多，实际覆盖为）：
- 覆写：`TrollEntity.java:241`、`StymphalianBirdEntity.java:119`、`SirenEntity.java:131`、`SeaSerpentEntity.java:166`、`PixieEntity.java:140`、`HippogryphEntity.java:171`、`HippocampusEntity.java:158`、`GorgonEntity.java:157`、`DragonBaseEntity.java:629`、`DeathWormEntity.java:215`、`CyclopsEntity.java:113`、`CockatriceEntity.java:137`、`AmphithereEntity.java:824`（均在 `common/src/main/java/com/iafenvoy/iceandfire/entity/`）。
- 调用：`StoneStatueEntity.java:194` `livingEntity.getBaseExperienceReward()`；`GorgonEntity.java:175` `this.getBaseExperienceReward()`。
- AW：`common/src/main/resources/iceandfire.accesswidener:13` 描述符从 `()I` 改为 `(Lnet/minecraft/server/level/ServerLevel;)I`。

**原因**：26.2 需要 `ServerLevel` 来查经验奖励表。

### 6.3 `Mob.dropFromLootTable` — 签名变化

| 项 | 旧 API | 新 API |
|---|---|---|
| 方法 | `protected void dropFromLootTable(DamageSource, boolean)` | `protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled)`（26.2 `Mob.java:412`） |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/mixin/MobEntityMixin.java:29-30` `@Inject(method = "dropFromLootTable", ...)` 参数改为 `(ServerLevel, DamageSource, boolean)`。

**原因**：26.2 需要 `ServerLevel` 上下文。这是容易被遗漏的一处。

### 6.4 `LivingEntity.onEffectRemoved` — 移除

| 项 | 旧 API | 新 API |
|---|---|---|
| 方法 | `protected void onEffectRemoved(MobEffectInstance)` | 已移除，拆分为 `onEffectUpdated(MobEffectInstance, boolean doRefreshAttributes, @Nullable Entity source)`（26.2 `LivingEntity.java:1091`）、`onEffectsRemoved(Collection<MobEffectInstance>)`（line 1105）、`onEffectAdded(MobEffectInstance, @Nullable Entity source)`（line 1075） |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/mixin/LivingEntityMixin.java:33` 注入目标 `onEffectRemoved` @ `refreshDirtyAttributes()V` INVOKE 需改指。该注入点语义是「效果移除后刷新属性」，在 26.2 最接近 `onEffectUpdated` 或 `onEffectsRemoved`。**待确认**：具体改写方向（需看 26.2 这三个回调的调用链再定）。

---

## 7. 渲染管线 — 架构重写（EntityRenderState / SubmitNodeCollector）

这是最大的一处破坏。1.21.1 的 `EntityRenderer.render(Entity, float, float, PoseStack, MultiBufferSource, int)` 直接渲染，26.2 改为两段式：`extractRenderState(entity, state, partialTicks)` 提取状态 + `submit(state, PoseStack, SubmitNodeCollector, CameraRenderState)` 提交渲染。**`MultiBufferSource` 在 26.2 整个类已不存在**。

### 7.1 EntityRenderer / LivingEntityRenderer / MobRenderer

| 项 | 旧 API | 新 API |
|---|---|---|
| 类签名 | `EntityRenderer<T extends Entity>` | `EntityRenderer<T extends Entity, S extends EntityRenderState>`（26.2 line 102/161） |
| 渲染方法 | `void render(T, float, float, PoseStack, MultiBufferSource, int)` | `void submit(S, PoseStack, SubmitNodeCollector, CameraRenderState)` + `void extractRenderState(T, S, float)` |
| `EntityRendererProvider.Context` | `getItemInHandRenderer()` | `getItemModelResolver()`（26.2 `EntityRendererProvider.java`） |
| `LivingEntityRenderer` | 三泛型不变 | `LivingEntityRenderer<T, S extends LivingEntityRenderState, M extends EntityModel<? super S>>` |
| `EntityModel` | `setupAnim(entity, limbSwing, ...)` + `renderToBuffer(...)` | 泛型变 `EntityModel<T extends EntityRenderState>`，渲染走 `Model.submit(...)` |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/render/entity/` 下所有实体渲染器（约 40+ 文件，含龙族、地精、塔巴模型等）及 `registry/IafRenderers.java:44-95` 的 `EntityRendererRegistry` 注册。所有模型类（`render/model/*`，基于 TabulaModel 的龙模型等）的 `setupAnim`/`renderToBuffer` 契约都要改造成状态模型。

**待确认**：每个具体模型改造的精确步骤；`EntityRenderDispatcher.render(...)` 实例方法在 26.2 移除后的替代（§13 相关调用）。

### 7.2 玩家渲染 PlayerRenderer → AvatarRenderer（移除/更名）

| 项 | 旧 API | 新 API |
|---|---|---|
| 类 | `net.minecraft.client.renderer.entity.player.PlayerRenderer` | `net.minecraft.client.renderer.entity.player.AvatarRenderer` |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/mixin/PlayerEntityRendererMixin.java:17,19` `@Mixin(PlayerRenderer.class)` + 注入 `render(AbstractClientPlayer, F, F, PoseStack, MultiBufferSource, I)` → 改指 `AvatarRenderer.submit(...)`。

**原因**：26.2 无 `PlayerRenderer` 类。

### 7.3 LevelRenderer（WorldRenderer）— renderLevel 移除

| 项 | 旧 API | 新 API |
|---|---|---|
| 方法 | `LevelRenderer.renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)` | `LevelRenderer.render(GraphicsResourceAllocator, DeltaTracker, boolean, CameraRenderState, Matrix4fc, GpuBufferSlice, Vector4f, boolean)`（26.2 `LevelRenderer.java:154`）；实体迭代移到 `LevelExtractor`（`.../renderer/extract/LevelExtractor.java:231`） |
| 缓冲 | `RenderBuffers.bufferSource()` → `MultiBufferSource.BufferSource` | `MultiBufferSource` 类型已移除 |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/mixin/WorldRendererMixin.java:33` 注入点 `renderLevel` @ `ClientLevel;entitiesForRendering()`（该目标在 26.2 移到 `LevelExtractor`）；`:48` `this.renderBuffers.bufferSource()`（返回类型已不存在）。`@Shadow RenderBuffers renderBuffers` 字段仍在（26.2 `LevelRenderer.java:103`）。

### 7.4 BlockEntityRenderer — 重写

| 项 | 旧 API | 新 API |
|---|---|---|
| 接口 | `BlockEntityRenderer<T>`，`render(T, float, PoseStack, MultiBufferSource, int, int)` | `BlockEntityRenderer<T, S extends BlockEntityRenderState>`：`createRenderState()` / `extractRenderState(...)` / `submit(S, PoseStack, SubmitNodeCollector, CameraRenderState)` |
| 调度器 | `BlockEntityRenderDispatcher.renderItem(BlockEntity, PoseStack, MultiBufferSource, int, int)` | `renderItem` 移除；`submit(S, PoseStack, SubmitNodeCollector, CameraRenderState)`（26.2 `BlockEntityRenderDispatcher.java:95`） |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/render/block/*.java` 全部 7 个 BE 渲染器（`JarBlockEntityRenderer`、`LecternBlockEntityRenderer`、`PodiumBlockEntityRenderer`、`EggInIceBlockEntityRenderer`、`DreadPortalBlockEntityRenderer`、`DreadSpawnerBlockEntityRenderer`、`PixieHouseBlockEntityRenderer`）。`render/item/MiscItemRenderer.java:29-33` 的 `getBlockEntityRenderDispatcher().renderItem(...)` 需换替代。

**待确认**：`MiscItemRenderer.renderItem` 的 26.2 替代（推测走 `ItemModelResolver` / `ItemStackRenderState`）。

### 7.5 ThrownItemRenderer — 类稳定，构造/泛型变

- 26.2 仍在 `net/minecraft/client/renderer/entity/ThrownItemRenderer.java`；构造 `(EntityRendererProvider.Context, float scale, boolean fullBright)` 与 `(Context)`（line 19/26），类变 `ThrownItemRenderer<T extends ThrownItem>` 风格。
- `IafRenderers.java:53,62,64` 的 `ThrownItemRenderer::new` 构造引用在更新泛型后仍可编译。**[待确认]** 泛型参数具体写法。

### 7.6 渲染时“再渲染一个实体”的调用（EntityRenderDispatcher）

26.2 `EntityRenderDispatcher.getRenderer(T)` 仍在（line 91），`renderers` 字段仍在但类型变为 `Map<EntityType<?>, EntityRenderer<?,?>>`（两泛型），`render(...)` 实例方法移除。

**修改位置**：
- `render/entity/feature/DragonRiderFeatureRenderer.java:116` `getEntityRenderDispatcher().render(entity, ...)`（骑乘者渲染）— 破坏。
- `render/block/DreadSpawnerBlockEntityRenderer.java:34` `getEntityRenderDispatcher().render(entity, 0,0,0, 0F, ...)` — 破坏。
- `render/entity/StoneStatueEntityRenderer.java:74` `getEntityRenderDispatcher().renderers.get(...)` — 泛型擦除后 AW 仍可能匹配，但返回类型 `EntityRenderer<?>` 需改为 `EntityRenderer<?,?>`。

**待确认**：26.2 下“渲染一个任意实体”的标准做法（推测：先 `extractRenderState` 再 `submit` 到 `SubmitNodeCollector`）。

---

## 8. ItemRenderer 移除 → ItemModelResolver / ItemStackRenderState

| 项 | 旧 API | 新 API |
|---|---|---|
| 类 | `net.minecraft.client.renderer.entity.ItemRenderer`（`renderStatic(...)`、`getFoilBufferDirect(...)`） | **26.2 无此类**。替代：`net.minecraft.client.renderer.item.ItemModelResolver`（`Minecraft.getItemModelResolver()`，`Minecraft.java:2895`）+ `net.minecraft.client.renderer.item.ItemStackRenderState`（`submit(PoseStack, SubmitNodeCollector, int, int, int)`，line 109）+ `ItemInHandRenderer.renderItem(LivingEntity, ItemStack, ItemDisplayContext, PoseStack, SubmitNodeCollector, int)`（`.../client/renderer/ItemInHandRenderer.java:130`）+ GUI 走 `GuiGraphicsExtractor.item(ItemStack, int, int, int)` |
| `ItemDisplayContext` | 枚举 | [稳定]（`NONE/THIRD_PERSON/FIRST_PERSON/GUI/GROUND/FIXED`，`FIXED`=8） |

**修改位置**（5 处 `getItemRenderer().renderStatic(...)`）：
- `render/block/PodiumBlockEntityRenderer.java:49`
- `render/entity/feature/DragonBannerFeatureRenderer.java:42`
- `render/entity/feature/PixieItemFeatureRenderer.java:34`
- `render/entity/GhostSwordEntityRenderer.java:39`
- `render/item/TideTridentItemRenderer.java:28`（另 `:36` 用 `ItemRenderer.getFoilBufferDirect(...)`，需换 26.2 的 foil/glint 处理）

**原因**：26.2 移除 `ItemRenderer` 类（不只是移动）。

---

## 9. GUI 管线 — 重写（GuiGraphics → GuiGraphicsExtractor）

### 9.1 类与渲染入口

| 项 | 旧 API | 新 API |
|---|---|---|
| 类 | `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor`；状态对象 `net.minecraft.client.renderer.state.gui.GuiRenderState` |
| `Screen` 渲染 | `Screen.render(GuiGraphics, int, int, float)` | `Screen.extractRenderState(GuiGraphicsExtractor, int, int, float)`（26.2 `.../screens/Screen.java:113`） |

**修改位置**（7 个 Screen 的 `render(GuiGraphics, ...)` 覆写）：
`screen/gui/BestiaryScreen.java:121`、`DragonForgeScreen.java:42`、`DragonScreen.java:34`、`HippocampusScreen.java:29`、`HippogryphScreen.java:29`、`LecternScreen.java:180`、`PodiumScreen.java:32`（均在 `common/src/main/java/com/iafenvoy/iceandfire/screen/gui/`）。

### 9.2 GuiGraphics 成员（多数更名/签名变化）

| 1.21.1 `GuiGraphics` | 26.2 `GuiGraphicsExtractor` | 判定 |
|---|---|---|
| `drawString(Font, String, int, int, int, boolean)` | `text(Font, String, int, int, int, boolean)`（line 243） | [更名] |
| `blit(ResourceLocation, ...)` 8-10 参数变体 | `blit(RenderPipeline, Identifier, ...)` — 首参变为 `RenderPipeline`（line 301 示例：`blit(RenderPipeline, Identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight)`） | [签名变化] |
| `blitSprite(...)` | `blitSprite(RenderPipeline, Identifier, ...)` | [签名变化] |
| `renderItem(ItemStack, ...)` | `item(ItemStack, int, int, int)`（lines 886-894） | [更名] |
| `bufferSource()` / `flush()` | 已移除 | [移除] |
| `pose()` | `pose()` 返回 `Matrix3x2fStack`（line 133）—— 2D 变换矩阵，非 `PoseStack` | [签名变化] |

**修改位置**：所有 Screen 的 `blit` / `drawString` / `renderItem` 调用；`screen/TitleScreenRenderManager.java` 的 `blit`（lines 95-110）与 `drawString`（line 122）。

**待确认**：26.2 `blit` 需要的具体 `RenderPipeline` 常量（如 `RenderPipelines.GUI_TEXTURED`）及其精确签名。

### 9.3 游戏内 HUD（Gui）— 目标移除

1.21.1 `Gui.renderCameraOverlays(GuiGraphics, DeltaTracker)`、`renderTextureOverlay(GuiGraphics, ResourceLocation, float)`、`POWDER_SNOW_OUTLINE_LOCATION` 字段在 26.2 **全部移除**（grep 无结果）；26.2 `Gui` 改为 `extractRenderState(DeltaTracker, boolean, boolean)`（line 145），HUD 迁到独立 `Hud` 类（`.../client/gui/Hud.java`）。

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/mixin/InGameHudMixin.java:22,26,32`（`@Shadow renderTextureOverlay`、`@Shadow POWDER_SNOW_OUTLINE_LOCATION`、注入 `renderCameraOverlays` @ `LocalPlayer;getTicksFrozen()I`）三个目标全部失效，需整体重写（大概率改指 `Hud`）。

### 9.4 TitleScreen / 标志 / 全景

| 项 | 旧 API | 新 API |
|---|---|---|
| `TitleScreen.render(GuiGraphics, int, int, float)` | `TitleScreen.extractRenderState(GuiGraphicsExtractor, int, int, float)`（line 289） | |
| `LogoRenderer.renderLogo(GuiGraphics, int, float)` | `LogoRenderer.extractRenderState(GuiGraphicsExtractor, int, float)` | [签名变化] |
| `PanoramaRenderer` | `net.minecraft.client.renderer.Panorama`，`extractRenderState(GuiGraphicsExtractor, int, int)`（line 22） | [更名+签名变化] |
| `SplashRenderer(String)` | `SplashRenderer(Component)`（line 22）；`render(...)` → `extractRenderState(GuiGraphicsExtractor, int, Font, float)`（line 26） | [签名变化] |

**修改位置**：
- `mixin/TitleScreenMixin.java:37` 注入 `TitleScreen.render` @ `LogoRenderer.renderLogo(GuiGraphics,IF)` INVOKE → 目标签名变化。
- `mixin/RotatingCubeMapRendererMixin.java:13,18` `@Mixin(PanoramaRenderer.class)` → `Panorama`，注入 `render(GuiGraphics, int, int, float, float)` → `extractRenderState`。
- `screen/TitleScreenRenderManager.java:56` `new SplashRenderer(String)` → `new SplashRenderer(Component)`。

### 9.5 RenderSystem 立即模式状态 — 移除

26.2 `RenderSystem` 仍在 `com.mojang.blaze3d.systems.RenderSystem`，但 `setShader`、`setShaderColor`、`setShaderTexture`、`enableBlend`、`disableBlend`、`blendFuncSeparate`、`defaultBlendFunc`、`enableDepthTest`、`disableDepthTest`、`viewport` **全部移除**；仅剩 `getDevice()`、`getModelViewStack()`、`setShaderFog`、`setShaderLights` 等 GpuDevice/RenderPass 取向 API。

**修改位置**（共 34 处 `RenderSystem.` 调用点）：
- `screen/TitleScreenRenderManager.java:93-113,119-120`（setShaderColor / enableBlend / disableBlend）
- `screen/gui/LecternScreen.java:95-101,128,130,136`（setShader(GameRenderer::getPositionTexShader)、setShaderColor、viewport）
- `screen/gui/HippogryphScreen.java:37-38`、`screen/gui/DragonScreen.java:42`（setShader、setShaderColor）
- `screen/gui/BestiaryScreen.java:136,146,697-721,808`（enableDepthTest/disableDepthTest/setShaderTexture）
- `screen/gui/bestiary/IndexPageButton.java:24-25`（enableBlend/enableDepthTest）
- `registry/IafRenderLayers.java:13-18`（enableBlend/blendFuncSeparate/disableBlend/defaultBlendFunc）

另：`com.mojang.blaze3d.platform.Lighting.setupFor3DItems()` 静态在 26.2 移除（`Lighting` 变为实例 `AutoCloseable`）。`LecternScreen.java:132` 使用，破坏。

---

## 10. GameRenderer / ShaderInstance / 后处理

| 项 | 旧 API | 新 API |
|---|---|---|
| 后处理加载 | `GameRenderer.loadEffect(ResourceLocation)`、`currentEffect()`、`shutdownEffect()` | 全部移除。替代：`Minecraft.getInstance().getShaderManager()` → `net.minecraft.client.renderer.ShaderManager`（`Minecraft.java:2561`），`@Nullable PostChain getPostChain(Identifier, Set<Identifier>)`（`ShaderManager.java:186`）；post-chain 从 `post_effect/` JSON（`FileToIdConverter.json("post_effect")`）加载 |
| 自定义着色器 | `net.minecraft.client.renderer.ShaderInstance`（26.2 无此类） | `ShaderManager` / `ShaderDefines` / `RenderPipeline` |
| `GameRenderer.renderLevel(DeltaTracker)` | 26.2 仍在（line 522），但内部 `Camera.setup(...)` 移除，相机操作走 `CameraRenderState` | |

**修改位置**：
- `render/SirenShaderRenderHelper.java:29,35,40`（`renderer.currentEffect()`、`renderer.loadEffect(SIREN_SHADER)`、`renderer.shutdownEffect()`）整文件重写。SIREN_SHADER 资源路径 `shaders/post/siren.json` 26.2 需移到 `post_effect/siren.json`。
- `mixin/GameRendererMixin.java:30` 注入 `renderLevel` @ `Camera.setup(...)` INVOKE → 目标移除，改指新相机提取调用。
- `mixin/GameRendererMixin.java:35-41` `@Inject(method="reloadShaders")` @ `ShaderInstance.<init>(ResourceProvider,String,VertexFormat)` — `reloadShaders` 与 `ShaderInstance` 均不存在，整个自定义着色器注册（`RenderVariables.DREAD_PORTAL_PROGRAM`、`IafRenderLayers.DREAD_PORTAL_PROGRAM`）需按 26.2 ShaderManager/ShaderDefines 模型重做。

**待确认**：26.2 注册自定义着色器（如 `rendertype_dread_portal`）的确切机制。

---

## 11. 粒子系统 — 重大重做

### 11.1 `ParticleEngine.SpriteParticleRegistration` — 移除

1.21.1 嵌套接口 `ParticleEngine.SpriteParticleRegistration<T>`（`ParticleEngine.java:572`）在 26.2 移除，替代为 `ParticleProvider.Sprite<T extends ParticleOptions>`（`net/minecraft/client/particle/ParticleProvider.java`）。

**修改位置**：
- `common/src/main/java/com/iafenvoy/iceandfire/impl/ParticleProviderHolder.java:20,28,34`（包装 `SpriteParticleRegistration<T>` 与 `ParticleProvider<T>`）。
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java:20` 的 `(t, f) -> ParticleFactoryRegistry.getInstance().register(t, f::create)` lambda 是 `SpriteParticleRegistration`，改按 26.2 `ParticleProvider.Sprite` 契约。
- AW：`iceandfire.accesswidener:2` `accessible class .../ParticleEngine$SpriteParticleRegistration` 条目失效（类已不存在），改指/移除。
- Fabric API：`ParticleFactoryRegistry` 在 26.2 更名 `ParticleProviderRegistry`（`net/fabricmc/fabric/api/client/particle/v1/ParticleProviderRegistry.java`），`register(type, ParticleProvider)` 与 `register(type, PendingParticleProvider)` 两个重载保留，`PendingParticleFactory` → `PendingParticleProvider`、`FabricSpriteProvider` → `FabricSpriteSet`。

### 11.2 `ParticleProvider.createParticle` — 增加 RandomSource

1.21.1：`Particle createParticle(T, ClientLevel, double x, double y, double z, double xAux, double yAux, double zAux)`
26.2：`@Nullable Particle createParticle(T, ClientLevel, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random)`（`ParticleProvider.java:9`）

**修改位置**：`registry/IafRenderers.java:98-108` 中所有工厂方法引用（`BloodParticle::factory`、`DragonFlameParticle::factory`、`DragonFrostParticle::factory`、`DreadPortalParticle::factory`、`DreadTorchParticle::factory`、`GhostAppearanceParticle.factory()`、`HydraBreathParticle::factory`、`PixieDustParticle::factory`、`SerpentBubbleParticle::factory`、`SirenMusicParticle::factory`）对应的 `particle/*.java` 工厂方法均需增加 `RandomSource` 参数。

### 11.3 `TextureSheetParticle` → `SingleQuadParticle`（更名）

**修改位置**：`particle/` 下所有继承 `TextureSheetParticle` 的粒子类（`BloodParticle`、`DragonFlameParticle`、`DragonFrostParticle`、`DreadPortalParticle`、`DreadTorchParticle`、`HydraBreathParticle`、`PixieDustParticle`、`SerpentBubbleParticle`、`SirenMusicParticle`）。import + 构造变化。

### 11.4 `ParticleRenderType` — 接口改 record，`CUSTOM` 移除

1.21.1：`ParticleRenderType` 是接口，`CUSTOM` 为匿名实例。
26.2：`ParticleRenderType` 是 record `(String name, String shorthand)`，常量 `SINGLE_QUADS`、`ITEM_PICKUP`、`ELDER_GUARDIANS`、`NO_RENDER`，**无 `CUSTOM`**。各粒子 `getRenderType()` 返回的 `PARTICLE_SHEET_LIT` 常量名需核对。

**修改位置**：`particle/GhostAppearanceParticle.java:67` `return ParticleRenderType.CUSTOM;` — 需改写为 26.2 合法类型。

**待确认**：26.2 中完全自定义四边形粒子如何表达（推测经 `SingleQuadParticle` + `ParticleRenderType` 选择）。

### 11.5 `SimpleParticleType(boolean)` 构造 — 稳定

26.2：`protected SimpleParticleType(final boolean overrideLimiter)`（`.../core/particles/SimpleParticleType.java:11`）。[稳定] AW line 20 保留。

---

## 12. Input：KeyMapping / Options / CameraType

### 12.1 KeyMapping 构造 — 签名变化

| 项 | 旧 API | 新 API |
|---|---|---|
| 构造 | `KeyMapping(String name, int key, String category)`（`.../client/KeyMapping.java:87`） | `KeyMapping(String name, int key, KeyMapping.Category category)`；`KeyMapping.Category` 是类，含 `MOVEMENT`、`GAMEPLAY` 等常量（`.../KeyMapping.java:204,207`） |

**修改位置**：`common/src/main/java/com/iafenvoy/iceandfire/registry/IafKeybindings.java:10-13` 第三个参数 `"key.categories.gameplay"` → `KeyMapping.Category.GAMEPLAY`。（Architectury `KeyMappingRegistry.register` 不变。）

### 12.2 Options / 其他输入

`Options.getCameraType()` / `setCameraType(CameraType)`（26.2 `Options.java:1921,1925`）、`keyShift` / `keyJump`（lines 659-660）、`renderDistance()`（line 1007）、`languageCode`、`KeyMapping.isDown()`（line 105）、`InputConstants.isKeyDown(Window, int)`（`com/mojang/blaze3d/platform/InputConstants.java:192`）、`Minecraft.getInstance().getWindow()` — 均 [稳定]。`Window` 移包到 `com.mojang.blaze3d.platform.Window` [移动]，`getGuiScale/getWidth/getHeight` 保留。`CameraType` 不变，`isFirstPerson()` 仍在。

**修改位置**：无（`ClientEvents.java:42-46,56,65-69` 的 `options.getCameraType()`、`keyShift.isDown()` 等均有效）。

---

## 13. 网络与包编解码（Architectury 封装，MC 侧稳定）

所有网络走 `dev.architectury.networking.NetworkManager`，payload 是纯 MC `CustomPacketPayload` record（`network/payload/*.java` 10 个文件），codec 由 `ByteBufCodecs.fromCodec(RecordCodecBuilder...)` 构建。

- `FriendlyByteBuf` 26.2 仍在（`.../network/FriendlyByteBuf.java`，`readResourceKey` line 590）。[稳定]
- `StreamCodec` / `ByteBufCodecs` / `CustomPacketPayload` / `RecordCodecBuilder` / `UUIDUtil` 26.2 均在（已核实 `LightningBoltS2CPayload`、`DragonControlC2SPayload`、`MultipartInteractC2SPayload` 等源码）。
- C2S/S2C 注册走 `NetworkManager.registerReceiver` / `registerS2CPayloadType`，属 Architectury 范畴。

**修改位置**：无（除非 Architectury 26.2 版本改变其网络 API —— 待确认）。

---

## 14. Screen / Menu 注册

| 项 | 旧 API | 新 API |
|---|---|---|
| `MenuType` 构造 | `public MenuType(MenuSupplier, FeatureFlagSet)` | **private**（26.2 `MenuType.java:50`） |

**修改位置**：`registry/IafScreenHandlers.java:24-25` 两处 `new MenuType<>(..., FeatureFlags.VANILLA_SET)`（`PODIUM_SCREEN`、`IAF_LECTERN_SCREEN`）将无法编译，改用 Architectury `MenuRegistry.ofExtended`（其余 5 个已是）或 26.2 提供的注册路径。

| `MenuScreens.register(...)` | `public static` | **private static**（26.2 `MenuScreens.java:53`） |

**修改位置**：`registry/IafScreenHandlers.java:32-40` `registerGui()` 的 7 处 `MenuScreens.register` 改走 Architectury `ScreenRegistry.register(...)` 或 26.2 公共注册路径。

**待确认**：Architectury 26.2 的 `ScreenRegistry` API 是否可用/签名。

---

## 15. 杂项：PoiTypes / NodeEvaluator / 其它

| 项 | 判定 | 说明 |
|---|---|---|
| `PoiTypes.TYPE_BY_STATE` | [签名/语义变化] | 字段仍在（26.2 `PoiTypes.java:59`），但值类型从 `Map<BlockState, PoiType>` 变为 `Map<BlockState, Holder<PoiType>>`。`IafTrades.java:36` `PoiTypes.TYPE_BY_STATE.put(state, BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(SCRIBE_POI.get()))` 的右值已是 `Holder<PoiType>`，恰与新值类型匹配，语义上反而自洽。AW line 19 擦除描述符仍匹配，可保留 |
| `NodeEvaluator.mob` | [稳定] | `NodeEvaluator.java:16` |
| `EntityTypeTags.UNDEAD` / `ItemTags.MEAT` / `BlockTags.SAND` / `Ingredient.of(...)` | [稳定] | `EntityTypeTags.java:11`、`ItemTags.java:89` |
| `TemptGoal` | [稳定] | 26.2 存在 |
| `PoseStack`、`DeltaTracker`、`VertexConsumer`、`OverlayTexture` | [稳定] | `OverlayTexture` 在 `.../renderer/texture/OverlayTexture.java` |
| `Camera.getMaxZoom(float)` / `Camera.move(...)` | [稳定] | `Camera.java:294/331`（私有/受保护，经 AW）；`ClientEvents.onCameraSetup` 使用 |
| `Screen.renderables` 字段 | [稳定] | AW line 18，擦除描述符匹配 |
| `SimpleParticleType(Z)` | [稳定] | AW line 20 |

---

## 16. 26.2 中被移动 / 移除 / 重做的类依赖清单

| 类/成员 | 26.2 状态 | 影响 |
|---|---|---|
| `net.minecraft.resources.ResourceLocation` | 移除 → `Identifier` | 全工程 import |
| `com.mojang.blaze3d.vertex.MultiBufferSource` | **移除**（无文件） | `render/*`、`mixin/LivingEntityRendererMixin`、`ClientEvents.onPostRenderLiving`、`WorldRendererMixin:48` |
| `net.minecraft.client.renderer.entity.ItemRenderer` | **移除** | 5 个 `renderStatic` 调用点 + `getFoilBufferDirect` |
| `net.minecraft.client.renderer.entity.player.PlayerRenderer` | 移除 → `AvatarRenderer` | `PlayerEntityRendererMixin` |
| `net.minecraft.client.renderer.entity.ThrownTrident` / `AbstractArrow` | 移包 → `world/entity/projectile/arrow/` | `TideTridentEntity.java:16` import；AW lines 6-10 类名 |
| `net.minecraft.world.entity.animal.Chicken` | 移包 → `animal/chicken/Chicken` | `ChickenMixin` import |
| `net.minecraft.Util` | 移包 → `net.minecraft.util.Util` | `ChunkRegionMixin:22` mixin 目标串 `Lnet/minecraft/Util;...` 改 `net/minecraft/util/Util` |
| `net.minecraft.client.gui.GuiGraphics` | 移除 → `GuiGraphicsExtractor` | 7 个 Screen + `TitleScreenRenderManager` + `InGameHudMixin` |
| `net.minecraft.client.renderer.ShaderInstance` | 移除 | `RenderVariables`、`GameRendererMixin`、`IafRenderLayers` |
| `net.minecraft.client.renderer.PanoramaRenderer` | 更名 → `Panorama` | `RotatingCubeMapRendererMixin` |
| `ParticleEngine.SpriteParticleRegistration` | 移除 → `ParticleProvider.Sprite` | `ParticleProviderHolder`、AW line 2 |
| `TextureSheetParticle` | 更名 → `SingleQuadParticle` | `particle/*` 9 个类 |
| `Gui.renderCameraOverlays` / `renderTextureOverlay` / `POWDER_SNOW_OUTLINE_LOCATION` | 移除 | `InGameHudMixin`（HUD 迁到 `Hud`） |
| `GameRenderer.loadEffect` / `currentEffect` / `shutdownEffect` / `reloadShaders` | 移除 | `SirenShaderRenderHelper`、`GameRendererMixin`、AW line 17 |
| `Camera.setup(...)` | 移除 → `CameraRenderState` | `GameRendererMixin:30` |
| `LevelRenderer.renderLevel` | 移除 → `render(...)` | `WorldRendererMixin` |
| `BlockEntityRenderDispatcher.renderItem` | 移除 → `submit(...)` | `MiscItemRenderer` |
| `Lighting.setupFor3DItems()` | 移除（`Lighting` 变实例 AutoCloseable） | `LecternScreen.java:132` |
| `SplashRenderer(String)` | 签名变 → `SplashRenderer(Component)` | `TitleScreenRenderManager.java:56` |
| `KeyMapping(String,String)` | 签名变 → `KeyMapping.Category` | `IafKeybindings.java:10-13` |
| `MenuType` 构造 / `MenuScreens.register` | private | `IafScreenHandlers.java:24-25,32-40` |
| `LivingEntity.getBaseExperienceReward()` | 加参 `(ServerLevel)` | 14 覆写 + 2 调用 + AW line 13 |
| `Mob.dropFromLootTable` | 加参 `(ServerLevel, ...)` | `MobEntityMixin.java:29` |
| `LivingEntity.onEffectRemoved` | 移除 → `onEffectUpdated`/`onEffectsRemoved` | `LivingEntityMixin.java:33` |

---

## 17. Access Widener 更新汇总（`common/src/main/resources/iceandfire.accesswidener`，fabric 下为同文件副本）

| 行 | 条目 | 26.2 动作 |
|---|---|---|
| 2 | `ParticleEngine$SpriteParticleRegistration` | 类已移除 → 改指/移除（`ParticleProvider$Sprite`） |
| 3 | `EntityRenderDispatcher renderers` | 字段仍在，类型 `Map<EntityType<?>, EntityRenderer<?,?>>`（擦除后描述符仍匹配）— 核验 |
| 6-10 | `ThrownTrident`/`AbstractArrow` | 移包 `world/entity/projectile/arrow/` → 更新类名 |
| 13 | `LivingEntity getBaseExperienceReward ()I` | 描述符改 `(Lnet/minecraft/server/level/ServerLevel;)I` |
| 17 | `GameRenderer loadEffect` | 已移除 → 删除 |
| 其余 (1,3-5,11-12,14-16,18-20) | `Mob goalSelector/targetSelector`、`NodeEvaluator.mob`、`ClientLevel.entityStorage`、`CombatTracker.getMostSignificantFall`、`Camera getMaxZoom/move`、`Screen renderables`、`PoiTypes TYPE_BY_STATE`、`SimpleParticleType <init>(Z)` | 26.2 均存在，保留 |

---

## 18. 待确认清单

1. `LivingEntityMixin.java:33`（`onEffectRemoved` → `onEffectUpdated` / `onEffectsRemoved`）的具体改写目标。
2. 26.2 注册自定义着色器（`rendertype_dread_portal`）的确切机制（ShaderManager/ShaderDefines 用法）。
3. 26.2 `blit` / `blitSprite` 所需的具体 `RenderPipeline` 常量（如 `RenderPipelines.GUI_TEXTURED`）及其签名。
4. `ParticleRenderType.CUSTOM` 在 26.2 的替代表达（自定义四边形粒子）。
5. `MiscItemRenderer.renderItem`（`BlockEntityRenderDispatcher.renderItem`）在 26.2 的替代。
6. 26.2 “渲染一个任意实体”的标准做法（替代 `EntityRenderDispatcher.render(...)` 的 `extractRenderState`+`submit` 流程），影响 `DragonRiderFeatureRenderer:116`、`DreadSpawnerBlockEntityRenderer:34`。
7. `EntityModel` / 各模型类的状态化改造精确步骤。
8. Architectury 26.2 的 `NetworkManager`、`ScreenRegistry` API 是否变化。
9. `EntityRendererProvider.Context` 在 `IafRenderers.java` 各渲染器构造中改用 `getItemModelResolver()` 的具体接线。

---

## 19. 迁移优先级（按影响面排序）

1. **渲染状态管线**（`EntityRenderer`/`LivingEntityRenderer`/`BlockEntityRenderer`/`EntityModel`/`PlayerRenderer`/`LevelRenderer.renderLevel`/`EntityRenderDispatcher.render`）— 触及 `render/`、`mixin/` 与全部 Screen，约 40+ 文件。这是架构基石。
2. **GuiGraphics → GuiGraphicsExtractor + RenderPipeline blit + Screen.extractRenderState** — 7 个 Screen + `TitleScreenRenderManager` + mixin；`RenderSystem` 立即模式调用（setShaderColor/blend/depth/viewport）全灭。
3. **RenderSystem.setShader / ShaderInstance / GameRenderer.getPositionTexShader / loadEffect/currentEffect/shutdownEffect** — `SirenShaderRenderHelper`、`LecternScreen`、`HippogryphScreen`、`DragonScreen`、`GameRendererMixin`、`IafRenderLayers`。
4. **粒子** — `SpriteParticleRegistration` → `ParticleProvider.Sprite`、`createParticle` +`RandomSource`、`TextureSheetParticle` → `SingleQuadParticle`、`ParticleRenderType.CUSTOM` 移除。
5. **ItemRenderer → ItemModelResolver / ItemStackRenderState** — 5 个 `renderStatic` 调用点。
6. **MultiBufferSource / RenderBuffers.bufferSource()** — 类型从树中移除，改为 `SubmitNodeCollector` + render states。
7. **KeyMapping 分类 String → `KeyMapping.Category`** — `IafKeybindings`。
8. **MenuType 私有构造 + MenuScreens.register 私有** — `IafScreenHandlers`（改用 Architectury 注册）。
9. **`getBaseExperienceReward(ServerLevel)`、`dropFromLootTable(ServerLevel,...)`、`onEffectRemoved` 移除**。
10. **Entity / Util / Chicken 移包**（import + mixin 目标串）。
11. **ResourceLocation → Identifier**（机械更名）。

值得注意的“坑”：
- `MultiBufferSource` 与 `ItemRenderer` 在 26.2 是**彻底移除**，不是移动。
- `RenderSystem` 包名没变但失去全部立即模式状态 API。
- `ParticleRenderType` 从接口变 record，`CUSTOM` 消失。
- `Lighting.setupFor3DItems()` 静态消失。
- `SplashRenderer(String)` → `SplashRenderer(Component)`。
- `Mob.dropFromLootTable` 多了 `ServerLevel` 首参 —— 很容易漏。
- `getBaseExperienceReward` 的覆写点有 **14 处**（研究初稿只列了 4 处），必须全量排查。
