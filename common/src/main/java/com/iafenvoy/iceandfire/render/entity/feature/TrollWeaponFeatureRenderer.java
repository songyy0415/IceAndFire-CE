package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.render.entity.TrollEntityRenderer;
import com.iafenvoy.iceandfire.render.entity.state.TrollRenderState;
import com.iafenvoy.iceandfire.render.model.TrollModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class TrollWeaponFeatureRenderer extends RenderLayer<TrollRenderState, TrollModel> {
    public TrollWeaponFeatureRenderer(TrollEntityRenderer renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, TrollRenderState state, float yRot, float xRot) {
        if (state.weaponType != null && !state.isStone) {
            RenderType tex = RenderTypes.entityCutout(state.weaponType.getTextureLocation());
            submitNodeCollector.submitCustomGeometry(matrixStackIn, tex, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.getParentModel().renderPartsToBuffer(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
        }
    }
}
