# P6-0 Rendering Migration Audit（MC 1.21.1 → 26.2）

> 本阶段**只分析，不改代码**。对比基线：`/d/aiminecraftdev/minecraft1.21.1mojmap` vs `/d/aiminecraftdev/minecraft26.2`。
> 当前编译状态（`./gradlew :common:compileJava`）：**2,100 javac errors** / 1,825 唯一错误位置 / 322 唯一报错文件。
> 注：此前 P5 报告中的 "157/100 errors" 为 `grep` 对 `2,157/2,100` 逗号截断的误读，本审计使用准确值。

---

## 一、摘要

26.2 对客户端渲染做了一次**架构级重构**：从「render(实体, …, PoseStack, MultiBufferSource, light)」改为 **RenderState 模式**——先 `extractRenderState(entity, state, ticks)` 把实体数据快照进 `EntityRenderState` 子类，再 `submit(state, poseStack, SubmitNodeCollector, camera)` / `render(state, …)` 从状态渲染；渲染缓冲 `MultiBufferSource` 被 `SubmitNodeCollector` 延迟渲染图取代；`GuiGraphics` 被 `GuiGraphicsExtractor` + `extractRenderState` 取代；粒子基类与 Screen/Menu 也一并重构。

**结论：这不是改名迁移，而是每个渲染类都需要结构性改写（新增 RenderState 类 + 拆分 extract/render）。**

| 子系统 | 报错文件 | 重构程度 |
|---|---|---|
| EntityRenderer / FeatureRenderer | 55 + 9 | **重写**（RenderState） |
| Model | 37 | **重写**（`EntityModel<T extends EntityRenderState>`） |
| BlockEntityRenderer | 7 | **重写**（BlockEntityRenderState） |
| ItemRenderer | 7 | 重写（DynamicItemRenderer 属 P6 禁改） |
| Particle | 10 | 重写（基类/构造/注册） |
| Screen / Gui / Hud | 12 | **重写**（GuiGraphicsExtractor + extractRenderState） |
| Mixin | 9 | 重写（目标类方法签名全变） |
| 渲染注册表 | 4 | 中等（IafRenderers/RenderLayers/ScreenHandlers/Keybindings） |
| compat（ponder/jei/emi/jade） | 6 | 中等 |

---

## 二、26.2 渲染架构重构概览

### 2.1 RenderState 模式（核心）

1.21.1：
```java
public void render(T entity, float entityYaw, float partialTicks,
                   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    // 直接读 entity 的字段/方法渲染
}
```

26.2：
```java
public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState> {
    public abstract S createRenderState();
    public final S createRenderState(T entity, float partialTicks) {
        S state = this.createRenderState();
        this.extractRenderState(entity, state, partialTicks);   // 快照
        return state;
    }
    public void extractRenderState(T entity, S state, float partialTicks) { }  // 覆写点
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) { }  // 提交到延迟图
    // render(S state, PoseStack, SubmitNodeCollector/Buffer, int light)
}
```

- `EntityRenderState`（`net.minecraft.client.renderer.entity.state`）持有：`entityType / x/y/z / ageInTicks / boundingBoxWidth|Height / eyeHeight / distanceToCameraSq / isInvisible / isDiscrete / displayFireAnimation / lightCoords`。
- `LivingEntityRenderState extends EntityRenderState`：`bodyRot / yRot / xRot / deathTime / walkAnimationPos|Speed / scale / isBaby / headItem(ItemStackRenderState) / …`。
- `EntityRenderDispatcher.submit(…)`（`net.minecraft.client.renderer.entity.EntityRenderDispatcher.java:144`）统一走 `submit(renderState, poseStack, submitNodeCollector, camera)`。

### 2.2 渲染缓冲：`MultiBufferSource` → `SubmitNodeCollector`

- 26.2 **已删除 `MultiBufferSource`**（`net.minecraft.client.renderer` 下无此类）。
- 渲染管线改为 `SubmitNodeCollector`（延迟渲染图，位于 `net.minecraft.client.gui.render.pip`），`submitNodeCollector.submitLeash / submitNameTag / …`。
- 实体/方块实体渲染方法的 buffer 参数类型随之改变。

### 2.3 Gui / Screen：`GuiGraphics` → `GuiGraphicsExtractor`

