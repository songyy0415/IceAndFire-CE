package com.iafenvoy.iceandfire.item.ability;
import net.minecraft.resources.Identifier;

import com.iafenvoy.iceandfire.event.ServerEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SummonLightningAbility implements PostHitAbility {
    @Override
    public void active(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (this.isEnable()) {
            if (attacker instanceof Player && attacker.attackAnim > 0.2) {
                return;
            }
            if (!attacker.level().isClientSide()) {
                LightningBolt lightningEntity = (LightningBolt) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("lightning_bolt")).create(target.level(), EntitySpawnReason.LOAD);
                assert lightningEntity != null;
                lightningEntity.addTag(ServerEvents.BOLT_DONT_DESTROY_LOOT);
                lightningEntity.addTag(attacker.getStringUUID());
                lightningEntity.setPos(target.position());
                if (!target.level().isClientSide()) {
                    target.level().addFreshEntity(lightningEntity);
                }
            }
        }
    }
}
