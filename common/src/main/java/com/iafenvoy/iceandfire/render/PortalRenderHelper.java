package com.iafenvoy.iceandfire.render;

import com.iafenvoy.iceandfire.registry.IafBlocks;
import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class PortalRenderHelper {
    private static int TICK = 0;

    public static int getTick() {
        return TICK;
    }

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(client -> {
            Level world = client.level;
            Player player = client.player;
            if (world == null || player == null) return;
            if (world.getBlockState(player.blockPosition()).is(IafBlocks.DREAD_PORTAL.get())) TICK++;
            else if (TICK > 0) TICK--;
        });
    }
}
