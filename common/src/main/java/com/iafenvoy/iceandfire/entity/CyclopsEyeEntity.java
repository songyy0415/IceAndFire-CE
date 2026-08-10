package com.iafenvoy.iceandfire.entity;

import com.iafenvoy.iceandfire.registry.IafEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CyclopsEyeEntity extends MultipartPartEntity {
    public CyclopsEyeEntity(EntityType<?> t, Level world) {
        super(t, world);
    }

    public CyclopsEyeEntity(LivingEntity parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
        super(IafEntities.CYCLOPS_MULTIPART.get(), parent, radius, angleYaw, offsetY, sizeX, sizeY,
                damageMultiplier);
    }

    @Override
    public EntityReference<LivingEntity> getOwnerReference() {
        return this.getParent() instanceof LivingEntity parent ? EntityReference.of(parent) : null;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity parent = this.getParent();
        if (parent instanceof CyclopsEntity && source.is(DamageTypes.ARROW)) {
            ((CyclopsEntity) parent).onHitEye(source, damage);
            return true;
        } else {
            return parent != null && parent.hurtOrSimulate(source, damage);
        }
    }
}
