# Mapping Validation Report

**验证对象**：`class_map.json`、`member_map.json`、`docs/yarn-to-mojmap-mapping.md`
**验证依据**：项目源码（605 文件）、MojMap 源码（`minecraft1.21.1mojmap`，5363 文件）、Yarn 源码（`minecraft 1.21.1 yarn`，5363 文件）
**验证方式**：编写独立验证脚本读取映射表 → 与两套权威源码逐一交叉核对。**未修改任何项目代码，未修改/重生成 Mapping Table。**

---

## 一、总体结论

### ❌ 当前 Mapping Table **不能**可靠支撑项目自动迁移。

| 层面 | 结论 |
|---|---|
| 类级映射 | ✅ 可靠（436/436 已映射，全部在 MojMap 源码中存在对应文件） |
| 字段映射 | ✅ 可靠（字段提取完整，110/110 等） |
| 枚举 / Record | ⚠️ 基本可靠（顺序对齐，个别需要人工确认） |
| **方法映射** | ❌ **严重缺失** —— 提取器 bug 导致约 **50% 的方法被静默丢弃** |
| Mixin | ❌ **不满足** —— 大量 `@Inject/@Shadow/@Invoke` 目标方法不在表中，且目标名已改名 |
| Descriptor | ⚠️ 类名部分可生成，但方法名部分因方法缺失无法生成 |

**核心缺陷**：`memberparse.is_valid_method()` 中 `if ret.strip() in JAVA_KEYWORDS or ret.strip() == ''` 这一行把 **void、boolean、int、float、double 等所有基本类型返回值的合法方法全部当作"表达式"拒绝**（基本类型都是 JAVA_KEYWORDS）。结果是：**凡返回基本类型或 void 的方法在生成表时就被丢弃**。

- 全部类方法捕获率：**50.2%**（14,512 / 28,881）
- 项目实际用到、但表中完全缺失的方法名：**671 个，分布在 159 个类**

**建议**：**不要**直接进入代码迁移阶段。必须先修复提取器 bug 并重新生成方法映射，或用另一种能保留 void/基本类型方法的对齐工具重跑方法层。否则迁移必然在大量方法调用处失败。

---

## 二、覆盖率

### 类级（② 完整性）

| 指标 | 数值 |
|---|---|
| 项目引用类总数 | 450 |
| `net.minecraft` 类引用 | 436 |
| 已映射 | **436（100%）** |
| 缺失 | **0** |
| `com.mojang.*`（无需改名） | 7 |
| 映射到 MojMap 但无对应 .java 文件 | 0 |

### 成员级（项目实际使用）

| 指标 | 数值 |
|---|---|
| 项目使用成员实例（去重后） | 1769 |
| 精确签名匹配 `exact` | 648 |
| 同名匹配 `name` | 551 |
| 人工核对 `verified` | 93 |
| 初始化值匹配 `value` | 97 |
| 声明顺序匹配 `order`/`type-order` | 85 / 139 |
| 表内标注 `unmatched` | 97 |
| **表内完全缺失（不在表中）** | **671** |

> ⚠️ 关键点：之前报告的 "97 个 unmatched" 只统计了**表中已有但未对齐**的成员。由于提取器丢方法，**671 个方法根本不在表中**，对覆盖率统计是"不可见"的——这是本次验证发现的**最大问题**。

### 已覆盖 / 缺失

- **已覆盖**：全部 436 个类；字段 100%；类 refs 100% 可映射。
- **缺失**：671 个项目使用的方法名（见 `validate_voidgap.json`）；其中 **56 个已确认改名（迁移阻塞项）**、29 个同名（无害）、383 个签名歧义（需人工查源码）、203 个在 MojMap 无签名匹配（需人工查源码）。

---

## 三、存在问题

### P1 —— 提取器丢弃 void/基本类型方法（根因，影响全局）

`memberparse.py:99`：
```python
if ret.strip() in JAVA_KEYWORDS or ret.strip() == '':
    return False
```
`JAVA_KEYWORDS` 含 `void boolean int float double long short byte char` 等。因此所有返回这些类型的方法（如 `tick()`, `render()`, `init()`, `getX()`, `isAlive()`, `dropLoot()`, `setPos()`...）都被过滤。

