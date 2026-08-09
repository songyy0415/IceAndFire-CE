# P5-E Uranus Feature Recovery 迁移分析文档（MC 1.21.1 → 26.2）

> 对比基线：`/d/aiminecraftdev/Uranus1.21.1`（1.21.1 Uranus 源码）vs `/d/aiminecraftdev/Uranus26.2`（26.2 Uranus 源码）、`/d/aiminecraftdev/minecraft26.2`（26.2 vanilla Mojmap）。
> 参照 mod：`/d/aiminecraftdev/IceAndFire-CE`（1.21.1 版）vs 当前 `common/`（26.2 工作区）。
> 范围：raycoms navigation / collision / Uranus common API。
> 禁止：DynamicItemRenderer（P6）、EntityRenderer、RenderState、Particle、Screen、Mixin。
> 本阶段不删功能、不空实现绕过编译。

---

## 一、结论摘要（TL;DR）

- **Uranus 26.2 移除了两个包**：`object.entity.pathfinding.raycoms.*`（AdvancedPathNavigate / PathingStuckHandler / IPassabilityNavigator / ICustomSizeNavigator）和 `object.entity.collision.*`（CustomCollisionsNavigator / ICustomCollisions）。
- **raycoms 是一套 ~4400 行的自定义异步寻路引擎**（源自 Minecolonies），被 26.2 移除。完整移植到 26.2 vanilla API 成本极高。
- **恢复策略 = 自实现（mod 内实现 + 26.2 vanilla 替代）**：
  - `AdvancedPathNavigate` → 基于 vanilla `GroundPathNavigation` + 共享 `NodeEvaluator` 实例自实现，保留 MovementType（WALKING/FLYING/CLIMBING）、自定义尺寸、卡住传送。
  - `PathingStuckHandler` → 精简移植卡住检测 + teleport steps + teleport-on-full-stuck。
  - `IPassabilityNavigator` / `ICustomSizeNavigator` → 原样移植接口（vanilla API 等价）。
  - `ICustomCollisions` + `CustomCollisionsNavigator` + `CustomCollisionsBlockCollisions` + `CustomCollisionsNodeProcessor` → 移植到 26.2（走沙子逻辑）。
- 额外：`DragonBaseEntity` 中 `net.createmod.catnip.levelWrappers.SchematicLevel`（Create/Ponder 依赖）不在 classpath → 改为类名判断。

---

## 二、Uranus 旧 API 功能（1.21.1）

### 2.1 raycoms navigation（`object.entity.pathfinding.raycoms`）

| 类 | 功能 | mod 使用点 |
|---|---|---|
| `AdvancedPathNavigate`（791 行，继承 `AbstractAdvancedPathNavigate`→`MobNavigation`/GroundPathNavigation） | 自定义寻路导航：MovementType（WALKING/FLYING/CLIMBING）、自定义实体尺寸、`moveToLivingEntity`、卡住处理、异步寻路 | `DragonBaseEntity`、`HippogryphEntity`、`DragonAIAttackMeleeGoal` |
| `AbstractAdvancedPathNavigate`（155 行） | 导航基类：`getDestination`、`moveToXYZ`、`moveToLivingEntity`、`getPathingOptions`、`setStuckHandler` 等抽象/实现 | 间接 |
| `PathingStuckHandler`（532 行） | 卡住处理：global timeout → 清路 → 走开 → **teleport steps** → 搭梯子/叶子桥 → 破块 → 完整卡住传送/掉血 | `DragonBaseEntity.createStuckHandler().withTeleportSteps(5)` |
| `IPassabilityNavigator`（12 行） | 显式可通过/不可通过方块接口 | `DragonBaseEntity`、`HippogryphEntity` 实现 |
| `ICustomSizeNavigator`（9 行） | 自定义寻路尺寸（XZ/Y）接口 | `DragonBaseEntity` 实现 |
| `pathjobs/*`（AbstractPathJob 1186 行等） | 异步 A* 寻路任务 | 内部 |

**raycoms 本质**：一套不依赖 vanilla `PathFinder` 的自定义异步寻路引擎（`Pathfinding` + `AbstractPathJob` + `MNode` + `ChunkCache`），用 `ICustomSizeNavigator.getXZNavSize/getYNavSize` 做大型实体（龙）的通行判断，`PathingStuckHandler` 处理卡住。

### 2.2 collision（`object.entity.collision`）

