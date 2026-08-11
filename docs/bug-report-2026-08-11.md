# Ice and Fire: CE（MC 26.2 迁移分支）代码审查报告

## 修复记录（2026-08-11）

审查后已按 P0→P1→P2 顺序修复，`./gradlew :common:compileJava` 与 `:fabric:compileJava` 均通过。各条目状态：

| 条目 | 状态 | 说明 |
|---|---|---|
| P0-1 龙锻炉客户端崩溃 | ✅ 已修 | `DragonForgeBlockEntity.getCurrentRecipe/getRecipes` 增加 `level.isClientSide()` 守卫 |
| P1-1 工具无法修复 | ✅ 已修 | `IafToolMaterials` 修复标签改在枚举构造时烧录，删除 `init()` 时序依赖 |
| P1-2 海蛇吐息无效 | ✅ 已修 | `SeaSerpentBubblesEntity.tick()` 条件反转为"射手存活才推进" |
| P1-3 精灵非玩家破坏丢失 | ✅ 已修 | `Jar/PixieHouseBlockEntity` 覆写 `preRemoveSideEffects` 释放精灵 |
| P1-4 龙锻炉切换清空内容 | ✅ 已修 | `DragonForgeCoreBlock` 覆写 `shouldChangedStateKeepBlockEntity` |
| P2-1 发光龙丢失描边 | ✅ 已修 | `AdvancedEntityRendererBase` 增加 outline 阶段二次提交 |
| P2-2 刷怪笼参数颠倒 | ✅ 已修 | `DreadSpawnerBlockEntityRenderer` 实参 `(spin, scale)` |
| P2-3 召唤水晶强制加载失效 | ✅ 已修 | `SummoningCrystalItem` 反转条件 + 防空指针 |
| P2-4 闪电龙冰伤害类型 | ✅ 已修 | `IafDamageTypes.causeIndirectDragonLightningDamage` 改用 `DRAGON_LIGHTNING_TYPE` |
| P2-5 龙甲进阶配方ID | ✅ 已修 | 12 个 advancement 引用改为 `dragonarmor_dragon_steel_*` |
| P2-6 紫晶配方拼写 | ✅ 已修 | 6 个配方文件重命名 `amythest`→`amethyst`（与物品/lang/进度一致） |
| P2-7 切石机 count 被丢弃 | ✅ 已修 | 5 个配方 `count` 移入 `result` |
| P2-8 FarmersDelight 配方 | ⏸ 保留 | 见下方说明 |
| P2-9 鸡蛇掉落条件键 | ✅ 已修 | `entity_properties` 键 `properties`→`predicate` |
| P2-10 dread_land 维度格式 | ✅ 已修 | 清理为合法 26.2 `dimension_type`（保留必需字段 + `skybox`） |
| P2-11 矿石精准采集判定 | ✅ 已修 | 4 个矿石表改为 `predicates` 部分匹配（silk_touch ≥ 1） |
| P2-12 龙骑士朝向 | ✅ 已修 | `DragonRiderFeatureRenderer` 改用 `bodyRot`（绝对身体偏航） |
| P2-13 OPTIONAL_UUID 时序 | ✅ 已修 | 新增 `IafEntityDataSerializers.init()` 并在注册前调用 |
| P2-14 死亡之虫 NaN 俯仰 | ✅ 已修 | `DeathWormAIAttackGoal` 除零保护 |
| P2-15 第一人称骑龙隐藏失效 | ✅ 已修 | 改由 `DragonRiderFeatureRenderer` 在渲染时跳过本地玩家头像 |
| P2-16 陈旧实体引用 | ✅ 已修 | `LivingEntityRendererMixin` 改用 `WeakHashMap<state, entity>` 关联 |

