# P6-I Screen / GUI / HUD Migration — Audit (Step 1)

Phase goal: migrate Screen/GUI/HUD rendering code from MC 1.21.1 `GuiGraphics` to MC 26.2 `GuiGraphicsExtractor` + deferred render graph. Mixins are **deferred** (target signatures depend on the stable GUI pipeline; per phase plan, handle in the Mixin phase). All API facts below verified against `/d/aiminecraftdev/minecraft26.2` (26.2 Mojmap sources).

## 1. File inventory & current errors (baseline 1,729 javac / 1,517 unique)

| File | Errors | Old API | 26.2 API |
|---|---|---|---|
| `screen/gui/PodiumScreen.java` | 6 | `imageHeight=` assignment, `renderLabels`/`renderBg`/`render(GuiGraphics)` | 4-arg `super(...,w,h)`, `extractLabels`, `extractBackground` |
| `screen/gui/DragonScreen.java` | 10 | GuiGraphics, `GameRenderer.getPositionTexShader`, `renderEntityInInventory`, `drawInBatch` | `graphics.blit`, `graphics.text`, `graphics.entity` |
| `screen/gui/DragonForgeScreen.java` | 8 | GuiGraphics, `imageHeight`, `renderLabels`/`renderBg`/`render` | extractLabels/extractBackground, blit+progress |
| `screen/gui/HippocampusScreen.java` | 6 | GuiGraphics, `renderEntityInInventoryFollowsMouse` | `InventoryScreen.extractEntityInInventoryFollowsMouse` |
| `screen/gui/HippogryphScreen.java` | 9 | GuiGraphics, GameRenderer, renderEntityInInventoryFollowsMouse | same as above |
| `screen/gui/LecternScreen.java` | 18 | GuiGraphics, BookModel custom render, `getTimer`, Lighting, viewport, `drawInBatch`, mouseClicked | `graphics.book`, `getDeltaTracker`, `MouseButtonEvent`, `graphics.text` |
| `screen/gui/bestiary/BestiaryScreen.java` | ~55 | GuiGraphics, drawString/drawInBatch, renderItem, RenderSystem shader/depth calls, `Blocks.WOOL.get(DyeColor)` | `graphics.text`, `graphics.item`, drop RenderSystem, `Blocks.WOOL.pick(DyeColor)` |
| `screen/gui/bestiary/ChangePageButton.java` | 3 | `renderWidget(GuiGraphics,...)` | abstract `extractContents(GuiGraphicsExtractor,...)` |
| `screen/gui/bestiary/IndexPageButton.java` | 5 | `renderWidget`, `renderString`, RenderSystem blend/depth | `extractContents`, `extractDefaultLabel`/ActiveTextCollector |
| `screen/TitleScreenRenderManager.java` | 12 | GuiGraphics, `SplashRenderer(String)` | `GuiGraphicsExtractor`, `SplashRenderer(Component)` |
| `screen/handler/DragonForgeScreenHandler.java` | 1 | `Registry.get(ResourceKey)` returns value | returns `Optional<Reference<T>>` → `.orElseThrow().value()` |
| `screen/handler/LecternScreenHandler.java` | 1 | `Level.random` public | `Level.getRandom()` |
| mixins (InGameHud/TitleScreen/RotatingCubeMap) | — | GuiGraphics in mixin signatures | **deferred** |

Handlers (Dragon/Hippogryph/Hippocampus/Podium/Bestiary) and Slots (Banner/DragonArmor/Lectern) compile clean against 26.2 `AbstractContainerMenu`/`Slot` — no changes.

## 2. Core architecture mapping (verified from 26.2 sources)

### 2.1 GuiGraphics → GuiGraphicsExtractor + deferred render state
`net.minecraft.client.gui.GuiGraphics` is **removed**. Screens extract into `GuiGraphicsExtractor` (a builder of `GuiRenderState`). Rendering entry:

```
Screen.extractRenderStateWithTooltipAndSubtitles(GuiGraphicsExtractor, int, int, float)   // final driver
  ├─ graphics.nextStratum()
  ├─ this.extractBackground(graphics, mx, my, a)      // 旧 renderBackground + renderBg
  ├─ graphics.nextStratum()
  ├─ this.extractRenderState(graphics, mx, my, a)     // iterates renderables; container overrides it
  └─ graphics.extractDeferredElements(mx, my, a)      // tooltip / deferred
```

Screens **no longer override `render(GuiGraphics,int,int,float)`** — the driver above is called by the framework. Custom `render()` overrides (which called `renderBackground`+`super.render`+`renderTooltip`) are **deleted**; the base flow already does background → widgets → tooltip.

### 2.2 AbstractContainerScreen (26.2, `net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java`)
- `imageWidth`/`imageHeight` are **`final`**, set via 4-arg constructor `super(menu, inv, title, imageWidth, imageHeight)`. Delete `this.imageHeight = X;` assignments.
- `renderLabels(GuiGraphics,int,int)` → **`extractLabels(GuiGraphicsExtractor,int,int)`** (called by `extractContents` after `pose().translate(leftPos, topPos)`; slot-relative coords as before).
- `renderBg(GuiGraphics,float,int,int)` → **no longer a hook**. Container texture + progress drawing moves into an override of **`extractBackground(GuiGraphicsExtractor,int,int,float)`**:
  ```java
  @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float a) {
      super.extractBackground(g, mx, my, a);
      g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      ...
  }
  ```
- `containerTick()` unchanged; `renderables`/`addRenderableWidget`/`clearWidgets` unchanged; `leftPos`/`topPos` fields unchanged.

