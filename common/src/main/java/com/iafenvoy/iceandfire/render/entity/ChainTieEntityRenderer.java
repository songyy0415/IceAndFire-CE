package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.ChainTieEntity;
import com.iafenvoy.iceandfire.render.entity.state.ChainTieRenderState;
import com.iafenvoy.iceandfire.render.model.ChainTieModel;
import com.iafenvoy.uranus.client.model.basic.BasicModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class ChainTieEntityRenderer extends EntityRenderer<ChainTieEntity, ChainTieRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/misc/chain_tie.png");
    private final ChainTieModel leashKnotModel = new ChainTieModel();

    public ChainTieEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ChainTieRenderState createRenderState() {
        return new ChainTieRenderState();
    }

    @Override
    public void extractRenderState(ChainTieEntity entityIn, ChainTieRenderState state, float partialTicks) {
        super.extractRenderState(entityIn, state, partialTicks);
        state.yRot = entityIn.getYRot();
        state.xRot = entityIn.getXRot();
    }

    @Override
    public void submit(ChainTieRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 0.5F, 0);
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        this.leashKnotModel.setupAnim(state);
        RenderType renderType = RenderTypes.entityCutout(TEXTURE);
        if (renderType != null) {
            submitNodeCollector.submitCustomGeometry(matrixStackIn, renderType, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                for (BasicModelPart part : this.leashKnotModel.parts()) {
                    part.render(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
                }
            });
        }
        matrixStackIn.popPose();
        super.submit(state, matrixStackIn, submitNodeCollector, camera);
    }

    public Identifier getTextureLocation(ChainTieRenderState state) {
        return TEXTURE;
    }
}