**P2-8 说明（保留）**：5 个 `recipe/delight/*.json` 引用 `farmersdelight:cooking`，FD 缺失时解析报错。Fabric 26.2 无"按模组存在与否条件加载配方"的机制；正确做法是 FD 加载时由 Java 动态注册（或内置资源包门控），属功能级改动。FD 存在时这些配方可正常工作，缺失时仅为引导日志报错 + 配方不可用（预期行为），故保留并在本报告中标注。若需彻底消除报错，可考虑在 `compat/delight` 中实现动态配方注册。

### P3 修复记录（2026-08-11，编号沿用报告 P3 表 22-40）

| 条目 | 状态 | 说明 |
|---|---|---|
| P3-22 容器 removeItemNoUpdate 返回空 | ✅ | 删除 Podium/Lectern BE 的错误覆写，继承 26.2 `BaseContainerBlockEntity` 正确实现 |
| P3-23 多重伤害 | ✅ | 移除 `ServerEvents.onPlayerAttack` 的 `player.attack(parent)` 与 C2S handler 的重复攻击，保留部件 `hurtServer` 中继 + 九头蛇头部反应 |
| P3-24 DeathWorm 越权控制 | ✅ | `ServerNetworkHelper` 三坐骑统一加 `isOwnedBy` 门控（含 `setPos`，防传送 exploit） |
| P3-25 new Random() | ✅ | 5 处改实体种子 `getRandom()`（火/冰/雷龙 + DragonBase 2 处），删除无用 import |
| P3-26 nextInt(0) | ✅ | `DragonUtils` 两个方法对 `wanderFromHomeDistance` 用 `Math.max(1, …)` 钳制 |
| P3-27 砂砾不炭化/冻结 | ✅ | 三个 `transformBlock*` 中 `is(SAND) && ==GRAVEL`（恒假）改为 `== GRAVEL` |
| P3-28 九头蛇脖子段 | ✅ | `headBoxes[HEADS+1]` → `headBoxes[HEADS+i]` |
| P3-29 龙家在 (0,0,0) 丢失 | ✅ | 去除读档时 `!= 0` 三判，信任 `hasHomePosition` 标志 |
| P3-30 DreadQueen 死分支 | ✅ | 删除 `updateRider` 大块 else-if 与 `IafDragonLogic` 死从句（DreadQueen 从未注册/实例化） |
| P3-31 死中继 | ✅ | 删除 `hurtServer` 内不可达的 `isClientSide()` 客户端中继 |
| P3-32 getParent 限制 | ✅ | 去掉 `instanceof ServerLevel` 限制（`Level.getEntity(UUID)` 客户端可用） |
| P3-33 渲染器重复注册 | ✅ | 删除 `IafRenderers` 中 `DREAD_SCUTTLER` 第二次注册 |
| P3-34 死亡之虫修复标签互换 | ✅ | `DEATHWORM_WHITE/RED` 修复标签改正 |
| P3-35 PixieHouse 空栈 popResource | ✅ | 删除 count-0 无效调用 |
| P3-36 use_tide_trident 谓词 | ✅ | `is_projectile` → `tags: [minecraft:is_projectile]`（26.2 扁平格式） |
| P3-37 特殊模型空 extents | ✅ | `getExtents` 实现为 `model.root().getExtentsForGui(...)`（同 vanilla） |
| P3-38 DreadPortal 死 renderType | ✅ | 删除死方法与未用 `IafRenderLayers` 依赖 |
| P3-39 BestiaryItem EnvType | ✅ | `Minecraft.getInstance()` 判空（专用服务器潜在 NPE） |
| P3-40 ChickenMixin require | ✅ | 根因修复：`@Redirect` 目标 owner 从 `LivingEntity` 改为字节码实际的 `Chicken`（`this.dropFromGiftLootTable` 的 invoke owner 是静态类型 Chicken）——坏鸡蛋机制此前**从未生效**，`require=0` 掩盖了它；owner 修正后 `require=1` 才安全且功能真正启用。首次只改 require 曾导致启动崩溃，已修正 |

P3 表 41-43（孤儿资产、EMI 标签翻译、客户端退出挂起）不在本次范围，未处理。

### 追加修复（2026-08-11，用户实测反馈）

