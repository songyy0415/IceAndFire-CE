package com.iafenvoy.iceandfire.entity.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockLaunchExplosion implements Explosion {
    private final float size;
    private final Level world;
    private final Entity source;
    private final double x;
    private final double y;
    private final double z;
    private final BlockInteraction mode;
    private final List<BlockPos> toBlow = new ArrayList<>();

    public BlockLaunchExplosion(Level world, Mob entity, double x, double y, double z, float size) {
        this(world, entity, x, y, z, size, BlockInteraction.DESTROY);
    }

    public BlockLaunchExplosion(Level world, Mob entity, double x, double y, double z, float size, BlockInteraction mode) {
        this(world, entity, null, x, y, z, size, mode);
    }

    public BlockLaunchExplosion(Level world, Mob entity, DamageSource source, double x, double y, double z, float size, BlockInteraction mode) {
        this.world = world;
        this.source = entity;
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
        this.mode = mode;
    }

    public void explode() {
        int radius = (int) this.size;
        for (BlockPos pos : BlockPos.betweenClosed(
                (int) this.x - radius, (int) this.y - radius, (int) this.z - radius,
                (int) this.x + radius, (int) this.y + radius, (int) this.z + radius)) {
            double dx = pos.getX() + 0.5D - this.x;
            double dy = pos.getY() + 0.5D - this.y;
            double dz = pos.getZ() + 0.5D - this.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance <= this.size && !this.world.getBlockState(pos).isAir())
                this.toBlow.add(pos);
        }
    }

    public void finalizeExplosion(boolean spawnParticles) {
        if (this.world.isClientSide())
            this.world.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, (1.0F + (this.world.getRandom().nextFloat() - this.world.getRandom().nextFloat()) * 0.2F) * 0.7F, false);

        boolean flag = this.mode != BlockInteraction.KEEP;
        if (spawnParticles) {
            if (!(this.size < 2.0F) && flag)
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            else
                this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
        }

        if (flag) {
            Collections.shuffle(this.toBlow, ThreadLocalRandom.current());

            for (BlockPos blockpos : this.toBlow) {
                BlockState blockstate = this.world.getBlockState(blockpos);
                if (!blockstate.isAir()) {
                    BlockPos blockpos1 = blockpos.immutable();
                    Vec3 Vector3d = new Vec3(this.x, this.y, this.z);
                    this.world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
                    if (this.world instanceof ServerLevel serverWorld)
                        blockstate.getBlock().wasExploded(serverWorld, blockpos, this);
                    FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK, this.world);
                    fallingBlockEntity.setStartPos(blockpos1);
                    fallingBlockEntity.setPos(blockpos1.getX() + 0.5D, blockpos1.getY() + 0.5D, blockpos1.getZ() + 0.5D);
                    double d5 = fallingBlockEntity.getX() - this.x;
                    double d7 = fallingBlockEntity.getEyeY() - this.y;
                    double d9 = fallingBlockEntity.getZ() - this.z;
                    float f3 = this.size * 2.0F;
                    double d12 = Math.sqrt(fallingBlockEntity.distanceToSqr(Vector3d)) / f3;
                    double d14 = ServerExplosion.getSeenPercent(Vector3d, fallingBlockEntity);
                    double d11 = (1.0D - d12) * d14;
                    fallingBlockEntity.setDeltaMovement(fallingBlockEntity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));
                }
            }
        }
    }

    @Override
    public ServerLevel level() {
        return this.world instanceof ServerLevel serverWorld ? serverWorld : null;
    }

    @Override
    public BlockInteraction getBlockInteraction() {
        return this.mode;
    }

    @Override
    public LivingEntity getIndirectSourceEntity() {
        return Explosion.getIndirectSourceEntity(this.source);
    }

    @Override
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    @Override
    public float radius() {
        return this.size;
    }

    @Override
    public Vec3 center() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public boolean canTriggerBlocks() {
        return true;
    }

    @Override
    public boolean shouldAffectBlocklikeEntities() {
        return true;
    }
}