- 26.2 删除 `GuiGraphics`；`Screen` 改为 `extractRenderStateWithTooltipAndSubtitles(GuiGraphicsExtractor, mouseX, mouseY, a)` → `extractRenderState(...)` / `extractBackground(...)` / `extractBlurredBackground(...)` / `extractPanorama(...)`。
- `Gui` 用 `GuiGraphicsExtractor` + `guiRenderState`。

### 2.4 Model：`EntityModel<T extends EntityRenderState>`

- 26.2 `EntityModel<T extends EntityRenderState>`：模型泛型从「Entity」改为「EntityRenderState」。
- `setupAnim(S state, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)` 的实体参数改为 state。
- `MobRenderer<Mob, PigRenderState, PigModel>` / `LivingEntityRenderer<T, S, M extends EntityModel<? super S>>`（3 个类型参数）。

### 2.5 Particle：基类 / 构造 / 注册

- `TextureSheetParticle` 已删除 → 直接用 `SingleQuadParticle`（26.2 存在）。
- `ParticleProvider.createParticle` 增加 `RandomSource random` 参数（26.2：`createParticle(options, level, x,y,z, xAux,yAux,zAux, random)`）。
- 粒子注册在 P4 已迁到 `ParticleProviderRegistry`；本阶段主要改粒子类自身（基类、`ParticleRenderData`/render 类型、sprite）。

### 2.6 BlockEntityRenderer

- 26.2：`BlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState>`（同样引入 state）。
- 原 `void render(T blockEntity, float f, PoseStack, MultiBufferSource, int i, int j)` → `render(S state, PoseStack, …)`。

---

## 三、错误分类与文件清单（1,825 唯一位置 / 322 文件）

