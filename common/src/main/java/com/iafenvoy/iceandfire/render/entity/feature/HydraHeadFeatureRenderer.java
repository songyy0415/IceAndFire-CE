package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.HydraEntity;
import com.iafenvoy.iceandfire.render.entity.HydraEntityRenderer;
import com.iafenvoy.iceandfire.render.entity.state.HydraRenderState;
import com.iafenvoy.iceandfire.render.model.HydraBodyModel;
import com.iafenvoy.iceandfire.render.model.HydraHeadModel;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class HydraHeadFeatureRenderer extends RenderLayer<HydraRenderState, HydraBodyModel> {
    public static final Identifier TEXTURE_STONE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hydra/stone.png");
    private static final float[][] TRANSLATE = new float[][]{
            {0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F},// 1 total heads
            {-0.15F, 0.15F, 0F, 0F, 0F, 0F, 0F, 0F, 0F},// 2 total heads
            {-0.3F, 0F, 0.3F, 0F, 0F, 0F, 0F, 0F, 0F},// 3 total heads
            {-0.4F, -0.1F, 0.1F, 0.4F, 0F, 0F, 0F, 0F, 0F},//etc...
            {-0.5F, -0.2F, 0F, 0.2F, 0.5F, 0F, 0F, 0F, 0F},
            {-0.7F, -0.4F, -0.2F, 0.2F, 0.4F, 0.7F, 0F, 0F, 0F},
            {-0.7F, -0.4F, -0.2F, 0, 0.2F, 0.4F, 0.7F, 0F, 0F},
            {-0.6F, -0.4F, -0.2F, -0.1F, 0.1F, 0.2F, 0.4F, 0.6F, 0F},
            {-0.6F, -0.4F, -0.2F, -0.1F, 0.0F, 0.1F, 0.2F, 0.4F, 0.6F},
    };
    private static final float[][] ROTATE = new float[][]{
            {0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F},// 1 total heads
            {10F, -10F, 0F, 0F, 0F, 0F, 0F, 0F, 0F},// 2 total heads
            {10F, 0F, -10F, 0F, 0F, 0F, 0F, 0F, 0F},// 3 total heads
            {25F, 10F, -10F, -25F, 0F, 0F, 0F, 0F, 0F},//etc...
            {30F, 15F, 0F, -15F, -30F, 0F, 0F, 0F, 0F},
            {40F, 25F, 5F, -5F, -25F, -40F, 0F, 0F, 0F},
            {40F, 30F, 15F, 0F, -15F, -30F, -40F, 0F, 0F},
            {45F, 30F, 20F, 5F, -5F, -20F, -30F, -45F, 0F},
            {50F, 37F, 25F, 15F, 0, -15F, -25F, -37F, -50F},
    };
    private static final HydraHeadModel[] modelArr = new HydraHeadModel[HydraEntity.HEADS];

    static {
        for (int i = 0; i < modelArr.length; i++)
            modelArr[i] = new HydraHeadModel(i);
    }

    public HydraHeadFeatureRenderer(HydraEntityRenderer renderer) {
        super(renderer);
    }

    public static Identifier getHeadTexture(HydraRenderState state) {
        return switch (state.variant) {
            case 1 -> HydraEntityRenderer.TEXUTURE_1;
            case 2 -> HydraEntityRenderer.TEXUTURE_2;
            default -> HydraEntityRenderer.TEXUTURE_0;
        };
    }

    protected static void translateToBody(HydraBodyModel model, PoseStack stack) {
        postRender(model.BodyUpper, stack);
    }

    protected static void postRender(AdvancedModelBox renderer, PoseStack matrixStackIn) {
        if (renderer.rotateAngleX == 0.0F && renderer.rotateAngleY == 0.0F && renderer.rotateAngleZ == 0.0F) {
            if (renderer.rotationPointX != 0.0F || renderer.rotationPointY != 0.0F)
                matrixStackIn.translate(renderer.rotationPointX * (float) 0.0625, renderer.rotationPointY * (float) 0.0625, renderer.rotateAngleZ * (float) 0.0625);
        } else {
            matrixStackIn.translate(renderer.rotationPointX * (float) 0.0625, renderer.rotationPointY * (float) 0.0625, renderer.rotateAngleZ * (float) 0.0625);
            if (renderer.rotateAngleZ != 0.0F)
                matrixStackIn.mulPose(Axis.ZP.rotation(renderer.rotateAngleZ));
            if (renderer.rotateAngleY != 0.0F)
                matrixStackIn.mulPose(Axis.YP.rotation(renderer.rotateAngleY));
            if (renderer.rotateAngleX != 0.0F)
                matrixStackIn.mulPose(Axis.XP.rotation(renderer.rotateAngleX));
        }
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int packedLightIn, HydraRenderState state, float yRot, float xRot) {
        if (state.isInvisible) return;
        matrixStackIn.pushPose();
        int heads = state.headCount;
        translateToBody(this.getParentModel(), matrixStackIn);
        RenderType type = RenderTypes.entityCutout(getHeadTexture(state));
        for (int head = 1; head <= heads; head++) {
            matrixStackIn.pushPose();
            float bodyWidth = 0.5F;
            matrixStackIn.translate(TRANSLATE[heads - 1][head - 1] * bodyWidth, 0, 0);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(ROTATE[heads - 1][head - 1]));
            HydraHeadModel headModel = modelArr[head - 1];
            submitNodeCollector.submitCustomGeometry(matrixStackIn, type, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                headModel.setupAnim(state);
                headModel.renderPartsToBuffer(fresh, buffer, packedLightIn, LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1);
            });
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
    }
}
