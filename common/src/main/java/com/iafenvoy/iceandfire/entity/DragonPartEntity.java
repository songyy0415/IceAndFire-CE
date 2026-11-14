package com.iafenvoy.iceandfire.entity;

import com.iafenvoy.iceandfire.registry.IafEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DragonPartEntity extends MultipartPartEntity {
    private final float baseRadius, baseOffsetY, baseSizeX, baseSizeY;

    public DragonPartEntity(EntityType<?> t, Level world) {
        super(t, world);
        this.baseRadius = 0;
        this.baseOffsetY = 0;
        this.baseSizeX = 0;
        this.baseSizeY = 0;
    }

    public DragonPartEntity(EntityType<?> type, DragonBaseEntity dragon, float baseRadius, float angleYaw, float baseOffsetY, float baseSizeX, float baseSizeY, float damageMultiplier) {
        super(type, dragon, baseRadius, angleYaw, baseOffsetY, baseSizeX, baseSizeY, damageMultiplier);
        this.baseRadius = baseRadius;
        this.baseOffsetY = baseOffsetY;
        this.baseSizeX = baseSizeX;
        this.baseSizeY = baseSizeY;
    }

    public DragonPartEntity(DragonBaseEntity parent, float baseRadius, float angleYaw, float baseOffsetY, float baseSizeX, float baseSizeY, float damageMultiplier) {
        super(IafEntities.DRAGON_MULTIPART.get(), parent, baseRadius, angleYaw, baseOffsetY, baseSizeX, baseSizeY, damageMultiplier);
        this.baseRadius = baseRadius;
        this.baseOffsetY = baseOffsetY;
        this.baseSizeX = baseSizeX;
        this.baseSizeY = baseSizeY;
    }

    public void updateScale(float scale) {
        this.radius = this.baseRadius * scale;
        this.offsetY = this.baseOffsetY * scale;
        this.setScaleX(this.baseSizeX * scale);
        this.setScaleY(this.baseSizeY * scale);
    }

    @Override
    public boolean isPickable() {
        // 上游（NeoForge）的 PartEntity 直接持有父实体引用，getParent() 永远非空；
        // 本移植用 UUID 从 Level 反查父实体，在客户端同步到父 UUID 之前（新生成/重新进入
        // 追踪范围的部位）或父实体不在当前维度时会返回 null。射线检测每帧都会调用本方法，
        // 因此必须判空，否则会抛 NullPointerException 导致客户端渲染线程崩溃。
        Entity parent = this.getParent();
        return parent != null && parent.isAlive();
    }

    @Override
    public void collideWithNearbyEntities() {
    }
}
