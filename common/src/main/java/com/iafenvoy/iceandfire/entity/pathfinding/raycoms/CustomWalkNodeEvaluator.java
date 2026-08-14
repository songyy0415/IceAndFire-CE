package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Walk node evaluator that honours {@link ICustomSizeNavigator} dimensions and {@link IPassabilityNavigator}
 * passability rules (replacing the removed Uranus raycoms node makers).
 */
public class CustomWalkNodeEvaluator extends WalkNodeEvaluator {
    private boolean flying;

    @Override
    public void prepare(PathNavigationRegion level, Mob entity) {
        super.prepare(level, entity);
        if (entity instanceof ICustomSizeNavigator sizeNavigator) {
            // getXZNavSize() returns the half-width (radius, = bbWidth/2). Vanilla
            // NodeEvaluator.prepare uses the FULL width (Mth.floor(bbWidth + 1)). Doubling the
            // radius restores the full XZ footprint so the pathfinder doesn't route a large mob
            // through gaps too narrow for its body (the halved footprint was the spin-in-place
            // root cause: paths through 3-wide gaps a 5.46-wide dragon physically cannot enter).
            this.entityWidth = Mth.floor(sizeNavigator.getXZNavSize() * 2 + 1.0F);
            this.entityDepth = Mth.floor(sizeNavigator.getXZNavSize() * 2 + 1.0F);
            // Full vertical clearance is checked by the inherited WalkNodeEvaluator (matching
            // Uranus's full-height footprint). entityHeight is kept in sync so the inherited
            // getPathTypeWithinMobBB uses the entity's real height rather than a truncated one.
            this.entityHeight = Mth.ceil(sizeNavigator.getYNavSize());
        }
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = context.getBlockState(pos);
        if (this.mob instanceof IPassabilityNavigator passabilityNavigator) {
            if (passabilityNavigator.isBlockExplicitlyPassable(state, pos, this.mob.blockPosition())) {
                return PathType.OPEN;
            }
            if (passabilityNavigator.isBlockExplicitlyNotPassable(state, pos, this.mob.blockPosition())) {
                return PathType.BLOCKED;
            }
        }
        if (this.flying && state.isAir()) {
            return PathType.OPEN;
        }
        return super.getPathType(context, x, y, z);
    }
}