**龙/海蛇靠近尾巴时突然消失（视锥剔除盒过小）**
- 现象：靠近龙把视角从龙身移向龙尾，到一定程度整条龙被剔除消失，视角移回又出现。
- 根因：26.2 `EntityRenderer.shouldRender` 的剔除盒是 `entity.getBoundingBox()`。龙的 EntityType 基础 AABB 只有 0.78×1.2，而模型（头 + 长尾 + 翅膀）按 `renderSize/3` 缩放，身体段延伸约 ±0.65×renderSize；尾巴/翅膀远超出剔除盒 → 相机移到尾巴处躯干盒出视锥即整条被剔除。
- 修复：`DragonBaseEntityRenderer.getBoundingBoxForCulling` 覆写为 `inflate(1.5 × getRenderSize())`；`SeaSerpentEntityRenderer` 同样处理（身体段约 ±3.6×scale，用 `inflate(4 × getSeaSerpentScale())`）。仅影响剔除盒，不碰实体 AABB/碰撞。`shouldRender` 自带 NaN 兜底，无崩溃风险。
- 编译通过。需实机确认；若紫晶龙/死亡之虫等其它长尾生物也有此现象，可用同样模式补覆写。

---

# 审查报告正文

- 审查日期：2026-08-11
- 分支：`1.21.1-arch-old`（含 426 个未提交迁移改动）
- 审查方式：12 路子系统并发扫描 + 逐条对抗性复核（独立验证者对照 `D:\aiminecraftdev\minecraft26.2` / `fabric-api-0.156.0-26.2` 源码核实）+ 完整性批评
- 约束：审查阶段未修改任何文件；对照 2026-08-10 19:15 的 `runclient.log` 引导日志
- 结论：52 条发现经复核全部确认；其中可执行 bug 47 条（另 3 条为"已修复状态确认"）

> ⚠️ 数据文件在引导日志之后又编辑过（mtime ~21:34–21:47），日志中多数报错已被当前树修复。本报告审查的是**当前工作区**。

---

## 严重级别总览

| 级别 | 数量 | 说明 |
|---|---|---|
| P0 崩溃 | 1 | 客户端硬崩溃 |
| P1 高 | 4 | 核心功能损坏 / 数据丢失 |
| P2 中 | 21 | 功能损坏 / 明显 bug |
| P3 低 | 21 | 视觉 / 健壮性 / 次要 |

---

## P0 — 崩溃

### P0-1. 龙锻炉客户端 ClassCastException 崩溃
- 位置：`common/src/main/java/com/iafenvoy/iceandfire/item/block/entity/DragonForgeBlockEntity.java:221-232`
- 问题：`getCurrentRecipe()`/`getRecipes()` 无条件 `((ServerLevel) this.level).recipeAccess()`。`tick()` 第 88 行在**客户端也会执行** `cookTime > 0 && canSmelt()`，而 `canSmelt()` → `getCurrentResult()` → `getCurrentRecipe()`。`cookTime` 通过菜单数据槽（第 60-70 行 `PropertyDelegate`）同步到客户端，`getTicker` 又无 `isClientSide()` 分流（`DragonForgeCoreBlock.java:119-121`）。
- 触发：打开正在熔炼的龙锻炉界面 → 客户端崩溃。
- 修复方向：`getCurrentRecipe/getRecipes` 先判 `level.isClientSide()`，客户端走缓存/展示路径。

---

## P1 — 核心功能损坏

### P1-1. 所有 IAF 工具/武器无法修复
- 位置：`common/src/main/java/com/iafenvoy/iceandfire/registry/IafToolMaterials.java:39,56` + `IceAndFire.java:64 vs 86`
- 问题：`IafItems.REGISTRY.register()`（init 第 64 行）构造工具时调用 `toolMaterial()`，此时 `repairItems` 还是占位符 `iceandfire:empty`；26.2 的 `Item.Properties.sword()/pickaxe()...` 在构造时就把 `material.repairItems()` 烧进 `REPAIRABLE` 组件（`minecraft26.2/.../Item.java:499`）。`IafToolMaterials.init()` 真正赋修复标签在 `process()`（第 86 行），**晚于**注册。
- 影响：银/铜/龙骨/龙钢/冥界等全部 50+ 工具在铁砧上无法修复。
- 修复方向：把修复标签在枚举构造时直接烧录（或在注册前先执行 `IafToolMaterials.init()`）。

