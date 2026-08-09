package com.iafenvoy.iceandfire.entity.pathfinding.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Re-implemented in-mod from the removed Uranus raycoms {@code CustomCollisionsNodeProcessor}: treats
 * blocks the entity can pass through ({@link ICustomCollisions#canPassThrough}) as {@link PathType#OPEN}.
 */
public class CustomCollisionsNodeProcessor extends WalkNodeEvaluator {
    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = context.getBlockState(pos);
        if (this.mob instanceof ICustomCollisions collisions && collisions.canPassThrough(pos, state, null)) {
            return PathType.OPEN;
        }
        return super.getPathType(context, x, y, z);
    }
}
