package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import com.iafenvoy.iceandfire.item.block.util.DreadBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class DreadStoneFaceBlock extends HorizontalDirectionalBlock implements DreadBlock, DragonProof {
    public DreadStoneFaceBlock(ResourceKey<Block> key) {
        super(Properties.ofFullCopy(Blocks.STONE).setId(key));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(UNBREAKABLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(UNBREAKABLE);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return state.getValue(UNBREAKABLE) ? 0 : super.getDestroyProgress(state, player, world, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return MapCodec.unit(this);
    }
}
