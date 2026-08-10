package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.item.block.PixieHouseBlock;
import com.iafenvoy.iceandfire.item.block.entity.PixieHouseBlockEntity;
import com.iafenvoy.iceandfire.render.block.state.PixieHouseRenderState;
import com.iafenvoy.iceandfire.render.entity.PixieEntityRenderer;
import com.iafenvoy.iceandfire.render.model.PixieHouseModel;
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

public class PixieHouseBlockEntityRenderer implements BlockEntityRenderer<PixieHouseBlockEntity, PixieHouseRenderState> {
    private static final PixieHouseModel MODEL = new PixieHouseModel();
    private static final Identifier HOUSE_TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/house/pixie_house_0.png");
    private static final Identifier HOUSE_TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/house/pixie_house_1.png");
    private static final Identifier HOUSE_TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/house/pixie_house_2.png");
    private static final Identifier HOUSE_TEXTURE_3 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/house/pixie_house_3.png");
    private static final Identifier HOUSE_TEXTURE_4 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/house/pixie_house_4.png");
    private static final Identifier HOUSE_TEXTURE_5 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/house/pixie_house_5.png");
    private final PixieModel pixieModel;

    public PixieHouseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.pixieModel = new PixieModel();
    }

    @Override
    public PixieHouseRenderState createRenderState() {
        return new PixieHouseRenderState();
    }

    @Override
    public void extractRenderState(PixieHouseBlockEntity entity, PixieHouseRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        if (entity.getBlockState().getBlock() instanceof PixieHouseBlock) {
            state.meta = PixieHouseBlockEntity.getHouseTypeFromBlock(entity.getBlockState().getBlock());
            state.rotation = entity.getBlockState().getValue(PixieHouseBlock.FACING).get2DDataValue() * 90;
        }
        state.hasPixie = entity.getLevel() != null && entity.hasPixie;
        state.pixieType = entity.pixieType;
    }

    @Override
    public void submit(PixieHouseRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.501F, 0.5F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.rotation));
        if (state.hasPixie) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0F, 0.95F, 0F);
            matrixStackIn.scale(0.55F, 0.55F, 0.55F);
            RenderType type = RenderTypes.entityCutout(this.getPixieTexture(state.pixieType));
            RenderType type2 = RenderTypes.eyes(this.getPixieTexture(state.pixieType));
            matrixStackIn.pushPose();
            this.pixieModel.animateInHouse();
            submitNodeCollector.submitCustomGeometry(matrixStackIn, type, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.pixieModel.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
            submitNodeCollector.submitCustomGeometry(matrixStackIn, type2, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.pixieModel.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
            });
            matrixStackIn.popPose();
            matrixStackIn.popPose();
        }
        RenderType pixieType = RenderTypes.entityCutout(this.getHouseTexture(state.meta));
        matrixStackIn.pushPose();
        submitNodeCollector.submitCustomGeometry(matrixStackIn, pixieType, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            MODEL.renderPartsToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
        matrixStackIn.popPose();
        matrixStackIn.popPose();
    }

    private Identifier getPixieTexture(int pixieType) {
        return switch (pixieType) {
            case 1 -> PixieEntityRenderer.TEXTURE_1;
            case 2 -> PixieEntityRenderer.TEXTURE_2;
            case 3 -> PixieEntityRenderer.TEXTURE_3;
            case 4 -> PixieEntityRenderer.TEXTURE_4;
            case 5 -> PixieEntityRenderer.TEXTURE_5;
            default -> PixieEntityRenderer.TEXTURE_0;
        };
    }

    private Identifier getHouseTexture(int meta) {
        return switch (meta) {
            case 1 -> HOUSE_TEXTURE_1;
            case 2 -> HOUSE_TEXTURE_2;
            case 3 -> HOUSE_TEXTURE_3;
            case 4 -> HOUSE_TEXTURE_4;
            case 5 -> HOUSE_TEXTURE_5;
            default -> HOUSE_TEXTURE_0;
        };
    }
}
