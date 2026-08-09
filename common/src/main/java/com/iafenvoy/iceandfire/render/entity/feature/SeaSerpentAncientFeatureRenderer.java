package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.render.entity.state.SeaSerpentRenderState;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class SeaSerpentAncientFeatureRenderer extends RenderLayer<SeaSerpentRenderState, TabulaModel<SeaSerpentRenderState>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/seaserpent/ancient_overlay.png");
    private static final Identifier TEXTURE_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/seaserpent/ancient_overlay_blink.png");

    public SeaSerpentAncientFeatureRenderer(RenderLayerParent<SeaSerpentRenderState, TabulaModel<SeaSerpentRenderState>> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, SeaSerpentRenderState state, float yRot, float xRot) {
        if (state.isAncient) {
            RenderType tex = RenderTypes.entityCutout(state.blinking ? TEXTURE_BLINK : TEXTURE, false);
            submitNodeCollector.order(1)
                .submitModel(this.getParentModel(), state, matrixStackIn, tex, lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        }
    }
}
