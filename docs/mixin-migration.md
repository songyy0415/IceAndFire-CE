# Mixin 迁移文档（MC 1.21.1 Mojmap -> MC 26.2 Mojmap）

> 范围：IceAndFire-CE 项目中全部 11 个 Mixin（位于 `common/src/main/java/com/iafenvoy/iceandfire/mixin/`）。
> 所有 1.21.1 / 26.2 结论均已对照 `D:/aiminecraftdev/minecraft1.21.1mojmap` 与
> `D:/aiminecraftdev/minecraft26.2` 两份参考源码树逐一核实。
> 凡无法完全确认的 26.2 目标一律标注「待确认 (to be confirmed)」。

**核心结论：** 11 个 Mixin 中，**没有任何一个可以原样迁移**到 26.2；2 个仅需一行/个别注入点重定位
（ChunkRegionMixin、LivingEntityMixin 的大部分），7 个因 26.2 渲染状态抽取（render-state / GPU）架构重构而
**失效，需要设计级重写**。最根本的问题是 26.2 用「`extractRenderState` + `submit`」管线替代了旧即时模式渲染
（`EntityRenderer.render`、`GuiGraphics`、`MultiBufferSource` 全部被替换）。

---

## 一、Mixin 配置文件分析

文件：`common/src/main/resources/iceandfire.mixins.json`

| 配置项 | 当前值 | 说明 / 26.2 是否需改 |
|---|---|---|
| `required` | `true` | 保持。任何 Mixin 应用失败都会导致客户端直接崩溃，因此迁移必须一次性全部到位。 |
| `minVersion` | `"0.8"` | Mixin 最低版本。项目已使用 MixinExtras（`com.llamalad7.mixinextras.sugar.Local`），需保证实际引入的 mixin 版本 >= 0.8。26.2 的 Fabric/Loom 链路一般自带更高版本，**保留即可**。 |
| `package` | `com.iafenvoy.iceandfire.mixin` | 不变。 |
| `compatibilityLevel` | `JAVA_21` | 保留。26.2 目标仍为 Java 21+（项目 `gradle.properties` 中 Java 版本未降级），**无需改动**。若迁移后仍沿用旧 Mixin 版本导致报错，仅需升级到支持 Java 21 的版本。 |
| `mixins`（common） | `ChickenMixin`、`ChunkRegionMixin`、`LivingEntityMixin`、`MobEntityMixin` | 4 个类名全部保留（类名不随 Minecraft 改名）。 |
| `client`（common） | `GameRendererMixin`、`InGameHudMixin`、`LivingEntityRendererMixin`、`PlayerEntityRendererMixin`、`RotatingCubeMapRendererMixin`、`TitleScreenMixin`、`WorldRendererMixin` | 7 个类名全部保留。 |
| `injectors.defaultRequire` | `1` | 保留。注意：该值意味着所有注入点必须匹配到目标，26.2 中任何未同步更新的注入点都会直接报错（与 `required: true` 叠加）。 |

> 结论：`iceandfire.mixins.json` 本身**不需要改动**（类名、包名、minVersion、compatibilityLevel 均不变），
> 需要修改的是各个 Mixin 类内部的 `@Mixin` 类、`@Inject` 的 `method`/`target` 描述符字符串与 handler 签名。

---

## 二、逐 Mixin 迁移方案

> 以下「旧target / 新target」均为对应 Minecraft 类的**点分隔全名**（即 `@Mixin` 中类名，
> 或描述符中类路径的语义化写法），迁移时请替换 import / 描述符字符串中的路径。
> 涉及描述符字符串（`Lnet/minecraft/...;`）处会显式给出。

---

Mixin:
ChickenMixin
旧target:
net/minecraft/world/entity/animal/Chicken
新target:
net/minecraft/world/entity/animal/chicken/Chicken
解决方案:
- 类所在包发生移动：`animal.Chicken` -> `animal.chicken.Chicken`（26.2 源码 `net/minecraft/world/entity/animal/chicken/Chicken.java`，`public class Chicken extends Animal`）。仅 `@Mixin(Chicken.class)` 的 import 需要替换，Mixin 类名不变。
- 注入点失效：26.2 的 `aiStep()`（源码第 118 行）不再调用 `spawnAtLocation(ItemLike)`，而是走
  `if (this.dropFromGiftLootTable(level, BuiltInLootTables.CHICKEN_LAY, this::spawnAtLocation)) { ... }`（第 136 行）。
  鸡蛋由战利品表 `CHICKEN_LAY` 产出；`Entity.spawnAtLocation(ItemLike)` 单参重载已被删除，现为
  `spawnAtLocation(ServerLevel, ItemLike)`（`Entity.java:2227`）。`@ModifyArg` 目标
  `Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;`
  将无法匹配。