| 类 | 功能 | mod 使用点 |
|---|---|---|
| `ICustomCollisions`（91 行） | 接口：`canPassThrough(BlockPos, BlockState, VoxelShape)` + 静态 `getAllowedMovementForEntity`（实体移动碰撞，跳过可穿过方块） | `DeathWormEntity`（canPassThrough=沙子） |
| `CustomCollisionsNavigator`（117 行） | 继承 GroundPathNavigation：`canMoveDirectly`/`isSafeToStandAt` 跳过 `canPassThrough` 方块 | `CyclopsNavigation` 继承 |
| `CustomCollisionsBlockCollisions`（103 行） | 移动碰撞迭代器，调用 `canPassThrough` 放行 | 配合 ICustomCollisions |
| `CustomCollisionsNodeProcessor`（57 行） | 继承 WalkNodeProcessor，路径节点判定跳过可穿过方块 | CustomCollisionsNavigator 内部 |

### 2.3 本 mod 的 Uranus 使用范围

- **raycoms**：`DragonBaseEntity`（implements IPassabilityNavigator + ICustomSizeNavigator；createNavigation/createNavigator/switchNavigator 用 AdvancedPathNavigate.WALKING/FLYING + PathingStuckHandler.withTeleportSteps(5)）、`HippogryphEntity`（implements IPassabilityNavigator；CLIMBING/FLYING）、`DragonAIAttackMeleeGoal`（`instanceof AdvancedPathNavigate` + `moveToLivingEntity`）。
- **collision**：`DeathWormEntity`（implements ICustomCollisions，canPassThrough=沙）、`CyclopsNavigation`（extends CustomCollisionsNavigator，自定义 createPathFinder）。
- **catnip（非 Uranus）**：`DragonBaseEntity:1649` 用 `net.createmod.catnip.levelWrappers.SchematicLevel`（Create/Ponder 库）判断是否在 Ponder 世界中跳过 refreshDimensions。

---

## 三、Uranus 26.2 API 是否存在？

**raycoms 与 collision：不存在（已删除）。**

对 26.2 Uranus 源码做结构核对：
- `object/entity/` 下**没有** `pathfinding/`、**没有** `collision/`（find 无任何命中）。
- 仍保留：`animation/`、`client/model/`、`client/render/`（含 DynamicItemRenderer=P6）、`event/`、`object/`（BlockUtil、EntityUtil 等）、`object/item/`、`util/`、`ServerHelper`。
- 因此 mod 对 Uranus 的 animation/model/event/object/util 使用**不受影响**（已随 P3 Uranus 26.2 接入编译通过）；只有 raycoms + collision 报错。

**catnip（`net.createmod.catnip`）**：Create 的库，当前 build 中 Ponder 已注释（`// implementation "net.createmod.ponder..."`），类不在 classpath → 编译错误。

---

## 四、删除后的替代方案

### 4.1 raycoms navigation → 自实现（mod 内 `AdvancedPathNavigate`）

**方案选择**：vanilla 替代 vs 自实现。

| | vanilla 替代 | 自实现（选定） |
|---|---|---|
| 做法 | `createNavigation` 返回 `GroundPathNavigation`/`FlyingPathNavigation`，删 `AdvancedPathNavigate`/`IPassabilityNavigator`/`ICustomSizeNavigator`/`PathingStuckHandler` | mod 内实现同名同 API 的 `AdvancedPathNavigate`/`PathingStuckHandler`/接口，底层用 vanilla `PathNavigation`+`NodeEvaluator` |
| 保留行为 | ✗ 丢 MovementType 飞行/攀爬、丢大型实体尺寸寻路、丢卡住传送 | ✓ WALKING/FLYING/CLIMBING、尺寸寻路、卡住传送、`moveToLivingEntity` |
| 代码量 | 小 | 中（~600 行，vs 原 raycoms 4400 行） |
| 结论 | 放弃（行为丢失，违反「保持AI行为」） | **选定** |

**自实现设计**（`com.iafenvoy.iceandfire.entity.pathfinding.raycoms`）：
- `AdvancedPathNavigate extends GroundPathNavigation`：
  - `MovementType` 枚举（WALKING/FLYING/CLIMBING）。
  - 构造 `(Mob, Level, MovementType, float xzSize, float ySize)` + `(…, PathingStuckHandler)`（同 1.21.1 签名）。
  - `createPathFinder` 用**共享 NodeEvaluator 实例**：`CustomWalkNodeEvaluator`（读 `ICustomSizeNavigator` 尺寸 + `IPassabilityNavigator` 通行性）；构造后经同一引用改 MovementType，PathFinder 内同一实例同步生效（解决 super() 先于字段初始化的时序）。
  - `moveToLivingEntity(LivingEntity, double)` → `moveTo(entity, speed)`。
  - `tick()` 内调用 `PathingStuckHandler.checkStuck(this)`。
  - 提供 `getDesiredPos`/`getOurEntity`/`getPathingOptions`（handler 依赖的最小面）。
