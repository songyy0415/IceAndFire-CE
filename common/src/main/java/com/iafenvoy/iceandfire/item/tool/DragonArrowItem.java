package com.iafenvoy.iceandfire.item.tool;

import com.iafenvoy.iceandfire.entity.DragonArrowEntity;
import com.iafenvoy.iceandfire.registry.IafEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DragonArrowItem extends ArrowItem {
    public DragonArrowItem(ResourceKey<Item> key) {
        super(new Item.Properties().setId(key));
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom) {
        return new DragonArrowEntity(IafEntities.DRAGON_ARROW.get(), shooter, world, shotFrom);
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        DragonArrowEntity arrowEntity = new DragonArrowEntity(IafEntities.DRAGON_ARROW.get(), pos.x(), pos.y(), pos.z(), world, stack.copyWithCount(1), null);
        arrowEntity.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrowEntity;
    }
}