### P1-2. 海蛇吐息气泡在服务端完全不移动/无伤害
- 位置：`common/src/main/java/com/iafenvoy/iceandfire/entity/SeaSerpentBubblesEntity.java:48`
- 问题：`if (isClientSide() || (shootingEntity == null || !shootingEntity.isAlive()) && hasChunkAt(...))` —— 服务端只有射手**死亡/为空**时气泡才前进，即海蛇活着时吐息不判定命中、零伤害。且 `tickCount>20 且不在水/雨中` 才移除，水中气泡会原地滞留。
- 修复方向：条件反转为"射手存活才推进"。

### P1-3. 精灵罐/精灵小屋在非玩家破坏下精灵永久丢失
- 位置：`common/src/main/java/com/iafenvoy/iceandfire/item/block/JarBlock.java:70-82`（`PixieHouseBlock.java` 同样）
- 问题：迁移把 `onRemove`（1.21.1 任何移除都会触发 `releasePixie`）改为 `playerWillDestroy`/`playerDestroy`，二者**只在玩家主动破坏时触发**。爆炸/活塞/`/fill destroy`/`destroyBlock` 不触发，且 `JarBlockEntity`/`PixieHouseBlockEntity` 不是 `Container`，26.2 的 `preRemoveSideEffects` 也不会兜底 → 罐子被炸时精灵不会放出、直接消失。
- 修复方向：参照 26.2 蜂巢块，用 `BlockEntity.preRemoveSideEffects` 或爆炸掉落路径补充。

### P1-4. 龙锻炉激活/拆解切换清空炉内内容
- 位置：`common/src/main/java/com/iafenvoy/iceandfire/item/block/DragonForgeCoreBlock.java:53-60`
- 问题：`setState` 用 `setBlock` 在激活/禁用两种 Block 间切换；未覆写 `shouldChangedStateKeepBlockEntity`（默认 false）→ `LevelChunk.setBlockState` 触发 `preRemoveSideEffects` 把容器清空。组装/拆解烤炉即丢物品。
- 注：1.21.1 的 `onRemove` 同样丢，属迁移前既有行为，但仍是数据丢失点。

---

## P2 — 功能损坏 / 明显 bug

