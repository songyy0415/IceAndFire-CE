package com.iafenvoy.iceandfire.item.block;

import com.iafenvoy.iceandfire.data.DragonType;
import com.iafenvoy.iceandfire.item.block.entity.DragonForgeBlockEntity;
import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import com.iafenvoy.iceandfire.registry.IafBlocks;
import com.iafenvoy.iceandfire.util.DragonTypeProvider;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//FIXME::Introduce a base block class for all dragon forge blocks
public class DragonForgeCoreBlock extends BaseEntityBlock implements DragonProof, DragonTypeProvider {
    private static final Map<DragonType, Block> ACTIVATED_MAP = new HashMap<>();
    private final DragonType dragonType;

    public DragonForgeCoreBlock(ResourceKey<Block> key, DragonType dragonType, boolean activated) {
        super(Properties.of().mapColor(MapColor.METAL).dynamicShape().strength(40, 500).sound(SoundType.METAL).lightLevel((state) -> activated ? 15 : 0).setId(key));
        this.dragonType = dragonType;
        if (activated) ACTIVATED_MAP.put(dragonType, this);
    }

    public static String name(DragonType dragonType, boolean activated) {
        return String.format(Locale.ROOT, "dragonforge_%s_core%s", dragonType.name(), activated ? "" : "_disabled");
    }

    public static void setState(DragonType dragonType, Level worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        worldIn.setBlock(pos, ACTIVATED_MAP.getOrDefault(dragonType, IafBlocks.DRAGONFORGE_FIRE_CORE.get()).defaultBlockState(), 3);
        if (blockEntity != null) {
            blockEntity.clearRemoved();
            worldIn.setBlockEntity(blockEntity);
        }
    }

    @Override
    public DragonType getDragonType() {
        return this.dragonType;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof DragonForgeBlockEntity forge)
                MenuRegistry.openExtendedMenu(serverPlayer, forge);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 26.2 removed Block.onRemove — block-break cleanup is now done via the player hooks (see
    // vanilla BeehiveBlock). Survival break goes through ServerPlayerGameMode.destroyBlock, which
    // calls playerWillDestroy (before removal) then playerDestroy (after). Creative break goes
    // through destroyAndAck -> destroyBlock as well, but the block entity passed to playerDestroy
    // is null there, so the drop must also happen in playerWillDestroy where the block entity is
    // still retrievable by position. dropContents drains the inventory, so both hooks are idempotent.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(level, pos, state, player);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DragonForgeBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        return result;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (blockEntity instanceof DragonForgeBlockEntity) {
            Containers.dropContents(level, pos, (DragonForgeBlockEntity) blockEntity);
            level.updateNeighbourForOutputSignal(pos, this);
            level.removeBlockEntity(pos);
        }
    }

    // 26.2: Block.onRemove is gone; block-state changes now call preRemoveSideEffects + removeBlockEntity
    // unless shouldChangedStateKeepBlockEntity returns true (see LevelChunk.setBlockState). setState()
    // swaps between the activated/disabled variants of this same block to assemble/disassemble the
    // forge — without this override that swap would drain the forge's whole inventory. When the block
    // is actually broken (new state = air) the air block's default (false) still applies.
    @Override
    public boolean shouldChangedStateKeepBlockEntity(BlockState oldState) {
        return oldState.getBlock() instanceof DragonForgeCoreBlock;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, IafBlockEntities.DRAGONFORGE_CORE.get(), DragonForgeBlockEntity::tick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DragonForgeBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }
}
