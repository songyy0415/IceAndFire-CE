package com.iafenvoy.iceandfire.entity;
import com.iafenvoy.iceandfire.util.IafEntityDataSerializers;
import net.minecraft.server.level.ServerLevel;

import com.iafenvoy.iceandfire.entity.util.IDreadMob;
import com.iafenvoy.iceandfire.entity.util.IHumanoid;
import com.iafenvoy.iceandfire.registry.IafEntities;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import java.util.Optional;
import java.util.UUID;

public class DreadMobEntity extends Monster implements IDreadMob {
    protected static final EntityDataAccessor<Optional<UUID>> COMMANDER_UNIQUE_ID = SynchedEntityData.defineId(DreadMobEntity.class, IafEntityDataSerializers.OPTIONAL_UUID);

    public DreadMobEntity(EntityType<? extends Monster> t, Level worldIn) {
        super(t, worldIn);
    }

    public static Entity necromancyEntity(LivingEntity entity) {
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(EntityTypeTags.ARTHROPOD)) {
            DreadScuttlerEntity lichSummoned = new DreadScuttlerEntity(IafEntities.DREAD_SCUTTLER.get(), entity.level());
            float readInScale = (entity.getBbWidth() / 1.5F);
            if (entity.level() instanceof ServerLevelAccessor serverWorldAccess)
                lichSummoned.finalizeSpawn(serverWorldAccess, ((ServerLevel) entity.level()).getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            lichSummoned.setSize(readInScale);
            return lichSummoned;
        }
        if (entity instanceof Zombie || entity instanceof IHumanoid) {
            DreadGhoulEntity lichSummoned = new DreadGhoulEntity(IafEntities.DREAD_GHOUL.get(), entity.level());
            float readInScale = (entity.getBbWidth() / 0.6F);
            if (entity.level() instanceof ServerLevelAccessor serverWorldAccess)
                lichSummoned.finalizeSpawn(serverWorldAccess, ((ServerLevel) entity.level()).getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            lichSummoned.setSize(readInScale);
            return lichSummoned;
        }
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(EntityTypeTags.UNDEAD) || entity instanceof AbstractSkeleton || entity instanceof Player) {
            DreadThrallEntity lichSummoned = new DreadThrallEntity(IafEntities.DREAD_THRALL.get(), entity.level());
            if (entity.level() instanceof ServerLevelAccessor serverWorldAccess) {
                lichSummoned.finalizeSpawn(serverWorldAccess, ((ServerLevel) entity.level()).getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            }
            lichSummoned.setCustomArmorHead(false);
            lichSummoned.setCustomArmorChest(false);
            lichSummoned.setCustomArmorLegs(false);
            lichSummoned.setCustomArmorFeet(false);
            for (EquipmentSlot slot : EquipmentSlot.values())
                lichSummoned.setItemSlot(slot, entity.getItemBySlot(slot));
            return lichSummoned;
        }
        if (entity instanceof AbstractHorse)
            return new DreadHorseEntity(IafEntities.DREAD_HORSE.get(), entity.level());
        if (entity instanceof Animal) {
            DreadBeastEntity lichSummoned = new DreadBeastEntity(IafEntities.DREAD_BEAST.get(), entity.level());
            float readInScale = (entity.getBbWidth() / 1.2F);
            if (entity.level() instanceof ServerLevelAccessor serverWorldAccess)
                lichSummoned.finalizeSpawn(serverWorldAccess, ((ServerLevel) entity.level()).getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            lichSummoned.setSize(readInScale);
            return lichSummoned;
        }
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COMMANDER_UNIQUE_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        if (this.getCommanderId() != null) {
            compound.putIntArray("CommanderUUID", UUIDUtil.uuidToIntArray(this.getCommanderId()));
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        UUID uuid;
        if (compound.read("CommanderUUID", UUIDUtil.CODEC).isPresent()) {
            uuid = compound.read("CommanderUUID", UUIDUtil.CODEC).orElse(null);
        } else {
            String s = compound.getString("CommanderUUID").orElse("");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.level().getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setCommanderId(uuid);
            } catch (Throwable ignored) {
            }
        }

    }


    @Override
    @Override
    public boolean considersEntityAsAlly(Entity entityIn) {
        return entityIn instanceof IDreadMob || super.isAlliedTo(entityIn);
    }

    public UUID getCommanderId() {
        return this.entityData.get(COMMANDER_UNIQUE_ID).orElse(null);
    }

    public void setCommanderId(UUID uuid) {
        this.entityData.set(COMMANDER_UNIQUE_ID, Optional.ofNullable(uuid));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.getCommander() instanceof DreadLichEntity lich)
            if (lich.getTarget() != null && lich.getTarget().isAlive())
                this.setTarget(lich.getTarget());
    }

    @Override
    public Entity getCommander() {
        try {
            UUID uuid = this.getCommanderId();
            LivingEntity player = uuid == null ? null : this.level().getPlayerByUUID(uuid);
            if (player != null) return player;
            else {
                if (!this.level().isClientSide()) {
                    Entity entity = this.level().getServer().getLevel(this.level().dimension()).getEntity(uuid);
                    if (entity instanceof LivingEntity) {
                        return entity;
                    }
                }
            }
        } catch (IllegalArgumentException var2) {
            return null;
        }
        return null;
    }

    public void onKillEntity(LivingEntity LivingEntityIn) {
        Entity commander = this instanceof DreadLichEntity ? this : this.getCommander();
        if (commander != null && !(LivingEntityIn instanceof DragonBaseEntity)) {// zombie dragons!!!!
            Entity summoned = necromancyEntity(LivingEntityIn);
            if (summoned != null) {
                summoned.copyPosition(LivingEntityIn);
                if (!this.level().isClientSide())
                    this.level().addFreshEntity(summoned);
                if (commander instanceof DreadLichEntity lich)
                    lich.setMinionCount(lich.getMinionCount() + 1);
                if (summoned instanceof DreadMobEntity mob)
                    mob.setCommanderId(commander.getUUID());
            }
        }

    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.isRemoved() && this.getCommander() != null && this.getCommander() instanceof DreadLichEntity lich)
            lich.setMinionCount(lich.getMinionCount() - 1);
        super.remove(reason);
    }
}
