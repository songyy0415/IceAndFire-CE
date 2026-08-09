# P6-I Screen / GUI / HUD Migration Report

Migrates all mod Screen/GUI/HUD rendering from MC 1.21.1 `GuiGraphics` to MC 26.2 `GuiGraphicsExtractor` + deferred `GuiRenderState`. All API facts verified against `/d/aiminecraftdev/minecraft26.2` (26.2 Mojmap sources). Mixins are **deferred** to the Mixin phase (target signatures depend on the stable GUI pipeline).

## 1. API mapping (1.21.1 → 26.2)

| 1.21.1 | 26.2 |
|---|---|
| `extends AbstractContainerScreen<H>` | unchanged |
| `super(menu,inv,title)` + `this.imageHeight=X` | `super(menu,inv,title,imageWidth,imageHeight)` (imageWidth/Height now `final`) |
| `render(GuiGraphics,int,int,float)` override | **removed** — framework drives `extractRenderStateWithTooltipAndSubtitles` → `extractBackground` + `extractRenderState` + `extractDeferredElements` |
| `renderBackground(GuiGraphics,...)` | `extractBackground(GuiGraphicsExtractor,int,int,float)` |
| `renderBg(GuiGraphics,float,int,int)` | moved into `extractBackground` override (`super.extractBackground` then `graphics.blit(RenderPipelines.GUI_TEXTURED,...)`) |
| `renderLabels(GuiGraphics,int,int)` | `extractLabels(GuiGraphicsExtractor,int,int)` (called inside `extractContents` after `pose().translate(leftPos,topPos)`) |
| `renderTooltip(GuiGraphics,int,int)` | automatic (`graphics.setTooltipForNextFrame` in `extractTooltip`) |
| `GuiGraphics` | `GuiGraphicsExtractor` (builder of `GuiRenderState`) |
| `ms.blit(tex,x,y,u,v,w,h[,texW,texH])` | `g.blit(RenderPipelines.GUI_TEXTURED,tex,x,y,(float)u,(float)v,w,h,srcW,srcH,texW,texH)` |
| `ms.drawString(font,str,x,y,col,shadow)` / `font.drawInBatch(...)` | `g.text(font,str,x,y,col,shadow)` — pose-aware; **color must carry alpha** (`ARGB.opaque(c)`, since `text` skips alpha==0) |
| `ms.renderItem(stack,x,y)` | `g.item(stack,x,y)` / `g.item(stack,x,y,seed)` |
| `ms.pose().pushPose()/popPose()` | `g.pose().pushMatrix()/popMatrix()` |
| `ms.pose().scale(a,b,c)` / `translate(x,y,z)` | `g.pose().scale(a,b)` / `g.pose().translate(x,y)` (`Matrix3x2fStack`, 2D) |
| `InventoryScreen.renderEntityInInventory(ms,x,y,size,trans,rot,cam,entity)` | build `EntityRenderState` (`dispatcher.getRenderer(e).createRenderState(e,1.0F)` — final, auto-extracts) then `g.entity(state,size,trans,rot,cam,x0,y0,x1,y1)` (box coords centered on old center) |
| `InventoryScreen.renderEntityInInventoryFollowsMouse(ms,...)` | `InventoryScreen.extractEntityInInventoryFollowsMouse(g,...)` (same args) |
| custom 3D BookModel render (Lighting+viewport+buffer) | `g.book(BookModel,texture,scale,open,flip,x0,y0,x1,y1)` (`GuiBookModelRenderState` PIP) |
| `RenderSystem.setShader(GameRenderer::getPositionTexShader)` | **removed** — `RenderPipeline` param on blit |
| `RenderSystem.setShaderColor(...)` | **removed** — ARGB `color`/`alpha` param on blit/text |
| `RenderSystem.enableBlend/enableDepthTest/setShaderTexture/viewport` | **removed** — pipeline handles state |
| `minecraft.getTimer().getGameTimeDeltaPartialTick(false)` | `minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)` |
| `mouseClicked(double,double,int)` | `mouseClicked(MouseButtonEvent,boolean)` — `event.x()/y()/button()` |
| `AbstractButton.renderWidget(GuiGraphics,...)` | abstract `extractContents(GuiGraphicsExtractor,int,int,float)`; `Button` now abstract, `Button.OnPress` unchanged |
| `AbstractWidget.renderString(...)` | button label via `g.text` or `extractDefaultLabel(ActiveTextCollector)` |
| `Blocks.WOOL.get(DyeColor.WHITE)` | `Blocks.WOOL.pick(DyeColor.WHITE)` (`ColorCollection<T>`) |
| `Registry.get(Identifier/ResourceKey)` → T | `Optional<Holder.Reference<T>>` → `.map(Holder.Reference::value).orElse(...)` / `.orElseThrow().value()` |
| `Level.random` | `Level.getRandom()` |
| `SplashRenderer(String)` | `SplashRenderer(Component)` |
| `MenuScreens.register(type, ctor)` | unchanged |
| `gameMode.handleInventoryButtonClick` / `menu.clickMenuButton` | unchanged |

