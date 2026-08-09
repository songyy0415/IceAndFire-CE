package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.registry.IafArmorMaterials;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

public class EarPlugsArmorItem extends Item {
    public EarPlugsArmorItem() {
        super(new Item.Properties().humanoidArmor(IafArmorMaterials.EARPLUGS, ArmorType.HELMET).durability(55));
    }

    private static boolean isAprilFool() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DATE) == 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (isAprilFool()) tooltip.accept(Component.translatable("item.iceandfire.air_pods.desc").withStyle(ChatFormatting.GREEN));
    }
}
