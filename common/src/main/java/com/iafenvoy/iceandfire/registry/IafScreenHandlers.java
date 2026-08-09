package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.screen.gui.*;
import com.iafenvoy.iceandfire.screen.gui.bestiary.BestiaryScreen;
import com.iafenvoy.iceandfire.screen.handler.*;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class IafScreenHandlers {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<DragonScreenHandler>> DRAGON_SCREEN = register("dragon", () -> MenuRegistry.ofExtended(DragonScreenHandler::new));
    public static final RegistrySupplier<MenuType<HippogryphScreenHandler>> HIPPOGRYPH_SCREEN = register("hippogryph", () -> MenuRegistry.ofExtended(HippogryphScreenHandler::new));
    public static final RegistrySupplier<MenuType<HippocampusScreenHandler>> HIPPOCAMPUS_SCREEN = register("hippocampus", () -> MenuRegistry.ofExtended(HippocampusScreenHandler::new));
    public static final RegistrySupplier<MenuType<DragonForgeScreenHandler>> DRAGON_FORGE_SCREEN = register("dragon_forge", () -> MenuRegistry.ofExtended(DragonForgeScreenHandler::new));
    public static final RegistrySupplier<MenuType<PodiumScreenHandler>> PODIUM_SCREEN = register("podium", () -> new MenuType<>(PodiumScreenHandler::new, FeatureFlags.VANILLA_SET));
    public static final RegistrySupplier<MenuType<LecternScreenHandler>> IAF_LECTERN_SCREEN = register("iaf_lectern", () -> new MenuType<>(LecternScreenHandler::new, FeatureFlags.VANILLA_SET));
    public static final RegistrySupplier<MenuType<BestiaryScreenHandler>> BESTIARY_SCREEN = register("bestiary", () -> MenuRegistry.ofExtended(BestiaryScreenHandler::new));

    private static <C extends AbstractContainerMenu> RegistrySupplier<MenuType<C>> register(String name, Supplier<MenuType<C>> type) {
        return REGISTRY.register(name, type);
    }

    public static void registerGui() {
        MenuScreens.register(IafScreenHandlers.IAF_LECTERN_SCREEN.get(), LecternScreen::new);
        MenuScreens.register(IafScreenHandlers.PODIUM_SCREEN.get(), PodiumScreen::new);
        MenuScreens.register(IafScreenHandlers.DRAGON_SCREEN.get(), DragonScreen::new);
        MenuScreens.register(IafScreenHandlers.HIPPOGRYPH_SCREEN.get(), HippogryphScreen::new);
        MenuScreens.register(IafScreenHandlers.HIPPOCAMPUS_SCREEN.get(), HippocampusScreen::new);
        MenuScreens.register(IafScreenHandlers.DRAGON_FORGE_SCREEN.get(), DragonForgeScreen::new);
        MenuScreens.register(IafScreenHandlers.BESTIARY_SCREEN.get(), BestiaryScreen::new);
    }
}
