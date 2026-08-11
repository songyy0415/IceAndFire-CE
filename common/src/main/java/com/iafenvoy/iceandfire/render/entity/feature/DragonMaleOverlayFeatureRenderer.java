package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class DragonMaleOverlayFeatureRenderer extends RenderLayer<DragonRenderState, TabulaModel<DragonRenderState>> {
    public DragonMaleOverlayFeatureRenderer(RenderLayerParent<DragonRenderState, TabulaModel<DragonRenderState>> renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int light, DragonRenderState state, float yRot, float xRot) {
        if (state.isMale && !state.isSkeletal && state.maleOverlayTexture != null) {
            this.getParentModel().setupAnim(state);
            submitNodeCollector.order(1).submitCustomGeometry(matrixStackIn, RenderTypes.entityTranslucent(state.maleOverlayTexture), (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                // Re-run setupAnim at deferred-draw time (see AdvancedEntityRendererBase).
                this.getParentModel().setupAnim(state);
                this.getParentModel().renderPartsToBuffer(fresh, buffer, light, OverlayTexture.NO_OVERLAY, -1);
            });
        }
    }
}
