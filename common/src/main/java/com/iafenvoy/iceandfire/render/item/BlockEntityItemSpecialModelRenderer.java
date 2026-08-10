package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.iceandfire.item.block.DreadPortalBlock;
import com.iafenvoy.iceandfire.item.block.GhostChestBlock;
import com.iafenvoy.iceandfire.item.block.PixieHouseBlock;
import com.iafenvoy.iceandfire.item.block.entity.DreadPortalBlockEntity;
import com.iafenvoy.iceandfire.item.block.entity.GhostChestBlockEntity;
import com.iafenvoy.iceandfire.item.block.entity.PixieHouseBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class BlockEntityItemSpecialModelRenderer implements SpecialModelRenderer<BlockEntity> {
    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
    }

    @Override
    public BlockEntity extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof PixieHouseBlock) return new PixieHouseBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            if (block instanceof DreadPortalBlock) return new DreadPortalBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            if (block instanceof GhostChestBlock) return new GhostChestBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        }
        return null;
    }

    @Override
    public void submit(BlockEntity entity, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color) {
        if (entity == null) return;
        submitEntity(Minecraft.getInstance().getBlockEntityRenderDispatcher(), entity, poseStack, submitNodeCollector, light);
    }

    private static <E extends BlockEntity, S extends BlockEntityRenderState> void submitEntity(BlockEntityRenderDispatcher dispatcher, E entity, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        BlockEntityRenderer<E, S> renderer = dispatcher.getRenderer(entity);
        if (renderer == null) return;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        S state = renderer.createRenderState();
        renderer.extractRenderState(entity, state, partialTick, Vec3.ZERO, null);
        state.lightCoords = light;
        dispatcher.submit(state, poseStack, submitNodeCollector, new CameraRenderState());
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<BlockEntity> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<BlockEntity> bake(SpecialModelRenderer.BakingContext context) {
            return new BlockEntityItemSpecialModelRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<BlockEntity>> type() {
            return MAP_CODEC;
        }
    }
}