## 2. Files changed (20)

**Screens** (10): `PodiumScreen`, `DragonForgeScreen`, `DragonScreen`, `HippocampusScreen`, `HippogryphScreen`, `LecternScreen`, `bestiary/BestiaryScreen`, `bestiary/ChangePageButton`, `bestiary/IndexPageButton`, `TitleScreenRenderManager`.

**Handlers/slots** (10): `BestiaryScreenHandler`, `DragonForgeScreenHandler`, `DragonScreenHandler`, `HippocampusScreenHandler`, `HippogryphScreenHandler`, `LecternScreenHandler`, `PodiumScreenHandler`, `slot/BannerSlot`, `slot/DragonArmorSlot`, `slot/LecternSlot` (pre-existing Mojmap migration finishing; `DragonForgeScreenHandler` now `.orElseThrow().value()` on the registry, `LecternScreenHandler` `Level.getRandom()`).

## 3. Behaviour / render-flow changes

- Custom `render()` overrides deleted; the 26.2 driver does background → widgets → tooltip. Background texture + progress moved into `extractBackground`; text moved to `extractLabels` / kept in `extractBackground` where it was behind slots.
- `imageHeight` custom sizes (DragonScreen 214, PodiumScreen 133) now passed via 4-arg super; default 176×166 elsewhere.
- Entity-in-GUI (DragonScreen dragon with mouse-rotation; Hippocampus/Hippogryph follows-mouse) now renders via offscreen PIP `g.entity(...)` with an extracted `EntityRenderState`.
- LecternScreen book: entire custom viewport/3D/`Lighting` block replaced by `g.book(...)` PIP; the 3 enchant-like page buttons still blit + text; `getRandom()`→field, `getTimer()`→`getDeltaTracker()`.
- BestiaryScreen: `renderItem`→`item`, `drawString`/`drawInBatch`→pose-aware `text` (colors wrapped in `ARGB.opaque`), `Blocks.WOOL.get`→`pick`, item-lookup uses `Holder.Reference::value`, item z-offset in `drawBlockStack` dropped (2D pose has no z; sequential draws layer in order).
- `RenderSystem` shader/blend/depth/viewport calls removed (pipeline-managed); `RenderPipelines.GUI_TEXTURED` drives all GUI blits.

## 4. Error statistics

| metric | before | after | delta |
|---|---|---|---|
| javac errors | 1,729 | 1,593 | **-136** |
| unique error messages (normalized) | 1,558 | 1,423 | **-135** |

- **screen/ directory errors: 18 → 0**
- **12 GUI files fully fixed** (removed from error list)
- **0 new error files** introduced
- No deleted GUI, no empty implementations, no `@SuppressWarnings`, no old `GuiGraphics` compat layer.