- 推荐方案（a）：把注入点改为「战利品表 key 参数」，将 `BuiltInLootTables.CHICKEN_LAY` 换成模组自带的
  掉落臭鸡蛋的战利品表。`dropFromGiftLootTable` 实际声明在 **`LivingEntity`**（26.2 `LivingEntity.java:1599`，
  `public boolean dropFromGiftLootTable(ServerLevel level, ResourceKey<LootTable> key, BiConsumer<ServerLevel, ItemStack> consumer)`），
  并非研究初稿中推测的 `Mob`——描述符必须使用声明类，否则 `@At` 匹配不到：
  `@ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropFromGiftLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Ljava/util/function/BiConsumer;)Z"))`
  修改第 2 个参数（`ResourceKey<LootTable>`，ordinal = 1）。随机性保留在战利品表内，逻辑最干净。
  （待确认：模组自定义战利品表的注册方式与 `BuiltInLootTables` 是否仍可用作参考。）
- 备选方案（b）：`@Inject` 到 `aiStep` HEAD，读到鸡蛋路径后自行执行臭鸡蛋逻辑（需保留 `this.random` 概率判断，
  并处理 `spawnAtLocation(ServerLevel, ItemLike)` 需要 `ServerLevel` 的新签名）。
- 备选方案（c）：`@ModifyArg` 第 3 个参数 `BiConsumer` 方法引用（不直接，不推荐）。
- 状态：需要修改。

---

Mixin:
ChunkRegionMixin
旧target:
net/minecraft/server/level/WorldGenRegion
新target:
net/minecraft/server/level/WorldGenRegion
解决方案:
- 类、字段、方法均未移动：26.2 `WorldGenRegion.java:62` 类不变；`currentlyGenerating` 字段第 75 行（现为
  `private @Nullable Supplier<String>`，`@Shadow` 不受影响）；`ensureCanWrite(BlockPos)` 第 272 行仍存在；
  内部仍调用 `Util.logAndPauseIfInIde(String)`。
- 唯一改动：`net.minecraft.Util` 移包为 `net.minecraft.util.Util`（26.2 `net/minecraft/util/Util.java`，
  `logAndPauseIfInIde(String)` 在第 779 行）。因此 `@At` 的 INVOKE 描述符必须更新：
  旧 `Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V`
  -> 新 `Lnet/minecraft/util/Util;logAndPauseIfInIde(Ljava/lang/String;)V`。
- 注意：项目内任何模组自身代码也不得再 import `net.minecraft.Util`，一律改为 `net.minecraft.util.Util`。
- 其余逻辑（`@Shadow currentlyGenerating`、`callback.setReturnValue(false)`）不变。
- 状态：需要修改（仅 1 行描述符）。

---

Mixin:
LivingEntityMixin
旧target:
net/minecraft/world/entity/LivingEntity
新target:
net/minecraft/world/entity/LivingEntity
解决方案:
- 类未移动。三处注入中两处 26.2 原样有效：
  - `tick` @At("RETURN") -> `CommonEvents.LIVING_TICK`：26.2 `LivingEntity.java:2755` 仍存在。**无需改动。**
  - `swing(InteractionHand, boolean)` @At("HEAD") -> 召唤幽灵剑：26.2 `LivingEntity.java:2026` 仍存在。
    `getItemInHand(InteractionHand)`（`@Shadow`）26.2 第 2252 行仍存在。**无需改动。**
- 第三处注入失效：`onEffectRemoved(MobEffectInstance)` 在 26.2 已被删除，替换为
  `protected void onEffectsRemoved(Collection<MobEffectInstance> effects)`（`LivingEntity.java:1105`，
  其内部第 1119 行仍调用 `this.refreshDirtyAttributes()`，因此 INVOKE 目标
  `Lnet/minecraft/world/entity/LivingEntity;refreshDirtyAttributes()V` 依然存在于新方法内；
  `refreshDirtyAttributes` 现为 `private`，但描述符 `()V` 不变，仍可匹配）。`method = "onEffectRemoved"` 将匹配不到任何方法。
