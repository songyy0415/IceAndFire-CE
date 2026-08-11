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
        this.getParentModel().setupAnim(state);
        submitNodeCollector.order(1).submitCustomGeometry(matrixStackIn, eyes, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            // Re-run setupAnim at deferred-draw time (see AdvancedEntityRendererBase).
            this.getParentModel().setupAnim(state);
            this.getParentModel().renderPartsToBuffer(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
    }
}
