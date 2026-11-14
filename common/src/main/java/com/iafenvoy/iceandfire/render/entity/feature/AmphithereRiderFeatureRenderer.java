package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.render.entity.state.AmphithereRenderState;
import com.iafenvoy.iceandfire.render.model.AmphithereModel;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Flight rework: draws the controlling player as part of the amphithere model instead of letting the player renderer
 * place it. {@code PlayerEntityRendererMixin} keeps the standalone player render disabled while the entity is drawn
 * here.
 * <p>
 * MC 26.2 splits entity drawing into an extraction and a submission pass, so the rider's {@link EntityRenderState} is
 * captured by {@code AmphithereEntityRenderer.extractRenderState} and submitted here (the same structure the dragon
 * rider layer uses).
 */
public class AmphithereRiderFeatureRenderer extends RenderLayer<AmphithereRenderState, AmphithereModel> {
    private static final float AMPHITHERE_SCALE = 2.0F;

    public AmphithereRiderFeatureRenderer(RenderLayerParent<AmphithereRenderState, AmphithereModel> renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, AmphithereRenderState state, float yRot, float xRot) {
        if (!IafCommonConfig.INSTANCE.amphithere.flightRework.getValue()) return;
        EntityRenderState controller = state.riderRenderState;
        if (controller == null) return;
        // First person: the local player's own avatar must not be drawn (vanilla skips the camera entity from the
        // standalone extraction, so this layer would be the only place it shows up).
        if (controller instanceof AvatarRenderState avatarState) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null && avatarState.id == localPlayer.getId()
                    && Minecraft.getInstance().options.getCameraType().isFirstPerson())
                return;
        }

        matrixStackIn.pushPose();
        this.translateToBody(matrixStackIn);
        matrixStackIn.translate(0, -0.01F * AMPHITHERE_SCALE, -0.035F * AMPHITHERE_SCALE);

        float riderRot = state.yRot;
        if (controller instanceof LivingEntityRenderState livingController) riderRot = livingController.bodyRot;

        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(180.0F));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(riderRot + 180));
        matrixStackIn.scale(1 / AMPHITHERE_SCALE, 1 / AMPHITHERE_SCALE, 1 / AMPHITHERE_SCALE);
        matrixStackIn.translate(0, -0.25F, 0);
        CameraRenderState camera = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        try {
            Minecraft.getInstance().getEntityRenderDispatcher().submit(controller, camera, 0.0, 0.0, 0.0, matrixStackIn, submitNodeCollector);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            crashreportcategory.setDetail("RenderState", controller.toString());
            throw new ReportedException(crashreport);
        }
        matrixStackIn.popPose();

        matrixStackIn.popPose();
    }

    protected void translateToBody(PoseStack stack) {
        this.postRender(this.getParentModel().BodyUpper, stack);
        this.postRender(this.getParentModel().Neck1, stack);
    }

    protected void postRender(AdvancedModelBox renderer, PoseStack matrixStackIn) {
        if (renderer.rotateAngleX == 0.0F && renderer.rotateAngleY == 0.0F && renderer.rotateAngleZ == 0.0F) {
            if (renderer.rotationPointX != 0.0F || renderer.rotationPointY != 0.0F || renderer.rotationPointZ != 0.0F)
                matrixStackIn.translate(renderer.rotationPointX * (float) 0.0625, renderer.rotationPointY * (float) 0.0625, renderer.rotationPointZ * (float) 0.0625);
        } else {
            matrixStackIn.translate(renderer.rotationPointX * (float) 0.0625, renderer.rotationPointY * (float) 0.0625, renderer.rotationPointZ * (float) 0.0625);
            if (renderer.rotateAngleZ != 0.0F)
                matrixStackIn.mulPose(Axis.ZP.rotation(renderer.rotateAngleZ));
            if (renderer.rotateAngleY != 0.0F)
                matrixStackIn.mulPose(Axis.YP.rotation(renderer.rotateAngleY));
            if (renderer.rotateAngleX != 0.0F)
                matrixStackIn.mulPose(Axis.XP.rotation(renderer.rotateAngleX));
        }
    }
}
