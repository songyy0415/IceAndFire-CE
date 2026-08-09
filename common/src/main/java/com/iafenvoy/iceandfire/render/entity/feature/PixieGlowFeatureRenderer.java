package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.render.entity.PixieEntityRenderer;
import com.iafenvoy.iceandfire.render.entity.state.PixieRenderState;
import com.iafenvoy.iceandfire.render.model.PixieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PixieGlowFeatureRenderer extends RenderLayer<PixieRenderState, PixieModel> {
    public PixieGlowFeatureRenderer(PixieEntityRenderer renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, PixieRenderState state, float yRot, float xRot) {
        RenderType eyes = RenderTypes.eyes(state.texture);
        submitNodeCollector.order(1)
            .submitModel(this.getParentModel(), state, matrixStackIn, eyes, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
