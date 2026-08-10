package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.registry.IafDataComponents;
import com.iafenvoy.iceandfire.registry.IafSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DeathwormGauntletItem extends Item {
    public DeathwormGauntletItem(ResourceKey<Item> key) {
        super(new Properties().durability(500).component(IafDataComponents.USER_ID.get(), -1).component(DataComponents.CONSUMABLE, Consumable.builder().animation(ItemUseAnimation.BOW).build()).setId(key));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 10;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (stack.getOrDefault(IafDataComponents.USER_ID.get(), -1) != entity.getId())
            stack.set(IafDataComponents.USER_ID.get(), entity.getId());
        MiscData.get(entity).setLungeTicks(this.getUseDuration(stack, entity) - remainingUseTicks);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity user, int timeLeft) {
        stack.set(IafDataComponents.USER_ID.get(), -1);
            return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player) {
            Vec3 Vector3d = player.getViewVector(1.0F).normalize();
            double range = 5;
            for (LivingEntity livingEntity : world.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - range, player.getY() - range, player.getZ() - range, player.getX() + range, player.getY() + range, player.getZ() + range), livingEntity -> livingEntity != player)) {
                Vec3 delta = new Vec3(livingEntity.getX() - player.getX(), livingEntity.getY() - player.getY(), livingEntity.getZ() - player.getZ());
                double d0 = delta.length();
                delta = delta.normalize();
                double d1 = Vector3d.dot(delta);
                boolean canSee = d1 > 1.0D - 0.5D / d0 && player.hasLineOfSight(livingEntity);
                if (canSee) {
                    livingEntity.hurt(world.damageSources().playerAttack(player), 3F);
                    livingEntity.knockback(0.5F,livingEntity.getX() - player.getX(),livingEntity.getZ() - player.getZ(),player.level().damageSources().playerAttack(player),0);
                }
            }
            player.getCooldowns().addCooldown(new ItemStack(this), 20);
        }
        user.playSound(IafSounds.DEATHWORM_ATTACK.get(), 1F, 1F);
        stack.set(IafDataComponents.USER_ID.get(), -1);

        return super.finishUsingItem(stack, world, user);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.deathworm_gauntlet.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.deathworm_gauntlet.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
