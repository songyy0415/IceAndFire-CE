package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.entity.util.BlacklistedFromStatues;
import com.iafenvoy.iceandfire.entity.util.IafEntityUtil;
import com.iafenvoy.iceandfire.entity.util.dragon.DragonUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CockatriceScepterItem extends Item {
    private final Random rand = new Random();

    public CockatriceScepterItem(ResourceKey<Item> key) {
        super(new Properties().durability(700).component(DataComponents.CONSUMABLE, Consumable.builder().animation(ItemUseAnimation.BOW).build()).setId(key));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.cockatrice_scepter.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.cockatrice_scepter.desc_1").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity livingEntity, int timeLeft) {
        if (!worldIn.isClientSide()) MiscData.get(livingEntity).clearScepterTargets();
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player)
            player.getCooldowns().addCooldown(new ItemStack(this), 20);
        return super.finishUsingItem(stack, world, user);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        playerIn.startUsingItem(hand);
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (level.isClientSide()) {
            this.attackTargets(player, stack);
            return;
        }
        if (player instanceof Player) {
            double dist = 32;
            Vec3 playerEyePosition = player.getEyePosition(1.0F);
            Vec3 playerLook = player.getViewVector(1.0F);
            Vec3 end = playerEyePosition.add(playerLook.x * dist, playerLook.y * dist, playerLook.z * dist);
            List<Entity> pointedEntities = new LinkedList<>();
            List<Entity> nearbyEntities = level.getEntities(player, player.getBoundingBox()
                    .expandTowards(playerLook.x * dist, playerLook.y * dist, playerLook.z * dist).inflate(1, 1, 1), entity -> {
                boolean blindness = entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(MobEffects.BLINDNESS)
                        || (entity instanceof BlacklistedFromStatues blacklisted && !blacklisted.canBeTurnedToStone());
                return entity.isPickable() && !blindness
                        && (entity instanceof Player || (entity instanceof LivingEntity living && DragonUtils.isAlive(living)));
            });
            double nearest = dist;
            for (Entity nearbyEntity : nearbyEntities) {
                AABB box = nearbyEntity.getBoundingBox().inflate(nearbyEntity.getPickRadius());
                Optional<Vec3> hit = box.clip(playerEyePosition, end);
                if (box.contains(playerEyePosition)) {
                    if (nearest >= 0.0D) {
                        pointedEntities.add(nearbyEntity);
                        nearest = 0.0D;
                    }
                } else if (hit.isPresent()) {
                    double distance = playerEyePosition.distanceTo(hit.get());
                    if (distance < nearest || nearest == 0.0D)
                        if (nearbyEntity.getRootVehicle() == player.getRootVehicle()) {
                            if (nearest == 0.0D) pointedEntities.add(nearbyEntity);
                        } else {
                            pointedEntities.add(nearbyEntity);
                            nearest = distance;
                        }
                }
            }
            for (Entity pointedEntity : pointedEntities)
                if (pointedEntity instanceof LivingEntity target) {
                    if (!target.isAlive()) return;
                    MiscData.get(player).addScepterTarget(target);
                }
            this.attackTargets(player, stack);
        }
    }

    private void attackTargets(final LivingEntity caster, ItemStack stack) {
        MiscData miscData = MiscData.get(caster);
        int durabilityCost = 0;
        for (UUID uuid : miscData.getTargetedByScepters()) {
            Entity entity = null;
            if (caster.level() instanceof ServerLevel serverWorld) entity = serverWorld.getEntity(uuid);
            else if (caster.level() instanceof ClientLevel clientWorld)
                entity = clientWorld.entityStorage.getEntityGetter().get(uuid);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()
                    || !IafEntityUtil.isEntityLookingAt(caster, target, 0.2F) || caster.isRemoved()) {
                if (!caster.level().isClientSide()) miscData.removeScepterTarget(uuid);
                continue;
            }
            if (!caster.level().isClientSide()) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 2));
                if (caster.tickCount % 20 == 0) {
                    durabilityCost++;
                    target.hurt(caster.level().damageSources().wither(), 2);
                }
            } else this.drawParticleBeam(caster, target);
        }
        if (durabilityCost > 0) {
            stack.hurtAndBreak(durabilityCost, caster, caster.getUsedItemHand());
            if (stack.isEmpty()) miscData.clearScepterTargets();
        }
    }

    private void drawParticleBeam(LivingEntity origin, LivingEntity target) {
        double d5 = 80F;
        double d0 = target.getX() - origin.getX();
        double d1 = target.getY() + (double) (target.getBbHeight() * 0.5F)
                - (origin.getY() + (double) origin.getEyeHeight() * 0.5D);
        double d2 = target.getZ() - origin.getZ();
        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        d0 = d0 / d3;
        d1 = d1 / d3;
        d2 = d2 / d3;
        double d4 = this.rand.nextDouble();
        while (d4 < d3) {
            d4 += 1.0D;
            origin.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000),
                    origin.getX() + d0 * d4, origin.getY() + d1 * d4 + (double) origin.getEyeHeight() * 0.5D,
                    origin.getZ() + d2 * d4, 0.0D, 0.0D, 0.0D);
        }
    }
}
