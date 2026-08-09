package com.iafenvoy.iceandfire.item.tool;

import com.iafenvoy.iceandfire.entity.AmphithereArrowEntity;
import com.iafenvoy.iceandfire.registry.IafEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class AmphithereArrowItem extends ArrowItem {
    public AmphithereArrowItem() {
        super(new Item.Properties());
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom) {
        return new AmphithereArrowEntity(IafEntities.AMPHITHERE_ARROW.get(), shooter, world, shotFrom);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.iceandfire.amphithere_arrow.desc").withStyle(ChatFormatting.GRAY));
    }
}
