package com.iafenvoy.iceandfire.entity.pathfinding.collision;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * Re-implemented in-mod from the removed Uranus raycoms {@code CustomCollisionsNavigator}: a ground
 * navigation whose node evaluator lets the entity path through blocks it can pass through
 * ({@link ICustomCollisions#canPassThrough}).
 */
public class CustomCollisionsNavigator extends GroundPathNavigation {
    public CustomCollisionsNavigator(Mob mob, Level world) {
        super(mob, world);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new CustomCollisionsNodeProcessor();
        this.nodeEvaluator.setCanOpenDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