### 2.3 Buttons / AbstractButton (26.2)
- `AbstractButton.renderWidget(GuiGraphics,int,int,float)` **removed** → must implement abstract **`extractContents(GuiGraphicsExtractor,int,int,float)`** and **`onPress(InputWithModifiers)`** is already final via `onPress(Button)` — `Button.OnPress` interface unchanged (`void onPress(Button)`).
- Sprite buttons: `extractDefaultSprite(GuiGraphicsExtractor)` (protected final) blits `widget/button*` sprites; custom textures via `graphics.blit(RenderPipelines.GUI_TEXTURED, loc, x, y, u, v, w, h, texW, texH)`.
- Text label: old `renderString` gone → `extractDefaultLabel(ActiveTextCollector)` or `extractScrollingStringOverContents(output, message, margin)`; for a plain string label use `graphics.text(font, str, x, y, color, shadow)`.

### 2.4 GuiGraphicsExtractor methods (26.2, `net/minecraft/client/gui/GuiGraphicsExtractor.java`)
| 1.21.1 | 26.2 |
|---|---|
| `ms.blit(tex, x, y, u, v, w, h)` | `g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float)u, (float)v, w, h, 256, 256)` |
| `ms.blit(tex, x, y, u, v, w, h, texW, texH)` | `g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, texW, texH)` |
| `ms.blitSprite(...)` | `g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h[, alpha])` |
| `ms.drawString(font, str, x, y, col, shadow)` | `g.text(font, str, x, y, col, shadow)` |
| `font.drawInBatch(...)` | `g.text(font, str, x, y, col, shadow)` (pose-aware) |
| `ms.renderItem(stack, x, y)` | `g.item(stack, x, y)` / `g.item(stack, x, y, seed)` / `g.fakeItem(...)` |
| `ms.pose()` | `g.pose()` — `Matrix3x2fStack`: `pushMatrix/popMatrix/translate/scale` (no rotateZ for text; 2D) |
| `ms.fill(x0,y0,x1,y1,col)` | `g.fill(x0,y0,x1,y1,col)` |
| `ms.setTooltipForNextFrame(...)` n/a | `g.setTooltipForNextFrame(font, lines, x, y)` |
| `RenderSystem.setShader(GameRenderer::getPositionTexShader)` | **removed** — `RenderPipeline` param on blit |
| `RenderSystem.setShaderColor(...)` | **removed** — ARGB `color`/`alpha` param on blit/text |
| `RenderSystem.enableBlend/DepthTest, setShaderTexture, viewport` | **removed** — pipeline system handles state |
| `InventoryScreen.renderEntityInInventory(ms,x,y,size,trans,rot,cam,entity)` | build `EntityRenderState` (via `dispatcher.getRenderer(e).createRenderState(e,1.0F)`), set `lightCoords=15728880`, then `g.entity(state, scale, translation, rotation, camAngle, x0,y0,x1,y1)` (box coords, not center+size) |
| `InventoryScreen.renderEntityInInventoryFollowsMouse(ms,x0,y0,x1,y1,size,oy,mx,my,e)` | `InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0,y0,x1,y1,size,oy,mx,my,e)` — same args, static |
| `minecraft.getTimer().getGameTimeDeltaPartialTick(false)` | `minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)` |
| custom BookModel 3D render (Lighting+viewport+buffer) | `g.book(BookModel, texture, scale, open, flip, x0,y0,x1,y1)` (`GuiBookModelRenderState` PIP) |
| `mouseClicked(double,double,int)` | `mouseClicked(MouseButtonEvent event, boolean doubleClick)` — `event.x()/y()/button()` |
| `Blocks.WOOL.get(DyeColor.WHITE)` | `Blocks.WOOL.pick(DyeColor.WHITE)` (`ColorCollection<T>`) |
| `Level.random` | `Level.getRandom()` |
| `Registry.get(ResourceKey)` → value | `Optional<Reference<T>>` → `.orElseThrow().value()` |
| `SplashRenderer(String)` | `SplashRenderer(Component)` |

### 2.5 LecternScreen book (verified from `EnchantmentScreen.java:145`)
```java
float a = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
float open = Mth.lerp(a, this.oOpen, this.open);
float flip = Mth.lerp(a, this.oFlip, this.flip);
graphics.book(this.bookModel, BOOK_TEXTURE, 40.0F, open, flip, left+14, top+14, left+52, top+45);
```
Replaces the entire custom perspective/viewport/`BookModel.setupAnim`+buffer block.

## 3. Migration strategy

1. **Step 2 — template**: `PodiumScreen` (simplest — static texture + labels, no entity/widgets). Verify `extractBackground`/`extractLabels`/4-arg ctor flow compiles. Template also proves `IafScreenHandlers.registerGui` (`MenuScreens.register`) still matches.
2. **Step 3 — batch**: 
   - DragonForge (progress bar via blit), Dragon (entity+text), Hippocampus/Hippogryph (entity follows mouse), Lectern (book+buttons+click), Bestiary (item stacks+drawings+page buttons), buttons (ChangePage/IndexPage).
   - TitleScreenRenderManager: `GuiGraphicsExtractor` params + `SplashRenderer(Component)`; `drawModName` uses `graphics.text`.
   - Handlers: `.orElseThrow().value()` (DragonForge), `level().getRandom()` (Lectern).
3. **Step 4**: compile verify, `docs/p6i-screen-gui-migration.md`, single `migration: migrate screen gui hud to MC26.2` commit.

Explicitly out of scope: mixins (target signatures), JEI (already compiling), DynamicItemRenderer.
