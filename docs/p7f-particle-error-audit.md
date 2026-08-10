# P7-F Particle Migration — Error Audit

Baseline: 294 errors. Particle module = 21 errors across 7 files.

## Particle error table (verified against 26.2 compile jar)

| 文件 | 行 | API | 旧写法 | 26.2变化 | 修改方案 |
| -- | -- | --- | --- | --- | ---- |
| DragonFlameParticle | 13 | `SingleQuadParticle` | — | 新增抽象 `getLayer()` | 实现 `getLayer() → Layer.OPAQUE` |
| DragonFlameParticle | 41 | `getRenderType()` | 覆写 getRenderType | 改为 `getGroup()`；`PARTICLE_SHEET_LIT` 常量移除 | 删除 getRenderType 覆写（SingleQuadParticle 提供 getGroup） |
| DragonFlameParticle | 43 | `ParticleRenderType.PARTICLE_SHEET_LIT` | 返回 LIT | `ParticleRenderType` 变 record：SINGLE_QUADS/ITEM_PICKUP/ELDER_GUARDIANS/NO_RENDER | 删除该行 |
| DragonFrostParticle | 15/43/45 | 同上 | 同上 | 同上 | 同上 |
| DragonFrostParticle | 48 | `ParticleProvider.createParticle` | 8 参 | 9 参（+`RandomSource`） | Provider record 补 RandomSource 参数 |
| DreadTorchParticle | 24 | `Particle.render(VertexConsumer,Camera,float)` | 覆写 render | render 移除；改 `extract(QuadParticleRenderState,Camera,float)` | render → extract；调 super.extract |
| DreadTorchParticle | 31 | `super.render` | — | 同上 | 同上 |
| PixieDustParticle | 35/40 | 同上 | 同上 | 同上 | 同上 |
| GhostAppearanceParticle | 17 | `MultiBufferSource` | import | 包移除（禁止恢复） | 重写为 26.2 model-particle 管线 |
| GhostAppearanceParticle | 23 | `Particle.getGroup` | — | 新增抽象 `getGroup()`（取代 getRenderType） | 实现 getGroup |
| GhostAppearanceParticle | 37 | `createParticle` | 8 参 | 9 参 | factory 补 random 参 |
| GhostAppearanceParticle | 40/57/59/65/67 | `render`/`MultiBufferSource`/`setupAnim` | MultiBufferSource 渲染模型 | 用 ElderGuardian 模式：ParticleGroup + submitModel | 重写（见方案） |
| HippogryphEntity | 324 | `ItemParticleOption` | `(ParticleType, ItemStack)` | 新 ctor 取 `Item`/`ItemStackTemplate` | `ItemStackTemplate.fromStack(itemstack)` |
| SeaSerpentEntity | 329 | `Level.addParticle` | 8 参 `(ParticleOptions, boolean, double×6)` | 9 参 `(ParticleOptions, overrideLimiter, alwaysShow, double×6)` | 补第二个 boolean false |

## API notes (verified against 26.2 compiled jar)

- `Particle`：`getGroup()`（抽象，取代 `getRenderType()`）；`render(VertexConsumer,Camera,float)` 整体移除；protected ctor `(ClientLevel,double,double,double)` 保留
- `SingleQuadParticle`：新增抽象 `getLayer() → Layer`（OPAQUE 等）；`extract(QuadParticleRenderState,Camera,float)` 取代 render；`getLightCoords` 保留
- `ParticleRenderType`：final record，常量 `SINGLE_QUADS/ITEM_PICKUP/ELDER_GUARDIANS/NO_RENDER`；ctor `(String name, String shorthand)` public
- `ParticleProvider<T>`：`createParticle(T, ClientLevel, double×6, RandomSource)` — 9 参
- `SpriteSet`：`get(int,int)`/`get(RandomSource)`/`first()` 未变
- `ParticleGroup<P>`：`extractRenderState(Frustum,Camera,float)` 抽象；`submitModel` 在 `OrderedSubmitNodeCollector`（`submitModel(Model<? super S>, S, PoseStack, RenderType, light, overlay, color, sprite, int, crumbling)`）
- `ElderGuardianParticleGroup`（vanilla 模板）：Particle + custom ParticleGroup + ParticleEngine wiring
- `ParticleEngine`：`createParticleGroup(ParticleRenderType)` private + `extract` 只遍历固定 `RENDER_ORDER`；自定义模型粒子需 mixin 注入
- `ItemStackTemplate.fromStack(ItemStack)` 存在；`ItemParticleOption(ParticleType<ItemParticleOption>, ItemStackTemplate)`
- `Level.addParticle(ParticleOptions, boolean overrideLimiter, boolean alwaysShow, double×6)`

## GhostAppearanceParticle 方案（ElderGuardian 模式）
1. 重写粒子：`extends Particle`，`getGroup()` 返回自定义 `ParticleRenderType`，9 参 Provider，移除 MultiBufferSource/render
2. 新增 `GhostAppearanceParticleGroup extends ParticleGroup<GhostAppearanceParticle>`：extractRenderState 构建 model+poseStack+renderType+color → `submit(SubmitNodeCollector)` 调 `submitModel`
3. 新增 `ParticleEngineMixin`：HEAD 注入 `createParticleGroup`（自定义 type → GhostAppearanceParticleGroup）+ TAIL 注入 `extract`（追加 ghost group render state）
4. 注册 mixin 到 iceandfire.mixins.json client

## Commit plan
1. `migration: migrate particle api to mc26.2` — 4 简单粒子 + 2 entity 调用点
2. `migration: migrate ghost model particle to mc26.2` — GhostAppearanceParticle 重写 + ParticleGroup + mixin
