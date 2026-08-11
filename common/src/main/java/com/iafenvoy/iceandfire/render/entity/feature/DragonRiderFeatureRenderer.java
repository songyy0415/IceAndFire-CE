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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class DragonRiderFeatureRenderer extends RenderLayer<DragonRenderState, TabulaModel<DragonRenderState>> {
    public DragonRiderFeatureRenderer(RenderLayerParent<DragonRenderState, TabulaModel<DragonRenderState>> renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, DragonRenderState state, float yRot, float xRot) {
        matrixStackIn.pushPose();
        if (!state.preyRenderStates.isEmpty()) {
            float dragonScale = state.dragonScale;
            for (int i = 0; i < state.preyRenderStates.size(); i++) {
                EntityRenderState prey = state.preyRenderStates.get(i);
                // First person: the ONLY place the local player's avatar is drawn while riding is
                // this feature render (vanilla skips the camera entity from standalone extraction).
                // Suppress it here — this replaces the old PlayerRenderer.render cancel, which is
                // ineffective in the 26.2 two-pass pipeline.
                if (prey instanceof AvatarRenderState avatarState) {
                    LocalPlayer localPlayer = Minecraft.getInstance().player;
                    if (localPlayer != null && avatarState.id == localPlayer.getId()
                            && Minecraft.getInstance().options.getCameraType().isFirstPerson())
                        continue;
                }
                // prey = true -> drawn in the jaws; prey = false -> the controlling rider, drawn on
                // the back. The migration had stubbed this to always-true (the list-size check), which
                // put the rider in the dragon's mouth; this restores the original 1.21.1 formula
                // computed in DragonBaseEntityRenderer.extractRenderState.
                boolean isPrey = i < state.preyIsPrey.size() ? state.preyIsPrey.get(i) : true;
                byte modelType = i < state.preyModelTypes.size() ? state.preyModelTypes.get(i) : 2;
                // Original used the passenger's interpolated absolute yaw. In 26.2 LivingEntityRenderState.yRot
                // is the HEAD yaw RELATIVE to the body (a small value while riding), so it must not be used —
                // bodyRot is the absolute body yaw. Fall back to the dragon's for non-living passengers whose
                // render state has no bodyRot.
                float riderRot = state.yRot;
                if (prey instanceof LivingEntityRenderState livingPrey) riderRot = livingPrey.bodyRot;
                int animationTicks = state.shakingPrey ? state.animationTick : 0;
                if (animationTicks == 0 || animationTicks >= 15) this.translateToBody(matrixStackIn);
                if (isPrey) {
                    if (animationTicks == 0 || animationTicks >= 15 || state.isFlying) {
                        this.translateToHead(matrixStackIn);
                        if (state.isLightningDragon) matrixStackIn.translate(0.1F, -0.2F, -0.1F); // offsetPerDragonType(LIGHTNING)
                        if (modelType == 0) {
                            // Offsets use the prey's own dimensions (the dragon's would scale them).
                            matrixStackIn.translate(-0.15F * prey.boundingBoxHeight, 0.1F * dragonScale - 0.1F * prey.boundingBoxHeight, -0.1F * dragonScale - 0.1F * prey.boundingBoxWidth);
                            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90.0F));
                            matrixStackIn.mulPose(Axis.YP.rotationDegrees(45.0F));
                        } else {
                            boolean horse = modelType == 1;
                            matrixStackIn.translate((horse ? -0.08F : -0.15F) * prey.boundingBoxWidth, 0.1F * dragonScale - 0.15F * prey.boundingBoxWidth, -0.1F * dragonScale - 0.1F * prey.boundingBoxWidth);
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
