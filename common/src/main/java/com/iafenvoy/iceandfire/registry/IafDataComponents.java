package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.HippogryphType;
import com.iafenvoy.iceandfire.item.component.BestiaryPageComponent;
import com.iafenvoy.iceandfire.item.component.DragonHornComponent;
import com.iafenvoy.iceandfire.item.component.DragonSkullComponent;
import com.iafenvoy.iceandfire.item.component.StoneStatusComponent;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

public final class IafDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.DATA_COMPONENT_TYPE);
    public static final RegistrySupplier<DataComponentType<Integer>> TICK_COUNTER = register("tick_counter", DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT)
    );
    public static final RegistrySupplier<DataComponentType<Integer>> USER_ID = register("user_id", DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT)
    );
    public static final RegistrySupplier<DataComponentType<Unit>> ACTIVE = register("active", DataComponentType.<Unit>builder()
            .persistent(Unit.CODEC)
            .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );
    public static final RegistrySupplier<DataComponentType<HippogryphType>> HIPPOGRYPH_EGG = register("hippogryph_egg", DataComponentType.<HippogryphType>builder()
            .persistent(IafRegistries.HIPPOGRYPH_TYPE.byNameCodec())
    );
    //FIXME::Data Fix For Crystal
    public static final RegistrySupplier<DataComponentType<CompoundTag>> NBT_COMPOUND = register("nbt_compound", DataComponentType.<CompoundTag>builder()
            .persistent(CompoundTag.CODEC)
    );
    public static final RegistrySupplier<DataComponentType<CompoundTag>> CRYSTAL_DRAGON_DATA = register("crystal_dragon_data", DataComponentType.<CompoundTag>builder()
            .persistent(CompoundTag.CODEC)
    );
    public static final RegistrySupplier<DataComponentType<BestiaryPageComponent>> BESTIARY_PAGES = register("bestiary_pages", DataComponentType.<BestiaryPageComponent>builder()
            .persistent(BestiaryPageComponent.CODEC)
    );
    public static final RegistrySupplier<DataComponentType<DragonHornComponent>> DRAGON_HORN = register("dragon_horn", DataComponentType.<DragonHornComponent>builder()
            .persistent(DragonHornComponent.CODEC)
    );
    public static final RegistrySupplier<DataComponentType<DragonSkullComponent>> DRAGON_SKULL = register("dragon_skull", DataComponentType.<DragonSkullComponent>builder()
            .persistent(DragonSkullComponent.CODEC)
    );
    public static final RegistrySupplier<DataComponentType<StoneStatusComponent>> STONE_STATUS = register("stone_status", DataComponentType.<StoneStatusComponent>builder()
            .persistent(StoneStatusComponent.CODEC)
    );

    public static <T> RegistrySupplier<DataComponentType<T>> register(String id, DataComponentType.Builder<T> builder) {
        return REGISTRY.register(id, builder::build);
    }
}
