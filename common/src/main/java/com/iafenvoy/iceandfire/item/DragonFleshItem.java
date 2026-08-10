package com.iafenvoy.iceandfire.item;
import net.minecraft.resources.Identifier;

import com.iafenvoy.iceandfire.data.DragonType;
import com.iafenvoy.iceandfire.registry.IafDragonTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DragonFleshItem extends Item {
    private final DragonType type;

    public DragonFleshItem(DragonType type) {
        super(new Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()));
        this.type = type;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
        if (!world.isClientSide()) {
            if (this.type == IafDragonTypes.FIRE)
                living.igniteForSeconds(5);
            else if (this.type == IafDragonTypes.ICE)
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
            else {
                LightningBolt lightning = (LightningBolt) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("lightning_bolt")).create(living.level(), EntitySpawnReason.LOAD);
                assert lightning != null;
                lightning.setPos(living.position());
                living.level().addFreshEntity(lightning);
            }
        }
        return super.finishUsingItem(stack, world, living);
    }
}
