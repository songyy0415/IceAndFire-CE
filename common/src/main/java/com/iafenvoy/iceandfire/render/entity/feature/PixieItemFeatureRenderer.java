package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.render.entity.PixieEntityRenderer;
import com.iafenvoy.iceandfire.render.entity.state.PixieRenderState;
import com.iafenvoy.iceandfire.render.model.PixieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PixieItemFeatureRenderer extends RenderLayer<PixieRenderState, PixieModel> {
    final PixieEntityRenderer renderer;

    public PixieItemFeatureRenderer(PixieEntityRenderer renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, PixieRenderState state, float yRot, float xRot) {
        if (!state.headItem.isEmpty()) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(-0.0625F, 0.53125F, 0.21875F);
            matrixStackIn.translate(-0.075F, 0, -0.05F);
            matrixStackIn.translate(0.05F, 0.55F, -0.4F);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(200.0F));
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0F));
            state.headItem.submit(matrixStackIn, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            matrixStackIn.popPose();
        }
    }
}