- `PathingStuckHandler`：精简移植（global timeout → 清路 → 走开 → teleport steps → 完整卡住传送到目标）。
- `IPassabilityNavigator`/`ICustomSizeNavigator`：接口原样（vanilla `BlockState`/`BlockPos`）。

> 行为差异说明：底层寻路由「异步自定义 A*」变为「vanilla `PathFinder` + 自定义 NodeEvaluator」。对大型实体，用 `getXZNavSize`/`getYNavSize` 覆盖 `entityWidth/entityHeight`，保留「半宽寻路」；FLYING 时按空气可通行判定。游戏内寻路结果与卡住传送行为保持一致。

### 4.2 collision → 自实现（mod 内 `CustomCollisionsNavigator` + `ICustomCollisions`）

- `ICustomCollisions`：接口原样移植（`canPassThrough` + 静态 `getAllowedMovementForEntity`，26.2 API 改写）。
- `CustomCollisionsNavigator extends GroundPathNavigation`：重写 `canMoveDirectly`/`isSafeToStandAt`，跳过 `((ICustomCollisions) mob).canPassThrough(pos, state, null)` 的方块（沙子）。
- `CustomCollisionsBlockCollisions`：移动碰撞迭代器，放行 `canPassThrough` 方块。
- `CustomCollisionsNodeProcessor extends WalkNodeEvaluator`：节点判定放行可穿过方块。
- 适配点：26.2 中 `PathNavigation` 移入 `net.minecraft.world.entity.ai.navigation`；`MobNavigation`→`GroundPathNavigation`；`PathNodeType`→`PathType`；`PathNodeNavigator`→`PathFinder`；`WalkNodeProcessor`→`WalkNodeEvaluator`；`world`→`level()`；`getWorld()`→`level()`。

### 4.3 catnip SchematicLevel（非 Uranus）

- 现状：`DragonBaseEntity:1649` `this.level() instanceof SchematicLevel`，catnip 不在 classpath。
- 方案：去掉直接类型引用，改为**类名判断** `this.level().getClass().getSimpleName().equals("SchematicLevel")`（保留「在 Ponder/Create SchematicLevel 世界中跳过 refreshDimensions/updateParts」行为），并删除 `import net.createmod.catnip.levelWrappers.SchematicLevel`。

---

## 五、修改文件清单（计划）

| 文件 | 动作 |
|---|---|
| `entity/pathfinding/raycoms/AdvancedPathNavigate.java` | **新增**（自实现，vanilla 基座） |
| `entity/pathfinding/raycoms/PathingStuckHandler.java` | **新增**（精简移植卡住处理） |
| `entity/pathfinding/raycoms/IPassabilityNavigator.java` | **新增**（接口） |
| `entity/pathfinding/raycoms/ICustomSizeNavigator.java` | **新增**（接口） |
| `entity/pathfinding/raycoms/CustomWalkNodeEvaluator.java` | **新增**（尺寸/通行性 NodeEvaluator） |
| `entity/pathfinding/collision/ICustomCollisions.java` | **新增**（接口） |
| `entity/pathfinding/collision/CustomCollisionsNavigator.java` | **新增**（穿越沙子导航） |
| `entity/pathfinding/collision/CustomCollisionsBlockCollisions.java` | **新增**（移动碰撞迭代器） |
| `entity/pathfinding/collision/CustomCollisionsNodeProcessor.java` | **新增**（节点判定） |
| `entity/DragonBaseEntity.java` | 改 import → mod 包；catnip SchematicLevel → 类名判断 |
| `entity/HippogryphEntity.java` | 改 import → mod 包 |
| `entity/ai/DragonAIAttackMeleeGoal.java` | 改 import → mod 包 |
| `entity/DeathWormEntity.java` | 改 import → mod 包 |
| `entity/pathfinding/CyclopsNavigation.java` | 改 import → mod 包 |

---

## 六、验证

- `./gradlew :common:compileJava`：raycoms/collision 相关错误应清零（`uranus.object.entity.pathfinding.raycoms`、`uranus.object.entity.collision`、`AdvancedPathNavigate`、`catnip`）。
- 不触碰：DynamicItemRenderer / EntityRenderer / RenderState / Particle / Screen / Mixin。