| 类别 | 唯一错误位置 | 文件数 | 代表文件 |
|---|---|---|---|
| render/entity/（EntityRenderer+FeatureRenderer） | ~? | 55 | DragonBaseEntityRenderer、CockatriceEntityRenderer、DragonSkullEntityRenderer、feature/*（BipedArmor、DragonArmor、DragonEyes、DragonMaleOverlay、HydraHead、DragonRider、DragonBanner、SeaSerpentAncient、GenericGlowing） |
| render/model/ | 37 | 37 | BipedBaseModel、DragonBaseModel、各生物 Model、DragonTabulaModelAnimator、DragonEggModel |
| render/block/ | 7 | 7 | JarBlockEntityRenderer、PixieHouseBlockEntityRenderer、LecternBlockEntityRenderer、DreadPortal/Spawner/EggInIce、Podium |
| render/item/ | 7 | 7 | GorgonHeadRenderer、DeathwormGauntletRenderer、TideTridentItemRenderer、armor/*（BasicArmorRenderer、ScaleArmorRenderer） |
| render/misc/ | 5 | 5 | LightningBoltData、LightningRenderer、PortalRenderHelper、RenderVariables、MiscItemRenderer |
| particle/ | 10 | 10 | Blood、DragonFlame/Frost、DreadPortal/Torch、GhostAppearance、HydraBreath、PixieDust、SerpentBubble、SirenMusic |
| screen/ | 12 | 12 | BestiaryScreen、LecternScreen、DragonScreen、DragonForgeScreen、Hippocampus/HippogryphScreen、PodiumScreen、TitleScreenRenderManager、*ScreenHandler、*PageButton |
| mixin/ | 9 | 9 | GameRenderer、InGameHud、TitleScreen、PanoramaRenderer、WorldRenderer(LevelRenderer)、LivingEntityRenderer、PlayerRenderer、LivingEntity、Mob、Chicken、WorldGenRegion |
| registry/（渲染相关） | 4 | 4 | IafRenderers、IafRenderLayers、IafScreenHandlers、IafKeybindings |
| compat/ | 6 | 6 | ponder/*（IceAndFirePonderPlugin、DragonForgeStoryBoard）、jei/*（DragonForgeRecipeCategory、IceAndFireJeiPlugin）、emi/*、jade/* |
| **非渲染（P6 之外）** | | | entity/ 66、item/ 58、world/ 20、registry/ 其余 8、data/util/event/network/recipe/effect ~14 |

> 注：`MultiBufferSource`、`GuiGraphics`、`TextureSheetParticle`、`MobRenderer` 类型参数数、`EntityModel` 泛型、`setupAnim`/`render`/`createParticle` 签名是当前绝大多数报错根因（“cannot find symbol” / “wrong number of type arguments” / “not within bounds”）。

---

## 四、逐子系统 API 对比与修改策略

### A. EntityRenderer / FeatureRenderer（55+9 文件，**重写**）

| 1.21.1 | 26.2 | 策略 |
|---|---|---|
| `MobRenderer<T, M extends EntityModel<T>>`（2 参） | `MobRenderer<T, S extends LivingEntityRenderState, M extends EntityModel<? super S>>`（3 参） | 新增 `S`（RenderState 子类，或直接用 vanilla `LivingEntityRenderState`） |
| `LivingEntityRenderer<T, M>` | `LivingEntityRenderer<T, S, M>` | 同上 |
| `render(T entity, float yaw, float partialTicks, PoseStack, MultiBufferSource, int light)` | `extractRenderState(T entity, S state, float partialTicks)` + `render(S state, PoseStack, SubmitNodeCollector, int light)` | 把 `render` 里读实体的代码移入 `extractRenderState`，`render` 只读 state |
| `setupRotations(T entity, PoseStack, float yaw, float pitch)` | `setupRotations(S state, PoseStack, float bodyRot, float scale)` | 签名改 state；读 `state.bodyRot/yRot/xRot` |
| `scale(T entity, PoseStack, float partialTicks)` | `scale(S state, PoseStack)` | 同上 |
| `model.setupAnim(entity, …)` | `model.setupAnim(state, …)` | 模型泛型改 state |
| `FeatureRenderer` 的 `render(...)` | 依赖父 Renderer 的 state | FeatureRenderer 同步改 |
| `hasCustomOutline`/`renderNameTag` 等 | `submitNameDisplay`/state 化 | 按需迁移 |

**策略**：为每个实体渲染器新建 `XxxRenderState extends LivingEntityRenderState`（字段只放渲染需要的快照）；`createRenderState()` + `extractRenderState()` 迁移所有对实体的读取；`render(state,…)` 只做绘制。FeatureRenderer 若只画图层（模型/纹理），迁移量相对小。

### B. Model（37 文件，**重写**）

| 1.21.1 | 26.2 | 策略 |
|---|---|---|
| `EntityModel<T extends Entity>` | `EntityModel<T extends EntityRenderState>` | 泛型改 state |
| `setupAnim(T entity, …)` | `setupAnim(T state, …)` | 读 state 的 rot/pos 快照 |
| `AdvancedEntityModel<T>`（Uranus） | 26.2 Uranus `AdvancedEntityModel<T>` 泛型需 `T extends EntityRenderState` | 模型基类同步改 |
| `ModelPart`/`AdvancedModelBox` 渲染 | `model.renderToBuffer(state, poseStack, …)` | 依赖新缓冲签名 |

**策略**：所有模型类泛型改为对应 RenderState；`setupAnim` 从 state 取数据。`DragonTabulaModelAnimator` 等动画器改读 state。

### C. BlockEntityRenderer（7 文件，**重写**）

| 1.21.1 | 26.2 | 策略 |
|---|---|---|
| `implements BlockEntityRenderer<T>` | `implements BlockEntityRenderer<T, S extends BlockEntityRenderState>` | 新增 state |
| `render(T blockEntity, float, PoseStack, MultiBufferSource, int, int)` | `extractRenderState(T, S, float)` + `render(S, PoseStack, …)` | 同上 |

### D. Particle（10 文件，**重写**）

| 1.21.1 | 26.2 | 策略 |
|---|---|---|
| `extends TextureSheetParticle` | `TextureSheetParticle` 删除 → `SingleQuadParticle` | 换基类 + 构造（sprite/材参数） |
| `createParticle(options, level, x,y,z, xAux,yAux,zAux)`（8 参） | `createParticle(options, level, x,y,z, xAux,yAux,zAux, RandomSource)`（9 参） | 加 `RandomSource` 参数 |
| `ParticleFactoryRegistry` | `ParticleProviderRegistry`（P4 已完成） | 已迁，核对 |
| `render(PoseStack, MultiBufferSource, …)` | 新粒子渲染（quad/图收集） | 按 26.2 基类 |

### E. Screen / Gui / Hud（12 文件，**重写**）

| 1.21.1 | 26.2 | 策略 |
|---|---|---|
| `Screen.render(GuiGraphics, mouseX, mouseY, partialTicks)` | `extractRenderState(GuiGraphicsExtractor, mouseX, mouseY, a)` + 后台 `extractBackground/…` | 迁移 `render` 绘制逻辑到 `extractRenderState` |
| `GuiGraphics.blit/drawString/fill` | `GuiGraphicsExtractor` 对应 API | 逐个替换 |
| 按钮 `render(GuiGraphics,…)` | `extractRenderState(GuiGraphicsExtractor,…)` | 同上 |
| `TitleScreenRenderManager` / Hud mixin | `GuiRenderState`/`GuiGraphicsExtractor` | 重写 |

### F. EntityRenderDispatcher / SubmitNodeCollector

- `EntityRenderDispatcher` 26.2 用 `submit(renderState, poseStack, submitNodeCollector, camera)`。
- 渲染缓冲参数从 `MultiBufferSource` 改为 `SubmitNodeCollector`（对提交到延迟渲染图的对象）。
- 这影响**所有** render 方法签名；`RenderStateHolder`（state 持有者）在 dispatcher/每帧抽取中承担「快照持有」职责。

### G. Mixin（9 渲染相关 + 2 游戏逻辑）

目标：`GameRenderer`、`Gui`、`LevelRenderer(WorldRenderer)`、`LivingEntityRenderer`、`PlayerRenderer`、`PanoramaRenderer`、`TitleScreen`、`LivingEntity`、`Mob`、`Chicken`、`WorldGenRegion`。
- 26.2 这些类的**目标方法签名全部变化**（如 `LevelRenderer`/`GameRenderer` 的渲染方法、`Gui.render`→extract）。
- 策略：逐条核对 26.2 目标方法，重写 `@Inject/@Redirect` 的 handler 签名；纯游戏逻辑的 `ChickenMixin`/`WorldGenRegionMixin` 属非渲染，可后置。

### H. 渲染注册表

- `IafRenderers`：`EntityRendererRegistry.register`（Architectury）→ 26.2 构造需传 `RendererProvider.Context`；`TabulaModelHandlerHelper`（Uranus）保留。
- `IafRenderLayers`：`RenderType`/`RenderTypeRegistry`（透明、发光等）——26.2 RenderType 与缓冲区变化。
- `IafScreenHandlers`：Screen/Menu 注册配合 Screen 重构。
- `IafKeybindings`：Client input（`KeyMapping`）变化。

---

## 五、P6 执行顺序（P6-A → P6-E）

依赖关系：**模型/state 基础设施先行 → EntityRenderer → BlockEntityRenderer/ItemRenderer → Particle/Screen → Mixin/收尾**。

### P6-A：渲染基础设施 + EntityRenderer（最大块）
1. 新增 `XxxRenderState` 类（按实体类型），必要时建 mod 基类 `IafLivingEntityRenderState`。
2. 迁移 **55 个 render/entity + 9 个 feature**：`createRenderState`/`extractRenderState`/`render(state,…)`/`setupRotations`/`scale`/`model.setupAnim`。
3. 同步改 `IafRenderers` / `IafRenderLayers` 注册。
4. **依赖**：B（模型泛型）须先行或并行。

### P6-B：Model 渲染（37 文件）
1. `AdvancedEntityModel`/`EntityModel` 泛型 → `T extends EntityRenderState`。
2. 全部模型 `setupAnim(state,…)` + `renderToBuffer` 新签名。
3. 动画器（`DragonTabulaModelAnimator` 等）改读 state。
4. **依赖**：需 A 定义好的 state；但 A 的 render 又依赖 B 的模型——**建议 A/B 合并为一个阶段或同 PR 推进**。

### P6-C：BlockEntityRenderer（7）+ ItemRenderer（7）
1. `BlockEntityRenderState` + `extractRenderState/render`。
2. ItemRenderer：`DynamicItemRenderer`（Uranus）属 **P6 禁改**，其余（GorgonHead、DeathwormGauntlet、TideTrident、armor）迁移。
3. **依赖**：A/B 的 state 与模型。

### P6-D：Particle（10）+ Screen/Gui/Hud（12）
1. Particle：换 `SingleQuadParticle` 基类、`createParticle` 加 `RandomSource`、render 新签名。
2. Screen：`GuiGraphics`→`GuiGraphicsExtractor`、`render`→`extractRenderState`、后台/按钮。
3. `TitleScreenRenderManager`、Hud 相关 mixin。
4. **依赖**：独立，可并行。

### P6-E：Mixin + 收尾
1. 9 个渲染 mixin 目标方法重写。
2. `IafScreenHandlers`/`IafKeybindings`、ponder/jei/emi/jade compat。
3. 清理 `IafRenderLayers` 遗留；验证 `./gradlew :common:compileJava` 渲染错误清零。
4. **依赖**：D 的 Screen 完成后。

> 建议实施顺序：**P6-A+B（合并）→ P6-C → P6-D → P6-E**，每步独立 compile 验证。

---

## 六、风险评估

| 风险 | 等级 | 说明 / 缓解 |
|---|---|---|
| **RenderState 拆分量大** | 🔴 高 | 55 renderer + 37 model 全部重写；每个 renderer 需新增 state 类并拆分 extract/render。缓解：先做 1-2 个样板（如 Cockatrice→Dragon 简单生物）跑通，再批量套模板。 |
| **MultiBufferSource 删除** | 🔴 高 | 渲染缓冲参数全变（SubmitNodeCollector）。凡直接构造/传 buffer 的代码需按新图 API 改写；mod 自定义渲染（Lightning、Portal、MiscItem）风险最高。 |
| **Uranus AdvancedEntityModel/动态模型** | 🟠 中 | Uranus 26.2 `AdvancedEntityModel<T extends EntityRenderState>` 需确认；`TabulaModel`/`AdvancedModelBox` 的 `setupAnim/renderToBuffer` 依赖新签名。 |
| **模型动画（Tabula animator）** | 🟠 中 | `DragonTabulaModelAnimator` 读实体字段（pose、部件旋转）→ 需在 state 中快照，改动精细。 |
| **Screen/Gui 重写** | 🟠 中 | `BestiaryScreen`（92 行错误）等大屏；`GuiGraphics`→`GuiGraphicsExtractor` 的 blit/drawString/fill 逐个核对。 |
| **Mixin 目标签名** | 🟠 中 | 目标类方法签名全变；`@Redirect`/`@Inject` 需对 26.2 源码逐条核对，错一处即运行时崩溃。 |
| **DynamicItemRenderer（P6 禁改）** | 🟡 低 | 明确不在本阶段；若某 ItemRenderer 依赖它，留到后续专用阶段。 |
| **兼容依赖（ponder/jei/emi/jade）** | 🟡 低 | ponder 当前已注释出 classpath；jei/emi 类别需与对应 API 26.2 对齐（P3 已迁 EMI/Jade）。 |
| **非渲染错误混入** | 🟡 低 | entity/66、item/58、world/20 等非渲染错误**不属于 P6**，须严格区分，避免本阶段误改。 |

**总风险**：中等偏高。P6 是全部阶段中改动面最大的一环（322 文件里约 145 个为渲染相关，占唯一错误位置 ~55%）。但模式统一（RenderState），一旦样板跑通可批量复制。

---

## 七、关键 API 映射速查

| 1.21.1 | 26.2 |
|---|---|
| `EntityRenderer<T>.render(T, float, float, PoseStack, MultiBufferSource, int)` | `EntityRenderer<T,S>.createRenderState()/extractRenderState(T,S,float)/render(S, PoseStack, SubmitNodeCollector, int)` |
| `MobRenderer<T,M>`（2 参） | `MobRenderer<T,S,M>`（3 参，`M extends EntityModel<? super S>`） |
| `EntityModel<T extends Entity>` | `EntityModel<T extends EntityRenderState>` |
| `MultiBufferSource` | 删除 → `SubmitNodeCollector`（延迟图） |
| `GuiGraphics` | 删除 → `GuiGraphicsExtractor` + `extractRenderState` |
| `Screen.render(GuiGraphics,…)` | `extractRenderState(GuiGraphicsExtractor,…)` |
| `BlockEntityRenderer<T>.render(T,…)` | `BlockEntityRenderer<T,S>` + state |
| `TextureSheetParticle` | 删除 → `SingleQuadParticle` |
| `ParticleProvider.createParticle(…8 参)` | `…(…, RandomSource)` 9 参 |
| `setupAnim(entity,…)` | `setupAnim(state,…)` |
| `setupRotations(entity, pose, yaw, pitch)` | `setupRotations(state, pose, bodyRot, scale)` |

---

## 八、待办（后续阶段落地时核对）

1. 确认 Uranus 26.2 `AdvancedEntityModel`/`TabulaModel` 对 `EntityRenderState` 的适配（P3 已接，需运行验证）。
2. 抽取 1 个简单生物 renderer 样板（如 Pixie/Siren）先跑通完整 RenderState 流程。
3. 逐条核对 9 个 mixin 的 26.2 目标方法签名。
4. 核对 `EntityRenderDispatcher`/`RenderStateHolder` 的 state 生命周期（每帧抽取一次，勿在 render 里改 state）。
