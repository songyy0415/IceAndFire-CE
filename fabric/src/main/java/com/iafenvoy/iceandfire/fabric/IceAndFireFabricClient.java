package com.iafenvoy.iceandfire.fabric;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.IceAndFireClient;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.iafenvoy.iceandfire.registry.IafRenderers;
import com.iafenvoy.iceandfire.render.entity.StoneStatueEntityRenderer;
import com.iafenvoy.iceandfire.screen.gui.bestiary.BestiaryScreen;
import dev.architectury.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class IceAndFireFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        IceAndFireClient.init();
        IceAndFireClient.process();
        IafRenderers.registerParticleRenderers(holder -> holder.applyRegister(ParticleProviderRegistry.getInstance()::register));
        // Drop cached bestiary page text and statue models when resources / the client level change.
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "bestiary_cache"),
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "bestiary_cache");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        BestiaryScreen.invalidatePageCache();
                    }
                });
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> {
            var renderer = client.getEntityRenderDispatcher().renderers.get(IafEntities.STONE_STATUE.get());
            if (renderer instanceof StoneStatueEntityRenderer statueRenderer) statueRenderer.clearEntityCache();
        });
        if (!Platform.isDevelopmentEnvironment())
            FabricLoader.getInstance().getModContainer(IceAndFire.MOD_ID).ifPresent(container -> ResourceManagerHelper.registerBuiltinResourcePack(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "iaf_legacy"), container, Component.translatable("resourcePack.iceandfire.legacy.name"), ResourcePackActivationType.NORMAL));
    }
}
