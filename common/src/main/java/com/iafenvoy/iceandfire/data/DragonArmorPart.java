package com.iafenvoy.iceandfire.data;

import java.util.Locale;
import net.minecraft.world.entity.EquipmentSlot;

public enum DragonArmorPart {
    HEAD, NECK, BODY, TAIL;

    public String getId() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static DragonArmorPart fromSlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND, OFFHAND, BODY -> null;
            case FEET -> TAIL;
            case LEGS -> BODY;
            case CHEST -> NECK;
            case HEAD -> HEAD;
        };
    }
}
