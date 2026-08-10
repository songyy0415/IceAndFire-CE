package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.item.block.JarBlock;
import com.iafenvoy.iceandfire.item.block.entity.JarBlockEntity;
import com.iafenvoy.iceandfire.render.block.state.JarRenderState;
import com.iafenvoy.iceandfire.render.entity.PixieEntityRenderer;
import com.iafenvoy.iceandfire.render.model.PixieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

public class JarBlockEntityRenderer implements BlockEntityRenderer<JarBlockEntity, JarRenderState> {
    private static final Identifier TEXTURE_0 = PixieEntityRenderer.TEXTURE_0;
    private static final Identifier TEXTURE_1 = PixieEntityRenderer.TEXTURE_1;
    private static final Identifier TEXTURE_2 = PixieEntityRenderer.TEXTURE_2;
    private static final Identifier TEXTURE_3 = PixieEntityRenderer.TEXTURE_3;
    private static final Identifier TEXTURE_4 = PixieEntityRenderer.TEXTURE_4;
    private static final Identifier TEXTURE_5 = PixieEntityRenderer.TEXTURE_5;
    private final PixieModel model;

    public JarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PixieModel();
    }

    @Override
    public JarRenderState createRenderState() {
        return new JarRenderState();
    }

    @Override
    public void extractRenderState(JarBlockEntity entity, JarRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        if (entity.getLevel() != null) {
            if (entity.getBlockState().getBlock() instanceof JarBlock jar) {
                state.pixieType = jar.getPixieType();
                state.hasPixie = !jar.isEmpty();
            } else {
                state.pixieType = entity.pixieType;
                state.hasPixie = entity.hasPixie;
            }
        }
        state.hasProduced = entity.hasProduced;
        state.rotationYaw = this.interpolateRotation(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
        state.animationProgress = entity.ticksExisted + partialTicks;
    }

    @Override
    public void submit(JarRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasPixie) {
            return;
        }
        RenderType type = switch (state.pixieType) {
            case 1 -> RenderTypes.entityCutout(TEXTURE_1);
            case 2 -> RenderTypes.entityCutout(TEXTURE_2);
            case 3 -> RenderTypes.entityCutout(TEXTURE_3);
            case 4 -> RenderTypes.entityCutout(TEXTURE_4);
            default -> RenderTypes.entityCutout(TEXTURE_0);
        };
        RenderType typeGlow = switch (state.pixieType) {
            case 1 -> RenderTypes.eyes(TEXTURE_1);
            case 2 -> RenderTypes.eyes(TEXTURE_2);
            case 3 -> RenderTypes.eyes(TEXTURE_3);
            case 4 -> RenderTypes.eyes(TEXTURE_4);
            default -> RenderTypes.eyes(TEXTURE_0);
        };
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.501F, 0.5F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
        matrixStackIn.pushPose();
        if (state.hasProduced) matrixStackIn.translate(0F, 0.90F, 0F);
        else matrixStackIn.translate(0F, 0.60F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.rotationYaw));
        matrixStackIn.scale(0.50F, 0.50F, 0.50F);
        this.model.animateInJar(state.hasProduced, state.animationProgress);
        submitNodeCollector.submitCustomGeometry(matrixStackIn, type, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            this.model.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
        submitNodeCollector.submitCustomGeometry(matrixStackIn, typeGlow, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            this.model.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
        matrixStackIn.popPose();
        matrixStackIn.popPose();
    }

    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f = yawOffset - prevYawOffset;
        while (f < -180) f += 360;
        while (f >= 180.0F) f -= 360.0F;
        return prevYawOffset + partialTicks * f;
    }
}
