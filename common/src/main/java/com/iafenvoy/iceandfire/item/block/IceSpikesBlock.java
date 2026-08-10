package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.entity.IceDragonEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IceSpikesBlock extends Block {
    protected static final VoxelShape VOXEL_SHAPE = Block.box(1, 0, 1, 15, 8, 15);

    public IceSpikesBlock() {
        super(Properties.of().mapColor(MapColor.ICE).noOcclusion().dynamicShape().randomTicks().sound(SoundType.GLASS).strength(2.5F).requiresCorrectToolForDrops());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return this.isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    private boolean isValidGround(BlockState blockState, LevelReader worldIn, BlockPos blockpos) {
        return blockState.canOcclude();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    public void stepOn(Level worldIn, BlockPos pos, BlockState pState, Entity entityIn) {
        if (!(entityIn instanceof IceDragonEntity)) {
            entityIn.hurt(worldIn.damageSources().cactus(), 1);
            if (entityIn instanceof LivingEntity livingEntity && entityIn.getDeltaMovement().x != 0 && entityIn.getDeltaMovement().z != 0)
                livingEntity.knockback(0.5F,entityIn.getDeltaMovement().x,entityIn.getDeltaMovement().z,entityIn.level().damageSources().generic(),0);
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

}