- 修改步骤：
  - `@Inject(method = "onEffectRemoved" ...)` -> `@Inject(method = "onEffectsRemoved" ...)`，INVOKE target 不变。
  - handler 参数由单个 `MobEffectInstance effect` 改为 `Collection<MobEffectInstance> effects`，在方法体内
    遍历集合，当某个 `effect.getEffect().value() instanceof FrozenStatusEffect e` 时调用
    `e.onRemoved((LivingEntity)(Object)this)`（等价于原语义）。
  - `tick` / `swing` 两个 handler 保持原样。
- 状态：需要修改（仅 `onEffectRemoved` 一处）。

---

Mixin:
MobEntityMixin
旧target:
net/minecraft/world/entity/Mob
新target:
net/minecraft/world/entity/Mob
解决方案:
- 类未移动。但 `Mob.dropFromLootTable` 签名改变：
  旧 `protected void dropFromLootTable(DamageSource, boolean)`（1.21.1 `Mob.java:482`）
  -> 新 `protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled)`
  （26.2 `Mob.java:412`），第一参数新增 `ServerLevel`。
- 注入点 `@Inject(method = "dropFromLootTable", at = @At("HEAD"))` 依赖 handler 参数与目标方法匹配：
  `(DamageSource, boolean, CallbackInfo)` 在 26.2 无法解析，必须改为 `(ServerLevel, DamageSource, boolean, CallbackInfo)`。
- handler 内部 `this.spawnAtLocation(new ItemStack(...))`（单参）已不存在，需改为
  `this.spawnAtLocation(level, new ItemStack(IafItems.WITHERBONE.get(), this.random.nextInt(2)))`，
  使用 `Entity.spawnAtLocation(ServerLevel, ItemStack)`（26.2 `Entity.java:2231`）。
- `@Unique iceandfire$isSkeleton(Entity)` 与 `this.random` 在 26.2 均仍可用。
- 状态：需要修改（handler 签名 + `spawnAtLocation` 调用）。

---

Mixin:
GameRendererMixin
旧target:
net/minecraft/client/renderer/GameRenderer
新target:
net/minecraft/client/renderer/GameRenderer
解决方案:
- 类仍在（26.2 `GameRenderer.java`），但两个注入点的底层目标全部消失：
  1. `onCameraSetup`：注入 `renderLevel` 内 `Camera.setup(BlockGetter, Entity, boolean, boolean, float)`
     （1.21.1 `GameRenderer.java:1215` / `Camera.java:50`）。26.2 `Camera` 已删除该公有 `setup(...)`，
     只剩私有 `setupPerspective`/`setupOrtho`（`Camera.java:323/327`），相机流程改为
     `mainCamera.update(deltaTracker)`（`GameRenderer` 内约 372 行）+ `Camera.extractRenderState(CameraRenderState, float)`
     （`Camera.java:115`）。INVOKE 目标无法匹配 -> 注入失败。`@Shadow mainCamera` 字段仍存在（`GameRenderer.java:122`），没问题。
  2. `registerProgram`（注册 rendertype_dread_portal 着色器）：`GameRenderer.reloadShaders(ResourceProvider)`
     **整个方法已删除**；`net.minecraft.client.renderer.ShaderInstance` **类已删除**（整个 26.2 树中不存在）。
     新着色器体系为 `net.minecraft.client.renderer.ShaderManager`（`Minecraft.getInstance().getShaderManager()`，
     `Minecraft.java:2561`）+ 资源包 `shaders/` 目录 + `PostChain`（`ShaderManager.getPostChain(...)`，
     `ShaderManager.java:186`，从 `post_effect/` 目录加载）。不存在可追加的 `List<Pair<ShaderInstance, Consumer<ShaderInstance>>>`
     程序列表，也不再基于 `DefaultVertexFormat` 构造程序。`@Local(ordinal=1)` 失效。
