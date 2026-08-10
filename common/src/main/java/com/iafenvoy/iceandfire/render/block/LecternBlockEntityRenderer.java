package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.item.block.LecternBlock;
import com.iafenvoy.iceandfire.item.block.entity.LecternBlockEntity;
import com.iafenvoy.iceandfire.render.block.state.LecternRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntityRenderer implements BlockEntityRenderer<LecternBlockEntity, LecternRenderState> {
    private static final Identifier LECTERN_BOOK_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/lectern_book.png");
    private final BookModel bookModel;

    public LecternBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public LecternRenderState createRenderState() {
        return new LecternRenderState();
    }

    @Override
    public void extractRenderState(LecternBlockEntity entity, LecternRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        state.pageFlip = entity.pageFlip;
        state.pageFlipPrev = entity.pageFlipPrev;
        state.partialTicks = partialTicks;
        state.yRot = this.getRotation(entity);
    }

    @Override
    public void submit(LecternRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.1F, 0.5F);
        matrixStackIn.scale(0.8F, 0.8F, 0.8F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yRot));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(112.0F));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0F));
        float f4 = state.pageFlipPrev + (state.pageFlip - state.pageFlipPrev) * state.partialTicks + 0.25F;
        float f5 = state.pageFlipPrev + (state.pageFlip - state.pageFlipPrev) * state.partialTicks + 0.75F;
        f4 = (f4 - Mth.floor(f4)) * 1.6F - 0.3F;
        f5 = (f5 - Mth.floor(f5)) * 1.6F - 0.3F;
        if (f4 < 0.0F) f4 = 0.0F;
        if (f5 < 0.0F) f5 = 0.0F;
        if (f4 > 1.0F) f4 = 1.0F;
        if (f5 > 1.0F) f5 = 1.0F;
        float f6 = 1.29F;
        this.bookModel.setupAnim(BookModel.State.forAnimation(state.partialTicks, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6));
        submitNodeCollector.submitCustomGeometry(matrixStackIn, RenderTypes.entityCutout(LECTERN_BOOK_TEXTURE), (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            this.bookModel.renderToBuffer(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
        matrixStackIn.popPose();
    }

    private float getRotation(LecternBlockEntity lectern) {
        return switch (lectern.getBlockState().getValue(LecternBlock.FACING)) {
            case EAST -> 90;
            case WEST -> -90;
            case SOUTH -> 0;
            default -> 180;
        };
    }
}
