# P6-H Particle System 迁移报告

> MC 1.21.1 → 26.2。基线 commit `b54c210`（闪电）。当前：javac 1,729 / unique 1,517（批前 1,876 / 1,640）。

## API 映射

| 1.21.1 | 26.2 |
|---|---|
| `extends TextureSheetParticle` | `extends SingleQuadParticle`（构造 `super(level,x,y,z,sprite)`，SpriteSet 解析为 `spriteProvider.first()`） |
| `this.pickSprite(spriteProvider)` | `this.setSprite(spriteProvider.get(this.random))`（随机单 sprite） |
| `ParticleRenderType.PARTICLE_SHEET_LIT` | `SingleQuadParticle.Layer.OPAQUE`（`getLayer()` 抽象方法） |
| `getRenderType()` | `getLayer()`（SingleQuadParticle 已返回 `SINGLE_QUADS` group） |
| `getLightColor(float)` | `protected int getLightCoords(float)` → `LightCoordsUtil.FULL_BRIGHT` |
| `render(VertexConsumer, Camera, float)`（缩放） | `public float getQuadSize(float)`（每帧尺寸缩放） |
| `ParticleProvider.createParticle(…8 参)` | `…(…, RandomSource random)` 9 参 |
| `TextureSheetParticle` | 已删除 → `SingleQuadParticle`（26.2 官方，`net.minecraft.client.particle`） |

## 修改文件（10 个粒子）

BloodParticle、DragonFlameParticle、DragonFrostParticle、DreadPortalParticle、DreadTorchParticle、GhostAppearanceParticle、HydraBreathParticle、PixieDustParticle、SerpentBubbleParticle、SirenMusicParticle

- 全部改 `SingleQuadParticle`；factory lambda 加 `RandomSource`；`getLayer()` OPAQUE/TRANSLUCENT；`getLightCoords`；`getQuadSize`（HydraBreath/PixieDust/DreadPortal/DreadTorch）

## 错误变化

- javac：1,876 → **1,729**（-147）；unique：1,640 → **1,517**（-123）
- 10 个粒子文件：**0 错误**；新增：0

## 未处理
- 注册链路 `ParticleProviderHolder` 运行时需 `PendingParticleProvider`（Fabric）以支持 `factory(SpriteSet)` 方法引用——当前编译 0 错误（IafRenderers 文件级其它错误遮蔽），运行时需核验。
- GhostAppearanceParticle 自定义模型渲染（`ParticleGroup`/`ParticleGroupRegistry`）——当前 0 错误，运行时需核验。
