package com.iafenvoy.iceandfire.item.armor;

import com.iafenvoy.iceandfire.registry.IafArmorMaterials;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.ArmorType;

public class BlindfoldItem extends Item {
    private final ArmorType armorType = ArmorType.HELMET;

    public BlindfoldItem(ResourceKey<Item> key) {
        super(new Item.Properties().humanoidArmor(IafArmorMaterials.BLINDFOLD, ArmorType.HELMET).durability(55).setId(key));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (entity instanceof Player player && player.getItemBySlot(this.armorType.getSlot()) == stack)
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
    }
}