| # | 位置 | 问题 | 类别 |
|---|---|---|---|
| P2-1 | `render/entity/AdvancedEntityRendererBase.java:79,90` | 延迟提交路径 `submitCustomGeometry` 无 `outlineColor` 参数，发光龙（光谱箭/队伍色）丢失轮廓描边；观察者模式发光也白/错色 | 渲染 |
| P2-2 | `render/block/DreadSpawnerBlockEntityRenderer.java:44` | `submitEntityInSpawner(..., state.scale, state.spin, ...)` 实参顺序与 26.2 签名 `(spin, scale)` 相反 → 刷怪笼显示实体巨大化且不旋转 | 渲染 |
| P2-3 | `item/SummoningCrystalItem.java:113` | `!flag && ... && isClientSide()` 位于服务端分支内恒为 false → `setChunkForced` 强制加载块的代码**不可达**，龙在未加载区块时召唤水晶传送失效 | 功能 |
| P2-4 | `registry/IafDamageTypes.java:57` | `causeIndirectDragonLightningDamage` 用了 `DRAGON_ICE_TYPE`（直接版第 52 行正确）→ 闪电龙远程吐息伤害类型错误 | 战斗 |
| P2-5 | `advancement/recipes/combat/dragonarmor_dragonsteel_{fire,ice,lightning}_{body,head,neck,tail}.json`（12 个） | 引用的配方 ID 是 `dragonarmor_dragonsteel_*`，实际配方为 `dragonarmor_dragon_steel_*` → 进度无法完成 | 进度 |
| P2-6 | `advancement/recipes/combat/armor_amythest_*.json` 等（6 个） | 配方 ID 拼写 `amethyst` vs 实际 `amythest` 不一致 → 进度无法完成 | 进度 |
| P2-7 | `recipe/stonecutter/*.json`（5 个） | 26.2 切石机配方删除顶层 `count` 字段（静默丢弃）→ 输出数量错误 | 配方 |
| P2-8 | `recipe/delight/*.json`（5 个） | `farmersdelight:cooking` 序列化器未注册且无条件加载，FD 缺失时这 5 个配方必然解析失败 | 配方 |
| P2-9 | `loot_table/entities/cockatrice.json:86` | `entity_properties` 条件键写 `properties`，26.2 编解码器要求 `predicate`（未知键被静默忽略）→ 无论是否着火都掉落烤鸡 | 掉落 |
| P2-10 | `dimension_type/dread_land.json` | 仍是 1.21.1 字段集（`ultrawarm/natural/.../effects`），未迁移到 26.2 的 `has_skylight/.../MonsterSettings` 扁平结构 | 维度 |
| P2-11 | `loot_table/blocks/{deepslate_silver_ore,silver_ore,sapphire_ore,dragon_ice_spikes}.json:17` | 精准采集用 `components.enchantments` **精确**匹配，带效率/经验修补等附魔的镐不满足 → 附魔工具挖矿掉落错误 | 掉落 |
| P2-12 | `render/entity/feature/DragonRiderFeatureRenderer.java:39` | 骑士旋转改用 26.2 头部相对 yaw 替代乘客绝对世界 yaw → 骑乘者朝向错误 | 渲染 |
| P2-13 | `util/IafEntityDataSerializers.java:21` | `OPTIONAL_UUID` 在类静态初始化器里懒注册，NeoForge 路径下首次实体生成才触发、晚于 RegisterEvent flush | 同步 |
| P2-14 | `entity/ai/DeathWormAIAttackGoal.java:104` | `Math.acos(d0 / vector3d.length())`，静止时 `0/0=NaN` → 俯仰角 NaN 持续污染 | 战斗 |
| P2-15 | `mixin/PlayerEntityRendererMixin.java:64` | 第一人称骑龙隐藏玩家模型的注入点在 26.2 两段式管线中无效 | 渲染 |
| P2-16 | `mixin/LivingEntityRendererMixin.java:28` | `extractRenderState` 存实体、`submit RETURN` 读取 → 26.2 解耦两遍管线中读取到的是上一帧/别的实体 | 渲染 |

---

## P3 — 低危害

