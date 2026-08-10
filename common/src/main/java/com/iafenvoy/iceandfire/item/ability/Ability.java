package com.iafenvoy.iceandfire.item.ability;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public interface Ability {
    default boolean isEnable() {
        return true;
    }

    default void addDescription(Consumer<Component> tooltip) {
    }
}
