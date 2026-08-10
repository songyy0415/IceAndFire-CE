package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.item.DragonEggItem;
import com.iafenvoy.iceandfire.item.block.entity.PodiumBlockEntity;
import com.iafenvoy.iceandfire.render.block.state.PodiumRenderState;
import com.iafenvoy.iceandfire.render.model.DragonEggModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PodiumBlockEntityRenderer implements BlockEntityRenderer<PodiumBlockEntity, PodiumRenderState> {
    private final ItemModelResolver itemModelResolver;

    public PodiumBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public PodiumRenderState createRenderState() {
        return new PodiumRenderState();
    }

    @Override
    public void extractRenderState(PodiumBlockEntity entity, PodiumRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        ItemStack stack = entity.getItem(0);
        state.hasItem = !stack.isEmpty();
        state.eggType = stack.getItem() instanceof DragonEggItem egg ? egg.type : null;
        float f2 = entity.prevTicksExisted + (entity.ticksExisted - entity.prevTicksExisted) * partialTicks;
        state.itemBob = Mth.sin(f2 / 10.0F) * 0.1F + 0.1F;
        state.itemAngle = f2 / 20.0F;
        state.item.clear();
        if (state.hasItem && state.eggType == null) {
            Level level = entity.getLevel();
            ItemOwner owner = new ItemOwner() {
                @Override
                public Level level() {
                    return level;
                }

                @Override
                public Vec3 position() {
                    return Vec3.atCenterOf(entity.getBlockPos());
                }

                @Override
                public float getVisualRotationYInDegrees() {
                    return 0.0F;
                }
            };
            this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, level, owner, 0);
        }
    }

    @Override
    public void submit(PodiumRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasItem) {
            return;
        }
        if (state.eggType != null) {
            DragonEggModel model = new DragonEggModel();
            model.renderPodium();
            RenderType type = RenderTypes.entityCutout(state.eggType.getTextureProvider().getEggTexture());
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5F, 0.475F, 0.5F);
            matrixStackIn.pushPose();
            matrixStackIn.pushPose();
            submitNodeCollector.submitCustomGeometry(matrixStackIn, type, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                model.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
            matrixStackIn.popPose();
            matrixStackIn.popPose();
            matrixStackIn.popPose();
            return;
        }
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.55F + state.itemBob, 0.5F);
        matrixStackIn.mulPose(Axis.YP.rotation(state.itemAngle));
        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 0.2F, 0);
        matrixStackIn.scale(0.65F, 0.65F, 0.65F);
        state.item.submit(matrixStackIn, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();
        matrixStackIn.popPose();
    }
}
