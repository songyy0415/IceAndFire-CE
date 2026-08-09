package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class DragonBannerFeatureRenderer extends RenderLayer<DragonRenderState, TabulaModel<DragonRenderState>> {
    public DragonBannerFeatureRenderer(RenderLayerParent<DragonRenderState, TabulaModel<DragonRenderState>> renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, DragonRenderState state, float yRot, float xRot) {
        matrixStackIn.pushPose();
        if (!state.bannerItem.isEmpty()) {
            float f = state.renderSize / 3F;
            float f2 = 1F / f;
            matrixStackIn.pushPose();
            Optional<AdvancedModelBox> optional = StreamSupport.stream(this.getParentModel().getAllParts().spliterator(), false).filter(cube -> cube.boxName.equals("BodyUpper")).findFirst();
            optional.ifPresent(box -> this.postRender(box, matrixStackIn));
            matrixStackIn.translate(0, -0.2F, 0.4F);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
            matrixStackIn.pushPose();
            matrixStackIn.scale(f2, f2, f2);
            state.bannerItem.submit(matrixStackIn, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            matrixStackIn.popPose();
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
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
