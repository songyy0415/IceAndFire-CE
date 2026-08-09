package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

        this.stuckLevel++;
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
