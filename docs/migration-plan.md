# IceAndFire-CE 迁移总体计划（MC 1.21.1 → MC 26.2）

> 目标：将一个 Minecraft 1.21.1 + Mojmap + Architectury 架构 Mod 的 **Fabric 部分**迁移到 Minecraft 26.2，
> 保持 Architectury common/fabric/neoforge 结构，不改 common 为纯 Fabric，保留多加载器能力。
>
> 本文档是迁移的总入口，按 `AGENTS.md` 的优先级顺序 P0–P7 组织。每个阶段列出：目标、涉及文件、具体 API 变化、验证命令、风险。
> 细节依据以下研究文档（`D:/IceAndFire-CE/docs/.research/`）：
>
> - 构建与版本：`deps.md`（依赖/构建升级）、`fabric-api.md`（Fabric Loader / Fabric API）
> - 架构：`common-arch.md`（common 模块）、`fabric-module.md`（fabric 模块）、`mixins.md`（Mixin 验证）
> - 外部依赖：`jupiter.md`、`uranus.md`、`compat.md`、`external-compat.md`
> - Minecraft API：`minecraft.md`
>
> 配套产出文档（后续生成）：`docs/jupiter-migration.md`、`docs/dependency-migration.md`、`docs/fabric-api-migration.md`、
> `docs/minecraft-api-migration.md`、`docs/mixin-migration.md`。
>
> 标记说明：`[待确认]` = 未能从磁盘参考源完整验证，需在迁移时确认（版本号 / API 是否存在 / 运行时行为）。

---

## 1. 项目结构概览

```
D:/IceAndFire-CE
├── settings.gradle          # 当前只 include 'common' 与 'fabric'（neoforge 未包含）
├── gradle.properties        # minecraft_version=1.21.1, enabled_platforms=fabric, Java 21 等
├── build.gradle             # 根脚本：loom 1.11-SNAPSHOT / architectury-plugin 3.4-SNAPSHOT / shadow 8.1.1
├── common/                  # 通用代码（Mojmap，Architectury 抽象），含 mixins + accesswidener
├── fabric/                  # Fabric 平台实现（本迁移目标）
├── neoforge/                # 存在但【未包含在构建中】；代码为 Yarn 映射，已过时
└── gradle/wrapper/          # Gradle 8.14
```

关键事实（已核验）：

- `settings.gradle:12-13` 只 include `common`、`fabric`；`gradle.properties:10` `enabled_platforms=fabric`。
- **neoforge 模块当前无法求值**：`neoforge/build.gradle:24` 引用 `$rootProject.neoforge_version`，但任何 `gradle.properties` 中都没有定义该属性（`deps.md` §1）。且 neoforge 源码为 **Yarn 映射**（如 `net.minecraft.entity.LivingEntity`、`net.minecraft.potion.Potions`），在 Mojmap 仓库中无法编译（`common-arch.md` §4-6）。**结论：本迁移以 Fabric 为目标，neoforge 模块保持排除，不做改写**（AGENTS.md 要求保留该目录结构，但不要求本次迁移其代码）。
- **26.2 不再有独立 mappings 产物**：MC 26.2 直接发布官方（mojmap）类名，`build.gradle:51-53` 的 `loom.layered { officialMojangMappings() }` 在迁移时必须删除（`deps.md` §4）。
- **Mapping 模式变化**：loom-no-remap 下不再有 `modImplementation`/`modApi`/`modCompileOnly`/`modLocalRuntime` 配置，全部改为 Gradle 原生 `implementation`/`api`/`compileOnly`/`runtimeOnly`；不再有 `remapJar`/`include(...)`，打包改用 `META-INF/jars/` 嵌套 + `fabric.mod.json` 的 `"jars"` 数组（`deps.md` §4，以 `Uranus26.2` 为蓝本）。

---

## 2. 版本对照总表（1.21.1 → 26.2）

来源：`deps.md` §3、§9（已对磁盘参考源核验；标 [待确认] 的需解析/网络确认）。

| 项 | 旧（1.21.1） | 新（26.2） |
|---|---|---|
| minecraft | 1.21.1 | 26.2 |
| Java | 21（Zulu-21） | 25（Zulu-25，`minecraft26.2/version.json` `java_version:25`） |
| Gradle | 8.14 | 9.5.1 |
| Loom 插件 | `dev.architectury.loom` 1.11-SNAPSHOT | `dev.architectury.loom-no-remap` 1.17-SNAPSHOT |
| architectury-plugin | 3.4-SNAPSHOT | 3.5-SNAPSHOT |
| Shadow | com.github.johnrengelman.shadow 8.1.1 | com.gradleup.shadow 8.3.10（Uranus）/ 9.4.3（architectury） |
| mappings | `loom.layered { officialMojangMappings() }` | 无（MC 直接携带 mojmap） |
| fabric-loader | 0.16.7 | 0.19.3 |
| fabric-api | 0.116.5+1.21.1 | 0.156.0+26.2 |
| architectury | 13.0.8 | 21.0.7 |
| modmenu | 11.0.3 | 20.0.1 |
| emi | 1.1.19+1.21.1 | 1.1.24（坐标后缀 [待确认]） |
| jei | jei-1.21.1-fabric-api 19.21.0.247 | jei-26.2-fabric-api 30.7.0.39 |
| jade（modrinth） | pA0xvozk | 新 hash；版本 26.2.10 |
| trinkets（modrinth） | JagCscwi | 新 hash；版本 4.1.0-beta.3+26.2（**eu.pb4 重写，mod id `trinkets_updated`**） |
| uranus（modrinth） | FH0tB0dy | 新 hash；版本 2.4.1-bugfix |
| jupiter（modrinth） | 5pNXzmee | 新 hash；版本 2.4.2 |
| integration | 0.2（jitpack，MC 1.20.4 构建） | [待确认]（loader-based，运行时验证） |
| ponder | Ponder-Fabric-1.21.1:1.0.61 | [待确认]（磁盘无 26.2 参考源） |
| cca（cardinal-components） | 6.1.1（modApi，无源码引用） | 大概率可删除 [待确认] |

---

## 3. 文件清单（迁移将涉及）

### 3.1 构建系统（P0–P1 主战场）

