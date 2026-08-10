package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.item.component.DragonHornComponent;
import com.iafenvoy.iceandfire.registry.IafDataComponents;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.util.TagValueUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DragonHornItem extends Item {
    public DragonHornItem() {
        super(new Properties().stacksTo(1));
    }

    public static int getDragonType(ItemStack stack) {
        if (stack.has(IafDataComponents.DRAGON_HORN.get())) {
            Optional<EntityType<?>> optional = BuiltInRegistries.ENTITY_TYPE.getOptional(stack.get(IafDataComponents.DRAGON_HORN.get()).entityType());
            if (optional.isPresent()) {
                EntityType<?> entityType = optional.get();
                if (entityType == IafEntities.FIRE_DRAGON.get()) return 1;
                if (entityType == IafEntities.ICE_DRAGON.get()) return 2;
                if (entityType == IafEntities.LIGHTNING_DRAGON.get()) return 3;
            }
        }
        return 0;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        stack = player.getItemInHand(hand);
        if (stack.is(IafItems.DRAGON_HORN.get()) && !stack.has(IafDataComponents.DRAGON_HORN.get())) {
            if (!player.level().isClientSide() && (Entity) target instanceof DragonBaseEntity dragon && dragon.isOwnedBy(player)) {
                TagValueOutput output = TagValueUtil.output();
                target.save(output);
                CompoundTag entityTag = output.buildResult();
                stack.set(IafDataComponents.DRAGON_HORN.get(), new DragonHornComponent(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()), target.getUUID(), entityTag));
                player.swing(hand);
                player.level().playSound(player, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 3.0F, 0.75F);
                target.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        if (stack.has(IafDataComponents.DRAGON_HORN.get())) {
            DragonHornComponent component = stack.get(IafDataComponents.DRAGON_HORN.get());
            Level world = context.getLevel();
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(component.entityType()).orElse(null);
            if (type != null) {
                Entity entity = type.create(world, EntitySpawnReason.LOAD);
                if (entity instanceof DragonBaseEntity dragon)
                    dragon.load(TagValueUtil.asInput(component.entityData(), world.registryAccess()));
                //Still needed to allow for intercompatibility
                UUID uuid = component.entityUuid();
                if (uuid != null) {
                    assert entity != null;
                    entity.setUUID(uuid);
                }

                assert entity != null;
                entity.setPos(context.getClickedPos().getX() + 0.5D, context.getClickedPos().getY() + 1, context.getClickedPos().getZ() + 0.5D); entity.setYRot(180 + (context.getHorizontalDirection()).toYRot()); entity.setXRot(0.0F);
                if (world.addFreshEntity(entity))
                    stack.remove(IafDataComponents.DRAGON_HORN.get());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (stack.has(IafDataComponents.DRAGON_HORN.get())) {
            DragonHornComponent component = stack.get(IafDataComponents.DRAGON_HORN.get());
            CompoundTag entityTag = component.entityData();
            if (!entityTag.isEmpty()) {
                Optional<EntityType<?>> optional = BuiltInRegistries.ENTITY_TYPE.getOptional(component.entityType());
                if (optional.isPresent()) {
                    EntityType<?> entityType = optional.get();
                    tooltip.accept((Component.translatable(entityType.getDescriptionId())).withStyle(this.getTextColorForEntityType(entityType)));
                    String name = Component.translatable("dragon.unnamed").getString();
                    if (!entityTag.getString("CustomName").orElse("").isEmpty())
                        name = entityTag.getString("CustomName").orElse("");

                    tooltip.accept((Component.literal(name)).withStyle(ChatFormatting.GRAY));
                    String gender = (Component.translatable("dragon.gender")).getString() + " " + (Component.translatable(entityTag.getBooleanOr("Gender", false) ? "dragon.gender.male" : "dragon.gender.female")).getString();
                    tooltip.accept((Component.literal(gender)).withStyle(ChatFormatting.GRAY));
                    int stagenumber = entityTag.getInt("AgeTicks").orElse(0) / 24000;
                    int stage1;
                    if (stagenumber >= 100) stage1 = 5;
                    else if (stagenumber >= 75) stage1 = 4;
                    else if (stagenumber >= 50) stage1 = 3;
                    else if (stagenumber >= 25) stage1 = 2;
                    else stage1 = 1;
                    tooltip.accept(Component.translatable("dragon.stage").append(Component.literal(" " + stage1 + " ")).append(Component.translatable("dragon.days.front")).append(Component.literal(stagenumber + " ")).append(Component.translatable("dragon.days.back")).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    private ChatFormatting getTextColorForEntityType(EntityType<?> type) {
        if (type == IafEntities.FIRE_DRAGON.get()) return ChatFormatting.DARK_RED;
        if (type == IafEntities.ICE_DRAGON.get()) return ChatFormatting.BLUE;
        if (type == IafEntities.LIGHTNING_DRAGON.get()) return ChatFormatting.DARK_PURPLE;
        return ChatFormatting.GRAY;
    }
}
