package com.iafenvoy.iceandfire.item;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DragonSeekerItem extends Item {
    private final SeekerType type;

    public DragonSeekerItem(ResourceKey<Item> key, SeekerType type) {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE).setId(key));
        this.type = type;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) return super.use(world, user, hand);
        if (!IafCommonConfig.INSTANCE.misc.enableDragonSeeker.getValue()) {
            user.sendSystemMessage(Component.translatable("text.iceandfire.not_enable"));
            return super.use(world, user, hand);
        }
        ItemStack stack = user.getItemInHand(hand);
        DragonBaseEntity dragon = null;
        double closest = Double.MAX_VALUE;
        for (DragonBaseEntity d : world.getEntitiesOfClass(DragonBaseEntity.class, new AABB(this.type.add(user.position(), true), this.type.add(user.position(), false)), entity -> {
            if (entity.isMobDead() && !this.type.trackDead) return false;
            return !entity.isTame() || this.type.trackTeamed;
        })) {
            double distance = d.distanceToSqr(user);
            if (distance < closest) {
                closest = distance;
                dragon = d;
            }
        }
        if (dragon == null) {
            user.sendSystemMessage(Component.translatable("item.iceandfire.dragon_seeker.not_found"));
            return InteractionResult.FAIL;
        }
        if (this.type.admin) {
            String pos1 = String.format("[%d, %d, %d]", (int) dragon.getX(), (int) dragon.getY(), (int) dragon.getZ()), pos2 = String.format("/tp @s %d %d %d", (int) dragon.getX(), (int) dragon.getY(), (int) dragon.getZ());
            Component locationText = Component.literal(pos1).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.SuggestCommand(pos2)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
            user.sendSystemMessage(Component.translatable("item.iceandfire.dragon_seeker.found_location").append(locationText));
        } else
            user.sendSystemMessage(Component.translatable("item.iceandfire.dragon_seeker.found"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        String name = BuiltInRegistries.ITEM.getKey(this).getPath();
        tooltip.accept(Component.translatable("item.iceandfire." + name + ".tooltip"));
        tooltip.accept(Component.translatable("item.iceandfire.dragon_seeker.credit").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)));
    }

    public enum SeekerType {
        NORMAL(150, true, true, false),
        EPIC(200, false, true, false),
        LEGENDARY(300, false, false, false),
        GODLY(500, false, false, true);
        private final int trackRange;
        private final boolean trackDead;
        private final boolean trackTeamed;
        private final boolean admin;

        SeekerType(int trackRange, boolean trackDead, boolean trackTeamed, boolean admin) {
            this.trackRange = trackRange;
            this.trackDead = trackDead;
            this.trackTeamed = trackTeamed;
            this.admin = admin;
        }

        public Vec3 add(Vec3 origin, boolean reverse) {
            int range = this.trackRange;
            if (reverse) range *= -1;
            return origin.add(range, range, range);
        }
    }
}
