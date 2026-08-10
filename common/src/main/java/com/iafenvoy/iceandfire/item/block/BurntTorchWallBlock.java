package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.util.DreadBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class BurntTorchWallBlock extends WallTorchBlock implements DreadBlock {
    public BurntTorchWallBlock() {
        super(ParticleTypes.SMOKE, Properties.of().mapColor(MapColor.WOOD).ignitedByLava().lightLevel((state) -> 0).sound(SoundType.WOOD).noOcclusion().dynamicShape().noCollision());
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
    }
}