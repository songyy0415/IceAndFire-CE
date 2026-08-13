package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

/**
 * Advanced path navigation re-implemented in-mod from the removed Uranus raycoms
 * {@code AdvancedPathNavigate}. Backed by vanilla {@link GroundPathNavigation} + a shared
 * {@link CustomWalkNodeEvaluator} that honours {@link ICustomSizeNavigator} / {@link IPassabilityNavigator}
 * and the requested {@link MovementType}, plus a {@link PathingStuckHandler}.
 */
public class AdvancedPathNavigate extends GroundPathNavigation {
    public enum MovementType {
        WALKING, FLYING, CLIMBING
    }

    private final Mob ourEntity;
    private MovementType movementType = MovementType.WALKING;
    private PathingStuckHandler stuckHandler;
    private BlockPos desiredPos;
    private final float width;
    private final float height;

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
        // evaluator instance (the same one the PathFinder holds) to the requested movement type.
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
        // Uranus allowed up to 5000 path nodes; vanilla budgets FOLLOW_RANGE * 16 (~2048 for a
        // dragon), aborting searches early with a partial path in dense terrain. BoundedPathFinder
        // enforces this floor so later vanilla budget updates cannot shrink it.
        return new BoundedPathFinder(evaluator, Math.max(maxVisitedNodes, 5000));
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
    protected void doStuckDetection(Vec3 mobPos) {
        // Vanilla's built-in stuck detection (100-tick stop + path timeout) races the in-mod
        // PathingStuckHandler (both run: super.tick() then checkStuck()). Uranus's navigation had
        // no vanilla stuck detection. Disable it so the in-mod handler is the single owner —
        // otherwise the path is stopped/recalculated every 100 ticks before the handler's 1200-tick
        // escalation can act (churn; worst for flying dragons holding a path while flying to their
        // own flight target).
    }

    public boolean moveToLivingEntity(LivingEntity entity, double speed) {
        return this.moveTo(entity, speed);
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
        return super.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        this.desiredPos = BlockPos.containing(x, y, z);
        return super.moveTo(x, y, z, speed);
    }

    public boolean moveTo(LivingEntity entity, double speed) {
        this.desiredPos = entity.blockPosition();
        return super.moveTo(entity, speed);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.stuckHandler != null) {
            this.stuckHandler.checkStuck(this);
        }
    }

    /**
     * Path finder that enforces a minimum node budget, so vanilla budget updates
     * ({@code updatePathfinderMaxVisitedNodes()}) cannot shrink it below what large mobs (dragons)
     * need to complete searches in dense terrain.
     */
    private static class BoundedPathFinder extends PathFinder {
        private final int minVisitedNodes;

        BoundedPathFinder(NodeEvaluator evaluator, int minVisitedNodes) {
            super(evaluator, minVisitedNodes);
            this.minVisitedNodes = minVisitedNodes;
        }

        @Override
        public void setMaxVisitedNodes(int maxVisitedNodes) {
            super.setMaxVisitedNodes(Math.max(maxVisitedNodes, this.minVisitedNodes));
        }
    }
}
