package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.event.ClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends net.minecraft.client.model.EntityModel<? super S>> {
    // 26.2 decouples extraction and submission into two passes over ALL visible entities, so a
    // single lastEntity field holds the WRONG entity at submit time. Key the entity by its render
    // state instead (extraction produces a fresh state per entity per frame); weak keys prevent
    // the map from retaining states/entities across frames when a submit never runs.
    @Unique
    private final WeakHashMap<S, T> iceandfire$stateToEntity = new WeakHashMap<>();

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void iceandfire$storeEntity(T entity, S state, float partialTick, CallbackInfo ci) {
        this.iceandfire$stateToEntity.put(state, entity);
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void onRender(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        T entity = this.iceandfire$stateToEntity.remove(state);
        if (entity != null)
            ClientEvents.onPostRenderLiving(entity, 0.0F, poseStack, collector, LightCoordsUtil.FULL_BRIGHT);
    }
}
