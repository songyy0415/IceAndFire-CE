# P7-G Mixin Migration — Error Audit

Baseline: 271 errors. Mixin module = 28 errors across 9 files (ParticleEngineMixin already clean).

## Mixin error table (verified against 26.2 compile jar)

| 文件 | 行 | 错误原因 | 26.2方案 |
| -- | -- | --- | ---- |
| ChickenMixin | 7,14 | `animal.Chicken` 移到 `animal.chicken.Chicken`；`spawnAtLocation(ItemLike)` 单参 → `(ServerLevel, ItemLike)` | 改 import + ModifyArg target |
| MobEntityMixin | 9,27 | `monster.WitherSkeleton` → `monster.skeleton.WitherSkeleton`；`dropFromLootTable(DamageSource, boolean)` → `(ServerLevel, DamageSource, boolean)` | 改 import + inject 签名加 ServerLevel |
| LivingEntityRendererMixin | 5,16 | `MultiBufferSource` 移除；旧 `render(LivingEntity,...)` 不存在 | 改 target `submit(S,PoseStack,SubmitNodeCollector,CameraRenderState)` RETURN + onPostRenderLiving 迁移 |
| PlayerEntityRendererMixin | 10,11,17,20,21,23 | `PlayerRenderer` 移除（26.2 用 `AvatarRenderer`）；MultiBufferSource 移除 | 重写：26.2 player render 取消逻辑 |
| GameRendererMixin | 21,36,38 | `ShaderInstance` 移除（26.2 shader 系统）；`Camera.setup` 移除 | 用 26.2 ShaderManager 注册；onCameraSetup 挂到新 hook |
| InGameHudMixin | 9,22,33 | `GuiGraphics` → `GuiGraphicsExtractor`；`Gui.renderCameraOverlays` 移除；`LocalPlayer` 冻结方法移除 | 挂 `Gui.extractRenderState(DeltaTracker,boolean,boolean)` |
| RotatingCubeMapRendererMixin | 5,6,13,19 | `GuiGraphics` → `GuiGraphicsExtractor`；`PanoramaRenderer` 移除 | 找 26.2 主菜单背景渲染 hook |
| TitleScreenMixin | 7,38,40 | `GuiGraphics` → `GuiGraphicsExtractor`；`TitleScreen.render` → `extractRenderState(GuiGraphicsExtractor,int,int,float)` | 改 target + GuiGraphicsExtractor |
| WorldRendererMixin | 37,48 | `Camera.getPosition()` → `position()`；`RenderBuffers.bufferSource()` 移除；LevelRenderer 管线 | `camera.position()`；lightning 用 26.2 管线 |

## API notes (verified against 26.2 compiled jar)

- `Chicken` → `net.minecraft.world.entity.animal.chicken.Chicken`；`WitherSkeleton` → `net.minecraft.world.entity.monster.skeleton.WitherSkeleton`
- `Entity.spawnAtLocation(ServerLevel, ItemLike)` / `(ServerLevel, ItemStack)` — 需 ServerLevel 首参
- `Mob.dropFromLootTable(ServerLevel, DamageSource, boolean)`
- **GUI 大改**：`GuiGraphics` → `GuiGraphicsExtractor`；`Gui.renderCameraOverlays` → `Gui.extractRenderState(DeltaTracker, boolean, boolean)`；`TitleScreen.render` → `extractRenderState(GuiGraphicsExtractor, int, int, float)`
- `ShaderInstance` 移除 → `ShaderManager`/`ShaderDefines`；`Camera.setup` 移除（camera 内部 tick）；`Camera.position()` 取代 `getPosition()`
- `PanoramaRenderer` / `PlayerRenderer` 移除；player 用 `AvatarRenderer`
- `LivingEntityRenderer.submit(S, PoseStack, SubmitNodeCollector, CameraRenderState)` — post-render 挂点
- `MultiBufferSource` 移除 → `SubmitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> …)`（GhostEntityRenderer 已验证）
- 关联 feature 也报错（render 模块）：ClientEvents.onPostRenderLiving / ChainRenderer / FrozenStateRenderer / CockatriceBeamRenderer（MultiBufferSource）、RenderVariables（ShaderInstance）、PortalRenderHelper

## Commit plan
1. `migration: migrate common mixins to mc26.2` — ChickenMixin, MobEntityMixin
2. `migration: migrate entity render mixins to mc26.2` — LivingEntityRendererMixin, PlayerEntityRendererMixin + onPostRenderLiving 迁移
3. `migration: migrate gui mixins to mc26.2` — InGameHudMixin, TitleScreenMixin, RotatingCubeMapRendererMixin
4. `migration: migrate world renderer + shader mixins to mc26.2` — WorldRendererMixin, GameRendererMixin + RenderVariables/PortalRenderHelper
