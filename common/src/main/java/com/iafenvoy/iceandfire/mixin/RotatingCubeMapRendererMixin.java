package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.config.IafClientConfig;
import com.iafenvoy.iceandfire.screen.TitleScreenRenderManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 900)
public abstract class RotatingCubeMapRendererMixin {
    @Shadow
    public int width;
    @Shadow
    public int height;

    @Unique
    private int iceandfire$slowTick = 0;

    @Inject(method = "extractPanorama", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderBackground(GuiGraphicsExtractor context, float tickDelta, CallbackInfo ci) {
        if (!IafClientConfig.INSTANCE.customMainMenu.getValue()) return;
        this.iceandfire$slowTick++;
        if (this.iceandfire$slowTick >= 3) {
            this.iceandfire$slowTick = 0;
            TitleScreenRenderManager.tick();
        }
        TitleScreenRenderManager.renderBackground(context, this.width, this.height);
        ci.cancel();
    }
}
