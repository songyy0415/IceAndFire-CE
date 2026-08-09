package com.iafenvoy.iceandfire.impl.neoforge;

import com.iafenvoy.iceandfire.data.component.ChainData;
import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.data.component.PortalData;
import com.iafenvoy.iceandfire.neoforge.IafAttachments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ComponentManagerImpl {
    public static ChainData getChainData(LivingEntity living) {
        return living.getData(IafAttachments.CHAIN_DATA.get());
    }

    public static MiscData getMiscData(LivingEntity living) {
        return living.getData(IafAttachments.MISC_DATA.get());
    }

    public static PortalData getPortalData(PlayerEntity player) {
        return player.getData(IafAttachments.PORTAL_DATA.get());
    }
}
