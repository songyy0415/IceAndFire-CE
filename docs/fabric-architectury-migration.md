# Fabric/Architectury API 层迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/architectury-api-1.21` vs `architectury-api26.2`（21.0.7）、`fabric-api1.21.1` vs `fabric-api-0.156.0-26.2`、`fabric-loader-0.19.3`。
> 禁止：Minecraft Rendering / EntityRenderer / RenderState / Uranus 特性恢复 / Mixin 重构。

---

## 一、Architectury 13.0.8 → 21.0.7

| 旧 API | 新 API | 修改文件 | 判定 |
|---|---|---|---|
| `dev.architectury.core.item.ArchitecturySpawnEggItem(RegistrySupplier<EntityType>, int, int, Properties)` | **移除** → 26.2 vanilla `SpawnEggItem(Item.Properties)` + `Item.Properties().spawnEgg(EntityType)`（`Items.registerSpawnEgg`：`spawnEgg(type)` 设 `DataComponents.ENTITY_DATA`；颜色改由物品模型数据驱动） | `registry/IafItems.java`（25 个 spawn egg） | ✅ 迁移：`new SpawnEggItem(new Item.Properties().spawnEgg(IafEntities.X.get()))`；颜色参数移除（egg 模型 P5 数据） |
| `dev.architectury.registry.registries.DeferredRegister` / `RegistrySupplier` / `create(MOD_ID, Registries.X)` / `.register(...)` | 同 | 全项目注册表 | ✅ 不变（编译通过） |
| `dev.architectury.networking.NetworkManager`（`registerReceiver` / `registerS2CPayloadType` / `sendToServer`） | 同 | `network/` | ✅ 不变 |
| `dev.architectury.event.events.common.{BlockEvent,InteractionEvent,EntityEvent,PlayerEvent}` / `client.ClientTickEvent` | 同 | `event/` | ✅ 不变 |
| `dev.architectury.injectables.annotations.ExpectPlatform`（`ComponentManager`） | 同 | `impl/` | ✅ 不变（fabric 实现在 P5 验证） |
| `dev.architectury.platform.Platform` / `utils.Env` | 同 | 各处 | ✅ 不变 |
| `dev.architectury.registry.client.rendering.EntityRendererRegistry` | 26.2 渲染注册变化（渲染器泛型 `EntityRenderer<T,S>`） | `registry/IafRenderers.java` | ⛔ **P6**（渲染） |
| `dev.architectury.registry.client.keymappings.KeyMappingRegistry` / `client.rendering.BlockEntityRendererRegistry` / `item.ItemPropertiesRegistry` | 26.2 客户端注册 API | 客户端 | ⛔ **P6** |

## 二、Fabric API 0.116.5+1.21.1 → 0.156.0+26.2

| 旧 API | 新 API | 修改文件 | 判定 |
|---|---|---|---|
| `FabricBrewingRecipeRegistryBuilder.BUILD` | **`FabricPotionBrewingBuilder.BUILD`**（`fabric-content-registries-v0` 类级改名；`addMix` 不变） | `fabric/IceAndFireFabric.java` | ✅ 迁移 |
| `ParticleFactoryRegistry` / `PendingParticleFactory` | **`ParticleProviderRegistry`** / `PendingParticleProvider`（`fabric-particles-v1` 改名） | `fabric/IceAndFireFabricClient.java` | ✅ 迁移 |
| `ParticleEngine.SpriteParticleRegistration`（MC 26.2 移除） | 无（26.2 `ParticleProvider.Sprite`）—— 项目 10 个粒子均用普通工厂，扩展分支为死代码 | `impl/ParticleProviderHolder.java` | ✅ 迁移：删除扩展分支，`applyRegister` 改单参 |
| `AttachmentRegistry.create(Identifier, builder...)`（`fabric-data-attachment-api-v1`） | 同（`initializer/persistent/syncWith/copyOnDeath` 不变） | `fabric/IafAttachments.java` | ✅ 不变 |
| `ResourceManagerHelper.registerBuiltinResourcePack`（`fabric-resource-loader-v0`） | v0 移入 deprecated 仍可用；v1 无等价公开 API | `fabric/IceAndFireFabricClient.java` | ✅ 保留（deprecated 警告） |
| `FabricDefaultAttributeRegistry`（`fabric-object-builder-api-v1`） | 同 | `fabric/IceAndFireFabric.java` | ✅ 不变（死导入，保留） |

## 三、Fabric Loader 0.16.7 → 0.19.3

| 项 | 判定 |
|---|---|
| `ModInitializer` / `ClientModInitializer` / `EnvType` / `Environment` / `FabricLoader.getInstance()` | ✅ 不变 |
| `fabric.mod.json` entrypoints（`main/client/emi/jade/modmenu`）/ `mixins` / `accessWidener` 字段 | ✅ 不变（schemaVersion 1 仍支持） |
| `depends.minecraft` `"26.x"` + `jars`（`META-INF/jars/integration-fabric.jar`） | ✅ P1 已配置 |
| `environment` 检查 / `Platform.isDevelopmentEnvironment()` | ✅ 不变 |

## 四、行为变化

- **Spawn egg 颜色**：1.21.1 `ArchitecturySpawnEggItem` 用代码指定 `backgroundColor/highlightColor`；26.2 颜色由物品模型数据驱动（P5 资源阶段提供 egg 模型/纹理）。生成实体行为不变。
- **粒子注册**：10 个粒子的 `ParticleProvider` 工厂引用需在 P6 增加 `RandomSource` 参数（`BloodParticle::factory` 等 `particle/` 文件）。

## 五、验证

- `:common:compileJava`：错误 2,178 → 2,182（net +5 —— SpawnEggItem 迁移清掉 25 个 Architectury 错误，但粒子工厂方法引用需 RandomSource 的 P6 渲染错误同量浮现）。
- `:fabric:compileJava`：被 common 编译阻塞（common 仍有 ~2,182 错误）；fabric 模块改动按 26.2 Fabric API 源码核对（`FabricPotionBrewingBuilder`、`ParticleProviderRegistry`、`ParticleProviderHolder` 单参）。