- 修改步骤（待确认，属设计级重写）：
  - `onCameraSetup`：重新定位到 26.2 的相机更新完成点。候选：(i) `@At("RETURN")` 注入 `GameRenderer.renderLevel(DeltaTracker)`
    （26.2 第 522 行仍存在）；(ii) 注入 `Camera.update(DeltaTracker)`（`Camera.java:90`）RETURN；
    (iii) 把逻辑并入渲染状态抽取管线（`LevelRenderState.cameraRenderState` / `CameraRenderState`）。
    `ClientEvents.onCameraSetup(Camera)` 若存在合法触发点可复用，否则需改造为基于 `CameraRenderState`。
  - dread portal 着色器：迁移到资源包方案。提供 `assets/<mod>/shaders/rendertype_dread_portal.json` + GLSL，
    渲染时通过 `ShaderManager` 查找，而不是构造 `ShaderInstance` 并缓存到 `RenderVariables.DREAD_PORTAL_PROGRAM`。
    所有 `RenderVariables.DREAD_PORTAL_PROGRAM` 消费方（`IafRenderLayers.DREAD_PORTAL_PROGRAM` 等）以及
    `DefaultVertexFormat.POSITION_COLOR` 渲染管线需要一并审查。待确认 26.2 中自定义 RenderType 的注册/查询方式。
- 状态：失效（两个注入点均需重写，第二个为着色器体系迁移）。

---

Mixin:
InGameHudMixin
旧target:
net/minecraft/client/gui/Gui
新target:
net/minecraft/client/gui/Hud
解决方案:
- `Gui` 不再是 HUD 渲染者。HUD/overlay 逻辑整体迁移到新类 `net.minecraft.client.gui.Hud`（26.2 `Hud.java`）。
  `Gui` 仅保留 `minecraft` 字段与文本/debug 辅助；`renderCameraOverlays`、`renderTextureOverlay`、
  `POWDER_SNOW_OUTLINE_LOCATION` 三个目标全部从 `Gui` 移除，`@Mixin(Gui.class)` 注入必然失败。
- 26.2 `Hud` 对应物（已核实）：
  - `private static final Identifier POWDER_SNOW_OUTLINE_LOCATION`（`Hud.java:116`）
  - overlay 方法：`private void extractCameraOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker)`
    （`Hud.java:269`，由 `Gui` 代理调用）；粉雪 overlay 在 `Hud.java:293-294`：
    `if (player.getTicksFrozen() > 0) this.extractTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, player.getPercentFrozen());`
  - 辅助方法：`extractTextureOverlay(GuiGraphicsExtractor, Identifier, float)`（`Hud.java:1026`）。
  - `getTicksFrozen()` 仍声明在 `Entity`（26.2 `Entity.java:2821`），`Hud.java:293` 中调用 owner 是 `Entity`。
- 修改步骤：
  - `@Mixin(Gui.class)` -> `@Mixin(Hud.class)`。
  - `@Inject(method = "renderCameraOverlays" ...)` -> `@Inject(method = "extractCameraOverlays" ...)`；
    INVOKE target 建议写声明类以增强稳健性：
    `Lnet/minecraft/world/entity/Entity;getTicksFrozen()I`（保留 `LocalPlayer` owner 通常也能靠继承解析，但 `Entity` 更稳）。
  - `@Shadow protected abstract void renderTextureOverlay(GuiGraphics, ResourceLocation, float);`
    -> `@Shadow protected abstract void extractTextureOverlay(GuiGraphicsExtractor, Identifier, float);`
  - `@Shadow @Final private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;`
    -> `@Shadow @Final private static Identifier POWDER_SNOW_OUTLINE_LOCATION;`（`ResourceLocation` 26.2 改名 `Identifier`，
    import 需同步）。
  - handler 签名 `(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci)`
    -> `(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci)`。
  - 透明度计算不变：`alpha = min(renderTick, i) / i`，其中 `i = player.getTicksRequiredToFreeze()`（`Entity` 26.2 第 2838 行）。
- 状态：需要修改（整体重写目标类与方法）。

---

Mixin:
LivingEntityRendererMixin
旧target:
net/minecraft/client/renderer/entity/LivingEntityRenderer
新target:
net/minecraft/client/renderer/entity/LivingEntityRenderer
解决方案:
- 类仍在，但 26.2 已泛型化：`LivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>`
  （`LivingEntityRenderer.java:38`），**不再有** `render(LivingEntity, float, float, PoseStack, MultiBufferSource, int)`。
  每帧 hook 变为 `public void extractRenderState(T entity, S state, float partialTicks)`（第 242 行）。
- 渲染流程：`EntityRenderer.extractRenderState(entity, state, partialTicks)`（`EntityRenderer.java:161`）抽取渲染状态，
  GPU 提交在之后进行；`MultiBufferSource` 类型已从树中删除，替换为 `GpuBufferSlice`/`RenderPipelines`。
  旧签名的「PoseStack 后置渲染回调」不复存在。
