package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.loot.DragonLootFunction;
import com.iafenvoy.iceandfire.loot.SeaSerpentLootFunction;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public final class IafLoots {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.LOOT_FUNCTION_TYPE);

    public static final RegistrySupplier<MapCodec<DragonLootFunction>> DRAGON_LOOT = register("dragon_loot", () -> DragonLootFunction.CODEC);
    public static final RegistrySupplier<MapCodec<SeaSerpentLootFunction>> SEA_SERPENT_LOOT = register("sea_serpent_loot", () -> SeaSerpentLootFunction.CODEC);

    private static <T extends LootItemFunction> RegistrySupplier<MapCodec<T>> register(String id, Supplier<MapCodec<T>> codec) {
        return REGISTRY.register(id, codec);
    }

    private IafLoots() {
    }
}
