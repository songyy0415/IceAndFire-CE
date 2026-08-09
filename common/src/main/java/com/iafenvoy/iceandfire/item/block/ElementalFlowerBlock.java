package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.registry.IafBlocks;
import com.iafenvoy.iceandfire.registry.tag.IafBlockTags;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElementalFlowerBlock extends BushBlock {
    private static final MapCodec<BushBlock> CODEC = simpleCodec(s -> new ElementalFlowerBlock());

    public ElementalFlowerBlock() {
        super(Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).offsetType(OffsetType.XZ).pushReaction(PushReaction.DESTROY));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    }

    @Override
    public MapCodec<BushBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean mayPlaceOn(BlockState state, BlockGetter world, BlockPos pos) {
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND) || state.is(BlockTags.SAND))
            return true;
        if (this == IafBlocks.FIRE_LILY.get())
            return state.is(BlockTags.SAND) || state.is(Blocks.NETHERRACK);
        else if (this == IafBlocks.LIGHTNING_LILY.get())
            return state.is(BlockTags.DIRT) || state.is(IafBlockTags.GRASSES);
        else
            return state.is(BlockTags.ICE) || state.is(BlockTags.SNOW) || state.is(BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER);
    }
}
