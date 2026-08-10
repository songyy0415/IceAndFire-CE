package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.TrollType;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

public class TrollArmorItem extends Item {
    private final TrollType trollType;
    private final ArmorType armorType;

    public TrollArmorItem(ResourceKey<Item> key, TrollType trollType, ArmorType type) {
        super(new Item.Properties().humanoidArmor(trollType.getMaterial(), type).durability(switch (type) {
            case HELMET -> 220;
            case CHESTPLATE -> 320;
            case LEGGINGS -> 300;
            case BOOTS -> 260;
            case BODY -> 0;
        }).setId(key));
        this.trollType = trollType;
        this.armorType = type;
    }

    public static String getName(TrollType trollType, ArmorType type) {
        return String.format(Locale.ROOT, "%s_troll_leather_%s", trollType.getName(), type.getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable(String.format(Locale.ROOT, "item.%s.troll_leather_armor_%s.desc", IceAndFire.MOD_ID, this.armorType.getName())).withStyle(ChatFormatting.GREEN));
    }
}
