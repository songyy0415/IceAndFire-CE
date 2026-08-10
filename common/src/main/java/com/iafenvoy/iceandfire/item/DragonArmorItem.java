package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.DragonArmorMaterial;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DragonArmorItem extends Item {
    public final DragonArmorMaterial type;
    public final DragonArmorPart dragonSlot;
    public String name;
    private Pattern baseName = Pattern.compile("[a-z]+_[a-z]+");

    public DragonArmorItem(ResourceKey<Item> key, DragonArmorMaterial type, DragonArmorPart dragonSlot) {
        super(type.fireProof() ? new Properties().fireResistant().setId(key) : new Properties().setId(key));
        this.type = type;
        this.dragonSlot = dragonSlot;
        if (type.dragonSteel()) this.baseName = Pattern.compile("[a-z]+_[a-z]+_[a-z]+_[a-z]+");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        String words = "dragon.armor_" + this.dragonSlot.name().toLowerCase(Locale.ROOT);
        tooltip.accept(Component.translatable(words).withStyle(ChatFormatting.GRAY));
    }
}
