package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import java.util.List;
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

public class DragonSteelArmorItem extends Item {
    public DragonSteelArmorItem(ResourceKey<Item> key, ArmorMaterial material, ArmorType slot) {
        super(new Item.Properties().humanoidArmor(material, slot).durability(switch (slot) {
            case HELMET -> IafCommonConfig.INSTANCE.armors.dragonsteelHelmetDurability.getValue();
            case CHESTPLATE -> IafCommonConfig.INSTANCE.armors.dragonsteelChestplateDurability.getValue();
            case LEGGINGS -> IafCommonConfig.INSTANCE.armors.dragonsteelLeggingsDurability.getValue();
            case BOOTS -> IafCommonConfig.INSTANCE.armors.dragonsteelBootsDurability.getValue();
            case BODY -> 0;
        }).setId(key));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.dragonscales_armor.desc").withStyle(ChatFormatting.GRAY));
    }
}
