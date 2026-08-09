package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.effect.FrozenStatusEffect;
import com.iafenvoy.iceandfire.effect.SirenCharmStatusEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

@SuppressWarnings("unused")
public final class IafStatusEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<FrozenStatusEffect> FROZEN = register("frozen", FrozenStatusEffect::new);
    public static final RegistrySupplier<SirenCharmStatusEffect> SIREN_CHARM = register("siren_charm", SirenCharmStatusEffect::new);

    private static <T extends MobEffect> RegistrySupplier<T> register(String name, Supplier<T> obj) {
        return REGISTRY.register(name, obj);
    }
}
