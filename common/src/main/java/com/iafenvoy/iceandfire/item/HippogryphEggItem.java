package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.data.HippogryphType;
import com.iafenvoy.iceandfire.entity.HippogryphEggEntity;
import com.iafenvoy.iceandfire.registry.IafDataComponents;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.iafenvoy.iceandfire.registry.IafHippogryphTypes;
import com.iafenvoy.iceandfire.registry.IafItems;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class HippogryphEggItem extends Item implements ProjectileItem {
    public HippogryphEggItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack createEggStack(HippogryphType parent1, HippogryphType parent2) {
        HippogryphType eggType = ThreadLocalRandom.current().nextBoolean() ? parent1 : parent2;
        ItemStack stack = new ItemStack(IafItems.HIPPOGRYPH_EGG.get());
        stack.set(IafDataComponents.HIPPOGRYPH_EGG.get(), eggType);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (!playerIn.isCreative()) itemstack.shrink(1);
        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));
        if (!worldIn.isClientSide()) {
            HippogryphEggEntity entityegg = new HippogryphEggEntity(IafEntities.HIPPOGRYPH_EGG.get(), worldIn, playerIn, itemstack);
            entityegg.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F, 1.0F);
            worldIn.addFreshEntity(entityegg);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        HippogryphType eggOrdinal = stack.getOrDefault(IafDataComponents.HIPPOGRYPH_EGG.get(), IafHippogryphTypes.BLACK);
        tooltip.accept(Component.translatable("entity.iceandfire.hippogryph." + eggOrdinal.name()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        return new HippogryphEggEntity(IafEntities.HIPPOGRYPH_EGG.get(), world, pos.x(), pos.y(), pos.z(), stack);
    }
}
