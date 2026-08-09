package com.iafenvoy.iceandfire.entity;

import com.google.common.base.Predicate;
import com.iafenvoy.iceandfire.entity.ai.DreadAITargetNonDreadGoal;
import com.iafenvoy.iceandfire.entity.util.IAnimalFear;
import com.iafenvoy.iceandfire.entity.util.IDreadMob;
import com.iafenvoy.iceandfire.entity.util.IHasArmorVariant;
import com.iafenvoy.iceandfire.entity.util.IVillagerFear;
import com.iafenvoy.iceandfire.entity.util.dragon.DragonUtils;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.AnimationHandler;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DreadThrallEntity extends DreadMobEntity implements IAnimatedEntity, IVillagerFear, IAnimalFear, IHasArmorVariant {
    public static final Animation ANIMATION_SPAWN = Animation.create(40);
    private static final EntityDataAccessor<Boolean> CUSTOM_ARMOR_HEAD = SynchedEntityData.defineId(DreadThrallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CUSTOM_ARMOR_CHEST = SynchedEntityData.defineId(DreadThrallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CUSTOM_ARMOR_LEGS = SynchedEntityData.defineId(DreadThrallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CUSTOM_ARMOR_FEET = SynchedEntityData.defineId(DreadThrallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CUSTOM_ARMOR_INDEX = SynchedEntityData.defineId(DreadThrallEntity.class, EntityDataSerializers.INT);
    private int animationTick;
    private Animation currentAnimation;

    public DreadThrallEntity(EntityType<? extends DreadThrallEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return createMobAttributes()
                //HEALTH
                .add(Attributes.MAX_HEALTH, 20.0D)
                //SPEED
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                //ATTACK
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                //FOLLOW RANGE
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                //ARMOR
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, IDreadMob.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (entity, level) -> DragonUtils.canHostilesTarget(entity)));
        this.targetSelector.addGoal(3, new DreadAITargetNonDreadGoal(this, LivingEntity.class, false, (entity, level) -> entity instanceof LivingEntity && DragonUtils.canHostilesTarget(entity)));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CUSTOM_ARMOR_INDEX, 0);
        builder.define(CUSTOM_ARMOR_HEAD, Boolean.FALSE);
        builder.define(CUSTOM_ARMOR_CHEST, Boolean.FALSE);
        builder.define(CUSTOM_ARMOR_LEGS, Boolean.FALSE);
        builder.define(CUSTOM_ARMOR_FEET, Boolean.FALSE);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getAnimation() == ANIMATION_SPAWN && this.getAnimationTick() < 30) {
            BlockState belowBlock = this.level().getBlockState(this.blockPosition().below());
            if (belowBlock.getBlock() != Blocks.AIR) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, belowBlock), this.getX() + (double) (this.getRandom().nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), this.getBoundingBox().minY, this.getZ() + (double) (this.getRandom().nextFloat() * this.getBbWidth() * 2.0F) - (double) this.getBbWidth(), this.getRandom().nextGaussian() * 0.02D, this.getRandom().nextGaussian() * 0.02D, this.getRandom().nextGaussian() * 0.02D);
                }
            }
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        if (this.getMainHandItem().getItem() == Items.BOW) {
            this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
        }
        AnimationHandler.INSTANCE.updateAnimations(this);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(randomSource, difficulty);
        if (this.getRandom().nextFloat() < 0.75F) {
            double chance = this.getRandom().nextFloat();
            if (chance < 0.0025F) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(IafItems.DRAGONSTEEL_ICE_SWORD.get()));
            }
            if (chance < 0.01F) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            }
            if (chance < 0.1F) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            if (chance < 0.75F) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(IafItems.DREAD_SWORD.get()));
            }
        }
        if (this.getRandom().nextFloat() < 0.75F) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            this.setCustomArmorHead(this.getRandom().nextInt(8) != 0);
        }
        if (this.getRandom().nextFloat() < 0.75F) {
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            this.setCustomArmorChest(this.getRandom().nextInt(8) != 0);
        }
        if (this.getRandom().nextFloat() < 0.75F) {
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            this.setCustomArmorLegs(this.getRandom().nextInt(8) != 0);
        }
        if (this.getRandom().nextFloat() < 0.75F) {
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
            this.setCustomArmorFeet(this.getRandom().nextInt(8) != 0);
        }
        this.setBodyArmorVariant(this.getRandom().nextInt(8));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, EntitySpawnReason reason, SpawnGroupData spawnDataIn) {
        SpawnGroupData data = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
        this.setAnimation(ANIMATION_SPAWN);
        this.populateDefaultEquipmentSlots(worldIn.getRandom(), difficultyIn);
        return data;
    }

    @Override
    public int getAnimationTick() {
        return this.animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        this.animationTick = tick;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("ArmorVariant", this.getBodyArmorVariant());
        compound.putBoolean("HasCustomHelmet", this.hasCustomArmorHead());
        compound.putBoolean("HasCustomChestplate", this.hasCustomArmorChest());
        compound.putBoolean("HasCustomLeggings", this.hasCustomArmorLegs());
        compound.putBoolean("HasCustomBoots", this.hasCustomArmorFeet());
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        this.setBodyArmorVariant(compound.getInt("ArmorVariant").orElse(0));
        this.setCustomArmorHead(compound.getBooleanOr("HasCustomHelmet", false));
        this.setCustomArmorChest(compound.getBooleanOr("HasCustomChestplate", false));
        this.setCustomArmorLegs(compound.getBooleanOr("HasCustomLeggings", false));
        this.setCustomArmorFeet(compound.getBooleanOr("HasCustomBoots", false));
    }

    @Override
    public Animation getAnimation() {
        return this.currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        this.currentAnimation = animation;
    }

    public boolean hasCustomArmorHead() {
        return this.entityData.get(CUSTOM_ARMOR_HEAD);
    }

    public void setCustomArmorHead(boolean head) {
        this.entityData.set(CUSTOM_ARMOR_HEAD, head);
    }

    public boolean hasCustomArmorChest() {
        return this.entityData.get(CUSTOM_ARMOR_CHEST);
    }

    public void setCustomArmorChest(boolean head) {
        this.entityData.set(CUSTOM_ARMOR_CHEST, head);
    }

    public boolean hasCustomArmorLegs() {
        return this.entityData.get(CUSTOM_ARMOR_LEGS);
    }

    public void setCustomArmorLegs(boolean head) {
        this.entityData.set(CUSTOM_ARMOR_LEGS, head);
    }

    public boolean hasCustomArmorFeet() {
        return this.entityData.get(CUSTOM_ARMOR_FEET);
    }

    public void setCustomArmorFeet(boolean head) {
        this.entityData.set(CUSTOM_ARMOR_FEET, head);
    }

    @Override
    public int getBodyArmorVariant() {
        return this.entityData.get(CUSTOM_ARMOR_INDEX);
    }

    @Override
    public void setBodyArmorVariant(int variant) {
        this.entityData.set(CUSTOM_ARMOR_INDEX, variant);
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_SPAWN};
    }

    @Override
    public boolean shouldAnimalsFear(Entity entity) {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.STRAY_STEP, 0.15F, 1.0F);
    }

}