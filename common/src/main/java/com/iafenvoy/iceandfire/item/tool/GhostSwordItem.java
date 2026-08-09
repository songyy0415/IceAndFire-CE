package com.iafenvoy.iceandfire.item.tool;

import com.iafenvoy.iceandfire.item.ability.BuiltinAbilities;
import com.iafenvoy.iceandfire.registry.IafToolMaterials;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class GhostSwordItem extends SwordItem {
    public GhostSwordItem() {
        super(IafToolMaterials.GHOST_SWORD_TOOL_MATERIAL, new Properties().component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(IafToolMaterials.GHOST_SWORD_TOOL_MATERIAL, 5, -1.0F)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, tooltip, type);
        BuiltinAbilities.SUMMON_GHOST_SWORD.addDescription(tooltip);
    }
}
