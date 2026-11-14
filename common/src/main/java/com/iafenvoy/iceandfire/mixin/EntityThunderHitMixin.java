package com.iafenvoy.iceandfire.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents the lightning summoned by the dragon bone sword from striking its own summoner
 * (upstream #268). {@link com.iafenvoy.iceandfire.item.ability.SummonLightningAbility} tags the
 * bolt with the summoner's UUID; the vanilla strike path never reads that tag, so the summoner
 * used to be hit by their own sword's lightning.
 */
@Mixin(Entity.class)
public abstract class EntityThunderHitMixin {
    @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
    private void iceandfire$skipSummonerLightning(ServerLevel level, LightningBolt lightningBolt, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (lightningBolt.entityTags().contains(self.getStringUUID())) ci.cancel();
    }
}
