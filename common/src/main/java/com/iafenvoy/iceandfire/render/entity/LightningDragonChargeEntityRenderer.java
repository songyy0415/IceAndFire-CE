package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.LightningDragonChargeEntity;
import com.iafenvoy.iceandfire.render.entity.state.LightningDragonChargeRenderState;
import com.iafenvoy.iceandfire.render.model.DreadLichSkullModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class LightningDragonChargeEntityRenderer extends EntityRenderer<LightningDragonChargeEntity, LightningDragonChargeRenderState> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/lightningdragon/charge.png");
    public static final Identifier TEXTURE_CORE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/lightningdragon/charge_core.png");
    private static final DreadLichSkullModel MODEL_SPIRIT = new DreadLichSkullModel();

    public LightningDragonChargeEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LightningDragonChargeRenderState createRenderState() {
        return new LightningDragonChargeRenderState();
    }

    @Override
    public void extractRenderState(LightningDragonChargeEntity entity, LightningDragonChargeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lightCoords = this.getPackedLightCoords(entity, partialTicks);
        state.f = (float) entity.tickCount + partialTicks;
        state.yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
    }

    @Override
    public void submit(LightningDragonChargeRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        RenderType swirl = RenderTypes.energySwirl(TEXTURE, state.f * 0.01F, state.f * 0.01F);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0F, 0.5F, 0F);
        matrixStackIn.translate(0F, -0.25F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yaw - 180.0F));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(state.f * 20.0F));
        matrixStackIn.translate(0F, 0.25F, 0F);
        this.renderModel(matrixStackIn, submitNodeCollector, RenderTypes.eyes(TEXTURE_CORE), state.lightCoords);
        matrixStackIn.popPose();

        matrixStackIn.pushPose();
        matrixStackIn.translate(0F, 0.5F, 0F);
        matrixStackIn.translate(0F, -0.25F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yaw - 180.0F));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(state.f * 15.0F));
        matrixStackIn.translate(0F, 0.25F, 0F);
        matrixStackIn.scale(1.5F, 1.5F, 1.5F);
        this.renderModel(matrixStackIn, submitNodeCollector, swirl, state.lightCoords);
        matrixStackIn.popPose();

        matrixStackIn.pushPose();
        matrixStackIn.translate(0F, 0.75F, 0F);
        matrixStackIn.translate(0F, -0.25F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yaw - 180.0F));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(state.f * 10.0F));
        matrixStackIn.translate(0F, 0.75F, 0F);
        matrixStackIn.scale(2.5F, 2.5F, 2.5F);
        this.renderModel(matrixStackIn, submitNodeCollector, swirl, state.lightCoords);
        matrixStackIn.popPose();

        super.submit(state, matrixStackIn, submitNodeCollector, camera);
    }

    private void renderModel(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, int lightCoords) {
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            MODEL_SPIRIT.renderPartsToBuffer(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
    }
}
