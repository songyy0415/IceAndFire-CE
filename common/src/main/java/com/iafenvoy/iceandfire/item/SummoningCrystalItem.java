package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.registry.IafDataComponents;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.world.DragonSummoning;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

public class SummoningCrystalItem extends Item {
    public SummoningCrystalItem(ResourceKey<Item> key) {
        super(new Properties().stacksTo(1).setId(key));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        //Data Fix
        if (stack.has(IafDataComponents.NBT_COMPOUND.get())) {
            stack.set(IafDataComponents.CRYSTAL_DRAGON_DATA.get(), stack.get(IafDataComponents.NBT_COMPOUND.get()));
            stack.remove(IafDataComponents.NBT_COMPOUND.get());
        }
    }

    public static boolean hasDragon(ItemStack stack) {
        return stack.getItem() instanceof SummoningCrystalItem
                && DragonSummoning.boundDragon(stack) != null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        boolean flag = false;
        String desc = "entity.iceandfire.fire_dragon";
        if (stack.getItem() == IafItems.SUMMONING_CRYSTAL_ICE.get()) desc = "entity.iceandfire.ice_dragon";
        if (stack.getItem() == IafItems.SUMMONING_CRYSTAL_LIGHTNING.get()) desc = "entity.iceandfire.lightning_dragon";
        CompoundTag nbt = stack.get(IafDataComponents.CRYSTAL_DRAGON_DATA.get());
        if (nbt != null)
            for (String tagInfo : nbt.keySet())
                if (tagInfo.contains("Dragon")) {
                    CompoundTag dragonTag = nbt.getCompound(tagInfo).orElse(new CompoundTag());
                    String dragonName = I18n.get(desc);
                    if (!dragonTag.getString("CustomName").orElse("").isEmpty())
                        dragonName = dragonTag.getString("CustomName").orElse("");
                    tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.bound", dragonName).withStyle(ChatFormatting.GRAY));
                    flag = true;
                }
        if (!flag) {
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.desc_0").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.desc_1").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!hasDragon(stack)) return InteractionResult.PASS;
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        if (context.getPlayer() instanceof ServerPlayer player)
            DragonSummoning.request(player, context.getHand(), target);
        return InteractionResult.SUCCESS;
    }
}
