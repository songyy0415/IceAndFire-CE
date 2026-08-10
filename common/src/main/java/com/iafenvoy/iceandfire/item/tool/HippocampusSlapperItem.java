package com.iafenvoy.iceandfire.item.tool;

import com.iafenvoy.iceandfire.registry.IafToolMaterials;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class HippocampusSlapperItem extends Item {
    public HippocampusSlapperItem(ResourceKey<Item> key) {
        super(new Item.Properties().sword(IafToolMaterials.HIPPOCAMPUS_SWORD_TOOL_MATERIAL.toolMaterial(), 3, -2.4F).setId(key));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity targetEntity, LivingEntity attacker) {
        targetEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
        targetEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 2));
        targetEntity.playSound(SoundEvents.GUARDIAN_FLOP, 3, 1);

        super.hurtEnemy(stack, targetEntity, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hippocampus_slapper.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hippocampus_slapper.desc_1").withStyle(ChatFormatting.GRAY));
    }
}