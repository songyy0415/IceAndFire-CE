package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import com.iafenvoy.iceandfire.item.block.util.DreadBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class DreadWoodLogBlock extends RotatedPillarBlock implements DragonProof, DreadBlock {
    public DreadWoodLogBlock(ResourceKey<Block> key) {
        super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).ignitedByLava().strength(2F, 10000F).sound(SoundType.WOOD).setId(key));
    }
}
