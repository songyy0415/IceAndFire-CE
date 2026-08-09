package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

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
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        CustomWalkNodeEvaluator evaluator = new CustomWalkNodeEvaluator();
        this.nodeEvaluator = evaluator;
        return new PathFinder(evaluator, maxVisitedNodes);
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
        this.desiredPos = target;
        return this.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
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
}
