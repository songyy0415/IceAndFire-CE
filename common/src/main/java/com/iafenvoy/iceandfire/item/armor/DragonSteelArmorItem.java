package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DragonSteelArmorItem extends ArmorItem {
    public DragonSteelArmorItem(Holder<ArmorMaterial> material, Type slot) {
        super(material, slot, new Properties().durability(switch (slot) {
            case HELMET -> IafCommonConfig.INSTANCE.armors.dragonsteelHelmetDurability.getValue();
            case CHESTPLATE -> IafCommonConfig.INSTANCE.armors.dragonsteelChestplateDurability.getValue();
            case LEGGINGS -> IafCommonConfig.INSTANCE.armors.dragonsteelLeggingsDurability.getValue();
            case BOOTS -> IafCommonConfig.INSTANCE.armors.dragonsteelBootsDurability.getValue();
            case BODY -> 0;
        }));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.dragonscales_armor.desc").withStyle(ChatFormatting.GRAY));
    }
}
