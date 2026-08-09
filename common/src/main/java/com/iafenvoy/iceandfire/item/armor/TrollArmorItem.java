package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.TrollType;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class TrollArmorItem extends ArmorItem {
    private final TrollType trollType;

    public TrollArmorItem(TrollType trollType, Type type) {
        super(trollType.getMaterial(), type, new Properties().durability(switch (type) {
            case HELMET -> 220;
            case CHESTPLATE -> 320;
            case LEGGINGS -> 300;
            case BOOTS -> 260;
            case BODY -> 0;
        }));
        this.trollType = trollType;
    }

    public static String getName(TrollType trollType, Type type) {
        return String.format(Locale.ROOT, "%s_troll_leather_%s", trollType.getName(), type.getName());
    }

    @Override
    public Holder<ArmorMaterial> getMaterial() {
        return this.trollType.getMaterial();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable(String.format(Locale.ROOT, "item.%s.troll_leather_armor_%s.desc", IceAndFire.MOD_ID, this.type.getName())).withStyle(ChatFormatting.GREEN));
    }
}
