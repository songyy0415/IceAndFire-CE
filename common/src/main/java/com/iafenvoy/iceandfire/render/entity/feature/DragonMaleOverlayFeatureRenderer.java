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
            submitNodeCollector.order(1)
                .submitModel(this.getParentModel(), state, matrixStackIn, RenderTypes.entityTranslucent(state.maleOverlayTexture), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        }
    }
}
