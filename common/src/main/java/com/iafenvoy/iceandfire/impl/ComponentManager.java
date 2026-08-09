package com.iafenvoy.iceandfire.impl;

import com.iafenvoy.iceandfire.data.component.ChainData;
import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.data.component.PortalData;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ComponentManager {
    @ExpectPlatform
    public static ChainData getChainData(LivingEntity living) {
        throw new AssertionError("This method should be replaced by Architectury.");
    }

    @ExpectPlatform
    public static MiscData getMiscData(LivingEntity living) {
        throw new AssertionError("This method should be replaced by Architectury.");
    }

    @ExpectPlatform
    public static PortalData getPortalData(Player player) {
        throw new AssertionError("This method should be replaced by Architectury.");
    }
}
