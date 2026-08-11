package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.registry.IafItems;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Chicken.class)
public abstract class ChickenMixin extends Entity {
    public ChickenMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // The invoke in Chicken.aiStep is emitted with owner Chicken (the static type of `this`), so the
    // target MUST use the Chicken owner — the old LivingEntity owner scanned 0 targets and the rotten
    // egg mechanic silently never applied (require=0 hid it). With the owner corrected, require=1 is
    // safe and the feature actually works.
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/chicken/Chicken;dropFromGiftLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Ljava/util/function/BiConsumer;)Z"), require = 1)
    private boolean layRottenEgg(Chicken instance, ServerLevel serverLevel, ResourceKey<LootTable> lootTable, BiConsumer<ServerLevel, ItemStack> consumer) {
        BiConsumer<ServerLevel, ItemStack> wrapped = (level, stack) -> {
            if (IafCommonConfig.INSTANCE.cockatrice.chickensLayRottenEggs.getValue() && this.getRandom().nextDouble() < IafCommonConfig.INSTANCE.cockatrice.eggChance.getValue())
                consumer.accept(level, new ItemStack(IafItems.ROTTEN_EGG.get()));
            else
                consumer.accept(level, stack);
        };
        return instance.dropFromGiftLootTable(serverLevel, lootTable, wrapped);
    }
}
