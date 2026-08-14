package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.entity.DragonEggEntity;
import com.iafenvoy.iceandfire.render.entity.state.DragonEggRenderState;
import com.iafenvoy.iceandfire.render.model.DragonEggModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DragonEggEntityRenderer extends LivingEntityRenderer<DragonEggEntity, DragonEggRenderState, DragonEggModel> {
    public DragonEggEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonEggModel(), 0.3F);
    }

    @Override
    public DragonEggRenderState createRenderState() {
        return new DragonEggRenderState();
    }

    @Override
    public void extractRenderState(DragonEggEntity entity, DragonEggRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.texture = entity.getEggType().getTextureProvider().getEggTexture();
        state.locationValid = entity.isLocationValid();
    }

    @Override
    public Identifier getTextureLocation(DragonEggRenderState state) {
        return state.texture;
    }

    // 26.2 vanilla LivingEntityRenderer.submit draws the ModelPart root, which is
    // empty for an AdvancedEntityModel (DragonEggModel) — the egg rendered
    // transparently. Submit the AdvancedModelBox hierarchy via renderPartsToBuffer.
    @Override
    public void submit(DragonEggRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        float scale = state.scale;
        poseStack.scale(scale, scale, scale);
        this.setupRotations(state, poseStack, state.bodyRot, scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(state, poseStack);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        RenderType renderType = RenderTypes.entityCutout(this.getTextureLocation(state));
        this.model.setupAnim(state);
        submitNodeCollector.order(0).submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            // Re-run setupAnim at deferred-draw time so each egg draws its own pose (the
            // per-renderer model is shared across all eggs; without this, every egg animates
            // in sync with the last-submitted one). Matches AdvancedEntityRendererBase.
            this.model.setupAnim(state);
            this.model.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
        poseStack.popPose();
        // Inline EntityRenderer.submit tail (leash + name display) instead of delegating to
        // super: as a LivingEntityRenderer, super.submit would re-run the full pose setup and the
        // layer loop, and would become a real double-render the moment DragonEggModel gains a
        // populated ModelPart root.
        if (state.leashStates != null) {
            for (EntityRenderState.LeashState leashState : state.leashStates) {
                submitNodeCollector.submitLeash(poseStack, leashState);
            }
        }
        this.submitNameDisplay(state, poseStack, submitNodeCollector, camera);
    }
}
