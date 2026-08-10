package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.entity.GhostSwordEntity;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.render.entity.state.GhostSwordRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GhostSwordEntityRenderer extends EntityRenderer<GhostSwordEntity, GhostSwordRenderState> {
    private final ItemModelResolver itemModelResolver;

    public GhostSwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public GhostSwordRenderState createRenderState() {
        return new GhostSwordRenderState();
    }

    @Override
    public void extractRenderState(GhostSwordEntity entity, GhostSwordRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lightCoords = this.getPackedLightCoords(entity, partialTicks);
        state.item.clear();
        this.itemModelResolver.updateForTopItem(state.item, new ItemStack(IafItems.GHOST_SWORD.get()), ItemDisplayContext.GROUND, entity.level(), entity, 0);
        state.yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.animProgress = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(GhostSwordRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yRot));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        matrixStackIn.translate(0, 0.5F, 0);
        matrixStackIn.scale(2F, 2F, 2F);
        matrixStackIn.mulPose(Axis.ZN.rotationDegrees(state.animProgress * 30.0F));
        matrixStackIn.translate(0, -0.15F, 0);
        state.item.submit(matrixStackIn, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();
    }
}