**量化**：
- Entity：源码约 336 个方法，表内仅 92；`tick/getX/setPos/isAlive` 全部缺失。
- LivingEntity：250 个方法，表内仅 58。
- 全表：28,881 方法 → 仅捕获 14,512。

### P2 —— Mixin 目标大量不在表中（高优先级）

10 个 mixin 的 `@Mixin` 类全部映射正确，但 `@Shadow/@Inject/@Invoke` 目标问题严重（表内缺失 + MojMap 已改名）：

| Mixin | 目标 | MojMap 实际名 | 表内状态 |
|---|---|---|---|
| GameRendererMixin | `renderWorld` | `renderLevel` | ❌ 缺失 |
| GameRendererMixin | `loadPrograms` | `reloadShaders` | ❌ 缺失 |
| GameRendererMixin | `clearPrograms`(invoke) | `shutdownShaders` | ❌ 缺失 |
| GameRendererMixin | `Camera.update`(invoke) | `Camera.setup` | ❌ 缺失 |
| GameRendererMixin | `camera`(shadow field) | `mainCamera` | ✅ type-order |
| InGameHudMixin | `renderMiscOverlays` | `renderCameraOverlays` | ❌ 缺失 |
| InGameHudMixin | `renderOverlay`(shadow) | `renderTextureOverlay` | ❌ 缺失 |
| InGameHudMixin | `POWDER_SNOW_OUTLINE` | `POWDER_SNOW_OUTLINE_LOCATION` | ✅ value |
| InGameHudMixin | `client`(shadow) | `minecraft` | ✅ type-order |
| InGameHudMixin | `getFrozenTicks`(invoke) | `getTicksFrozen` | ❌ 缺失 |
| LivingEntityMixin | `swingHand` | `swing` | ❌ 缺失 |
| LivingEntityMixin | `onStatusEffectRemoved` | `onEffectRemoved` | ❌ 缺失 |
| LivingEntityMixin | `updateAttributes`(invoke) | `refreshDirtyAttributes`（重构，非改名） | ❌ 缺失 |
| LivingEntityMixin | `getStackInHand`(shadow) | `getItemInHand` | ✅ exact |
| MobEntityMixin | `dropLoot` | `dropFromLootTable` | ❌ 缺失 |
| ChunkRegionMixin | `isValidForSetBlock` | `ensureCanWrite`（重构） | ❌ 缺失 |
| ChunkRegionMixin | `currentlyGeneratingStructureName`(shadow) | `currentlyGenerating` | ✅ type-order |
| TitleScreenMixin | `splashText`(shadow) | `splash` | ✅ type-order |
| TitleScreenMixin | `LogoDrawer.draw`(invoke) | `LogoRenderer.renderLogo` | ❌ 缺失 |
| WorldRendererMixin | `render` | `renderLevel` | ❌ 缺失 |
| WorldRendererMixin | `bufferBuilders`(shadow) | `renderBuffers` | ✅ type-order |
| WorldRendererMixin | `ClientWorld.getEntities`(invoke) | `entitiesForRendering` | ❌ 缺失 |
| WorldRendererMixin | `BufferBuilderStorage.getEntityVertexConsumers` | `RenderBuffers.bufferSource()` | ❌ 缺失 |
| WorldRendererMixin | `RenderTickCounter` 参数类型 | `DeltaTracker`（接口替换） | ⚠️ 类映射有，参数类型变化需人工 |
| WorldRendererMixin | `getTickDelta(false)` | `getRealtimeDeltaTicks()` | ❌ 缺失 |

### P3 —— `IafRenderLayers.java` 无法迁移（渲染关键路径）

