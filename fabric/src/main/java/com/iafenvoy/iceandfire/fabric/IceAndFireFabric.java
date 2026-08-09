package com.iafenvoy.iceandfire.fabric;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.fabric.compat.trinkets.TrinketsRegistry;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.integration.IntegrationExecutor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.alchemy.Potions;

public final class IceAndFireFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        IceAndFire.init();
        IceAndFire.process();
        IafAttachments.init();
        FabricPotionBrewingBuilder.BUILD.register(builder -> builder.addMix(Potions.WATER, IafItems.SHINY_SCALES.get(), Potions.WATER_BREATHING));
        IntegrationExecutor.runWhenLoad("trinkets_updated", () -> TrinketsRegistry::registerItems);
    }
}
