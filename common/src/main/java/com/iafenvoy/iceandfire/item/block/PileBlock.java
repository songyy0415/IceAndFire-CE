package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.registry.IafSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PileBlock extends Block {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
    protected static final VoxelShape[] SHAPES = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    public PileBlock(ResourceKey<Block> key) {
        super(Properties.of().mapColor(MapColor.DIRT).strength(0.3F, 1).randomTicks().sound(new SoundType(1.0F, 1.0F, IafSounds.GOLD_PILE_BREAK.get(), IafSounds.GOLD_PILE_STEP.get(), IafSounds.GOLD_PILE_BREAK.get(), IafSounds.GOLD_PILE_STEP.get(), IafSounds.GOLD_PILE_STEP.get())).pushReaction(PushReaction.DESTROY).setId(key));
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type == PathComputationType.LAND && state.getValue(LAYERS) < 5;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS) - 1];
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState blockstate = worldIn.getBlockState(pos.below());
        Block block = blockstate.getBlock();
        if (block != Blocks.ICE && block != Blocks.PACKED_ICE && block != Blocks.BARRIER) {
            if (block != Blocks.HONEY_BLOCK && block != Blocks.SOUL_SAND)
                return Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP) || block instanceof PileBlock && blockstate.getValue(LAYERS) == 8;
            else return true;
        } else return false;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, LevelReader levelReader, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        return !stateIn.canSurvive(levelReader, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, levelReader, tickAccess, currentPos, facing, facingPos, facingState, random);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.getBlock() == this) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(8, i + 1));
        } else return super.getStateForPlacement(context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack item = player.getInventory().getSelectedItem();

        if (!item.isEmpty() && item.getItem() != null && item.getItem() == this.asItem() && state.getValue(LAYERS) < 8) {
            world.setBlock(pos, state.setValue(LAYERS, state.getValue(LAYERS) + 1), 3);
            if (!player.isCreative()) {
                item.shrink(1);
                Inventory inventory = player.getInventory();
                if (item.isEmpty())
                    inventory.setItem(inventory.getSelectedSlot(), ItemStack.EMPTY);
                else
                    inventory.setItem(inventory.getSelectedSlot(), item);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}