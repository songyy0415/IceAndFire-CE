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
            this.entityWidth = Mth.floor(sizeNavigator.getXZNavSize() + 1.0F);
            this.entityDepth = Mth.floor(sizeNavigator.getXZNavSize() + 1.0F);
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
