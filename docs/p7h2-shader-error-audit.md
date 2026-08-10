# P7-H2 Shader Migration Audit — Minecraft 26.2 Shader Pipeline

日期: 2026-08-10
基线: `./gradlew clean compileJava` — 唯一错误 **243**（shader 子集 10 错误 / 3 文件）

## 1. Shader 错误清单

| File | Line | Old API | Error | 26.2 Replacement |
| ---- | ---- | ------- | ----- | ---------------- |
| `render/RenderVariables.java` | 3 | `ShaderInstance` import | cannot find symbol | **删除整个类** — 唯一字段 `DREAD_PORTAL_PROGRAM` 已残留 |
| `render/RenderVariables.java` | 6 | `ShaderInstance DREAD_PORTAL_PROGRAM` | cannot find symbol | **删除** — `IafRenderLayers.getDreadlandsPortal()` 已用 `RenderTypes.entityCutout(DREAD_PORTAL)`（jar 验证，无 shader） |
| `render/SirenShaderRenderHelper.java` | 29 | `GameRenderer.currentEffect()` → `PostChain` | cannot find symbol | `currentPostEffect()` 返回 `Identifier`（public） |
| `render/SirenShaderRenderHelper.java` | 30 | `processor.getName()` (PostChain) | cannot find symbol | 直接比较 `currentPostEffect()` 与 `SIREN_SHADER` |
| `render/SirenShaderRenderHelper.java` | 35 | `GameRenderer.loadEffect(Identifier)` | cannot find symbol | `setPostEffect(Identifier)`（**private** → access widener） |
| `render/SirenShaderRenderHelper.java` | 40 | `GameRenderer.shutdownEffect()` | cannot find symbol | `clearPostEffect()`（public） |
| `mixin/GameRendererMixin.java` | 21 | `@Inject renderLevel` @ `Camera.setup(...)` | cannot find symbol | `Camera.setup` 删除 → 新挂点 `GameRenderer.update(DeltaTracker)` RETURN |
| `mixin/GameRendererMixin.java` | 36 | `@Inject reloadShaders` @ `ShaderInstance.<init>` | cannot find symbol | `reloadShaders`/`ShaderInstance` 均删除 → 移除整个 registerProgram |
| `mixin/GameRendererMixin.java` | 38 | `new ShaderInstance(factory, "rendertype_dread_portal", ...)` | cannot find symbol | 移除（dead） |
| `mixin/GameRendererMixin.java` | 38 | `Pair.of(...)` 推断 | incompatible types | 移除 |

## 2. 26.2 Shader API（javap jar 验证）

### ShaderManager
```java
public class ShaderManager extends SimplePreparableReloadListener<Configs> implements AutoCloseable {
    public PostChain getPostChain(Identifier, Set<Identifier>);   // null-safe
    public String getShader(Identifier, ShaderType);
}
```
- `POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect")` — 常量池验证：post-effect JSON 从 `assets/<ns>/post_effect/<id>.json` 加载（**不是**旧 `shaders/post/`）
- `Minecraft.getShaderManager()` → `ShaderManager`（public）

### GameRenderer 后处理（1.21.1 → 26.2）
| 1.21.1 | 26.2 | 访问 |
| ------ | ---- | ---- |
| `loadEffect(Identifier)` | `setPostEffect(Identifier)` | **private** → 需 AW `accessible method ... setPostEffect (Lnet/minecraft/resources/Identifier;)V` |
| `shutdownEffect()` | `clearPostEffect()` | public |
| `currentEffect()` → PostChain | `currentPostEffect()` → `Identifier`（null=无） | public |
| — | `togglePostEffect()` | public |
| — | `checkEntityPostEffect(Entity)` | public（vanilla 仅 creeper/spider/enderman，不适用） |

`render()` 内消费：`if (postEffectId != null && effectActive) getPostChain(postEffectId, MAIN_TARGETS)` — null-safe，缺失只跳过。

