package com.iafenvoy.iceandfire.fabric;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.IceAndFireClient;
import com.iafenvoy.iceandfire.registry.IafRenderers;
import dev.architectury.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class IceAndFireFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        IceAndFireClient.init();
        IceAndFireClient.process();
        IafRenderers.registerParticleRenderers(holder -> holder.applyRegister(ParticleProviderRegistry.getInstance()::register));
        if (!Platform.isDevelopmentEnvironment())
            FabricLoader.getInstance().getModContainer(IceAndFire.MOD_ID).ifPresent(container -> ResourceManagerHelper.registerBuiltinResourcePack(ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "iaf_legacy"), container, Component.translatable("resourcePack.iceandfire.legacy.name"), ResourcePackActivationType.NORMAL));
    }
}
