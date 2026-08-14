package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Advanced path navigation re-implemented in-mod from the removed Uranus raycoms
 * {@code AdvancedPathNavigate}. Backed by vanilla {@link GroundPathNavigation} + a shared
 * {@link CustomWalkNodeEvaluator} that honours {@link ICustomSizeNavigator} / {@link IPassabilityNavigator}
 * and the requested {@link MovementType}, plus a {@link PathingStuckHandler}.
 *
 * <p>Path computation is dispatched to a background executor (like Uranus's async
 * {@code Pathfinding} executor) and applied on the server thread in {@link #tick()}. Each job uses
 * a fresh {@link PathFinder}/{@link CustomWalkNodeEvaluator} so no mutable evaluator state is shared
 * across threads; the job only reads the (effectively immutable) mob attributes and a position
 * snapshot.</p>
 */
public class AdvancedPathNavigate extends GroundPathNavigation {
    public enum MovementType {
        WALKING, FLYING, CLIMBING
    }

    private static final int MIN_PATH_NODES = 5000;
    private static final ExecutorService PATH_EXECUTOR = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "IceAndFire-Pathfinding");
        thread.setDaemon(true);
        return thread;
    });

    private final Mob ourEntity;
    private MovementType movementType = MovementType.WALKING;
    private PathingStuckHandler stuckHandler;
    private BlockPos desiredPos;
    private final float width;
    private final float height;

    private Future<Path> pendingPath;
    private double pendingSpeed;

    public AdvancedPathNavigate(Mob mob, Level world, MovementType type, float xzSize, float yzSize) {
        this(mob, world, type, xzSize, yzSize, PathingStuckHandler.createStuckHandler().withTeleportSteps(6).withTeleportOnFullStuck());
    }

    public AdvancedPathNavigate(Mob mob, Level world, MovementType type, float xzSize, float yzSize, PathingStuckHandler stuckHandler) {
        super(mob, world);
        this.ourEntity = mob;
        this.movementType = type;
        this.width = xzSize;
        this.height = yzSize;
        this.stuckHandler = stuckHandler;
        // createPathFinder ran during super() with the default WALKING type; re-point the shared
        // evaluator instance to the requested movement type (it is only used for passability reads
        // by the move helpers, not for path search itself).
        if (this.nodeEvaluator instanceof CustomWalkNodeEvaluator customEvaluator) {
            customEvaluator.setFlying(type == MovementType.FLYING);
            customEvaluator.setCanOpenDoors(true);
        }
        if (type == MovementType.FLYING) {
            // Don't snap high-altitude targets down to the surface (GroundPathNavigation.createPath
            // does that via findSurfacePosition unless this flag is set). A flying dragon targets
            // points in the air (roost spots, flight targets), so keep the raw target.
            this.setCanPathToTargetsBelowSurface(true);
        }
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        CustomWalkNodeEvaluator evaluator = new CustomWalkNodeEvaluator();
        this.nodeEvaluator = evaluator;
        return new PathFinder(evaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        // GroundPathNavigation gates path computation on being grounded/in water (onGround() ||
        // isInWater() || isPassenger()), which a flying dragon is not — so air pathing always
        // returned null. The removed Uranus raycoms navigation did not gate on groundedness;
        // restore that for the FLYING movement type (the CustomWalkNodeEvaluator's flying flag
        // already treats air blocks as OPEN).
        return this.movementType == MovementType.FLYING || super.canUpdatePath();
    }

    @Override
    protected double getGroundY(Vec3 target) {
        // A flying dragon's wanted position is at its own altitude — don't clamp it down to the
        // ground (PathNavigation.getGroundY returns the floor when the block below the target is
        // solid). The flight move helper steers via its own 3D flight target, but keep the wanted
        // position consistent for anything that reads it.
        return this.movementType == MovementType.FLYING ? target.y : super.getGroundY(target);
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            return;
        }

        // Vanilla followThePath() measures "close enough to the next node" against the raw block
        // centre with maxDistanceToWaypoint = bbWidth / 2. For a large mob that radius equals its
        // body radius, so the waypoint it steers toward sits inside its own body; the slow-turning
        // GroundMoveHelper then chases that near point and spins in place. Uranus's
        // continueFollowingPath() instead used a full-body-width reach distance measured against the
        // width-adjusted node position and looked ahead several nodes, keeping the steering target
        // clearly ahead of the body. Replicate that here for both ground and flying movement types.
        double reach = Math.max(1.2D, this.mob.getBbWidth());
        int maxDropHeight = 3;
        int startIndex = this.path.getNextNodeIndex();
        int endIndex = Math.min(this.path.getNodeCount(), startIndex + 4);

        for (int i = startIndex; i < endIndex; i++) {
            Vec3 next = this.path.getEntityPosAtNode(this.mob, i);
            double xDiff = Math.abs(this.mob.getX() - next.x);
            double zDiff = Math.abs(this.mob.getZ() - next.z);
            double yDiff = Math.abs(this.mob.getY() - next.y);
            double xzReach = reach - yDiff * 0.1D;
            boolean closeHorizontally = xDiff < xzReach && zDiff < xzReach;
            boolean closeVertically = yDiff <= Math.min(1.0F, Math.ceil(this.mob.getBbHeight() / 2.0F))
                    || yDiff <= Math.ceil(this.mob.getBbWidth() / 2.0F) * maxDropHeight;
            if (closeHorizontally && closeVertically) {
                this.path.advance();
            }
        }
    }

    @Override
    protected void doStuckDetection(Vec3 mobPos) {
        // Vanilla's built-in stuck detection (100-tick stop + path timeout) races the in-mod
        // PathingStuckHandler (both run: super.tick() then checkStuck()). Uranus's navigation had
        // no vanilla stuck detection. Disable it so the in-mod handler is the single owner —
        // otherwise the path is stopped/recalculated every 100 ticks before the handler's 1200-tick
        // escalation can act (churn; worst for flying dragons holding a path while flying to their
        // own flight target).
    }

    // --- Async path computation -------------------------------------------------------------

    private void submitPathJob(Set<BlockPos> targets, int radiusOffset, boolean above, double speed) {
        this.pendingSpeed = speed;
        if (this.pendingPath != null && !this.pendingPath.isDone()) {
            return; // a path is already being computed; ignore redundant requests
        }
        if (!this.canUpdatePath()) {
            return; // can't path right now (e.g. an airborne ground mob)
        }
        final Mob mob = this.mob;
        if (mob.getY() < this.level.getMinY()) {
            return;
        }
        final float maxPathLength = Math.max((float) mob.getAttributeValue(Attributes.FOLLOW_RANGE), 16.0F);
        BlockPos fromPos = above ? mob.blockPosition().above() : mob.blockPosition();
        int radius = (int) (maxPathLength + radiusOffset);
        // Build the chunk snapshot on the server thread: PathNavigationRegion reads the chunk cache
        // via ChunkSource.getChunkNow(), which is not safe to touch from a worker thread. Only the
        // expensive A* itself runs on the executor.
        final PathNavigationRegion region = new PathNavigationRegion(this.level, fromPos.offset(-radius, -radius, -radius), fromPos.offset(radius, radius, radius));
        final boolean flying = this.movementType == MovementType.FLYING;
        this.pendingPath = PATH_EXECUTOR.submit(() -> computePath(mob, targets, maxPathLength, flying, region));
    }

    private static Path computePath(Mob mob, Set<BlockPos> targets, float maxPathLength, boolean flying, PathNavigationRegion region) {
        if (targets.isEmpty()) {
            return null;
        }
        CustomWalkNodeEvaluator evaluator = new CustomWalkNodeEvaluator();
        evaluator.setFlying(flying);
        evaluator.setCanOpenDoors(true);
        evaluator.setCanFloat(true);
        int maxVisitedNodes = Math.max(Mth.floor(maxPathLength * 16.0F), MIN_PATH_NODES);
        PathFinder finder = new PathFinder(evaluator, maxVisitedNodes);
        return finder.findPath(region, mob, targets, maxPathLength, 1, 1.0F);
    }

    private void pollPendingPath() {
        Future<Path> pending = this.pendingPath;
        if (pending == null || !pending.isDone()) {
            return;
        }
        Path path = null;
        try {
            path = pending.get();
        } catch (Exception ignored) {
        }
        this.pendingPath = null;
        if (path != null) {
            super.moveTo(path, this.pendingSpeed);
        }
    }

    private boolean isFollowingPath() {
        return this.pendingPath != null || (this.path != null && !this.path.isDone());
    }

    @Override
    public boolean isDone() {
        return this.pendingPath == null && super.isDone();
    }

    // --- Move entry points (async) ----------------------------------------------------------

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        BlockPos target = BlockPos.containing(x, y, z);
        if (!target.equals(this.desiredPos) || !this.isFollowingPath()) {
            this.desiredPos = target;
            this.submitPathJob(Set.of(target), 8, false, speed);
        }
        return true;
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        BlockPos target = entity.blockPosition();
        if (!target.equals(this.desiredPos) || !this.isFollowingPath()) {
            this.desiredPos = target;
            this.submitPathJob(Set.of(target), 16, true, speed);
        }
        return true;
    }

    public boolean moveToLivingEntity(LivingEntity entity, double speed) {
        return this.moveTo((Entity) entity, speed);
    }

    public BlockPos getDesiredPos() {
        return this.desiredPos;
    }

    public Mob getOurEntity() {
        return this.ourEntity;
    }

    public PathingStuckHandler getStuckHandler() {
        return this.stuckHandler;
    }

    public void setStuckHandler(PathingStuckHandler handler) {
        this.stuckHandler = handler;
    }

    public boolean moveAwayFromXYZ(BlockPos currentPosition, double range, double speed) {
        int radius = Math.max(1, (int) range);
        BlockPos target = currentPosition.offset(this.mob.getRandom().nextInt(2 * radius) - radius, 0, this.mob.getRandom().nextInt(2 * radius) - radius);
        // Move-away is an escape, not a destination the dragon is actually trying to reach:
        // leave desiredPos unchanged (Uranus passed safeDestination=false here). Otherwise every
        // move-away resets the stuck handler's escalation, so completeStuckAction's teleport-to-goal
        // at stuckLevel >= 9 never fires and the dragon loops stuck -> move-away -> stuck forever.
        this.submitPathJob(Set.of(target), 8, false, speed);
        return true;
    }

    @Override
    public void tick() {
        this.pollPendingPath();
        if (this.path != null) {
            super.tick();
        } else {
            // No path has been applied yet (still computing async, or none found). Vanilla's
            // PathNavigation.tick() dereferences this.path whenever isDone() returns false, but our
            // isDone() override reports false while a path is being computed (to keep goals alive),
            // so skip the follow logic and just advance the tick counter until a path exists.
            this.tick++;
            if (this.hasDelayedRecomputation) {
                this.recomputePath();
            }
        }
        if (this.stuckHandler != null) {
            this.stuckHandler.checkStuck(this);
        }
    }

    @Override
    public void stop() {
        if (this.pendingPath != null) {
            this.pendingPath.cancel(true);
        }
        this.pendingPath = null;
        super.stop();
    }

    @Override
    public void recomputePath() {
        this.hasDelayedRecomputation = false;
        if (this.desiredPos != null && !this.isFollowingPath()) {
            this.submitPathJob(Set.of(this.desiredPos), 8, false, this.speedModifier);
        }
    }
}
