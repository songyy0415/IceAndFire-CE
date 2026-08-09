package com.iafenvoy.iceandfire.impl.fabric;

import com.iafenvoy.iceandfire.data.component.ChainData;
import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.data.component.PortalData;
import com.iafenvoy.iceandfire.fabric.IafAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("UnstableApiUsage")
public class ComponentManagerImpl {
    public static ChainData getChainData(LivingEntity living) {
        return living.getAttachedOrCreate(IafAttachments.CHAIN_DATA);
    }

    public static MiscData getMiscData(LivingEntity living) {
        return living.getAttachedOrCreate(IafAttachments.MISC_DATA);
    }

    public static PortalData getPortalData(Player player) {
        return player.getAttachedOrCreate(IafAttachments.PORTAL_DATA);
    }
}
