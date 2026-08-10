package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.item.block.entity.EggInIceBlockEntity;
import com.iafenvoy.iceandfire.render.block.state.EggInIceRenderState;
import com.iafenvoy.iceandfire.render.model.DragonEggModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class EggInIceBlockEntityRenderer implements BlockEntityRenderer<EggInIceBlockEntity, EggInIceRenderState> {
    public EggInIceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public EggInIceRenderState createRenderState() {
        return new EggInIceRenderState();
    }

    @Override
    public void extractRenderState(EggInIceBlockEntity egg, EggInIceRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(egg, state, crumblingOverlay);
        state.type = egg.type;
        state.ticksExisted = egg.ticksExisted;
    }

    @Override
    public void submit(EggInIceRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.type != null) {
            DragonEggModel model = new DragonEggModel();
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, -0.8F, 0.5F);
            matrixStackIn.pushPose();
            model.renderFrozen(state.ticksExisted);
            Identifier eggTexture = state.type.getTextureProvider().getEggTexture();
            RenderType renderType = RenderTypes.entityCutout(eggTexture);
            if (renderType != null) {
                submitNodeCollector.submitCustomGeometry(matrixStackIn, renderType, (pose, buffer) -> {
                    PoseStack fresh = new PoseStack();
                    fresh.last().pose().set(pose.pose());
                    fresh.last().normal().set(pose.normal());
                    model.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            matrixStackIn.popPose();
            matrixStackIn.popPose();
        }
    }
}
