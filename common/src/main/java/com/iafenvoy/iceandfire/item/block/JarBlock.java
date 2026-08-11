package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.item.block.entity.JarBlockEntity;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.registry.IafSounds;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Locale;

public class JarBlock extends BaseEntityBlock {
    protected static final VoxelShape AABB = Block.box(3, 0, 3, 13, 16, 13);
    private final boolean empty;
    private final int pixieType;

    public JarBlock(ResourceKey<Block> key, int pixieType) {
        super(pixieType != -1 ? Properties.of().mapColor(MapColor.NONE).instrument(NoteBlockInstrument.HAT).noOcclusion().dynamicShape().strength(1, 2).sound(SoundType.GLASS).lightLevel((state) -> 10).setId(key) : Properties.of().mapColor(MapColor.NONE).instrument(NoteBlockInstrument.HAT).noOcclusion().dynamicShape().strength(1, 2).sound(SoundType.GLASS).setId(key));
        this.empty = pixieType == -1;
        this.pixieType = pixieType;
    }

    public static String name(int pixieType) {
        if (pixieType == -1) return "pixie_jar_empty";
        return String.format(Locale.ROOT, "pixie_jar_%d", pixieType);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    // 26.2 removed Block.onRemove — block-break cleanup is now done via the player hooks (see
    // vanilla BeehiveBlock). Survival break goes through ServerPlayerGameMode.destroyBlock, which
    // calls playerWillDestroy (before removal) then playerDestroy (after). Creative break goes
    // through destroyAndAck -> destroyBlock as well, but the block entity passed to playerDestroy
    // is null there, so the release must also happen in playerWillDestroy where the block entity
    // is still retrievable by position. releasePixie clears hasPixie, so both hooks are idempotent.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(level, pos, state, player);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof JarBlockEntity jar && jar.hasPixie)
            jar.releasePixie();
        return result;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide() && blockEntity instanceof JarBlockEntity jar && jar.hasPixie)
            jar.releasePixie();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!this.empty && world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof JarBlockEntity jar && jar.hasPixie && jar.hasProduced) {
            jar.hasProduced = false;
            ItemEntity item = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(IafItems.PIXIE_DUST.get()));
            if (!world.isClientSide())
                world.addFreshEntity(item);
            world.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5, IafSounds.PIXIE_HURT.get(), SoundSource.NEUTRAL, 1, 1, false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (world.getBlockEntity(pos) instanceof JarBlockEntity jar) {
            if (!this.empty) {
                jar.hasPixie = true;
                jar.pixieType = this.pixieType;
            } else
                jar.hasPixie = false;
            jar.setChanged();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, IafBlockEntities.PIXIE_JAR.get(), JarBlockEntity::tick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JarBlockEntity(pos, state, this.empty);
    }

    public int getPixieType() {
        return this.pixieType;
    }

    public boolean isEmpty() {
        return this.empty;
    }
}
