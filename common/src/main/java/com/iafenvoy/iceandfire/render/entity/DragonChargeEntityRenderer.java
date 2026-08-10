package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.registry.IafBlocks;
import com.iafenvoy.iceandfire.render.entity.state.DragonChargeRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.level.block.Blocks;

public class DragonChargeEntityRenderer extends EntityRenderer<Fireball, DragonChargeRenderState> {
    public final boolean isFire;
    private final BlockModelResolver blockModelResolver;

    public DragonChargeEntityRenderer(EntityRendererProvider.Context context, boolean isFire) {
        super(context);
        this.isFire = isFire;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public DragonChargeRenderState createRenderState() {
        return new DragonChargeRenderState();
    }

    @Override
    public void extractRenderState(Fireball entity, DragonChargeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.blockModelResolver.update(state.blockModel, this.isFire ? Blocks.MAGMA_BLOCK.defaultBlockState() : IafBlocks.DRAGON_ICE.get().defaultBlockState(), BlockDisplayContext.create());
        state.lightCoords = this.getPackedLightCoords(entity, partialTicks);
    }

    @Override
    public void submit(DragonChargeRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0D, 0.5D, 0.0D);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(-90.0F));
        matrixStackIn.translate(-0.5D, -0.5D, 0.5D);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0F));
        state.blockModel.submit(matrixStackIn, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();
    }
}
