package com.iafenvoy.iceandfire.world.processor;

import com.iafenvoy.iceandfire.registry.IafBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DreadPortalProcessor implements StructureProcessor {
    public static final DreadPortalProcessor INSTANCE = new DreadPortalProcessor();
    public static final MapCodec<DreadPortalProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    public static BlockState getRandomCrackedBlock(RandomSource random) {
        float rand = random.nextFloat();
        if (rand < 0.3) return IafBlocks.DREAD_STONE_BRICKS.get().defaultBlockState();
        else if (rand < 0.6) return IafBlocks.DREAD_STONE_BRICKS_CRACKED.get().defaultBlockState();
        else return IafBlocks.DREAD_STONE_BRICKS_MOSSY.get().defaultBlockState();
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader world, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings data) {
        RandomSource random = data.getRandom(processedBlockInfo.pos());
        float integrity = 1.0F;
        if (random.nextFloat() <= integrity && processedBlockInfo.state().is(IafBlocks.DREAD_STONE_BRICKS.get())) {
            BlockState state = getRandomCrackedBlock(random);
            return new StructureTemplate.StructureBlockInfo(processedBlockInfo.pos(), state, null);
        }
        return processedBlockInfo;
    }

    @Override
    public MapCodec<DreadPortalProcessor> codec() {
        return CODEC;
    }
}
