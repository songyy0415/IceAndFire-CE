package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.entity.GhostChestBlockEntity;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

public class GhostChestBlock extends ChestBlock {
    public GhostChestBlock() {
        super(IafBlockEntities.GHOST_CHEST::get, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava().strength(2.5F).sound(SoundType.WOOD));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GhostChestBlockEntity(pos, state);
    }

    @Override
    protected Stat<Identifier> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return Mth.clamp(ChestBlockEntity.getOpenCount(blockAccess, pos), 0, 15);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return super.getDrops(state, builder);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return side == Direction.UP ? blockState.getSignal(blockAccess, pos, side) : 0;
    }
}
