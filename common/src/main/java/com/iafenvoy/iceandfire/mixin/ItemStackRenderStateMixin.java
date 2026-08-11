package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.render.item.ItemDisplayContextHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Exposes the current {@link ItemDisplayContext} to special item model renderers (see
 * {@link ItemDisplayContextHolder}). 26.2 does not pass the display context into
 * {@code SpecialModelRenderer.submit}, so it is captured here at the top of
 * {@code ItemStackRenderState.submit} — before any layer (including a special model layer) is
 * submitted — for both the GUI-item-atlas path and the in-hand render path.
 */
@Mixin(ItemStackRenderState.class)
public abstract class ItemStackRenderStateMixin {
    @Shadow
    private ItemDisplayContext displayContext;

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V", at = @At("HEAD"))
    private void iceandfire$captureDisplayContext(PoseStack ignored1, SubmitNodeCollector ignored2, int ignored3, int ignored4, int ignored5, CallbackInfo ci) {
        ItemDisplayContextHolder.CURRENT.set(this.displayContext);
    }
}