- 修改步骤（待确认，行为级重写）：
  - 注入点重定向：`@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))`
    -> `@Inject(method = "extractRenderState", at = @At("RETURN"))`，handler `(T entity, S state, float partialTicks, CallbackInfo ci)`。
  - `ClientEvents.onPostRenderLiving(LivingEntity, float, PoseStack, MultiBufferSource, int)` **无法原样调用**
    —— 新签名没有 PoseStack / MultiBufferSource / light。原 post-render 工作（feature renders / overlays）必须移植到
    RenderState + `FeatureRenderer`-风格管线（把数据写入 `S` 渲染状态，在 submit 阶段绘制）。待确认具体移植步骤。
- 状态：失效（渲染管线架构性重写）。

---

Mixin:
PlayerEntityRendererMixin
旧target:
net/minecraft/client/renderer/entity/player/PlayerRenderer
新target:
net/minecraft/client/renderer/entity/player/AvatarRenderer
解决方案:
- `PlayerRenderer` 类在 26.2 已**不存在**。玩家渲染改为 `net.minecraft.client.renderer.entity.player.AvatarRenderer`
  （`AvatarRenderer<AvatarlikeEntity extends Avatar & ClientAvatarEntity, AvatarRenderState, PlayerModel> extends LivingEntityRenderer`，
  `AvatarRenderer.java:48`）。它没有 `render(AbstractClientPlayer, ...)` 覆写，per-entity hook 是
  `public void extractRenderState(AvatarlikeEntity entity, AvatarRenderState state, float partialTicks)`（第 166 行）。
  26.2 `EntityRenderer.extractRenderState(T, S, float)`（第 161 行）为各实体共用入口，实际绘制在稍后的 submit 阶段进行。
  `LocalPlayer` / `RemotePlayer` 在 26.2 仍存在（`client/player/LocalPlayer.java`、`client/player/RemotePlayer.java`）。
- 修改步骤（待确认，逻辑重写）：
  - `@Mixin(PlayerRenderer.class)` -> `@Mixin(AvatarRenderer.class)`。
  - `@Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)`
    -> `@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)`，
    handler `(AvatarlikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci)`。
  - 判定逻辑复用：`entity.getVehicle() instanceof DragonBaseEntity` 仍有效（entity 即玩家）；
    `Minecraft.getInstance().options.getCameraType().isFirstPerson()` 在 26.2 仍存在。
  - `DragonRiderFeatureRenderer.RENDERING_RIDERS` 属于客户端 feature renderer 机制，必须移植到 26.2 的
    feature-renderer / RenderState 管线（见 LivingEntityRendererMixin）。在 extract 阶段取消是否是正确的 seam
    （vs 在 submit 阶段取消）需要验证。待确认。
- 状态：失效（目标类被替换，逻辑重写）。

---

Mixin:
RotatingCubeMapRendererMixin
旧target:
net/minecraft/client/renderer/PanoramaRenderer
新target:
net/minecraft/client/renderer/Panorama
解决方案:
- `PanoramaRenderer` 类在 26.2 已删除，替换为 `net.minecraft.client.renderer.Panorama`（`Panorama.java:9`，
  `TitleScreen` 第 31 行改为 import `Panorama`）。
- 旧 `render(GuiGraphics, int width, int height, float alpha, float tickDelta)` 替换为
  `public void extractRenderState(GuiGraphicsExtractor graphics, int width, int height)`（`Panorama.java:22/35`）——
  只有 2 个 int，没有 alpha / tickDelta 参数；内部把 `PanoramaRenderState` 写入
  `gameRenderState.guiRenderState.panoramaRenderState`，并经 `RenderPipelines.GUI_TEXTURED` 提交。
- 修改步骤：
  - `@Mixin(value = PanoramaRenderer.class, priority = 900)` -> `@Mixin(value = Panorama.class, priority = 900)`。
  - `@Inject(method = "render" ...)` -> `@Inject(method = "extractRenderState" ...)`，
    handler `(GuiGraphicsExtractor context, int width, int height, CallbackInfo ci)`。
  - 逻辑不变：`customMainMenu` 开启时调用 `TitleScreenRenderManager.tick()` / `TitleScreenRenderManager.renderBackground(context, width, height)`
    后 `ci.cancel()`（慢速 tick 计数器 `iceandfire$slowTick` 保留）。
  - 配套：`TitleScreenRenderManager.renderBackground` 需改为接收 `GuiGraphicsExtractor`，并把方法内部所有
    `GuiGraphics` -> `GuiGraphicsExtractor`、`ResourceLocation` -> `Identifier`、`RenderSystem.setShaderColor/blit` 相关调用
    迁移到 26.2 GUI 管线（见 minecraft.md §9）。
