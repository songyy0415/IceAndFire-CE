package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.DragonColor;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

public class DragonScaleArmorItem extends Item {
    private final DragonColor color;
    private final ArmorType armorType;

    public DragonScaleArmorItem(DragonColor color, ArmorType slot) {
        super(new Item.Properties().humanoidArmor(color.getMaterial(), slot).durability(switch (slot) {
            case HELMET -> 397;
            case CHESTPLATE -> 577;
            case LEGGINGS -> 541;
            case BOOTS -> 469;
            case BODY -> 0;
        }));
        this.color = color;
        this.armorType = slot;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("dragon." + this.color.getName().toLowerCase(Locale.ROOT)).withStyle(this.color.getColorFormatting()));
        tooltip.accept(Component.translatable("item.dragonscales_armor.desc").withStyle(ChatFormatting.GRAY));
    }

    public DragonColor getColor() {
        return this.color;
    }
}
