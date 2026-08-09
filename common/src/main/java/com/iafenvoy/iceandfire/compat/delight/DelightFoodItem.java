package com.iafenvoy.iceandfire.compat.delight;

import dev.architectury.platform.Platform;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DelightFoodItem extends Item {
    public DelightFoodItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (!Platform.isModLoaded("farmersdelight"))
            tooltip.accept(Component.translatable("item.iceandfire.tooltip.require.delight"));
    }
}
