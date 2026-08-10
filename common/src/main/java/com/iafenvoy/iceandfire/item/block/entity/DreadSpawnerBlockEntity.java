package com.iafenvoy.iceandfire.item.block.entity;

import com.iafenvoy.iceandfire.entity.util.DreadSpawnerBaseLogic;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DreadSpawnerBlockEntity extends BlockEntity implements Spawner {
    private final DreadSpawnerBaseLogic spawner = new DreadSpawnerBaseLogic() {
        @Override
        public void broadcastEvent(Level world, BlockPos pos, int status) {
            world.blockEvent(pos, Blocks.SPAWNER, status, 0);
        }

        @Override
        public void setNextSpawnData(Level world, BlockPos pos, SpawnData spawnEntry) {
            super.setNextSpawnData(world, pos, spawnEntry);
            if (world != null) {
                BlockState blockstate = world.getBlockState(pos);
                world.sendBlockUpdated(pos, blockstate, blockstate, 4);
            }
        }
    };

    public DreadSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(IafBlockEntities.DREAD_SPAWNER.get(), pos, state);
    }

    @Override
    public void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        this.spawner.load(this.level, this.worldPosition, nbt);
    }

    public void save(ValueOutput nbt) {
        super.saveAdditional(nbt);
        this.spawner.save(nbt);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag compoundtag = this.saveCustomOnly(registryLookup);
        compoundtag.remove("SpawnPotentials");
        return compoundtag;
    }

    @Override
    public boolean triggerEvent(int p_59797_, int p_59798_) {
        return this.spawner.onEventTriggered(this.level, p_59797_) || super.triggerEvent(p_59797_, p_59798_);
    }

    public BaseSpawner getLogic() {
        return this.spawner;
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, DreadSpawnerBlockEntity blockEntity) {
        blockEntity.spawner.clientTick(world, pos);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, DreadSpawnerBlockEntity blockEntity) {
        blockEntity.spawner.serverTick((ServerLevel) world, pos);
    }

    @Override
    public void setEntityId(EntityType<?> type, RandomSource random) {
        this.spawner.setEntityId(type, this.level, random, this.worldPosition);
        this.setChanged();
    }
}