- 状态：需要修改（类改名 + 方法改名）。

---

Mixin:
TitleScreenMixin
旧target:
net/minecraft/client/gui/screens/TitleScreen
新target:
net/minecraft/client/gui/screens/TitleScreen
解决方案:
- 类与 `splash` 字段未移动（26.2 `TitleScreen.java:51`，仍为 `@Nullable SplashRenderer`）。两处注入中：
  - `init` @At("RETURN")：26.2 `TitleScreen.init()` 第 105 行仍存在，**有效，无需改动**。
  - `render` @LogoRenderer INVOKE：`render(GuiGraphics, int, int, float)` 已改名
    `public void extractRenderState(GuiGraphicsExtractor, int, int, float)`（`TitleScreen.java:289`）。
    内部 logo 绘制改为 `this.logoRenderer.extractRenderState(graphics, this.width, this.logoRenderer.keepLogoThroughFade() ? 1.0F : widgetFade)`
    （第 309 行）；`LogoRenderer.renderLogo(GuiGraphics, int, float)` 已删除，新 API 为
    `LogoRenderer.extractRenderState(GuiGraphicsExtractor, int, float)`（4 参重载 `(…, int, float, int)`）`LogoRenderer.java:30/34`，
    并新增 `keepLogoThroughFade()`。
  - `@Local(ordinal = 2) int i`（旧代码 `k = Mth.ceil(g*255)<<24` 得到的 alpha int）在 26.2 已**不存在**——
    `extractRenderState` 使用 float `widgetFade`。`16777215 | i` 的 ARGB 合成必须重建。
- 修改步骤：
  - `@Inject(method = "render" ...)` -> `@Inject(method = "extractRenderState" ...)`；INVOKE target 改为
    `Lnet/minecraft/client/gui/components/LogoRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V`。
  - handler `(GuiGraphicsExtractor context, int mouseX, int mouseY, float a, CallbackInfo ci)`；
    用新的 `@Local`（例如 `@Local float widgetFade`）捕获 logo 透明度 float，把计算出的 int/ARGB
    传给 `TitleScreenRenderManager.drawModName`（例如 `ARGB.white(widgetFade)` 这类 26.2 API —— 待确认）。
  - 配套：`TitleScreenRenderManager.drawModName` 需改为接收 `GuiGraphicsExtractor`（内部 GUI 管线同步迁移）。
  - 配套：`TitleScreenRenderManager.getSplash()` 目前 `new SplashRenderer(String)`——26.2 `SplashRenderer` 构造器
    改为 `SplashRenderer(Component)`（`SplashRenderer.java:22`），需改传 `Component`。
  - `init` 注入保持不变。
- 状态：需要修改（render 注入点；init 部分 OK）。

---

Mixin:
WorldRendererMixin
旧target:
net/minecraft/client/renderer/LevelRenderer
新target:
net/minecraft/client/renderer/LevelRenderer
解决方案:
- 类仍在（26.2 `LevelRenderer.java`），但渲染方法彻底重写：
  - `renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)` ->
    `public void render(GraphicsResourceAllocator, DeltaTracker, boolean renderOutline, CameraRenderState cameraState,
    Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky)`
    （`LevelRenderer.java:154`）。没有 `Camera` 参数（改为 `CameraRenderState`），没有 `PoseStack`（改为 `Matrix4fc`）。
  - `ClientLevel.entitiesForRendering()` 在 `LevelRenderer.render` 中**不再被调用**。方法本身仍存在
    （`ClientLevel.java:444`），但已由渲染状态抽取阶段 `net.minecraft.client.renderer.extract.LevelExtractor`
    在实体循环中使用（`LevelExtractor.java:231`）。INVOKE 目标无法匹配 -> 注入失败。
  - `@Shadow @Final RenderBuffers renderBuffers` 字段仍存在（`LevelRenderer.java:103`），但
    **`RenderBuffers.bufferSource()` 已删除**（26.2 `RenderBuffers.java` 无该方法，类改为 `implements AutoCloseable`，
    围绕 GPU / frame-graph 管线）。自定义几何无法再写入 `bufferSource()`。
  - `camera.getPosition()` -> `CameraRenderState.position()`（`Vec3`）；`@Local PoseStack matrices` 不存在，
    矩阵 push 需转到 `RenderSystem.getModelViewStack()` 或传入的 `Matrix4fc modelViewMatrix`。
