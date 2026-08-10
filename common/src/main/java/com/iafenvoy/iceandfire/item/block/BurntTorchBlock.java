package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.util.DreadBlock;
import com.iafenvoy.iceandfire.item.block.util.WallBlock;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class BurntTorchBlock extends TorchBlock implements DreadBlock, WallBlock {
    public BurntTorchBlock() {
        super(ParticleTypes.SMOKE, Properties.of().mapColor(MapColor.WOOD).ignitedByLava().lightLevel((state) -> 0).sound(SoundType.WOOD).noOcclusion().dynamicShape().noCollision());
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
    }

    @Override
    public Block wallBlock() {
        return IafBlocks.BURNT_TORCH_WALL.get();
    }
}