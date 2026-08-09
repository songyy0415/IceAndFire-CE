package com.iafenvoy.iceandfire.network;

import com.iafenvoy.iceandfire.config.IafClientConfig;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.entity.util.ISyncMount;
import com.iafenvoy.iceandfire.event.ClientEvents;
import com.iafenvoy.iceandfire.item.block.entity.JarBlockEntity;
import com.iafenvoy.iceandfire.item.block.entity.PixieHouseBlockEntity;
import com.iafenvoy.iceandfire.item.block.entity.PodiumBlockEntity;
import com.iafenvoy.iceandfire.network.payload.*;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientNetworkHelper {
    private static CameraType prev = CameraType.FIRST_PERSON;

    public static void registerReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, DragonSetBurnBlockS2CPayload.ID, DragonSetBurnBlockS2CPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player != null) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof DragonBaseEntity dragon) {
                    dragon.setBreathingFire(payload.breathing());
                    dragon.burningTarget = new BlockPos(payload.target());
                }
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, LightningBoltS2CPayload.ID, LightningBoltS2CPayload.CODEC, (payload, ctx) -> ctx.queue(() -> ClientEvents.LIGHTNINGS.addAll(payload.lightnings())));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, StartRidingMobS2CPayload.ID, StartRidingMobS2CPayload.CODEC, (payload, ctx) -> {
            Options options = Minecraft.getInstance().options;
            Player player = ctx.getPlayer();
            if (player != null) {
                Entity entity = player.level().getEntity(payload.dragonId());
                if (entity instanceof ISyncMount && entity instanceof TamableAnimal tamable) {
                    if (tamable.isOwnedBy(player) && tamable.distanceTo(player) < 14) {
                        if (payload.ride()) {
                            if (payload.baby()) tamable.startRiding(player, true, false);
                            else {
                                player.startRiding(tamable, true, false);
                                if (IafClientConfig.INSTANCE.dragonAuto3rdPerson.getValue()) {
                                    prev = options.getCameraType();
                                    options.setCameraType(CameraType.THIRD_PERSON_BACK);
                                }
                            }
                        } else {
                            if (payload.baby()) tamable.stopRiding();
                            else {
                                player.stopRiding();
                                if (IafClientConfig.INSTANCE.dragonAuto3rdPerson.getValue())
                                    options.setCameraType(prev);
                            }
                        }
                    }
                }
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, UpdatePixieHouseS2CPayload.ID, UpdatePixieHouseS2CPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(payload.blockPos());
                if (blockEntity instanceof PixieHouseBlockEntity house) {
                    house.hasPixie = payload.hasPixie();
                    house.pixieType = payload.pixieType();
                } else if (blockEntity instanceof JarBlockEntity jar) {
                    jar.hasPixie = payload.hasPixie();
                    jar.pixieType = payload.pixieType();
                }
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, UpdatePixieJarS2CPayload.ID, UpdatePixieJarS2CPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player != null)
                if (player.level().getBlockEntity(payload.blockPos()) instanceof JarBlockEntity jar)
                    jar.hasProduced = payload.isProducing();
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, UpdatePodiumS2CPayload.ID, UpdatePodiumS2CPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player != null)
                if (player.level().getBlockEntity(payload.blockPos()) instanceof PodiumBlockEntity podium)
                    podium.setItem(0, payload.heldStack());
        });
    }
}