- 修改步骤（待确认，属设计级重写）：自定义闪电渲染必须迁移到新的抽取/GPU 管线。候选：
  - 注入 `LevelExtractor.extractRenderState` / 其实体循环，把 bolt 几何存入自定义 `RenderState`，
    经注册的 `RenderPipelines` 着色器在提交阶段绘制；或
  - 使用自定义 `FeatureRenderer` / `RenderSystem` 层级的提交 hook（`LevelRenderer.submitFeatures`、`FeatureRenderDispatcher`）。
  - `LightningBoltData` / `LightningRenderer`（模组代码）目前经由 `MultiBufferSource`（`renderBuffers.bufferSource()`）绘制，
    该调用路径必须替换；`LightningRenderer.update/render` 的签名以及 `BoltRenderInfo` / `SpawnFunction` 辅助类
    大概率需要 GPU/buffer 重写。待确认具体 API。
- 状态：失效（渲染管线架构性重写）。

---

## 三、总览汇总表

| Mixin | 目标类（1.21.1） | 目标类（26.2） | 状态 | 变更成员 |
|---|---|---|---|---|
| ChickenMixin | `animal.Chicken` | `animal.chicken.Chicken` | 需要修改 | 类移包；`aiStep` 中 `spawnAtLocation(ItemLike)` 调用消失，改为 `dropFromGiftLootTable` + 战利品表 `CHICKEN_LAY`；`spawnAtLocation` 新增 `ServerLevel` 参数 |
| ChunkRegionMixin | `server.level.WorldGenRegion` | 同 | 需要修改 | 仅 INVOKE 描述符：`net.minecraft.Util` -> `net.minecraft.util.Util` |
| LivingEntityMixin | `world.entity.LivingEntity` | 同 | 需要修改 | `onEffectRemoved(MobEffectInstance)` -> `onEffectsRemoved(Collection<MobEffectInstance>)`；`tick`/`swing` 不受影响 |
| MobEntityMixin | `world.entity.Mob` | 同 | 需要修改 | `dropFromLootTable(DamageSource, boolean)` -> `dropFromLootTable(ServerLevel, DamageSource, boolean)`；handler 内部 `spawnAtLocation` 需传 `ServerLevel` |
| GameRendererMixin | `client.renderer.GameRenderer` | 同 | 失效 | `Camera.setup(...)` 删除；`reloadShaders` / `ShaderInstance` / `DefaultVertexFormat` 程序注册体系 -> `ShaderManager` + 资源包 `shaders/` |
| InGameHudMixin | `client.gui.Gui` | `client.gui.Hud` | 需要修改 | HUD 移到 `Hud`；`renderCameraOverlays`->`extractCameraOverlays`；`renderTextureOverlay`->`extractTextureOverlay`；`GuiGraphics`->`GuiGraphicsExtractor`；`ResourceLocation`->`Identifier` |
| LivingEntityRendererMixin | `client.renderer.entity.LivingEntityRenderer` | 同 | 失效 | `render(LivingEntity,FF,PoseStack,MultiBufferSource,I)` 删除 -> `extractRenderState(T,S,float)`；`MultiBufferSource` 类型消失 |
| PlayerEntityRendererMixin | `...entity.player.PlayerRenderer` | `...entity.player.AvatarRenderer` | 失效 | 类被替换；`render(AbstractClientPlayer,...)` 删除 -> `extractRenderState(AvatarlikeEntity,AvatarRenderState,float)` |
| RotatingCubeMapRendererMixin | `client.renderer.PanoramaRenderer` | `client.renderer.Panorama` | 需要修改 | 类改名；`render(GuiGraphics,int,int,float,float)` -> `extractRenderState(GuiGraphicsExtractor,int,int)` |
| TitleScreenMixin | `client.gui.screens.TitleScreen` | 同 | 需要修改 | `render`->`extractRenderState`；`LogoRenderer.renderLogo`->`extractRenderState`；`@Local int i`（alpha）消失；`SplashRenderer(String)`->`SplashRenderer(Component)` |
| WorldRendererMixin | `client.renderer.LevelRenderer` | 同 | 失效 | `renderLevel`->`render(...)`；`entitiesForRendering()` 调用移出（迁至 `LevelExtractor`）；`RenderBuffers.bufferSource()` 删除 |

