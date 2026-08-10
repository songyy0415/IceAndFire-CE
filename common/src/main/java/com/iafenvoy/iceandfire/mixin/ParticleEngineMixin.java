package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.particle.GhostAppearanceParticle;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Shadow
    private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void iceandfire$createParticleGroup(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (type == GhostAppearanceParticle.PARTICLE_TYPE)
            cir.setReturnValue(new GhostAppearanceParticle.Group((ParticleEngine) (Object) this));
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void iceandfire$extractGhost(ParticlesRenderState particlesRenderState, Frustum frustum, Camera camera, float partialTick, CallbackInfo ci) {
        ParticleGroup<?> group = this.particles.get(GhostAppearanceParticle.PARTICLE_TYPE);
        if (group != null)
            particlesRenderState.add(group.extractRenderState(frustum, camera, partialTick));
    }
}
