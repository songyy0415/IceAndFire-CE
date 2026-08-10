package com.iafenvoy.iceandfire.item.block.entity;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.entity.GhostEntity;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import com.iafenvoy.iceandfire.registry.IafEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.concurrent.ThreadLocalRandom;

public class GhostChestBlockEntity extends ChestBlockEntity {
    private boolean generatedGhost = false;

    public GhostChestBlockEntity(BlockPos pos, BlockState state) {
        super(IafBlockEntities.GHOST_CHEST.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        this.generatedGhost = nbt.getBooleanOr("generatedGhost", false);
    }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("generatedGhost", this.generatedGhost);
    }

    @Override
    public void startOpen(ContainerUser user) {
        super.startOpen(user);
        if (!(user instanceof Player player)) return;
        assert this.level != null;
        if ((!this.generatedGhost || IafCommonConfig.INSTANCE.ghost.alwaysSpawnFromChest.getValue()) && this.level.getDifficulty() != Difficulty.PEACEFUL) {
            this.generatedGhost = true;
            GhostEntity ghost = IafEntities.GHOST.get().create(this.level, EntitySpawnReason.SPAWNER);
            assert ghost != null;
            ghost.setPos(this.worldPosition.getX() + 0.5F, this.worldPosition.getY() + 0.5F, this.worldPosition.getZ() + 0.5F); ghost.setYRot(ThreadLocalRandom.current().nextFloat() * 360F); ghost.setXRot(0);
            if (this.level instanceof ServerLevel serverWorld) {
                ghost.finalizeSpawn(serverWorld, this.level.getCurrentDifficultyAt(this.worldPosition), EntitySpawnReason.SPAWNER, null);
                if (!player.isCreative()) ghost.setTarget(player);
                ghost.setPersistenceRequired();
                this.level.addFreshEntity(ghost);
            }
            ghost.setAnimation(GhostEntity.ANIMATION_SCARE);
            ghost.restrictTo(this.worldPosition, 4);
            ghost.setFromChest(true);
        }
    }

    @Override
    protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int p_155336_, int p_155337_) {
        super.signalOpenCount(level, pos, state, p_155336_, p_155337_);
        level.updateNeighborsAt(pos.below(), state.getBlock());
    }
}
