package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;
import java.util.Set;

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

    @Override
    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
        // Large breaking mobs (dragons, cyclops, ...) don't need full vertical clearance at every
        // path node — they dig when stuck. Cap the checked height so overhead canopies/overhangs
        // stop blocking every node in forests/terrain. This restores the removed Uranus raycoms
        // strip semantics ("mobs that break blocks may consider the ground passable"). The XZ
        // footprint (entityWidth × entityDepth) is still enforced at ground level.
        int checkHeight = Math.min(this.entityHeight, 3);
        EnumSet<PathType> blockTypes = EnumSet.noneOf(PathType.class);
        for (int dx = 0; dx < this.entityWidth; dx++) {
            for (int dy = 0; dy < checkHeight; dy++) {
                for (int dz = 0; dz < this.entityDepth; dz++) {
                    int xx = dx + x;
                    int yy = dy + y;
                    int zz = dz + z;
                    PathType blockType = this.getPathType(context, xx, yy, zz);
                    BlockPos mobPosition = this.mob.blockPosition();
                    boolean canPassDoors = this.canPassDoors();
                    if (blockType == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && canPassDoors) {
                        blockType = PathType.WALKABLE_DOOR;
                    }
                    if (blockType == PathType.DOOR_OPEN && !canPassDoors) {
                        blockType = PathType.BLOCKED;
                    }
                    if (blockType == PathType.RAIL
                        && this.getPathType(context, mobPosition.getX(), mobPosition.getY(), mobPosition.getZ()) != PathType.RAIL
                        && this.getPathType(context, mobPosition.getX(), mobPosition.getY() - 1, mobPosition.getZ()) != PathType.RAIL) {
                        blockType = PathType.UNPASSABLE_RAIL;
                    }
                    blockTypes.add(blockType);
                }
            }
        }
        return blockTypes;
    }
}
