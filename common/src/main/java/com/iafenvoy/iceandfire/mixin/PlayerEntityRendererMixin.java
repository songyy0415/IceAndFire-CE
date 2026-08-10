package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC26.2 port of the 1.21.1 "hide player model while riding a dragon" mixin.
 *
 * <p>Old behavior ({@code PlayerRenderer.render} HEAD, cancellable):
 * <ul>
 *   <li>LocalPlayer riding a {@link DragonBaseEntity}: cancel the render (own model hidden in
 *       first person, and the standalone world render suppressed in third person).</li>
 *   <li>RemotePlayer riding a dragon: cancel the standalone world render.</li>
 *   <li>Renders issued by {@code DragonRiderFeatureRenderer} (tracked via the removed
 *       {@code RENDERING_RIDERS} set) were allowed so the rider still appears on the dragon.</li>
 * </ul>
 *
 * <p>{@code PlayerRenderer} no longer exists in 26.2; the player model is drawn by
 * {@link AvatarRenderer}. The old two-call-site distinction now maps onto the 26.2
 * extract/submit pipeline:
 * <ul>
 *   <li><b>Standalone render</b> — the level extracts the avatar through
 *       {@code LevelExtractor.extractVisibleEntities} → {@code EntityRenderDispatcher.shouldRender}
 *       → {@code EntityRenderer.shouldRender}. Cancelling this gate (inherited by
 *       {@code AvatarRenderer}, not overridden) drops the rider from the world entity list, which
 *       is the same effect as cancelling {@code PlayerRenderer.render} when the player was not in
 *       {@code RENDERING_RIDERS}.</li>
 *   <li><b>Dragon-feature render</b> — {@code DragonBaseEntityRenderer.extractRenderState} creates
 *       a prey copy via {@code EntityRenderDispatcher.extractEntity} (which never consults
 *       {@code shouldRender}), and {@code DragonRiderFeatureRenderer.submit} submits it via
 *       {@code EntityRenderDispatcher.submit}. That path is unaffected, so the rider still appears
 *       on the dragon.</li>
 *   <li><b>First person</b> — the old mixin always cancelled the LocalPlayer's own model in first
 *       person, including the dragon-feature render. Vanilla already skips the camera entity from
 *       standalone extraction; the feature render is suppressed here by cancelling
 *       {@code AvatarRenderer.submit} for the local player's state while the camera is first
 *       person (identified by {@code AvatarRenderState.id} == the local player's entity id).</li>
 * </ul>
 */
@Mixin(EntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z", at = @At("HEAD"), cancellable = true)
    private void iceandfire$hideStandaloneDragonRider(Entity entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Avatar avatar && avatar.getVehicle() instanceof DragonBaseEntity)
            cir.setReturnValue(false);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void iceandfire$hideLocalPlayerModelInFirstPerson(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatarState) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && avatarState.id == player.getId() && Minecraft.getInstance().options.getCameraType().isFirstPerson())
                ci.cancel();
        }
    }
}