| # | 位置 | 问题 |
|---|---|---|
| P3-1 | `block/entity/PodiumBlockEntity.java:159` / `LecternBlockEntity.java:216` | `removeItemNoUpdate` 返回 EMPTY 不排空（潜在容器路径隐患） |
| P3-2 | `event/ServerEvents.java:201` | Multipart 父实体一次攻击可能多次受伤（event + hurtServer + C2S 三条路径叠加） |
| P3-3 | `network/ServerNetworkHelper.java:53` | C2S 龙控包 DeathWorm 分支无 `isOwnedBy` 校验即可改状态/传送 |
| P3-4 | `entity/FireDragonEntity.java:93` 等 | AI/逻辑里 `new Random()` 未用实体种子 RandomSource → 行为不可复现 |
| P3-5 | `entity/util/dragon/DragonUtils.java:41` | `nextInt(wanderFromHomeDistance)`，配置允许 0 → `IllegalArgumentException` |
| P3-6 | `entity/util/dragon/IafDragonDestructionManager.java:268` | `is(BlockTags.SAND) && getBlock()==Blocks.GRAVEL` 恒假 → 砂砾永远不会炭化/冻结 |
| P3-7 | `entity/HydraEntity.java:260` | 次级脖子部位更新用硬编码 `HEADS+1` 而非 `HEADS+i` |
| P3-8 | `entity/DragonBaseEntity.java:807` | 家在 (0,0,0) 时读档被丢弃（`!=0` 三判） |
| P3-9 | `entity/DragonBaseEntity.java:2214` | `DreadQueenEntity` 未注册 EntityType，却仍有特判分支（死代码） |
| P3-10 | `entity/MultipartPartEntity.java:233` | 客户端伤害中继在 26.2 `hurtServer/hurtClient` 拆分后不可达 |
| P3-11 | `entity/MultipartPartEntity.java:203` | `getParent()` 限定 ServerLevel，客户端部件解析不了父实体 |
| P3-12 | `registry/IafRenderers.java:74,77` | `DREAD_SCUTTLER` 渲染器重复注册 |
| P3-13 | `registry/IafArmorMaterials.java:34` | `DEATHWORM_WHITE/RED` 修复标签红白互换 |
| P3-14 | `item/block/PixieHouseBlock.java:70` | `popResource(new ItemStack(this, 0))` 空栈无操作 |
| P3-15 | `advancement/iceandfire/use_tide_trident.json:20` | 26.2 已移除 `is_projectile` 字段，条件静默失效 |
| P3-16 | `render/item/AdvancedSpecialModelRenderer.java:55` | 特殊模型 `getExtents` 空实现 → GUI 包围盒/超大物品检测失效 |
| P3-17 | `render/block/DreadPortalBlockEntityRenderer.java:95` | `renderType()` 死代码 |
| P3-18 | `item/BestiaryItem.java:54` | common 代码调 `Minecraft.getInstance().getWindow()`（EnvType 违规） |
| P3-19 | `mixin/ChickenMixin.java:25` | `@Redirect require=0` 且未列 refmap → 目标变化时静默失效 |
| P3-20 | `assets/.../models/item/amythest_gem.json` 等 | 孤儿资产：引用未注册物品/缺失贴图（`pixie_jar_5`、`amethyst_gem` 等） |
| P3-21 | `lang/en_us.json` + tags | 83 个标签缺 `tag.item/tag.block` 翻译 → EMI"Untranslated tag"刷屏（仅文本） |
| P3-22 | `runclient.log:810` | 客户端退出时 `Watchdog (Client shutdown from post-main)` 挂起被杀（需实机复现） |

---

## 已确认修复（日志报错 → 当前树已好，非 bug）

- **`iceandfire:dragon_loot` / `sea_serpent_loot` 未知注册键**：`IafLoots.REGISTRY.register()` 已加入 `IceAndFire.init()`，JSON 形状与注册的 MapCodec 匹配。
- **16 个进度加载失败 + `entity_sub_predicate_type: minecraft:type`**：336 个进度文件已全部迁移到 26.2 实体谓词格式。
- **~400 个配方解析错误**：当前全部使用 26.2 合法字符串简写/成分形式；`ambrosia` 空表等已修。
- **`minecraft:chain` 未知物品**：仅剩"无 lang 键/孤儿资产"层面的遗留（EMI 提示），需确认 26.2 中该物品的最终 ID。

---

## 覆盖缺口（建议人工复核）

审查对以下区域覆盖不足，这些是迁移高危区但**无确认发现**：
1. **美杜莎/石像/独眼体系**（`GorgonEntity`、`StoneStatueEntity` NBT 快照保存、`GorgonHeadItem`/`CyclopsEyeItem` 使用路径）。
2. **兼容层 + NeoForge 独有代码**（`compat/jei`、`compat/jade`、`neoforge/compat/curios`、`neoforge/impl` —— Fabric 环境永远不会编译/运行到它们）。
3. **GUI/Screen 体系**（`BestiaryScreen`、`DragonScreen`、`DragonForgeScreen`、`LecternScreen` 等 26.2 控件 API 迁移）。
