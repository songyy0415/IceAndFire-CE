package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public final class IafPotions {
    public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.POTION);
    public static final RegistrySupplier<Potion> DEPETRIFICATION = REGISTRY.register("depetrification",
            () -> new Potion("depetrification", new MobEffectInstance(IafStatusEffects.depetrificationHolder(), 20 * 60)));

    private IafPotions() {
    }
}
