package com.iafenvoy.iceandfire.fabric.compat.trinkets;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SimpleTickItemWrapper implements TrinketCallback {
    private final Item item;

    public SimpleTickItemWrapper(Item item) {
        this.item = item;
    }

    @Override
    public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel)
            this.item.inventoryTick(stack, serverLevel, entity, null);
    }
}