| 文件 | 变化 |
|---|---|
| `gradle.properties` | 所有版本属性、Java 路径、`enabled_platforms` |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 8.14 → 9.5.1 |
| `build.gradle`（根） | 插件 ID、`minecraft` 坐标、Java release、mappings 块、repositories |
| `settings.gradle` | 如需重新纳入模块；当前保持 common+fabric |
| `common/build.gradle` | 配置关键字与坐标 |
| `fabric/build.gradle` | 配置关键字、打包方式（shadowJar→META-INF/jars） |
| `neoforge/build.gradle` | 本次不构建（记录说明即可） |
| `.github/workflows/build.yml` | Java 21→25、Gradle 8.14→9.5.1 |
| `mappings-patch/` | 不再被任何构建引用，可移除（`deps.md` §1） |

### 3.2 Fabric 模块（P2 / P5）

| 文件 | 变化 |
|---|---|
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java` | 酿造 API 类名重命名、Trinkets mod id |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java` | 粒子注册、内置资源包、`Identifier` |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java` | `Identifier`（附件 API 本身稳定） |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/ModMenu.java` | 无变化（Jupiter builder 两版本一致） |
| `fabric/src/main/java/com/iafenvoy/iceandfire/impl/fabric/ComponentManagerImpl.java` | 无变化 |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/TrinketsRegistry.java` | eu.pb4 重写 |
| `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/compat/trinkets/SimpleTickItemWrapper.java` | eu.pb4 重写 + `Item.inventoryTick` 新签名 |
| `fabric/src/main/resources/fabric.mod.json` | depends、`jars` 数组、（可选）`"jei"` entrypoint |
| `fabric/src/main/resources/iceandfire.accesswidener` | 与 common 副本同步更新 |
| `fabric/src/main/resources/data/trinkets/{entities,slots,tags}/...` | 数据格式基本兼容；是否重写 [待确认] |

### 3.3 配置 / 外部依赖（P3）

| 文件 | 变化 |
|---|---|
| `common/src/main/java/com/iafenvoy/iceandfire/config/IafCommonConfig.java:36` | `ResourceLocation`→`Identifier`（`AutoInitConfigContainer` 构造） |
| `common/src/main/java/com/iafenvoy/iceandfire/config/IafClientConfig.java:15` | `ResourceLocation`→`Identifier`（`FileConfigContainer` 构造） |
| `common/src/main/java/com/iafenvoy/iceandfire/compat/emi/ForgeRecipeHolder.java:34` | `getRecipeManager()`→`getRecipeMap().byType()`；`getDisplayHeight()` 1120 疑似 bug |
| `common/src/main/java/com/iafenvoy/iceandfire/compat/jade/MultipartComponentProvider.java:36` | `HealthElement` 4 参数构造 |
| `common/src/main/java/com/iafenvoy/iceandfire/compat/jei/*.java`（5 文件） | [待确认] 26.2 JEI API |
| `common/src/main/java/com/iafenvoy/iceandfire/compat/ponder/*.java`（2 文件） | [待确认] 26.2 Ponder API |
| `common/src/main/java/com/iafenvoy/iceandfire/compat/delight/DelightFoodItem.java` | 无变化（仅 `Platform.isModLoaded`） |

### 3.4 Mixin（P4 服务端 / P6 客户端）

`common/src/main/java/com/iafenvoy/iceandfire/mixin/`（11 个，配置见 `common/src/main/resources/iceandfire.mixins.json`）：

| Mixin | 26.2 状态（`mixins.md`） |
|---|---|
| `ChickenMixin` | 类移包 + 下蛋路径重写（`spawnAtLocation(ItemLike)` 删除） |
| `ChunkRegionMixin` | `Util`→`util.Util`（仅 target 字符串） |
| `LivingEntityMixin` | `onEffectRemoved`→`onEffectsRemoved(Collection)` |
| `MobEntityMixin` | `dropFromLootTable(ServerLevel,DamageSource,boolean)` + `spawnAtLocation` 带 level |
| `GameRendererMixin` | `Camera.setup` 删除；`reloadShaders`/`ShaderInstance` 删除 → ShaderManager |
| `InGameHudMixin` | `Gui`→`Hud`；`renderCameraOverlays`→`extractCameraOverlays` |
| `LivingEntityRendererMixin` | `render(...)`→`extractRenderState` 渲染状态架构 |
| `PlayerEntityRendererMixin` | `PlayerRenderer`→`AvatarRenderer` |
| `RotatingCubeMapRendererMixin` | `PanoramaRenderer`→`Panorama`；`extractRenderState` |
| `TitleScreenMixin` | `render`→`extractRenderState`；`LogoRenderer.renderLogo`→`extractRenderState`；`@Local int i` 失效 |
| `WorldRendererMixin` | `renderLevel`→`render`；`entitiesForRendering()` 移至 LevelExtractor；`bufferSource()` 删除 |

### 3.5 客户端渲染 / 屏幕 / 粒子（P6 主战场）

- 实体渲染器：`common/.../render/entity/*.java`（约 60 个，含 `feature/` 下的 `DragonRiderFeatureRenderer`、`DragonBannerFeatureRenderer` 等）——全部从 `render(...)` 迁到 `extractRenderState/submit`。
- 方块实体渲染器：`render/block/{Jar,Lectern,Podium,EggInIce,DreadPortal,DreadSpawner,PixieHouse}BlockEntityRenderer.java`。
- 物品渲染器：`render/item/*.java`（`MiscItemRenderer`、`TideTridentItemRenderer`、`DeathwormGauntletRenderer`、`GorgonHeadRenderer`、`TrollWeaponRenderer` 等）+ `render/item/armor/{Scale,Basic}ArmorRenderer.java`。
- 模型 / 动画：`render/model/**`（约 40 个）——`AdvancedEntityModel`、`AdvancedModelBox`、`TabulaModel`、`BasicModelPart`、`ITabulaModelAnimator`、`HideableLayer`、`HideableModelRenderer`、`ModelAnimator`，以及 `render/model/animator/*`（含 `DragonTabulaModelAnimator`）。Uranus 26.2 已做 render-state 重写，本项目模型类须同步。
- 杂项渲染：`render/misc/{ChainRenderer,CockatriceBeamRenderer,FrozenStateRenderer,LightningBoltData,LightningRenderer}.java`、`render/PortalRenderHelper.java`、`render/SirenShaderRenderHelper.java`、`render/RenderVariables.java`、`render/TitleScreenRenderManager.java`（在 `screen/` 下）、`render/texture/DragonTextureProvider.java`、`registry/IafRenderLayers.java`。
- 屏幕：`screen/gui/{Bestiary,DragonForge,Dragon,Hippocampus,Hippogryph,Lectern,Podium}Screen.java` + `screen/gui/bestiary/*` + `screen/TitleScreenRenderManager.java`——`render(GuiGraphics,...)`→`extractRenderState(GuiGraphicsExtractor,...)`。
- 粒子：`particle/{Blood,DragonFlame,DragonFrost,DreadPortal,DreadTorch,HydraBreath,SerpentBubble,SirenMusic,PixieDust,GhostAppearance}Particle.java` 等——`TextureSheetParticle`→`SingleQuadParticle`、`createParticle` 加 `RandomSource`、`ParticleRenderType.CUSTOM` 删除。
- 注册：`registry/IafRenderers.java`（粒子/实体/方块实体渲染器注册）、`registry/IafRenderLayers.java`、`registry/IafKeybindings.java`。

### 3.6 Minecraft API 迁移涉及（P4）

- 全局：全项目 `ResourceLocation`→`Identifier`（数百处 import）。
- `entity/TideTridentEntity.java:30,37,41` —— `ThrownTrident`/`AbstractArrow` 移包到 `world/entity/projectile/arrow/`。
- `entity/{Troll,StymphalianBird,Siren,SeaSerpent}Entity.java` + `StoneStatueEntity.java:194` —— `getBaseExperienceReward` 加 `ServerLevel` 参数。
- `entity/DragonBaseEntity.java`、`HippogryphEntity.java`、`entity/ai/DragonAIAttackMeleeGoal.java` —— 涉及 Uranus raycoms 寻路（**Uranus 26.2 已删除该包**，见 §7）。
- `entity/DeathWormEntity.java:78` —— `ICustomCollisions`（Uranus 已删除）。
- `entity/pathfinding/CyclopsNavigation.java:9` —— `CustomCollisionsNavigator`（Uranus 已删除）。
- `registry/IafTrades.java:36` —— `PoiTypes.TYPE_BY_STATE` value 变 `Holder<PoiType>`（语义变化，AW 保留）。
- `registry/IafScreenHandlers.java` —— `MenuType` 私有构造 + `MenuScreens.register` 私有。
- `event/ServerEvents.java`、`screen/TitleScreenRenderManager.java` —— `Platform.isNeoForge()`/`isFabric()` 平台门（保留或复核）。

---

## 4. P0 环境确认

**目标**：确认工具链/依赖版本与磁盘参考源一致，记录基线。

**具体项**（对照 `deps.md` §3、§7）：

| 检查项 | 旧值 | 目标值 |
|---|---|---|
| Java | 21（`gradle.properties:5`） | 25（Zulu-25；`minecraft26.2/version.json` `java_version:25`） |
| Gradle | 8.14 | 9.5.1 |
| Loom | `dev.architectury.loom` 1.11-SNAPSHOT | `dev.architectury.loom-no-remap` 1.17-SNAPSHOT |
| architectury-plugin | 3.4-SNAPSHOT | 3.5-SNAPSHOT |
| fabric-loader | 0.16.7 | 0.19.3 |
| fabric-api | 0.116.5+1.21.1 | 0.156.0+26.2 |
| architectury | 13.0.8 | 21.0.7 |

**涉及文件**：`gradle.properties`、`build.gradle`、`gradle/wrapper/gradle-wrapper.properties`、`.github/workflows/build.yml`。

**验证命令**：
```
./gradlew tasks
./gradlew buildEnvironment
```

**风险**：
- 网络/代理：Modrinth 26.2 各依赖的 version-file hash 需要联网解析（`deps.md` §5，全部 [待确认]）。
- 参考源版本与"最新"不一致（0.19.3 / 0.156.0 / 21.0.7 是磁盘可用的参考版本，实际可更高）。

---

## 5. P1 Gradle / Loom / Architectury 升级

**目标**：构建系统整体升级到 26.2 生态（loom-no-remap、无 mappings、Java 25、Gradle 9.5.1），common/fabric 模块可配置、可编译。

**涉及文件**：`build.gradle`（根）、`gradle.properties`、`common/build.gradle`、`fabric/build.gradle`、`.github/workflows/build.yml`。

**具体变化**：
1. 根 `build.gradle`：
   - 插件：`dev.architectury.loom` 1.11-SNAPSHOT → **`dev.architectury.loom-no-remap` 1.17-SNAPSHOT**；`architectury-plugin` 3.4-SNAPSHOT → 3.5-SNAPSHOT；`com.github.johnrengelman.shadow` 8.1.1 → **`com.gradleup.shadow`**（8.3.10/9.4.3，`deps.md` §8.7）。
   - 依赖：`minecraft "net.minecraft:minecraft:..."` → **`minecraft "com.mojang:minecraft:$rootProject.minecraft_version"`**；删除 `mappings loom.layered { officialMojangMappings() }`（26.2 无独立 mappings，`deps.md` §4）。
   - Java：`sourceCompatibility/targetCompatibility/release` 21 → **25**。
   - repositories：删除 `flatDir { dirs '../mappings-patch' }`。
2. `gradle.properties`：`org.gradle.java.home` → Zulu-25；更新全部版本属性（§2 表）。
3. `common/build.gradle`：`modImplementation "net.fabricmc:fabric-loader:..."` 等 → 原生 `implementation`（loom-no-remap 无 mod* 配置，`deps.md` §4）；更新 architectury/uranus/jupiter/integration 坐标。
4. `fabric/build.gradle`：参照 `Uranus26.2/fabric/build.gradle` 重写：
   - 删除 `shadowBundle`/`shadowJar`/`remapJar`（`fabric/build.gradle:14-29,73-80`）。
   - common 资源（`iceandfire.mixins.json`、`iceandfire.accesswidener`）在 `processResources` 中 `from(project(':common').file(...))` 复制进 fabric 资源。
   - Integration（及可选 architectury-fabric）放入 `META-INF/jars/` 嵌套，并在 `fabric.mod.json` 增加 `"jars"`。
   - `loom { injectAccessWidener(tasks.named('jar')) }`。
5. `settings.gradle`：保持只 include `common`+`fabric`（neoforge 不纳入）。

**验证命令**：
```
./gradlew compileJava
./gradlew build
```

**风险**：
- loom-no-remap DSL 与旧脚本差异大（`modImplementation` 等关键字全部替换）——以 `Uranus26.2` 为权威蓝本。
- architectury 21.0.7 已在本地 Gradle 缓存（`deps.md` §3）；若实际解析失败需先修仓库源。
- Shadow 插件 ID 变更（`com.github.johnrengelman.shadow` 已弃用/迁移到 `com.gradleup.shadow`）。
- `META-INF/jars` 打包时不能用 `shadowJar from(zipTree(...))` 展开嵌套 jar（`deps.md` §4 警告）。

---

## 6. P2 Fabric Loader / Fabric API 适配

**目标**：Fabric Loader 0.19.3 + Fabric API 0.156.0 的 API 变化适配到 fabric 模块。

**涉及文件**：
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabric.java`
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IceAndFireFabricClient.java`
- `fabric/src/main/java/com/iafenvoy/iceandfire/fabric/IafAttachments.java`
- `fabric/src/main/resources/fabric.mod.json`
- `fabric/src/main/resources/iceandfire.accesswidener`（与 common 副本一致）

**具体 API 变化**（来源 `fabric-api.md` §3、§5、§6；`fabric-module.md` §6）：

| 位置 | 旧 API | 新 API |
|---|---|---|
| `IceAndFireFabric.java:9,19` | `FabricBrewingRecipeRegistryBuilder.BUILD` | **`FabricPotionBrewingBuilder.BUILD`**（同名模块/包，仅类名+回调参数类型变化）；lambda `builder.addMix(Potions.WATER, item, Potions.WATER_BREATHING)` 不变（`PotionBrewing.Builder.addMix(Holder<Potion>,Item,Holder<Potion>)` 在 26.2 仍存在） |
| `IceAndFireFabric.java:8` | `FabricDefaultAttributeRegistry` | **死 import**，可删除 |
| `IceAndFireFabricClient.java:8,20` | `ParticleFactoryRegistry` | **`ParticleProviderRegistry`**（同包）；`getInstance()::register` 主路径不变；扩展 `SpriteParticleRegistration` 分支为死代码（无粒子使用），可删除 |
| `IceAndFireFabricClient.java:9,10,22` | `ResourceManagerHelper`/`ResourcePackActivationType` | 保留可用（26.2 位于 `deprecated/fabric-resource-loader-v0`，随 fabric-api BOM 发布，预期编译告警）；参数 `ResourceLocation`→`Identifier`。长期迁到 `fabric-resource-loader-v1`（暂无等价公开 API，`fabric-api.md` §3.4） |
| `IceAndFireFabricClient.java:13`、`IafAttachments.java` | `net.minecraft.resources.ResourceLocation` | **`net.minecraft.resources.Identifier`**（`Identifier.fromNamespaceAndPath`，同签名） |
| `IafAttachments.java:17-19` | `AttachmentRegistry.create(ResourceLocation, Consumer<Builder>)` | 仅参数类型改 `Identifier`；`initializer/persistent/syncWith/copyOnDeath` 全部保留；`AttachmentSyncPredicate.all()` 保留 |
| `fabric.mod.json` depends | `"minecraft":"1.21.x"` | `"26.x"`；建议加 `"fabricloader":">=0.18.4"`、`"fabric-api":"*"`；加 `"jars"` 数组（P1） |

**AccessWidener（`iceandfire.accesswidener`，20 行；`fabric-module.md` §4 表）**：
- **删除**：`ParticleEngine$SpriteParticleRegistration`（类在 26.2 删除 → `ParticleProvider$Sprite`）；`GameRenderer loadEffect`（方法删除，改为 `setPostEffect`/`clearPostEffect` 等）。
- **改包**：`ThrownTrident`（ID_LOYALTY/ID_FOIL/dealtDamage）、`AbstractArrow`（pickupItemStack/setPierceLevel）→ `net/minecraft/world/entity/projectile/arrow/`。
- **改描述符**：`LivingEntity getBaseExperienceReward ()I` → `(Lnet/minecraft/server/level/ServerLevel;)I`。
- **复核**：`EntityRenderDispatcher renderers` 现为 `Map<EntityType<?>, EntityRenderer<?,?>>`（泛型擦除后描述符可能仍匹配，需实机验证）；`PoiTypes.TYPE_BY_STATE` value 变 `Holder<PoiType>`（语义，AW 保留）。
- 其余 9 行（`Mob.goalSelector/targetSelector`、`NodeEvaluator.mob`、`ClientLevel.entityStorage`、`CombatTracker.getMostSignificantFall`、`Camera.getMaxZoom/move`、`Screen.renderables`、`SimpleParticleType.<init>(Z)`）在 26.2 均存在，保留。
- 注意：AW 是**单文件共享**（`common/build.gradle:6` 声明一次，fabric 通过 `loom.accessWidenerPath` 继承）；修 common 的 `common/src/main/resources/iceandfire.accesswidener`，fabric 副本同步。

**验证命令**：
```
./gradlew compileJava
```

**风险**：
- **酿造配方注册是最大断点**：26.2 fabric-api 的 `FabricPotionBrewingBuilder` 仍存在但事件改为在 vanilla `PotionBrewing` 代码构建流程中触发（`fabric-api.md` §3.2 / `fabric-module.md` §7.1）——迁移后需实测 `Shiny Scales → Water Breathing` 药水是否仍生效；若无效需改用 Mixin 进 `PotionBrewing.bootstrap`（[待确认] 最终机制）。
- deprecated `fabric-resource-loader-v0` 的 Mojmap 编译兼容需冒烟测试（`fabric-api.md` §7）。
- 附件 API 仍为 `@ApiStatus.Experimental`，保留 `@SuppressWarnings("UnstableApiUsage")`。

---

## 7. P3 外部依赖适配（Jupiter / Uranus / EMI / Trinkets / Jade / Integration / Ponder / JEI）

**目标**：升级外部依赖到 26.2 版本并修复其 API 断点。分库说明见对应研究文档。

### 7.1 Jupiter（`jupiter.md`）
- **坐标**：`maven.modrinth:jupiter:5pNXzmee` → 26.2（版本 2.4.2，具体 hash [待确认]）。
- 包根 `com.iafenvoy.jupiter` **不变**；项目用到的全部 API 除一处外无变化。
- **唯一代码变化**：
  - `common/.../config/IafCommonConfig.java:36`：`super(ResourceLocation.fromNamespaceAndPath(...), ...)` → `Identifier.fromNamespaceAndPath(...)`。
  - `common/.../config/IafClientConfig.java:15`：同上。
- `fabric/.../ModMenu.java` 的 `ConfigSelectScreen.builder(...).server(...).client(...).build()` 两版本一致，**无变化**。
- 文件：`common/.../config/IafCommonConfig.java`、`IafClientConfig.java`、`common/build.gradle:21`、`fabric/build.gradle:44`。

### 7.2 Uranus（`uranus.md`）
- **坐标**：`maven.modrinth:uranus:FH0tB0dy` → 26.2（版本 2.4.1-bugfix，hash [待确认]）。
- 服务端/工具类（`RandomHelper`、`Event`、`EntityEvents`/`LivingEntityEvents`/`PlayerEvents`、`VecUtil`、`BlockUtil`、`EntityUtil`、`FoodUtils`、`MemorizeSupplier`、`ServerHelper`、`ShapeBuilder`、动画 `Animation`/`AnimationHandler`/`IAnimatedEntity`）**基本无源码变化**。
- **已删除且被本项目使用的 API（需在 mod 内重写或退回 vanilla）**：
  - raycoms 寻路：`AdvancedPathNavigate`、`IPassabilityNavigator`、`PathingStuckHandler`、`ICustomSizeNavigator`（`DragonBaseEntity`、`HippogryphEntity`、`DragonAIAttackMeleeGoal`）→ 改回 vanilla `PathNavigation` 或自研飞/尺寸寻路（**高风险**）。
  - 自定义碰撞：`CustomCollisionsNavigator`（`CyclopsNavigation`）、`ICustomCollisions`（`DeathWormEntity`）→ 删除接口，改用 `Entity` 碰撞覆写。
  - `DynamicItemRenderer`（`registry/IafRenderers.java:140-148`、`render/item/*`）→ 需自研 `ItemModel`/BEWLR 式替代（[待确认] 机制）。
- **客户端模型 render-state 重写**（Uranus 26.2 已重写，本项目模型类须跟进）：`TabulaModel`、`AdvancedEntityModel`、`BasicEntityModel`、`ITabulaModelAnimator`、`HideableLayer`、`ArmorModelBase`、`IArmorRendererBase` —— 全部 `Entity` 泛型改 `EntityRenderState`、构造传 `ModelPart` root、`render/setupAnim` 重写、`MatrixStack→PoseStack`、`VertexConsumer.vertex→addVertex`（详见 P6 / `uranus.md` §4.4）。这是客户端最大工作块。
- 文件：`common/build.gradle:20`、`fabric/build.gradle:43` + 上述实体/模型/渲染器文件。

### 7.3 EMI（`compat.md` §4.1 / `external-compat.md` §1）
- **坐标**：`dev.emi:emi-fabric:1.1.19+1.21.1:api` → 1.1.24（后缀 [待确认]）；`:api` classifier 保留。
- **唯一断点**：`compat/emi/ForgeRecipeHolder.java:34` `registry.getRecipeManager().getAllRecipesFor(...)` → `registry.getRecipeMap().byType(...)`（返回 `Collection<RecipeHolder<T>>`；`getRecipeMap()` 可空需判空）。
- **顺手修**：`getDisplayHeight()` 返回 `1120`（疑似 bug，正常应为小值，对照纹理 170×79）。
- 其余 EMI API（`EmiRecipeCategory` 3 参构造、`EmiTexture` 5 参构造、`EmiIngredient.of`、`EmiStack.of`、`WidgetHolder.addTexture/addSlot`、`SlotWidget.large/recipeContext`）两版本一致。
- entrypoint `"emi"` 两版本都读，**无变化**。

### 7.4 Trinkets（`compat.md` §4.3 / `external-compat.md` §2 / `fabric-module.md` §2.6）—— **完全重写，最高工作量**
- **26.2 是另一个 mod**：`dev.emi.trinkets` → **`eu.pb4.trinkets`**（Patbox "Trinkets Updated"），mod id `trinkets` → **`trinkets_updated`**，版本 4.1.0-beta.3+26.2，坐标需重解析（hash [待确认]）。
- **API 映射**：
  - `TrinketsApi.registerTrinket(Item, Trinket)` → `eu.pb4.trinkets.api.callback.TrinketCallback.setCallback(Item, TrinketCallback)`。
  - `Trinket` → `TrinketCallback`（`tick(ItemStack, TrinketSlotAccess, LivingEntity)`，方法体不变）。
  - `SlotReference` → `TrinketSlotAccess` / `TrinketSlotReference`。
- **`Item.inventoryTick` 签名变化穿透到 wrapper**：26.2 为 `Item.inventoryTick(ItemStack, ServerLevel, Entity, @Nullable EquipmentSlot)`。`SimpleTickItemWrapper.tick` 内的调用需改 `if (entity.level() instanceof ServerLevel serverLevel) this.item.inventoryTick(stack, serverLevel, entity, null)`；客户端侧行为与正确 `EquipmentSlot` [待确认]。
- **数据文件**：`data/trinkets/{entities,slots,tags}/...` 的 JSON 结构与 26.2 `SlotLoader`/`EntitySlotLoader` 大体兼容（`amount` 仍解析、`legs/belt` 仍存在），但 eu.pb4 方案推荐走 `trinkets:equipment` 数据组件 + datagen，是否保留 tag 方案 [待确认]。
- **CCA**：`dev.onyxstudios.cardinal-components-api:6.1.1` 无任何源码引用，且 26.2 Trinkets 改用 `dev.yumi.mc.core:yumi-mc-foundation`——**大概率删除 CCA 依赖** [待确认]。
- **`IceAndFireFabric.java:20`**：`IntegrationExecutor.runWhenLoad("trinkets", ...)` 的字符串必须改为 `"trinkets_updated"`，否则静默失效。
- 文件：`fabric/.../compat/trinkets/TrinketsRegistry.java`、`SimpleTickItemWrapper.java`、`fabric/src/main/resources/data/trinkets/**`、`fabric/build.gradle:58-60`、`IceAndFireFabric.java:20`、`gradle.properties`（`cca_version`）。

### 7.5 Jade（`compat.md` §4.2 / `external-compat.md` §3）
- **坐标**：`maven.modrinth:jade:pA0xvozk` → 新 hash；版本 26.2.10。
- **唯一断点**：`compat/jade/MultipartComponentProvider.java:36` `new HealthElement(maxHealth, health)` → **4 参** `new HealthElement(Hud.HeartType, maxHealth, health, absorption)`。mojmap 下 heart-type 枚举名 [待确认]（1.21.1 为 `net.minecraft.client.gui.Gui.HeartType`）。`ArmorElement(float)` 不变。
- 其余（`@WailaPlugin`、`IWailaPlugin`、`IComponentProvider`、`EntityAccessor`/`BlockAccessor`、`ITooltip.add/addAll/clear`）两版本一致；`"jade"` entrypoint 一致。
- 建议：`HealthElement` 属 `impl` 包（不稳定 API），可考虑改为纯 `Component` 文本替代（`external-compat.md` §3.4 风险）。

### 7.6 Integration（`external-compat.md` §4 / `compat.md` §2.7）
- 坐标不变（`integration_version=0.2`，`integration-common`/`-fabric`）。
- 该库 loader-based、无 Minecraft 版本 API 依赖，理论上 26.2 可编译运行；但其 `@ExpectPlatform` 需 Architectury 注解处理，**需运行时验证 26.2 下 `IntegrationExecutor` 是否工作** [待确认]。
- 若失效，退路：把 `IntegrationExecutor.runWhenLoad` 模式内联为 `Platform.isModLoaded` / `FabricLoader.isModLoaded` 门。

### 7.7 JEI（[待确认]）与 Ponder（[待确认]）
- JEI：磁盘无 26.2 参考源，26.2 API 全部 [待确认]。当前版本 `mezz.jei:jei-1.21.1-fabric-api:19.21.0.247` → `jei-26.2-fabric-api:30.7.0.39`（`deps.md` §3，源自 emi26.2 的 properties）。
- **潜在问题**：`fabric.mod.json` 没有 `"jei"` entrypoint 键，JEI 插件在 Fabric 端可能从未加载（`compat.md` §5 Surprise 3）——迁移时验证；如需要补 `"jei":["com.iafenvoy.iceandfire.compat.jei.IceAndFireJeiPlugin"]`。
- Ponder：磁盘无 26.2 参考源，`Ponder-Fabric-26.2:<v>` 坐标 [待确认]；`compat/ponder/*` 的 `PonderStoryBoard`/`SceneBuilder` API 变化 [待确认]。
- Delight：无变化。

**验证命令**：
```
./gradlew compileJava
```

**风险**：
- Trinkets 完全重写（7.4）是本阶段最大风险项。
- Jupiter/Uranus/Trinkets/Jade 的 Modrinth hash 均需联网解析。
- JEI/Ponder 无 26.2 参考源——若上游无 26.2 版本，需暂时摘除其编译依赖与 entrypoint（记录为功能降级 [待确认]）。

---

## 8. P4 Minecraft API 迁移

**目标**：完成与加载器无关的 MC API 迁移（服务端/通用逻辑优先），让 common 编译通过。

**涉及文件与具体变化**（`minecraft.md` 全篇）：

| 变化 | 文件 | 说明 |
|---|---|---|
| `ResourceLocation`→`Identifier` | 全 common/fabric | 机械重命名，无行为变化 |
| `Util`→`net.minecraft.util.Util` | `mixin/ChunkRegionMixin.java:22` target 字符串 | 26.2 移包 |
| `Chicken`→`animal.chicken.Chicken` | `mixin/ChickenMixin.java` | 移包 |
| `ThrownTrident`/`AbstractArrow`→`projectile.arrow/` | `entity/TideTridentEntity.java:30,37,41`、AW | 移包 |
| `getBaseExperienceReward(ServerLevel)` | `entity/{Troll,StymphalianBird,Siren,SeaSerpent}Entity.java`（override）、`StoneStatueEntity.java:194`（调用） | 加参数 |
| `Mob.dropFromLootTable(ServerLevel,DamageSource,boolean)` | `mixin/MobEntityMixin.java:29` | 处理函数加 `ServerLevel`；内部 `spawnAtLocation` 带 level |
| `onEffectRemoved`→`onEffectsRemoved(Collection)` | `mixin/LivingEntityMixin.java:33` | 遍历集合找 `FrozenStatusEffect` |
| `Item.inventoryTick(ItemStack,ServerLevel,Entity,@Nullable EquipmentSlot)` | `fabric/.../trinkets/SimpleTickItemWrapper.java:18` 及其它 `inventoryTick` 调用者 | 需 `ServerLevel` 守卫 |
| `KeyMapping(String,int,KeyMapping.Category)` | `registry/IafKeybindings.java:10-13` | 第三参 String→`KeyMapping.Category.GAMEPLAY` |
| `MenuType` 私有构造 / `MenuScreens.register` 私有 | `registry/IafScreenHandlers.java:24-25`、`registerGui()` | 改用 Architectury `MenuRegistry`/`ScreenRegistry`（[待确认] Architectury 21.0.7 对应 API） |
| `PoiTypes.TYPE_BY_STATE` value→`Holder<PoiType>` | `registry/IafTrades.java:36` | 语义变化，读取侧适配 |
| `Entity.spawnAtLocation` 单参删除（须带 level） | 各类 spawn 调用点 | `spawnAtLocation(ServerLevel, ItemLike/ItemStack, ...)` |

**验证命令**：
```
./gradlew compileJava
```

**风险**：
- 覆盖面广（`ResourceLocation`→`Identifier` 数百处）易漏。
- `getBaseExperienceReward` 的 override 签名不一致会导致编译失败，属显式错误可逐一修复。
- Architectury 21.0.7 自身是否还有 API 变化 [待确认]（`minecraft.md` §13 网络、§15 创造标签栏均以 Architectury 抽象为界）。

---

## 9. P5 Fabric 实现迁移

**目标**：fabric 模块自身的平台代码迁移（P2 已覆盖 Fabric API 类名；本阶段完成平台桥接/附件/元数据）。

**涉及文件**：
- `fabric/.../fabric/IceAndFireFabric.java`、`IceAndFireFabricClient.java`、`IafAttachments.java`（P2 已改，此处复核）
- `fabric/.../impl/fabric/ComponentManagerImpl.java` —— **无变化**（`getAttachedOrCreate` 来自 fabric `AttachmentTarget`，26.2 稳定）
- `fabric/src/main/resources/fabric.mod.json` —— `depends`、`jars`、可选 `"jei"`
- `common/.../impl/ComponentManager.java` —— `@ExpectPlatform` 桥保持（唯一平台桥，`common-arch.md` §5.1）

**具体项**：
- 附件数据流：common 的 `ChainData`/`MiscData`/`PortalData`（`CODEC`+`PACKET_CODEC`）与 fabric `AttachmentRegistry` 的 `persistent/syncWith/copyOnDeath` 组合在 26.2 全部保留（`fabric-api.md` §3.1），无需改逻辑。
- 酿造（`IceAndFireFabric.java:19`）：改名后复核实际药水注册是否生效（见 P2 风险）。
- Trinkets mod id（`IceAndFireFabric.java:20`）：`"trinkets"`→`"trinkets_updated"`（见 P3）。
- 内置资源包 `iaf_legacy`：`Identifier` 重命名后复核 `ResourcePackActivationType.NORMAL` 行为。

**验证命令**：
```
./gradlew compileJava
./gradlew build
```

**风险**：
- 低（本阶段代码量小）；主要风险是 P2 遗留的酿造机制与 P3 的 Trinkets。

---

## 10. P6 Client 迁移

**目标**：把 26.2 的 **render-state 提取架构**（`extractRenderState` + `submit` + `SubmitNodeCollector` + `RenderPipeline`）落到全部客户端代码。这是全项目最大、最重构化的一块（`minecraft.md` §20；`mixins.md` 结论：11 个 Mixin 0 个可直接平移，7 个需设计重构）。

**涉及文件**：见 §3.5 全部清单（render/entity、render/block、render/item、render/model、render/misc、screen/gui、particle、相关 mixin、`IafRenderers`、`IafRenderLayers`、`TitleScreenRenderManager`、`SirenShaderRenderHelper`、`RenderVariables`、`PortalRenderHelper`）。

**具体 API 变化**：

1. **实体渲染管线**（`minecraft.md` §8）：
   - `EntityRenderer`/`LivingEntityRenderer`/`MobRenderer` 的 `render(T,float,float,PoseStack,MultiBufferSource,int)` → `extractRenderState(T,S,float)` + `submit(S,PoseStack,SubmitNodeCollector,CameraRenderState)`。
   - `EntityRendererProvider.Context` 的 `getItemInHandRenderer()` → `getItemModelResolver()`；`ItemInHandRenderer` 移到 `client/renderer/ItemInHandRenderer`。
   - `PlayerRenderer` → **`AvatarRenderer`**（`mixin/PlayerEntityRendererMixin` 改 `@Mixin(AvatarRenderer.class)`，注入 `extractRenderState`）。
   - `EntityRenderDispatcher.render(...)` 删除 → "渲染一个实体" 的替代路径 [待确认]（`DragonRiderFeatureRenderer:54,116`、`DreadSpawnerBlockEntityRenderer:34`、`StoneStatueEntityRenderer:74` 受影响）。
2. **方块实体渲染**（`minecraft.md` §8.5）：`BlockEntityRenderer<T>` → `BlockEntityRenderer<T,S extends BlockEntityRenderState>`（`createRenderState`/`extractRenderState`/`submit`）；`BlockEntityRenderDispatcher.renderItem` 删除（`MiscItemRenderer.java:29-33`）。
3. **实体模型 / 动画（Uranus）**：`TabulaModel`/`AdvancedEntityModel`/`BasicEntityModel`/`ITabulaModelAnimator`/`HideableLayer`/`ArmorModelBase`/`IArmorRendererBase` 全部 re-type 到 render-state（`uranus.md` §4.4）。`Model.renderToBuffer` 变为 final，改为 `Model.submit` + `ModelPart` root 绘制。
4. **GUI**（`minecraft.md` §9）：
   - `GuiGraphics` → `GuiGraphicsExtractor`；`Screen.render(GuiGraphics,int,int,float)` → `extractRenderState(GuiGraphicsExtractor,int,int,float)`（7 个 Screen）。
   - `drawString`→`text`、`renderItem`→`item`、`blit/blitSprite` 需 **`RenderPipeline` 首参**（`RenderPipelines.GUI_TEXTURED` 等，常量确切值 [待确认]）。
   - `Gui` HUD → **`Hud`**（`mixin/InGameHudMixin` 改 `@Mixin(Hud.class)`，`renderCameraOverlays`→`extractCameraOverlays`）。
   - `PanoramaRenderer`→`Panorama`、`LogoRenderer.renderLogo`→`extractRenderState`、`SplashRenderer(String)`→`SplashRenderer(Component)`。
   - `RenderSystem` 即时模式 API（`setShader`/`setShaderColor`/`setShaderTexture`/`enableBlend`/`disableBlend`/`enableDepthTest`/`disableDepthTest`/`viewport`/`Lighting.setupFor3DItems`）全部删除 → 改为 GPU/RenderPipeline 路径（34 处 `RenderSystem.` 调用点，`minecraft.md` §9.5/§17）。
5. **着色器 / 后处理**（`minecraft.md` §10）：
   - `ShaderInstance`/`GameRenderer.reloadShaders` 删除 → `ShaderManager` + `ShaderDefines`；dread portal 着色器迁到资源包 `shaders/` + `ShaderManager` 查找（`GameRendererMixin` 重构，`RenderVariables.DREAD_PORTAL_PROGRAM` 消费链重审）。
   - `GameRenderer.loadEffect/currentEffect/shutdownEffect` 删除 → `getShaderManager().getPostChain(Identifier,...)`；`SirenShaderRenderHelper` 全重写；`shaders/post/siren.json` → `post_effect/siren.json`。
   - `GameRendererMixin` 的 `Camera.setup` INVOKE 目标删除 → 改挂 26.2 相机更新点（如 `mainCamera.update` 后 / `renderLevel` RETURN），`ClientEvents.onCameraSetup` 复用需验证。
6. **粒子**（`minecraft.md` §7）：`TextureSheetParticle`→`SingleQuadParticle`；`ParticleProvider.createParticle` 加 `RandomSource` 参数（`IafRenderers.registerParticleRenderers` 全部 factory）；`ParticleRenderType.CUSTOM` 删除（`GhostAppearanceParticle.getRenderType()` 重写）；`SpriteParticleRegistration`→`ParticleProvider$Sprite`。
7. **物品渲染**（`minecraft.md` §11）：`ItemRenderer` 删除 → `ItemModelResolver`/`ItemStackRenderState.submit`（`PodiumBlockEntityRenderer`、`DragonBannerFeatureRenderer`、`PixieItemFeatureRenderer`、`GhostSwordEntityRenderer`、`TideTridentItemRenderer`（含 `getFoilBufferDirect`））。
8. **闪电/LevelRenderer**（`mixin/WorldRendererMixin`、`render/misc/LightningRenderer`）：`LevelRenderer.renderLevel`→`render(GraphicsResourceAllocator,DeltaTracker,boolean,CameraRenderState,Matrix4fc,GpuBufferSlice,Vector4f,boolean)`；`entitiesForRendering()` 移入 `LevelExtractor`；`RenderBuffers.bufferSource()`/`MultiBufferSource` 删除 → 自定义几何迁到 `LevelExtractor` render-state 或 `RenderPipelines` 路径（[待确认] 具体机制）。

**验证命令**：
```
./gradlew compileJava
./gradlew runClient
```

**风险**：
- **最高风险阶段**。26.2 render-state/GPU 重构波及 100+ 文件（`render/**` 约 140 个、`screen/gui/**`、7 个客户端 Mixin、粒子）。
- `MultiBufferSource`、`ItemRenderer`、`ShaderInstance` 整体消失，不是改名而是架构替换。
- 大量 "待确认" 设计决策：dread portal 着色器机制、自定义闪电绘制路径、`EntityRenderDispatcher.render` 替代、"渲染一个实体于 HUD/方块" 的 26.2 方式。
- 建议严格分小步：先 `compileJava` 修签名，再逐渲染器行为对照。

---

## 11. P7 测试验证

**目标**：端到端验收（对照 `AGENTS.md` §9）。

**验证命令**：
```
./gradlew build
./gradlew runClient        # Fabric 26.2 客户端启动成功
```

**检查清单**：
- Mod 加载成功（`IceAndFire.init/process`、`IceAndFireClient.init/process` 无异常）。
- Uranus 正常（动画/寻路/碰撞退化点行为）。
- Jupiter 功能正常（`ConfigManager`/`ServerConfigManager`/ModMenu 配置界面）。
- EMI 显示正常（DragonForge 配方，`getRecipeMap().byType` 路径）。
- Trinkets 正常（`trinkets_updated`，Hydra Heart 可装备/触发 tick）。
- Jade 提示正常（`HealthElement` 4 参）。
- Integration 正常（`runWhenLoad` 门）。
- JEI / Ponder：若上游有 26.2 版本则验证；否则记录为降级 [待确认]。

**回归风险点**：
- 酿造药水（`FabricPotionBrewingBuilder` 实际触发）。
- 内置资源包 `iaf_legacy`（deprecated v0 模块）。
- 药水酿造 / 粒子渲染 / 自定义着色器 / 闪电渲染 / 骑龙第三人称视角 / HUD 覆盖（dread portal overlay）等核心表现。

**验收边界**：Fabric 26.2 客户端可启动、核心生物与物品功能可用即视为本迁移验收通过；neoforge 模块不在本次范围内（保持未纳入构建）。

---

## 12. 迁移顺序建议与阶段输出

| 阶段 | 交付物 / commit 主题 |
|---|---|
| P0 | 环境确认记录（版本基线） |
| P1 | `migration: upgrade build to MC 26.2 (loom-no-remap)` |
| P2 | `migration: adapt Fabric Loader/Fabric API` |
| P3 | `migration: migrate Jupiter/Uranus/EMI/Trinkets/Jade integration`（可按库拆多个 commit） |
| P4 | `migration: migrate Minecraft API (server/common)` |
| P5 | `migration: migrate Fabric implementation` |
| P6 | `migration: migrate client render pipeline to render-state`（建议按 实体渲染/方块渲染/模型动画/GUI/粒子/着色器 拆子提交） |
| P7 | `migration: verification & fixes` |

> 每个阶段保持 `./gradlew compileJava` 可编译、`./gradlew build` 可打包；避免一次性改完全部错误（`AGENTS.md` §6 分阶段编译策略）。

---

## 13. 已知待确认项汇总

- 26.2 各依赖 Modrinth version-file hash（uranus/jupiter/jade/trinkets）——需联网解析。
- `dev.emi:emi-fabric` 26.2 坐标后缀；JEI 26.2 API 与 Fabric `"jei"` entrypoint 必要性。
- Ponder 26.2 坐标与 API。
- Integration 0.2 在 26.2 的运行时兼容性（`IntegrationExecutor`）。
- CCA 依赖是否彻底删除。
- `FabricPotionBrewingBuilder` 酿造配方是否实际生效（否则需 Mixin `PotionBrewing.bootstrap`）。
- `HealthElement` 的 mojmap heart-type 枚举名。
- `EntityRenderDispatcher.render`、`BlockEntityRenderDispatcher.renderItem`、`RenderBuffers.bufferSource` 的 26.2 替代路径。
- dread portal 着色器 / 自定义闪电 / `LevelExtractor` 钩子的具体实现机制。
- Architectury 21.0.7 的 `ScreenRegistry`/`MenuRegistry`/`CreativeTabRegistry` 等抽象 API 是否变化。