### 26.2 后处理 JSON 格式（对比 vanilla `post_effect/invert.json`）
```json
{
    "targets": { "swap": {} },                     // 旧为数组 ["swap"]，新为 map
    "passes": [
        {
            "vertex_shader": "minecraft:core/screenquad",   // 旧为 "name"
            "fragment_shader": "minecraft:post/color_convolve",
            "inputs": [ { "sampler_name": "In", "target": "minecraft:main" } ],  // 旧为 "intarget"
            "output": "swap",                       // 旧为 "outtarget"
            "uniforms": { "ColorConfig": [ { "name": "RedMatrix", "type": "vec3", "value": [1,1,1] } ] }  // 旧 uniform 无 type/block 名
        }
    ]
}
```
- `Pass` codec 字段（反编译验证）：`vertex_shader` / `fragment_shader` / `inputs` / `output` / `uniforms`
- `Input` codec：`sampler_name` + `target`（target = 引用目标 id，如 `minecraft:main`）
- `InternalTarget` codec：Optional width/height/persistent/clearColor — `{}` 合法
- uniform 块名 = .fsh 内 `layout(std140) uniform X` 的 X
- 26.2 `color_convolve.fsh`：块 `ColorConfig` = RedMatrix/GreenMatrix/BlueMatrix (vec3)；**Saturation 为硬编码 const 1.8**（旧 siren.json 里的 Saturation uniform 失效，须删除）
- 26.2 `blit.fsh`：块 `BlitConfig` = ColorModulate (vec4)

### Camera（26.2 生命周期）
- `Camera.setup(BlockGetter, Entity, boolean, boolean, float)` **删除**
- 新生命周期：`GameRenderer.update(DeltaTracker)` → `mainCamera.update(DeltaTracker)`（= 旧 setup：`alignWithEntity` + `setupPerspective`）；随后 `GameRenderer.extract(DeltaTracker, boolean)` → `extractCamera` → `mainCamera.extractRenderState(CameraRenderState, float)`（快照到 render state）；`renderLevel` 只消费 `CameraRenderState`
- `Minecraft.runTick` 顺序：`update` → `extract` → `render`（javap Minecraft 验证：offset 387/441/520）
- **正确挂点**：`GameRenderer.update(DeltaTracker)` RETURN — camera 已定位，`CameraRenderState` 尚未提取，`camera.move(...)` 调整可被快照捕获
- `Camera.move(float,float,float)`（protected）、`getMaxZoom(float)`（private）仍在 — AW 已覆盖（iceandfire.accesswidener 行 14-15）
- `Options.getCameraType()` / `CameraType.THIRD_PERSON_BACK/FRONT` 仍在（jar 验证）

## 3. 决策

1. **RenderVariables** — 整类删除。唯一消费者 `GameRendererMixin.registerProgram` 同步删除。`IafRenderLayers.getDreadlandsPortal()` → `RenderTypes.entityCutout(DREAD_PORTAL)` 已承担 texture/transparency（无 shader 需求）
2. **SirenShaderRenderHelper** — 重写为 26.2 `setPostEffect`/`clearPostEffect`/`currentPostEffect`；`setPostEffect` 加 AW 条目
3. **Siren 资源** — `assets/iceandfire/shaders/post/siren.json` → `assets/iceandfire/post_effect/siren.json`，格式重写为 26.2 codec
4. **GameRendererMixin** — 删除 registerProgram（ShaderInstance 注册链）；camera hook 迁到 `update(DeltaTracker)` RETURN
5. **Dread portal shader 资源** — 删除 `assets/minecraft/shaders/core/rendertype_dread_portal.{json,fsh,vsh}`：无消费方，且 26.2 `ShaderManager.prepare` 的 `listResources("shaders", isShader)` 会因 `.fsh`/`.vsh` 后缀发现它们并尝试编译（`byLocation` 验证）
