package com.iafenvoy.iceandfire.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class IafItemUtil {
    private IafItemUtil() {
    }

    /**
     * mc26.2 hurtAndBreak requires a ServerPlayer; apply damage only on the logical server.
     */
    public static void damageStackServerSide(ItemStack stack, int amount, LivingEntity user) {
        if (user.level() instanceof ServerLevel serverLevel && user instanceof ServerPlayer serverPlayer)
            stack.hurtAndBreak(amount, serverLevel, serverPlayer, item -> {
            });
    }
}
