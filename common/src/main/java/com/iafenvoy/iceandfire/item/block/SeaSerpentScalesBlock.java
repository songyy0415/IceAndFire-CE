package com.iafenvoy.iceandfire.item.block;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class SeaSerpentScalesBlock extends Block {
    final ChatFormatting color;
    final String name;

    public SeaSerpentScalesBlock(String name, ChatFormatting color) {
        super(Properties.of().mapColor(MapColor.STONE).strength(30F, 500F).sound(SoundType.STONE).requiresCorrectToolForDrops());
        this.color = color;
        this.name = name;
    }

}
