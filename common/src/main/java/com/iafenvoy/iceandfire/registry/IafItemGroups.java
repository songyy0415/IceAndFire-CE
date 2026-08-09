package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class IafItemGroups {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> BLOCKS = register("blocks", () -> CreativeTabRegistry.create(Component.translatable("itemGroup." + IceAndFire.MOD_ID + ".blocks"), () -> new ItemStack(IafBlocks.DRAGON_SCALE_RED.get())));
    public static final RegistrySupplier<CreativeModeTab> ITEMS = register("items", () -> CreativeTabRegistry.create(Component.translatable("itemGroup." + IceAndFire.MOD_ID + ".items"), () -> new ItemStack(IafItems.DRAGON_SKULL_FIRE.get())));
    public static final RegistrySupplier<CreativeModeTab> TOOLS_WEAPONS = register("tools_weapons", () -> CreativeTabRegistry.create(Component.translatable("itemGroup." + IceAndFire.MOD_ID + ".tools_weapons"), () -> new ItemStack(IafItems.DRAGONSTEEL_LIGHTNING_SWORD.get())));
    public static final RegistrySupplier<CreativeModeTab> ARMORS = register("armors", () -> CreativeTabRegistry.create(Component.translatable("itemGroup." + IceAndFire.MOD_ID + ".armors"), () -> new ItemStack(IafItems.DRAGONSTEEL_FIRE_HELMET.get())));

    private static RegistrySupplier<CreativeModeTab> register(String name, Supplier<CreativeModeTab> group) {
        return REGISTRY.register(name, group);
    }
}
