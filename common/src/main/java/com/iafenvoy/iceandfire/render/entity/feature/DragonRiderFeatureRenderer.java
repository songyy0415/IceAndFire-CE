package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class DragonRiderFeatureRenderer extends RenderLayer<DragonRenderState, TabulaModel<DragonRenderState>> {
    private final boolean excludeDreadQueenMob;

    public DragonRiderFeatureRenderer(RenderLayerParent<DragonRenderState, TabulaModel<DragonRenderState>> renderIn, boolean excludeDreadQueenMob) {
        super(renderIn);
        this.excludeDreadQueenMob = excludeDreadQueenMob;
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, DragonRenderState state, float yRot, float xRot) {
        matrixStackIn.pushPose();
        if (!state.preyRenderStates.isEmpty()) {
            float dragonScale = state.dragonScale;
            for (int i = 0; i < state.preyRenderStates.size(); i++) {
                EntityRenderState prey = state.preyRenderStates.get(i);
                boolean isPrey = i < state.preyModelTypes.size();
                byte modelType = i < state.preyModelTypes.size() ? state.preyModelTypes.get(i) : 2;
                float riderRot = state.yRot;
                int animationTicks = state.shakingPrey ? state.animationTick : 0;
                if (animationTicks == 0 || animationTicks >= 15) this.translateToBody(matrixStackIn);
                if (isPrey) {
                    if (animationTicks == 0 || animationTicks >= 15 || state.isFlying) {
                        this.translateToHead(matrixStackIn);
                        if (modelType == 0) {
                            matrixStackIn.translate(-0.15F * state.boundingBoxHeight, 0.1F * dragonScale - 0.1F * state.boundingBoxHeight, -0.1F * dragonScale - 0.1F * state.boundingBoxWidth);
                            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90.0F));
                            matrixStackIn.mulPose(Axis.YP.rotationDegrees(45.0F));
                        } else {
                            boolean horse = modelType == 1;
                            matrixStackIn.translate((horse ? -0.08F : -0.15F) * state.boundingBoxWidth, 0.1F * dragonScale - 0.15F * state.boundingBoxWidth, -0.1F * dragonScale - 0.1F * state.boundingBoxWidth);
                            matrixStackIn.mulPose(Axis.XN.rotationDegrees(90.0F));
                        }
                    } else matrixStackIn.translate(0, 0.555F * dragonScale, -0.5F * dragonScale);
                } else matrixStackIn.translate(0, -0.01F * dragonScale, -0.035F * dragonScale);
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(180.0F));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(riderRot + 180));
                matrixStackIn.scale(1 / dragonScale, 1 / dragonScale, 1 / dragonScale);
                matrixStackIn.translate(0, -0.25F, 0);
                CameraRenderState camera = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
                try {
                    Minecraft.getInstance().getEntityRenderDispatcher().submit(prey, camera, 0.0, 0.0, 0.0, matrixStackIn, submitNodeCollector);
                } catch (Throwable throwable3) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable3, "Rendering entity in world");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
                    crashreportcategory.setDetail("RenderState", prey.toString());
                    throw new ReportedException(crashreport);
                }
                matrixStackIn.popPose();
            }
        }
        matrixStackIn.popPose();
    }

    protected void translateToBody(PoseStack stack) {
        this.postRender(this.getParentModel().getCube("BodyUpper"), stack);
        this.postRender(this.getParentModel().getCube("Neck1"), stack);
    }

    protected void translateToHead(PoseStack stack) {
        this.postRender(this.getParentModel().getCube("Neck2"), stack);
        this.postRender(this.getParentModel().getCube("Neck3"), stack);
        this.postRender(this.getParentModel().getCube("Head"), stack);
    }

    protected void postRender(AdvancedModelBox renderer, PoseStack matrixStackIn) {
        if (renderer.rotateAngleX == 0.0F && renderer.rotateAngleY == 0.0F && renderer.rotateAngleZ == 0.0F) {
            if (renderer.rotationPointX != 0.0F || renderer.rotationPointY != 0.0F || renderer.offsetZ != 0.0F)
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
