package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.render.entity.GorgonEntityRenderer;
import com.iafenvoy.iceandfire.render.entity.state.GorgonRenderState;
import com.iafenvoy.iceandfire.render.model.GorgonModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class GorgonEyesFeatureRenderer extends RenderLayer<GorgonRenderState, GorgonModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_eyes.png");

    public GorgonEyesFeatureRenderer(GorgonEntityRenderer renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int packedLight, GorgonRenderState state, float yRot, float xRot) {
        if (state.getAnimation() == GorgonEntity.ANIMATION_SCARE || state.getAnimation() == GorgonEntity.ANIMATION_HIT) {
            RenderType eyes = RenderTypes.eyes(TEXTURE);
            submitNodeCollector.submitCustomGeometry(matrixStackIn, eyes, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                // Re-run setupAnim at deferred-draw time (see AdvancedEntityRendererBase).
                this.getParentModel().setupAnim(state);
                this.getParentModel().renderPartsToBuffer(fresh, buffer, packedLight, OverlayTexture.NO_OVERLAY, -1);
            });
        }
    }
}
