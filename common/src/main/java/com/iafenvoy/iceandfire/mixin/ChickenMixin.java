package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Chicken.class)
public abstract class ChickenMixin extends Entity {
    public ChickenMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropFromGiftLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Ljava/util/function/BiConsumer;)Z"))
    private boolean layRottenEgg(LivingEntity instance, ServerLevel serverLevel, ResourceKey<LootTable> lootTable, BiConsumer<ServerLevel, ItemStack> consumer, Operation<Boolean> original) {
        BiConsumer<ServerLevel, ItemStack> wrapped = (level, stack) -> {
            if (IafCommonConfig.INSTANCE.cockatrice.chickensLayRottenEggs.getValue() && this.getRandom().nextDouble() < IafCommonConfig.INSTANCE.cockatrice.eggChance.getValue())
                consumer.accept(level, new ItemStack(IafItems.ROTTEN_EGG.get()));
            else
                consumer.accept(level, stack);
        };
        return original.call(instance, serverLevel, lootTable, wrapped);
    }
}
