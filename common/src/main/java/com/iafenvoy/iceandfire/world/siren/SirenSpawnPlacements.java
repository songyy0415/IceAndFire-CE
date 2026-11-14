package com.iafenvoy.iceandfire.world.siren;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.entity.SirenEntity;
import com.iafenvoy.iceandfire.world.DangerousGeneration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Flight rework patch (siren beach spawns): the spawn rule the addon registered through NeoForge's
 * {@code RegisterSpawnPlacementsEvent}. Registered from {@code IafEntities.commonSetup()}.
 */
public final class SirenSpawnPlacements {
    private static final int SURFACE_TOLERANCE = 1;
    private static final DangerousGeneration DANGER_RADIUS = new DangerousGeneration() {
    };

    private SirenSpawnPlacements() {
    }

    public static boolean canSirenSpawn(EntityType<SirenEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getLevel().isDarkOutside()
                && DANGER_RADIUS.isFarEnoughFromSpawn(level, pos)
                && isOnSurface(level, pos)
                && isAwayFromPlayers(level, reason, pos)
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean isOnSurface(ServerLevelAccessor level, BlockPos pos) {
        return pos.getY() >= level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() - SURFACE_TOLERANCE;
    }

    private static boolean isAwayFromPlayers(ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos) {
        double minDistance = IafCommonConfig.INSTANCE.siren.beachMinPlayerDistance.getValue();
        if (reason != EntitySpawnReason.NATURAL || minDistance <= 0) return true;
        return !level.getLevel().hasNearbyAlivePlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, minDistance);
    }
}
