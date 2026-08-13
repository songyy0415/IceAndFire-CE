package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

/**
 * Stuck handling for pathing. Re-implemented in-mod from the removed Uranus raycoms
 * {@code PathingStuckHandler}: detects a stuck mob and, depending on stuck level, clears the
 * path, moves away, or teleports ahead along the path ({@code withTeleportSteps}) / teleports to
 * the goal on full stuck ({@code withTeleportOnFullStuck}).
 */
public class PathingStuckHandler {
    /**
     * The distance at which we consider a target arrived.
     */
    private static final double MIN_TARGET_DIST = 3.0D;
    /**
     * Constants related to teleporting.
     */
    private static final int MIN_TP_DELAY = 120 * 20;
    private static final int MIN_DIST_FOR_TP = 10;
    /**
     * Amount of path steps allowed to teleport on stuck, 0 = disabled.
     */
    private int teleportRange = 0;
    /**
     * Max timeout per block to go, default = 5 sec per block.
     */
    private int timePerBlockDistance = 100;
    /**
     * The current stuck level, determines actions taken.
     */
    private int stuckLevel = 0;
    /**
     * Global timeout counter, used to determine when we're completely stuck.
     */
    private int globalTimeout = 0;
    /**
     * The previously desired go-to position of the entity.
     */
    private BlockPos prevDestination = BlockPos.ZERO;
    /**
     * Whether teleport to goal at full stuck is enabled.
     */
    private boolean canTeleportGoal = false;
    /**
     * Temporary comparison variables to compare with last update.
     */
    private boolean hadPath = false;
    private int lastPathIndex = -1;
    private int progressedNodes = 0;
    /**
     * Delay before taking unstuck actions in ticks, default 60 seconds.
     */
    private int delayBeforeActions = 60 * 20;
    private int delayToNextUnstuckAction = this.delayBeforeActions;
    /**
     * Whether the stuck handler may break blocks blocking the way ahead (stuck levels 3-8 and the
     * full-stuck action). Gated by the MOB_GRIEFING game rule, like the dragon's own digging.
     */
    private boolean canBreakBlocks = true;
    /**
     * How many blocks ahead to break on full stuck (0 = disabled).
     */
    private int completeStuckBlockBreakRange = 3;

    private PathingStuckHandler() {
    }

    public static PathingStuckHandler createStuckHandler() {
        return new PathingStuckHandler();
    }

    public PathingStuckHandler withTeleportSteps(int range) {
        this.teleportRange = range;
        return this;
    }

    public PathingStuckHandler withTeleportOnFullStuck() {
        this.canTeleportGoal = true;
        return this;
    }

    public PathingStuckHandler withBlockBreaking(int completeStuckBlockBreakRange) {
        this.canBreakBlocks = completeStuckBlockBreakRange > 0;
        this.completeStuckBlockBreakRange = completeStuckBlockBreakRange;
        return this;
    }

    public void checkStuck(AdvancedPathNavigate navigator) {
        BlockPos desiredPos = navigator.getDesiredPos();
        if (desiredPos == null || desiredPos.equals(BlockPos.ZERO)) {
            return;
        }

        Mob mob = navigator.getOurEntity();
        double distanceToGoal = mob.distanceToSqr(desiredPos.getX() + 0.5, desiredPos.getY() + 0.5, desiredPos.getZ() + 0.5);

        // Close enough to be considered at the goal.
        if (distanceToGoal < MIN_TARGET_DIST * MIN_TARGET_DIST) {
            this.resetGlobalStuckTimers();
            return;
        }

        // Global timeout check.
        if (this.prevDestination.equals(desiredPos)) {
            this.globalTimeout++;
            if (this.globalTimeout > Math.max(MIN_TP_DELAY, this.timePerBlockDistance * Math.max(MIN_DIST_FOR_TP, Math.sqrt(distanceToGoal)))) {
                this.completeStuckAction(navigator);
                return;
            }
        } else {
            this.resetGlobalStuckTimers();
        }
        this.prevDestination = desiredPos;

        Path path = navigator.getPath();
        if (path == null || path.isDone()) {
            // With no path reset the last path index point to -1.
            this.lastPathIndex = -1;
            this.progressedNodes = 0;
            // Stuck when we have no path and had no path last update before.
            if (!this.hadPath) {
                this.tryUnstuck(navigator);
            }
        } else {
            if (path.getNextNodeIndex() == this.lastPathIndex) {
                // Stuck when we have a path, but are not progressing on it.
                this.tryUnstuck(navigator);
            } else if (this.lastPathIndex != -1) {
                this.progressedNodes = path.getNextNodeIndex() > this.lastPathIndex ? this.progressedNodes + 1 : this.progressedNodes - 1;
                // Not stuck when progressing.
                if (this.progressedNodes > 5) {
                    this.resetStuckTimers();
                }
            }
        }

        this.lastPathIndex = path != null ? path.getNextNodeIndex() : -1;
        this.hadPath = path != null && !path.isDone();
    }

    private void resetGlobalStuckTimers() {
        this.globalTimeout = 0;
        this.prevDestination = BlockPos.ZERO;
        this.resetStuckTimers();
    }

    private void resetStuckTimers() {
        this.stuckLevel = 0;
        this.delayToNextUnstuckAction = this.delayBeforeActions;
        // Reset the path-progress baseline too (Uranus does this). Leaving lastPathIndex stale
        // makes the very next checkStuck see getNextNodeIndex() == lastPathIndex and re-fire a
        // spurious stuck action.
        this.lastPathIndex = -1;
        this.progressedNodes = 0;
    }

