package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.data.SeaSerpentType;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class SeaSerpentScaleItem extends Item {
    private final SeaSerpentType type;

    public SeaSerpentScaleItem(ResourceKey<Item> key, SeaSerpentType type) {
        super(new Properties().setId(key));
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("sea_serpent." + this.type.getName()).withStyle(this.type.getColor()));
    }
}
