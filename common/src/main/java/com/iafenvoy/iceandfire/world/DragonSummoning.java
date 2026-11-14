package com.iafenvoy.iceandfire.world;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.registry.IafDataComponents;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side summoning for the summoning crystals. The dragon is looked up in every level; when it is
 * currently unloaded the stored chunk position is force-loaded for a bounded amount of time (and always
 * released again) before retrying.
 */
public final class DragonSummoning {
    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private record Pending(ServerPlayer player, InteractionHand hand, ItemStack crystal, UUID dragonId,
                           ServerLevel source, ChunkPos chunk, ServerLevel destination, BlockPos target,
                           float yaw, int deadline) {
        boolean isValid() {
            return !this.player.hasDisconnected() && this.player.isAlive() && this.player.level() == this.destination
                    && this.player.getItemInHand(this.hand) == this.crystal
                    && this.dragonId.equals(boundDragon(this.crystal));
        }

        void release() {
            this.source.setChunkForced(this.chunk.x(), this.chunk.z(), false);
        }
    }

    private DragonSummoning() {
    }

    public static UUID boundDragon(ItemStack stack) {
        CompoundTag data = stack.get(IafDataComponents.CRYSTAL_DRAGON_DATA.get());
        if (data != null) for (String key : data.keySet()) {
            if (!key.contains("Dragon")) continue;
            CompoundTag dragon = data.getCompound(key).orElse(new CompoundTag());
            UUID id = dragon.read("DragonUUID", UUIDUtil.CODEC).orElse(null);
            if (id != null) return id;
        }
        return null;
    }

    public static void request(ServerPlayer player, InteractionHand hand, BlockPos target) {
        ItemStack crystal = player.getItemInHand(hand);
        UUID id = boundDragon(crystal);
        if (id == null || PENDING.containsKey(id)) return;
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity != null) {
                if (!summon(player, hand, crystal, entity, (ServerLevel) player.level(), target, player.getYRot())) fail(player);
                return;
            }
        }
        if (IafCommonConfig.INSTANCE.dragon.chunkLoadSummonCrystal.getValue()) {
            // Bound concurrent requests per player as well as per dragon.
            if (PENDING.values().stream().anyMatch(p -> p.player == player)) return;
            for (ServerLevel level : server.getAllLevels()) {
                DragonPosWorldData data = DragonPosWorldData.get(level);
                BlockPos pos = data == null ? null : data.getDragonPos(id);
                if (pos == null) continue;
                ChunkPos chunk = ChunkPos.containing(pos);
                level.setChunkForced(chunk.x(), chunk.z(), true);
                PENDING.put(id, new Pending(player, hand, crystal, id, level, chunk,
                        (ServerLevel) player.level(), target.immutable(), player.getYRot(), server.getTickCount() + 200));
                return;
            }
        }
        fail(player);
    }

    /** Called once per server tick; retries pending summons and always releases the forced chunk. */
    public static void tick(MinecraftServer server) {
        for (Iterator<Pending> it = PENDING.values().iterator(); it.hasNext(); ) {
            Pending pending = it.next();
            if (pending.player.level().getServer() != server) continue;
            boolean valid = pending.isValid();
            Entity entity = valid ? pending.source.getEntity(pending.dragonId) : null;
            if (entity == null && valid && server.getTickCount() < pending.deadline) continue;
            try {
                if (valid && (entity == null || !summon(pending.player, pending.hand, pending.crystal, entity,
                        pending.destination, pending.target, pending.yaw))) fail(pending.player);
            } finally {
                pending.release();
                it.remove();
            }
        }
    }

    /** Releases all pending tickets (called on server stop). */
    public static void clear(MinecraftServer server) {
        PENDING.values().removeIf(pending -> {
            if (pending.player.level().getServer() != server) return false;
            pending.release();
            return true;
        });
    }

    private static boolean summon(ServerPlayer player, InteractionHand hand, ItemStack crystal, Entity entity,
                                  ServerLevel destination, BlockPos target, float yaw) {
        if (!(entity instanceof DragonBaseEntity dragon) || !dragon.isAlive() || dragon.isModelDead()
                || !dragon.isBoundToCrystal() || !dragon.isTame()
                || !(dragon.getOwner() instanceof Player owner && player.getUUID().equals(owner.getUUID()))
                || crystal.getItem() != dragon.dragonType.getCrystalItem()
                || !dragon.getUUID().equals(boundDragon(crystal))
                || !destination.getWorldBorder().isWithinBounds(target) || destination.isOutsideBuildHeight(target)) return false;
        Entity moved;
        if (dragon.level() == destination) {
            dragon.teleportTo(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            dragon.setYRot(yaw);
            moved = dragon;
        } else {
            moved = dragon.teleport(new TeleportTransition(destination, Vec3.atCenterOf(target),
                    Vec3.ZERO, yaw, 0, TeleportTransition.DO_NOTHING));
        }
        if (!(moved instanceof DragonBaseEntity summoned)) return false;
        summoned.setCrystalBound(false);
        summoned.setDeltaMovement(Vec3.ZERO);
        MinecraftServer server = player.level().getServer();
        if (server != null) for (ServerLevel level : server.getAllLevels()) {
            DragonPosWorldData data = DragonPosWorldData.get(level);
            if (data != null) data.removeDragon(dragon.getUUID());
        }
        crystal.remove(IafDataComponents.CRYSTAL_DRAGON_DATA.get());
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
        player.playSound(SoundEvents.GLASS_BREAK, 1, 1);
        player.swing(hand, true);
        player.sendSystemMessage(Component.translatable("message.iceandfire.dragonTeleport"));
        return true;
    }

    private static void fail(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.iceandfire.noDragonTeleport"));
    }
}
