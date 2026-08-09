package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.data.DragonColor;
import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DragonScalesBlock extends Block implements DragonProof {
    final DragonColor type;

    public DragonScalesBlock(DragonColor type) {
        super(Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).dynamicShape().strength(30F, 500).sound(SoundType.STONE).requiresCorrectToolForDrops());
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, context, display, tooltip, options);
        tooltip.accept(Component.translatable("dragon." + this.type.getName()).withStyle(this.type.getColorFormatting()));
    }
}
