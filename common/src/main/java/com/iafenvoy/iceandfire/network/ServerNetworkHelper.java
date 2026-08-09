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
            if (player != null) {
                Entity entity = player.level().getEntity(payload.dragonId());
                if (ServerEvents.isRidingOrBeingRiddenBy(entity, player)) {
                    BlockPos pos = payload.pos();
                        /*
                            For some of these entities the `setPos` is handled in `Entity#move`
                            Doing it here would cause server-side movement checks to fail (resulting in "moved wrongly" messages)
                        */
                    switch (entity) {
                        case DragonBaseEntity dragon -> {
                            if (dragon.isOwnedBy(player))
                                dragon.setControlState(payload.controlState());
                        }
                        case HippogryphEntity hippogryph -> {
                            if (hippogryph.isOwnedBy(player))
                                hippogryph.setControlState(payload.controlState());
                        }
                        case HippocampusEntity hippo -> {
                            if (hippo.isOwnedBy(player))
                                hippo.setControlState(payload.controlState());
                            hippo.setPos(pos.getX(), pos.getY(), pos.getZ());
                        }
                        case DeathWormEntity deathWorm -> {
                            deathWorm.setControlState(payload.controlState());
                            deathWorm.setPos(pos.getX(), pos.getY(), pos.getZ());
                        }
                        case AmphithereEntity amphithere -> {
                            if (amphithere.isOwnedBy(player))
                                amphithere.setControlState(payload.controlState());
                            // TODO :: Is this handled by Entity#move due to recent changes?
                            amphithere.setPos(pos.getX(), pos.getY(), pos.getZ());
                        }
                        default -> {
                        }
                    }
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
                            else livingEntity.interact(player, InteractionHand.MAIN_HAND);
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
                        player.attack(livingEntity);
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
                            if (payload.baby()) tamable.startRiding(player, true);
                            else player.startRiding(tamable, true);
                        } else {
                            if (payload.baby()) tamable.stopRiding();
                            else player.stopRiding();
                        }
            }
        });
    }
}
