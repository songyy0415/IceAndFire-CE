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
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public final class IafLoots {
    public static final DeferredRegister<LootItemFunctionType<?>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.LOOT_FUNCTION_TYPE);

    public static final RegistrySupplier<LootItemFunctionType<DragonLootFunction>> DRAGON_LOOT = register("dragon_loot", () -> DragonLootFunction.CODEC);
    public static final RegistrySupplier<LootItemFunctionType<SeaSerpentLootFunction>> SEA_SERPENT_LOOT = register("sea_serpent_loot", () -> SeaSerpentLootFunction.CODEC);

    private static <T extends LootItemFunction> RegistrySupplier<LootItemFunctionType<T>> register(String id, Supplier<MapCodec<T>> obj) {
        return REGISTRY.register(id, () -> new LootItemFunctionType<>(obj.get()));
    }
}
