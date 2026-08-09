package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.TideTridentEntity;
import com.iafenvoy.iceandfire.render.entity.state.TideTridentRenderState;
import com.iafenvoy.iceandfire.render.model.TideTridentModel;
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
import net.minecraft.util.Mth;

public class TideTridentEntityRenderer extends EntityRenderer<TideTridentEntity, TideTridentRenderState> {
    public static final Identifier TRIDENT = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/misc/tide_trident.png");
    private final TideTridentModel tridentModel = new TideTridentModel();

    public TideTridentEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TideTridentRenderState createRenderState() {
        return new TideTridentRenderState();
    }

    @Override
    public void extractRenderState(TideTridentEntity entityIn, TideTridentRenderState state, float partialTicks) {
        super.extractRenderState(entityIn, state, partialTicks);
        state.isFoil = entityIn.isFoil();
        state.yaw = Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
        state.pitch = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
    }

    @Override
    public void submit(TideTridentRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yaw - 90.0F));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(state.pitch + 90.0F));
        RenderType renderType = RenderTypes.entitySolid(this.getTextureLocation(state));
        if (renderType != null) {
            submitNodeCollector.submitCustomGeometry(matrixStackIn, renderType, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.tridentModel.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
        }
        if (state.isFoil) {
            submitNodeCollector.submitCustomGeometry(matrixStackIn, RenderTypes.entityGlint(), (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.tridentModel.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
        }
        matrixStackIn.popPose();
        super.submit(state, matrixStackIn, submitNodeCollector, camera);
    }

    public Identifier getTextureLocation(TideTridentRenderState state) {
        return TRIDENT;
    }
}
