package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.entity.PodiumBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
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
    protected static final VoxelShape AABB = Block.box(2, 0, 2, 14, 23, 14);

    public PodiumBlock(ResourceKey<Block> key) {
        super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava().noOcclusion().dynamicShape().strength(2.0F).sound(SoundType.WOOD).setId(key));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    // 26.2 removed Block.onRemove — block-break cleanup is now done via the player hooks (see
    // vanilla BeehiveBlock). Survival break goes through ServerPlayerGameMode.destroyBlock, which
    // calls playerWillDestroy (before removal) then playerDestroy (after). Creative break goes
    // through destroyAndAck -> destroyBlock as well, but the block entity passed to playerDestroy
    // is null there, so the drop must also happen in playerWillDestroy where the block entity is
    // still retrievable by position. dropContents drains the inventory, so both hooks are idempotent.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(level, pos, state, player);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PodiumBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        return result;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (blockEntity instanceof PodiumBlockEntity) {
            Containers.dropContents(level, pos, (PodiumBlockEntity) blockEntity);
            level.updateNeighbourForOutputSignal(pos, this);
        }
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
        return MapCodec.unit(this);
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