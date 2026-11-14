package com.iafenvoy.iceandfire.network;

import com.iafenvoy.iceandfire.entity.*;
import com.iafenvoy.iceandfire.entity.util.ISyncMount;
import com.iafenvoy.iceandfire.event.ServerEvents;
import com.iafenvoy.iceandfire.network.payload.*;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;

public class ServerNetworkHelper {
    public static void registerReceivers() {
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(DragonSetBurnBlockS2CPayload.ID, DragonSetBurnBlockS2CPayload.CODEC);
            NetworkManager.registerS2CPayloadType(LightningBoltS2CPayload.ID, LightningBoltS2CPayload.CODEC);
            NetworkManager.registerS2CPayloadType(StartRidingMobS2CPayload.ID, StartRidingMobS2CPayload.CODEC);
            NetworkManager.registerS2CPayloadType(UpdatePixieHouseS2CPayload.ID, UpdatePixieHouseS2CPayload.CODEC);
            NetworkManager.registerS2CPayloadType(UpdatePixieJarS2CPayload.ID, UpdatePixieJarS2CPayload.CODEC);
            NetworkManager.registerS2CPayloadType(UpdatePodiumS2CPayload.ID, UpdatePodiumS2CPayload.CODEC);
        }

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, DragonControlC2SPayload.ID, DragonControlC2SPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player == null) return;
            Entity entity = player.level().getEntity(payload.dragonId());
            // Either the player rides the creature (adult), or the creature rides the player
            // (baby dragon on the shoulder).
            if (entity == null || entity.getControllingPassenger() != player && entity.getVehicle() != player) return;
            // The payload's legacy position field is deliberately ignored: movement is server validated.
            switch (entity) {
                case DragonBaseEntity dragon -> {
                    if (dragon.isOwnedBy(player)) {
                        dragon.setControlState(payload.controlState());
                        if (dragon.getVehicle() == player && dragon.isDismounting()) {
                            dragon.stopRiding();
                            dragon.dismount(false);
                        }
                    }
                }
                case HippogryphEntity hippogryph -> {
                    if (hippogryph.isOwnedBy(player))
                        hippogryph.setControlState(payload.controlState());
                }
                case HippocampusEntity hippo -> {
                    if (hippo.isOwnedBy(player))
                        hippo.setControlState(payload.controlState());
                }
                case DeathWormEntity deathWorm -> deathWorm.setControlState(payload.controlState());
                case AmphithereEntity amphithere -> {
                    if (amphithere.isOwnedBy(player))
                        amphithere.setControlState(payload.controlState());
                }
                default -> {
                }
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, MultipartInteractC2SPayload.ID, MultipartInteractC2SPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            ctx.queue(() -> {
                if (player != null && player.level() instanceof ServerLevel serverWorld) {
                    Entity entity = serverWorld.getEntity(payload.creatureID());
                    if (entity instanceof LivingEntity livingEntity) {
                        double dist = player.distanceTo(livingEntity);
                        if (dist < 100) {
                            float dmg = payload.dmg();
                            if (dmg > 0F) livingEntity.hurtOrSimulate(player.level().damageSources().mobAttack(player), dmg);
                            else livingEntity.interact(player, InteractionHand.MAIN_HAND, livingEntity.position());
                        }
                    }
                }
            });
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PlayerHitMultipartC2SPayload.ID, PlayerHitMultipartC2SPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player != null) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof LivingEntity livingEntity) {
                    double dist = player.distanceTo(livingEntity);
                    if (dist < 100) {
                        // Parent damage is applied server-side via the part's hurtServer relay; this
                        // payload only carries the non-damage side effect (hydra head reaction).
                        // The removed player.attack(...) here made the parent take an extra hit per
                        // client swing on top of the relay.
                        if (livingEntity instanceof HydraEntity hydra)
                            hydra.triggerHeadFlags(payload.index());
                    }
                }
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, StartRidingMobC2SPayload.ID, StartRidingMobC2SPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.getPlayer();
            if (player != null) {
                Entity entity = player.level().getEntity(payload.dragonId());
                if (entity instanceof ISyncMount && entity instanceof TamableAnimal tamable)
                    if (tamable.isOwnedBy(player) && tamable.distanceTo(player) < 14)
                        if (payload.ride()) {
                            if (payload.baby()) tamable.startRiding(player, true, false);
                            else player.startRiding(tamable, true, false);
                        } else {
                            if (payload.baby()) tamable.stopRiding();
                            else player.stopRiding();
                        }
            }
        });
    }
}