该类 `extends RenderLayer`（→`RenderType`，构造器 8 参数匹配 ✓），但：
- `MultiPhaseParameters.builder().program().texture().transparency().cull().lightmap().overlay().build()` 链：builder 方法全部 **unmatched**。MojMap 对应 `CompositeState.builder().setShaderState().setTextureState().setTransparencyState().setCullState().setLightmapState().setOverlayState().createCompositeState()`。
- 继承的 Shader 常量：`ENTITY_CUTOUT_PROGRAM`(→`RENDERTYPE_ENTITY_CUTOUT_SHADER`)、`ENTITY_CUTOUT_NONULL_PROGRAM`(→`RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER`)、`BEACON_BEAM_PROGRAM`(→`RENDERTYPE_BEACON_BEAM_SHADER`)、`ENABLE_CULLING`(→`CULL`)、`ENABLE_LIGHTMAP`(→`LIGHTMAP`)、`ENABLE_OVERLAY_COLOR`(→`OVERLAY`) —— 全部 **unmatched**。
- 内嵌类型 `MultiPhaseParameters/Texture/ShaderProgram/Transparency` → `CompositeState/TextureStateShard/ShaderStateShard/TransparencyStateShard` 类级无映射。

### P4 —— 一致性冲突（⑤）

需要类中 **56** 个冲突，其中 **22 个与项目实际使用相交**：
- **可由参数个数消歧**（无害）：`Identifier.of`（1 参→`parse`，2 参→`fromNamespaceAndPath`）、`BlockRotation.rotate`、`BlockPos.set` 等。
- **真实歧义（需人工确认）**：`DataTracker.get`→`get`/`getValue`、`Registry.getEntry`→`getHolder`/`wrapAsHolder`、`EntityType.create`→`create`/`createNothing`/`of`、`RecipeManager.get`→`byKey`/`byKeyTyped`、`SoundEvent.of`→`create`/`createVariableRangeEvent`、`Biome.CODEC`→`CODEC`/`DIRECT_CODEC`、`ServerWorld.getPlayers`→`getPlayers`/`players` 等。

### P5 —— 97 个 used-unmatched 中约 90% 是误报

经逐条对照源码，`InventoryScreen.mouseX/mouseY`、`Entity.pos/yaw`、`DamageSource.attacker/source`、`BlockPos.dx/pos`、`Biome.name`、`Direction.name` 等均为**局部变量/参数名**；`Text.contains`/`HoverEvent.tooltip` 为**翻译键**；`ComponentType.codec`、`RenderLayer.*`(builder 方法) 为**归属错误的 Builder 方法**。真正需要补的约为 10 个。

---

## 四、需要补充的映射（项目实际引用但缺失）

### 第一优先级：已确认改名的 56 个（节选）

| Yarn 类 | Yarn 成员 | MojMap 成员 | 验证 |
|---|---|---|---|
| Entity | `initDataTracker` | `defineSynchedData` | ✅ 源码 |
| Entity | `setUuid` | `setUUID` | ✅ |
| EntityType | `isIn` | `is` | ✅ |
| LivingEntity | `playSound` | `makeSound` | ✅ 源码 |
| LivingEntity | `initDataTracker` | `defineSynchedData` | ✅ |
| MatrixStack | `multiply` | `mulPose` | ✅ |
| MatrixStack | `push/pop` | `pushPose/popPose` | ✅ |
| Block | `appendProperties` | `createBlockStateDefinition` | ✅ |
| Item | `getMaxUseTime` | `getUseDuration` | ✅ |
| ItemStack | `getMaxUseTime` | `getUseDuration` | ✅ |
| ItemStack | `isOf/isIn` | `is` | ✅ |
| NbtCompound | `putUuid` | `putUUID` | ✅ |
| MobEntity | `tryAttack` | `doHurtTarget` | ✅ |
| MobEntity | `canTarget` | `canAttackType` | ✅ |
| MobEntity | `loot` | `pickUpItem` | ✅ |
| Goal | `setControls` | `setFlags` | ✅ |
| AnimalEntity | `isBreedingItem` | `isFood` | ✅ |
| TameableEntity | `isTeammate` | `isAlliedTo` | ✅ |
| PlayerEntity | `equipStack` | `setItemSlot` | ✅ |
| StatusEffectInstance | `update` | `tick` | ✅ |
| HitResult | `squaredDistanceTo` | `distanceTo` | ✅ |
| World | `addBlockEntity` | `setBlockEntity` | ✅ |
| ScreenHandler | `calculateComparatorOutput` | `getRedstoneSignalFromBlockEntity` | ✅ |
| …（其余见 `validate_realgap.json`） | | | |

