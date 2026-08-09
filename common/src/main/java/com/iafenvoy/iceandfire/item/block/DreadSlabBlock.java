package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import com.iafenvoy.iceandfire.item.block.util.DreadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class DreadSlabBlock extends SlabBlock implements DragonProof, DreadBlock {
    public DreadSlabBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(UNBREAKABLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UNBREAKABLE);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return state.getValue(UNBREAKABLE) ? 0 : super.getDestroyProgress(state, player, world, pos);
    }
}
