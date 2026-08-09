package com.iafenvoy.iceandfire.entity.pathfinding.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Re-implemented in-mod from the removed Uranus raycoms {@code ICustomCollisions}: lets an entity
 * declare blocks it can path/collide through (e.g. DeathWorm through sand).
 */
public interface ICustomCollisions {
    boolean canPassThrough(BlockPos pos, BlockState state, VoxelShape shape);
}
