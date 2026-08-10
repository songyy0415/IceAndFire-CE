package com.iafenvoy.iceandfire.entity;

import com.google.common.collect.ImmutableList;
import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.util.BlacklistedFromStatues;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StoneStatueEntity extends LivingEntity implements BlacklistedFromStatues {
    private static final EntityDataAccessor<String> TRAPPED_ENTITY_TYPE = SynchedEntityData.defineId(StoneStatueEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> TRAPPED_ENTITY_WIDTH = SynchedEntityData.defineId(StoneStatueEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TRAPPED_ENTITY_HEIGHT = SynchedEntityData.defineId(StoneStatueEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TRAPPED_ENTITY_SCALE = SynchedEntityData.defineId(StoneStatueEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CRACK_AMOUNT = SynchedEntityData.defineId(StoneStatueEntity.class, EntityDataSerializers.INT);
    private EntityDimensions stoneStatueSize = EntityDimensions.fixed(0.5F, 0.5F);
    private CompoundTag trappedEntityTag = new CompoundTag();

    public StoneStatueEntity(EntityType<? extends LivingEntity> t, Level worldIn) {
        super(t, worldIn);
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Mob.createMobAttributes()
                //HEALTH
                .add(Attributes.MAX_HEALTH, 20)
                //SPEED
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                //ATTACK
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    public static StoneStatueEntity buildStatueEntity(LivingEntity parent) {
        StoneStatueEntity statue = IafEntities.STONE_STATUE.get().create(parent.level(), EntitySpawnReason.LOAD);
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        try {
            if (!(parent instanceof Player)) {
                parent.saveWithoutId(output);
            }
        } catch (Exception e) {
            IceAndFire.LOGGER.debug("Encountered issue creating stone statue from {}", parent);
        }
        CompoundTag entityTag = output.buildResult();
        assert statue != null;
        statue.setTrappedTag(entityTag);
        statue.setTrappedEntityTypeString(BuiltInRegistries.ENTITY_TYPE.getKey(parent.getType()).toString());
        statue.setTrappedEntityWidth(parent.getBbWidth());
        statue.setTrappedHeight(parent.getBbHeight());
        statue.setTrappedScale(parent.getAgeScale());
        return statue;
    }

    @Override
    public void push(Entity entityIn) {
    }

    @Override
    public void baseTick() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRAPPED_ENTITY_TYPE, "minecraft:pig");
        builder.define(TRAPPED_ENTITY_WIDTH, 0.5F);
        builder.define(TRAPPED_ENTITY_HEIGHT, 0.5F);
        builder.define(TRAPPED_ENTITY_SCALE, 1F);
        builder.define(CRACK_AMOUNT, 0);
    }

    public EntityType<?> getTrappedEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(this.getTrappedEntityTypeString())).orElse(BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("pig")));
    }

    public String getTrappedEntityTypeString() {
        return this.entityData.get(TRAPPED_ENTITY_TYPE);
    }

    public void setTrappedEntityTypeString(String string) {
        this.entityData.set(TRAPPED_ENTITY_TYPE, string);
    }

    public CompoundTag getTrappedTag() {
        return this.trappedEntityTag;
    }

    public void setTrappedTag(CompoundTag tag) {
        this.trappedEntityTag = tag;
    }

    public float getTrappedWidth() {
        return this.entityData.get(TRAPPED_ENTITY_WIDTH);
    }

    public void setTrappedEntityWidth(float size) {
        this.entityData.set(TRAPPED_ENTITY_WIDTH, size);
    }

    public float getTrappedHeight() {
        return this.entityData.get(TRAPPED_ENTITY_HEIGHT);
    }

    public void setTrappedHeight(float size) {
        this.entityData.set(TRAPPED_ENTITY_HEIGHT, size);
    }

    public float getTrappedScale() {
        return this.entityData.get(TRAPPED_ENTITY_SCALE);
    }

    public void setTrappedScale(float size) {
        this.entityData.set(TRAPPED_ENTITY_SCALE, size);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CrackAmount", this.getCrackAmount());
        tag.putFloat("StatueWidth", this.getTrappedWidth());
        tag.putFloat("StatueHeight", this.getTrappedHeight());
        tag.putFloat("StatueScale", this.getTrappedScale());
        tag.putString("StatueEntityType", this.getTrappedEntityTypeString());
        tag.putString("StatueEntityTag", this.getTrappedTag().toString());
    }

    @Override
    public float getAgeScale() {
        return this.getTrappedScale();
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.setCrackAmount(tag.getByteOr("CrackAmount", (byte) 0));
        this.setTrappedEntityWidth(tag.getFloatOr("StatueWidth", 0.0F));
        this.setTrappedHeight(tag.getFloatOr("StatueHeight", 0.0F));
        this.setTrappedScale(tag.getFloatOr("StatueScale", 0.0F));
        this.setTrappedEntityTypeString(tag.getString("StatueEntityType").orElse(""));
        try {
            this.setTrappedTag(TagParser.parseCompoundFully(tag.getString("StatueEntityTag").orElse("{}")));
        } catch (CommandSyntaxException e) {
            this.setTrappedTag(new CompoundTag());
        }
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose poseIn) {
        return this.stoneStatueSize;
    }

    @Override
    public void tick() {
        super.tick();
        this.setYRot(this.yBodyRot);
        this.yHeadRot = this.getYRot();
        if (Math.abs(this.getBbWidth() - this.getTrappedWidth()) > 0.01 || Math.abs(this.getBbHeight() - this.getTrappedHeight()) > 0.01) {
            double prevX = this.getX();
            double prevZ = this.getZ();
            this.stoneStatueSize = EntityDimensions.scalable(this.getTrappedWidth(), this.getTrappedHeight());
            this.refreshDimensions();
            this.setPos(prevX, this.getY(), prevZ);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && amount > 0) {
            if (this.level() instanceof ServerLevel serverWorld && this.getTrappedEntityType().create(serverWorld, EntitySpawnReason.LOAD) instanceof LivingEntity livingEntity)
                ExperienceOrb.award(serverWorld, this.position(), livingEntity.getBaseExperienceReward(serverWorld));
            this.remove(RemovalReason.KILLED);
            return true;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void kill(ServerLevel level) {
        this.remove(RemovalReason.KILLED);
    }

    public Iterable<ItemStack> getArmorSlots() {
        return ImmutableList.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public int getCrackAmount() {
        return this.entityData.get(CRACK_AMOUNT);
    }

    public void setCrackAmount(int crackAmount) {
        this.entityData.set(CRACK_AMOUNT, crackAmount);
    }

    @Override
    public boolean canBeTurnedToStone() {
        return false;
    }
}
