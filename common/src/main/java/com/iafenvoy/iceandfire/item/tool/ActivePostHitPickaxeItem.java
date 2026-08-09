package com.iafenvoy.iceandfire.item.tool;

import com.iafenvoy.iceandfire.item.ability.PostHitAbility;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class ActivePostHitPickaxeItem extends PickaxeItem {
    private final PostHitAbility ability;
    public ActivePostHitPickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings, PostHitAbility ability) {
        super(material, attackDamage, attackSpeed, settings);
        this.ability = ability;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (this.ability.isEnable()) {
            this.ability.active(stack, target, attacker);
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (this.ability.isEnable()) {
            this.ability.addDescription(tooltip);
        }
    }
}
