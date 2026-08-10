package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class DragonBoneBlock extends RotatedPillarBlock implements DragonProof {
    public DragonBoneBlock(ResourceKey<Block> key) {
        super(Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.WOOD).strength(30F, 500F).requiresCorrectToolForDrops().setId(key));
    }
}
