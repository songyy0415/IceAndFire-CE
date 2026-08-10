package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadHorseEntity;
import com.iafenvoy.iceandfire.render.entity.state.DreadHorseRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DreadHorseEntityRenderer extends AbstractHorseRenderer<DreadHorseEntity, DreadHorseRenderState, HorseModel> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_knight_horse.png");
    public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_knight_horse_eyes.png");

    public DreadHorseEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), new HorseModel(context.bakeLayer(ModelLayers.HORSE)));
        this.addLayer(new DreadHorseEyesLayer(this, TEXTURE_EYES));
    }

    @Override
    public DreadHorseRenderState createRenderState() {
        return new DreadHorseRenderState();
    }

    @Override
    public Identifier getTextureLocation(DreadHorseRenderState state) {
        return TEXTURE;
    }

    private static class DreadHorseEyesLayer extends RenderLayer<DreadHorseRenderState, HorseModel> {
        private final Identifier texture;

        DreadHorseEyesLayer(RenderLayerParent<DreadHorseRenderState, HorseModel> renderer, Identifier texture) {
            super(renderer);
            this.texture = texture;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, DreadHorseRenderState state, float yRot, float xRot) {
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.eyes(this.texture), (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.getParentModel().renderToBuffer(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
        }
    }
}