    /**
     * Final action when completely stuck before resetting stuck handler and path.
     */
    private void completeStuckAction(AdvancedPathNavigate navigator) {
        Mob mob = navigator.getOurEntity();
        if (this.canTeleportGoal) {
            BlockPos tpPos = findTeleportPos(mob.level(), navigator.getDesiredPos());
            if (tpPos != null) {
                mob.teleportTo(tpPos.getX() + 0.5, tpPos.getY(), tpPos.getZ() + 0.5);
            }
        }
        if (this.completeStuckBlockBreakRange > 0 && isGriefingAllowed(mob)) {
            BlockPos desired = navigator.getDesiredPos();
            if (desired != null) {
                Direction facing = getFacing(mob.blockPosition(), desired);
                if (facing.getAxis().isHorizontal()) {
                    for (int i = 1; i <= this.completeStuckBlockBreakRange; i++) {
                        BlockPos ahead = mob.blockPosition().relative(facing, i);
                        if (!mob.level().getBlockState(ahead).isAir() || !mob.level().getBlockState(ahead.above()).isAir()) {
                            mob.level().destroyBlock(ahead, false);
                            mob.level().destroyBlock(ahead.above(), false);
                            break;
                        }
                    }
                }
            }
        }
        navigator.stop();
        this.resetGlobalStuckTimers();
    }

    /**
     * Tries unstuck options depending on the level.
     */
    private void tryUnstuck(AdvancedPathNavigate navigator) {
        if (this.delayToNextUnstuckAction-- > 0) {
            return;
        }
        this.delayToNextUnstuckAction = 50;

        // Clear path.
        if (this.stuckLevel == 0) {
            this.stuckLevel++;
            this.delayToNextUnstuckAction = 100;
            navigator.stop();
            return;
        }

        // Move away.
        if (this.stuckLevel == 1) {
            this.stuckLevel++;
            this.delayToNextUnstuckAction = 200;
            navigator.stop();
            navigator.moveAwayFromXYZ(navigator.getOurEntity().blockPosition(), 10, 1.0F);
            return;
        }

        // Skip ahead (teleport steps).
        if (this.stuckLevel == 2 && this.teleportRange > 0 && this.hadPath) {
            Path path = navigator.getPath();
            if (path != null) {
                int index = Math.min(path.getNextNodeIndex() + this.teleportRange, path.getNodeCount() - 1);
                Node node = path.getNode(index);
                if (node != null) {
                    navigator.getOurEntity().teleportTo(node.x + 0.5, node.y, node.z + 0.5);
                    this.delayToNextUnstuckAction = 300;
                }
            }
        }

        // Break blocks blocking the way ahead (levels 3-8) — matches Uranus's block-breaking
        // unstuck levels. Teleporting didn't free us, so chew through the obstruction.
        if (this.stuckLevel >= 3 && this.stuckLevel <= 8 && this.canBreakBlocks && isGriefingAllowed(navigator.getOurEntity())) {
            this.delayToNextUnstuckAction = 100;
            this.breakBlocksAhead(navigator);
        }

        this.chanceStuckLevel(navigator);

        if (this.stuckLevel >= 9) {
            this.completeStuckAction(navigator);
            this.resetStuckTimers();
        }
    }

    /**
     * Breaks a few blocks ahead of the mob, toward the desired position, to free it from an
     * obstruction. Mirrors Uranus's {@code breakBlocksAhead} unstuck action.
     */
    private void breakBlocksAhead(AdvancedPathNavigate navigator) {
        Mob mob = navigator.getOurEntity();
        BlockPos target = navigator.getDesiredPos();
        if (target == null) {
            return;
        }
        BlockPos mobPos = mob.blockPosition();
        Direction facing = getFacing(mobPos, target);
        if (!facing.getAxis().isHorizontal()) {
            return;
        }
        for (int i = 1; i <= 3; i++) {
            BlockPos ahead = mobPos.relative(facing, i);
            if (!mob.level().getBlockState(ahead).isAir()) {
                mob.level().destroyBlock(ahead, false);
            }
            BlockPos above = ahead.above();
            if (!mob.level().getBlockState(above).isAir()) {
                mob.level().destroyBlock(above, false);
            }
        }
    }

    /**
     * Returns the dominant horizontal direction from {@code from} toward {@code to}.
     */
    private static Direction getFacing(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (dz != 0) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return Direction.NORTH;
    }

    /**
     * Random chance to decrease to a previous level of stuck, matching Uranus.
     */
    private void chanceStuckLevel(AdvancedPathNavigate navigator) {
        this.stuckLevel++;
        // 20% chance to decrease to the previous level again.
        if (this.stuckLevel > 1 && navigator.getOurEntity().getRandom().nextInt(6) == 0) {
            this.stuckLevel -= 2;
        }
    }

    /**
     * Block-breaking is gated by the MOB_GRIEFING game rule (server-side only), matching the
     * dragon's own digging so the stuck handler never destroys blocks in non-griefing worlds.
     */
    private static boolean isGriefingAllowed(Mob mob) {
        return mob.level() instanceof ServerLevel serverLevel
            && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);
    }

    private static BlockPos findTeleportPos(Level world, BlockPos start) {
        for (int y = 0; y <= 10; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = start.offset(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isAir() && world.getBlockState(pos.below()).isSolid()) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
