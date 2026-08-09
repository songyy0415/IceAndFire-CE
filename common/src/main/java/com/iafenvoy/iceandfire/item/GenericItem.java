package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.registry.IafItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class GenericItem extends Item {
    private final int description;

    public GenericItem(int textLength) {
        super(new Properties());
        this.description = textLength;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (this == IafItems.CREATIVE_DRAGON_MEAL.get()) return true;
        else return super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (this.description > 0)
            for (int i = 0; i < this.description; i++)
                tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc_" + i).withStyle(ChatFormatting.GRAY));
    }
}