### 第二优先级：Mixin 目标（见第三节 P2 表，26 处）

### 第三优先级：`IafRenderLayers` 的 builder 方法与 Shader 常量（见 P3）

### 需人工查 MojMap 源码确认（203 个无签名匹配 + 383 个歧义）

例如：`BlockEntity.readNbt/writeNbt`（→`loadAdditional`/`saveAdditional`）、`BlockEntity.markDirty`（→`setChanged`）、`PersistentStateManager.get/getOrCreate`（→`computeIfAbsent`）、`Structure.getStructurePosition`（→`findGenerationPoint`）等。

---

## 五、迁移风险

### 🔴 高

1. **方法缺失**（P1）：671 个项目使用方法不在表中，自动改名会直接漏改，编译失败。
2. **Mixin 目标缺失**（P2）：26 处 `@Inject/@Shadow/@Invoke` 目标不在表中，且 MojMap 已改名（`renderWorld→renderLevel` 等）。按 AGENTS.md 混入规则，这些必须人工逐一确认，否则运行时注入失败。
3. **结构性重构**：`updateAttributes→refreshDirtyAttributes`、`isValidForSetBlock→ensureCanWrite`、`getTickDelta(false)→getRealtimeDeltaTicks()`、`RenderTickCounter→DeltaTracker` —— 不是改名，是方法/参数结构变化，自动迁移无法处理。

### 🟡 中

4. **一致性冲突 22 处**（P4）：需按调用参数类型逐个消歧。
5. **`IafRenderLayers.java`**（P3）：builder 链 + Shader 常量 + 内嵌类型，一整段需重写。
6. **enum/record 顺序对齐**（`bypos`）：437 处 `bypos`，若两源码声明顺序不同则错位。

### 🟢 低

7. **类级映射**：436 类全部经源码验证，可靠。
8. **字段映射**：完整捕获，可靠。
9. **Descriptor 类名部分**：450 个类 refs 全部可映射，无不可恢复。

---

## 六、最终建议

### 结论：**暂不可以进入代码迁移阶段。**

### 必须补齐的映射（阻塞项）

1. **修复提取器 bug**：`memberparse.is_valid_method()` 放行 void/基本类型返回值。
2. **用修复后的提取器重跑成员对齐**（仅重生成 `member_map.json` 的方法部分，不触碰类映射；或直接基于现有 `class_map.json` 用签名匹配重新对齐方法）。
3. **人工核对 26 处 Mixin 目标**（P2 表），逐条对照 MojMap 源码确认新名。
4. **人工核对 56 个已确认改名项 + 22 个一致性冲突 + 203/383 个无匹配/歧义项**，填入补充映射。
5. **单独处理 `IafRenderLayers.java`**（P3）：builder 链 `set*State`/`createCompositeState`、Shader 常量 `RENDERTYPE_*_SHADER`、内嵌类型类映射。

### 预处理建议

- 在迁移工具中加入"方法名不在表 → 标红报错"而非静默跳过，确保不产生"假迁移成功"。
- 对 Mixin 文件单独走一次"目标可解析"断言。
- 迁移后执行 `gradlew build` 作为编译级回归。

---

## 附：验证脚本与产物（`.migration\`，只读分析，不修改表）

- `validate_all.py` → `validate_report.json`（覆盖率/冲突/descriptor 汇总）
- `validate_consistency.py` → `validate_consistency.txt`（全部类冲突）
- `validate_mixin.py` → `validate_mixin.json`（Mixin 目标）
- `validate_usage_lines.py` → `validate_usage_lines.json`（97 个 unmatched 的真实使用行证据）
- `validate_voidgap.py` → `validate_voidgap.json`（671 个缺失方法）
- `validate_realgap.py` → `validate_realgap.json`（56 改名 + 383 歧义 + 203 无匹配）