> 状态说明：**OK** = 26.2 原样适用；**需要修改** = 目标类/方法存在，仅需改描述符、签名或重定位注入点；
> **失效** = 底层目标在 26.2 已不存在，必须设计级重写。
> 本项目中无「OK」类；「需要修改」7 个、「失效」4 个（GameRendererMixin、LivingEntityRendererMixin、
> PlayerEntityRendererMixin、WorldRendererMixin）。

---

## 四、已无有效目标 / 需要设计级重写的 Mixin 替换策略

以下 4 个 Mixin 在 26.2 已无 1.21.1 意义上的有效目标，不能仅靠改字符串恢复，需先确定新架构落点再编码：

### 1. GameRendererMixin（两个注入点均失效）
- `onCameraSetup`（相机钩子）：落点候选——`@At("RETURN")` 注入 `GameRenderer.renderLevel(DeltaTracker)`（26.2 第 522 行仍存在），
  或注入 `Camera.update(DeltaTracker)`（`Camera.java:90`）RETURN，或将逻辑并入 `CameraRenderState` / `LevelRenderState.cameraRenderState` 抽取。
  优先确认 `ClientEvents.onCameraSetup` 的消费者是否兼容新落点，避免重复触发。
- `registerProgram`（dread portal 着色器）：整体迁移到 26.2 资源包着色器体系——提供 `assets/<mod>/shaders/rendertype_dread_portal.json` + GLSL，
  运行时经 `ShaderManager`（`Minecraft.getInstance().getShaderManager()`）查找，替代「构造 `ShaderInstance` + 缓存进 `RenderVariables.DREAD_PORTAL_PROGRAM`」。
  同时审查所有 `RenderVariables.DREAD_PORTAL_PROGRAM` / `IafRenderLayers.DREAD_PORTAL_PROGRAM` 消费点与 `DefaultVertexFormat.POSITION_COLOR` 渲染路径。
  待确认：26.2 自定义 RenderType 与管线常量（`RenderPipelines`）的注册方式。

### 2. LivingEntityRendererMixin（`render(...)` 删除）
- 重定向到 `extractRenderState(T entity, S state, float partialTicks)` @ `@At("RETURN")`，handler `(T, S, float, CallbackInfo)`。
- 原 `ClientEvents.onPostRenderLiving(LivingEntity, float, PoseStack, MultiBufferSource, int)` 无法复用——新签名无 PoseStack/MultiBufferSource/light。
  post-render 内容需改写为写入 `S`（LivingEntityRenderState 子类）并在 submit 阶段通过 `FeatureRenderer` 管线绘制。
  待确认：模组各 feature renderer 向 26.2 `FeatureRenderer` 契约迁移的具体步骤。

### 3. PlayerEntityRendererMixin（`PlayerRenderer` 类删除）
- 目标类换为 `AvatarRenderer`，注入 `extractRenderState` HEAD（可取消）。
- 取消逻辑本身可保留（`entity.getVehicle() instanceof DragonBaseEntity`、`isFirstPerson()` 均仍有效），
  但 `DragonRiderFeatureRenderer.RENDERING_RIDERS` 集合需在 26.2 feature-renderer/RenderState 管线下重建。
  待确认：在 extract 阶段取消是否等价于旧的「跳过该玩家渲染」，还是需在 submit 阶段取消。

### 4. WorldRendererMixin（`renderLevel` + `bufferSource()` 删除）
- 闪电渲染迁移候选（按优先级）：
  (a) 注入 `LevelExtractor.extractRenderState`（`LevelExtractor.java:231` 的实体循环是 `entitiesForRendering()` 的新消费点），
      把 bolt 数据写入自定义 RenderState，注册对应 `RenderPipelines` 着色器在提交阶段绘制；
  (b) 使用提交阶段的 hook（`LevelRenderer.submitFeatures`、`FeatureRenderDispatcher`）。
- `LightningRenderer.update/render`、`LightningBoltData`（含 `BoltRenderInfo`、`SpawnFunction`）目前直接写 `MultiBufferSource`，
  必须重写为 GPU/帧图管线（`GpuBufferSlice` 等）。待确认最终绘制 API。
