package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.event.ClientEvents;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class IafKeybindings {
    public static final KeyMapping DRAGON_BREATH = new KeyMapping("key.dragon_fireAttack", GLFW.GLFW_KEY_R, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping DRAGON_STRIKE = new KeyMapping("key.dragon_strike", GLFW.GLFW_KEY_G, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping DRAGON_DOWN = new KeyMapping("key.dragon_down", GLFW.GLFW_KEY_X, KeyMapping.Category.GAMEPLAY);
    public static final KeyMapping DRAGON_CHANGE_VIEW = new KeyMapping("key.dragon_change_view", GLFW.GLFW_KEY_F7, KeyMapping.Category.GAMEPLAY);

    public static void init() {
        KeyMappingRegistry.register(DRAGON_BREATH);
        KeyMappingRegistry.register(DRAGON_STRIKE);
        KeyMappingRegistry.register(DRAGON_DOWN);
        KeyMappingRegistry.register(DRAGON_CHANGE_VIEW);
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (DRAGON_CHANGE_VIEW.consumeClick()) {
                if (ClientEvents.currentView + 1 > 3) ClientEvents.currentView = 0;
                else ClientEvents.currentView++;
            }
        });
    }
}
