package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.SeaSerpentType;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class SeaSerpentArmorItem extends ArmorItem {
    public final SeaSerpentType armorType;

    public SeaSerpentArmorItem(SeaSerpentType armorType, Holder<ArmorMaterial> material, Type slot) {
        super(material, slot, new Properties().durability(switch (slot) {
            case HELMET -> 330;
            case CHESTPLATE -> 480;
            case LEGGINGS -> 450;
            case BOOTS -> 390;
            case BODY -> 0;
        }));
        this.armorType = armorType;
    }

    @Override
    public String getDescriptionId() {
        return String.format(Locale.ROOT, "item.%s.sea_serpent_%s", IceAndFire.MOD_ID, this.type.getName());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (entity instanceof Player player && player.getItemBySlot(this.getEquipmentSlot()) == stack) {
            int headMod = player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SeaSerpentArmorItem ? 1 : 0;
            int chestMod = player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SeaSerpentArmorItem ? 1 : 0;
            int legMod = player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof SeaSerpentArmorItem ? 1 : 0;
            int footMod = player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof SeaSerpentArmorItem ? 1 : 0;
            int modifier = headMod + chestMod + legMod + footMod - 1;
            if (modifier >= 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 50, 0, false, false));
                if (player.isInWaterOrRain())
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50, modifier, false, false));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        tooltip.accept(Component.translatable("sea_serpent." + this.armorType.getName()).withStyle(this.armorType.getColor()));
        tooltip.accept(Component.translatable("item.iceandfire.sea_serpent_armor.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.sea_serpent_armor.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
