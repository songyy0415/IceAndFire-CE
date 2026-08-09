package com.iafenvoy.iceandfire.fabric.compat.trinkets;

import com.iafenvoy.iceandfire.registry.IafItems;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.world.item.Item;

public class TrinketsRegistry {
    public static void registerItems() {
        registerSingle(IafItems.HYDRA_HEART.get());
    }

    private static void registerSingle(Item item) {
        TrinketCallback.setCallback(item, new SimpleTickItemWrapper(item));
    }
}
