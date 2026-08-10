package com.iafenvoy.iceandfire;

import com.iafenvoy.iceandfire.config.IafClientConfig;
import com.iafenvoy.iceandfire.event.ClientEvents;
import com.iafenvoy.iceandfire.event.CommonEvents;
import com.iafenvoy.iceandfire.network.ClientNetworkHelper;
import com.iafenvoy.iceandfire.registry.IafKeybindings;
import com.iafenvoy.iceandfire.registry.IafRenderers;
import com.iafenvoy.iceandfire.registry.IafScreenHandlers;
import com.iafenvoy.iceandfire.render.PortalRenderHelper;
import com.iafenvoy.iceandfire.render.SirenShaderRenderHelper;
import com.iafenvoy.jupiter.ConfigManager;
import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class IceAndFireClient {
    public static void init() {
        ConfigManager.getInstance().registerConfigHandler(IafClientConfig.INSTANCE);

        IafRenderers.registerEntityRenderers();
        IafKeybindings.init();

    }

    public static void process() {
        IafScreenHandlers.registerGui();
        IafRenderers.registerRenderLayers();
        IafRenderers.registerModelPredicates();
        IafRenderers.registerBlockEntityRenderers();
        IafRenderers.registerArmorRenderers();
        IafRenderers.registerItemRenderers();
        PortalRenderHelper.init();

        CommonEvents.LIVING_TICK.register(ClientEvents::onLivingUpdate);
        ClientTickEvent.CLIENT_POST.register(SirenShaderRenderHelper::tick);

        ClientNetworkHelper.registerReceivers();
    }
}
