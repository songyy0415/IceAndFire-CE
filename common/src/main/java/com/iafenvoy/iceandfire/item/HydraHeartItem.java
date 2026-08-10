package com.iafenvoy.iceandfire.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class HydraHeartItem extends Item {
    public HydraHeartItem(ResourceKey<Item> key) {
        super(new Properties().stacksTo(1).setId(key));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player player && slot == EquipmentSlot.MAINHAND) {
            double healthPercentage = player.getHealth() / Math.max(1, player.getMaxHealth());
            if (healthPercentage < 1.0D) {
                int level = 0;
                if (healthPercentage < 0.25D) level = 3;
                else if (healthPercentage < 0.5D) level = 2;
                else if (healthPercentage < 0.75D) level = 1;
                //Consider using EffectInstance.combine
                if (!player.hasEffect(MobEffects.REGENERATION) || player.getEffect(MobEffects.REGENERATION).getAmplifier() < level)
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, level, true, false));
            }
            //In hotbar
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hydra_heart.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hydra_heart.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
