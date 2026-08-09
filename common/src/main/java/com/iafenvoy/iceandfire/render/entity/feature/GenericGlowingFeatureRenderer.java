package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.uranus.client.model.AdvancedEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class GenericGlowingFeatureRenderer<S extends LivingEntityRenderState, M extends AdvancedEntityModel<S>> extends RenderLayer<S, M> {
    private final Identifier texture;

    public GenericGlowingFeatureRenderer(RenderLayerParent<S, M> renderIn, Identifier texture) {
        super(renderIn);
        this.texture = texture;
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
        RenderType eyes = RenderTypes.eyes(this.texture);
        submitNodeCollector.submitCustomGeometry(matrixStackIn, eyes, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            this.getParentModel().renderPartsToBuffer(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
    }
}
