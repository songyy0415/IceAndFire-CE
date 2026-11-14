package com.iafenvoy.iceandfire.fabric;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.fabric.compat.trinkets.TrinketsRegistry;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.registry.IafPotions;
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
        FabricPotionBrewingBuilder.BUILD.register(builder -> builder.addMix(Potions.AWKWARD, IafItems.GORGON_HEAD.get(), IafPotions.DEPETRIFICATION.asHolder()));
        IntegrationExecutor.runWhenLoad("trinkets_updated", () -> TrinketsRegistry::registerItems);
    }
}
