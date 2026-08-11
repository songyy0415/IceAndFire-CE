package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC26.2 port of the 1.21.1 "hide the player model while riding a dragon" mixin.
 *
 * <p>Old behavior ({@code PlayerRenderer.render} HEAD, cancellable):
 * <ul>
 *   <li>LocalPlayer riding a {@link DragonBaseEntity}: cancel the render (own model hidden in
 *       first person, and the standalone world render suppressed in third person).</li>
 *   <li>RemotePlayer riding a dragon: cancel the standalone world render.</li>
 *   <li>Renders issued by {@code DragonRiderFeatureRenderer} were allowed so the rider still
 *       appears on the dragon.</li>
 * </ul>
 *
 * <p>{@code PlayerRenderer} no longer exists in 26.2; the player model is drawn by
 * {@code AvatarRenderer} (a {@code LivingEntityRenderer}). The two jobs split differently:
 * <ul>
 *   <li><b>Standalone render</b> — the level extracts the avatar through
 *       {@code EntityRenderer.shouldRender}. Cancelling that gate (inherited by
 *       {@code AvatarRenderer}, not overridden) drops the rider from the world entity list, which
 *       is the same effect as cancelling {@code PlayerRenderer.render} when the player was not in
 *       the rider set — this covers third-person local and all remote players.</li>
 *   <li><b>First person</b> — vanilla already skips the camera entity from standalone extraction,
 *       so the only place the local player's avatar is drawn in first person is the dragon feature
 *       renderer. Suppressing it is handled in {@code DragonRiderFeatureRenderer} (it checks the
 *       {@code AvatarRenderState} id against the local player).</li>
 * </ul>
 */
@Mixin(EntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z", at = @At("HEAD"), cancellable = true)
    private void iceandfire$hideStandaloneDragonRider(Entity entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Avatar avatar && avatar.getVehicle() instanceof DragonBaseEntity)
            cir.setReturnValue(false);
    }
}
