package com.iafenvoy.iceandfire.item.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class FallingGenericBlock extends FallingBlock {
    private static final MapCodec<? extends FallingBlock> CODEC = simpleCodec(FallingGenericBlock::new);

    public FallingGenericBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter getter, BlockPos pos) {
        return 16777215;
    }

    public static FallingGenericBlock builder(ResourceKey<Block> key, float hardness, float resistance, SoundType sound, MapColor color, NoteBlockInstrument instrument) {
        Properties props = Properties.of().mapColor(color).instrument(instrument).sound(sound).strength(hardness, resistance).setId(key);
        return new FallingGenericBlock(props);
    }
}
