package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.entity.PodiumBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PodiumBlock extends BaseEntityBlock {
    private static final MapCodec<? extends BaseEntityBlock> CODEC = simpleCodec(s -> new PodiumBlock());
    protected static final VoxelShape AABB = Block.box(2, 0, 2, 14, 23, 14);

    public PodiumBlock() {
        super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava().noOcclusion().dynamicShape().strength(2.0F).sound(SoundType.WOOD));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof PodiumBlockEntity) {
            Containers.dropContents(worldIn, pos, (PodiumBlockEntity) tileentity);
            worldIn.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                MenuProvider screenHandlerFactory = this.getMenuProvider(state, world, pos);
                if (screenHandlerFactory != null)
                    player.openMenu(screenHandlerFactory);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PodiumBlockEntity(pos, state);
    }
}