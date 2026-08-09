package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.registry.tag.CommonItemTags;
import com.iafenvoy.uranus.util.function.MemorizeSupplier;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public final class IafArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.ARMOR_MATERIAL);

    public static final RegistrySupplier<ArmorMaterial> COPPER = register("copper", new int[]{1, 3, 4, 2}, 15, SoundEvents.ARMOR_EQUIP_GOLD, 0, new MemorizeSupplier<>(() -> Ingredient.of(Items.COPPER_INGOT)));
    public static final RegistrySupplier<ArmorMaterial> SILVER = register("silver", new int[]{1, 4, 5, 2}, 20, SoundEvents.ARMOR_EQUIP_CHAIN, 0, new MemorizeSupplier<>(() -> Ingredient.of(CommonItemTags.INGOTS_SILVER)));
    public static final RegistrySupplier<ArmorMaterial> BLINDFOLD = register("blindfold", new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0, new MemorizeSupplier<>(() -> Ingredient.of(CommonItemTags.STRINGS)));
    public static final RegistrySupplier<ArmorMaterial> SHEEP = register("sheep", new int[]{1, 3, 2, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0, new MemorizeSupplier<>(() -> Ingredient.of(Blocks.WOOL.get(DyeColor.WHITE))));
    public static final RegistrySupplier<ArmorMaterial> EARPLUGS = register("earplugs", new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0, new MemorizeSupplier<>(() -> Ingredient.of(Blocks.OAK_BUTTON)));
    public static final RegistrySupplier<ArmorMaterial> DEATHWORM_YELLOW = register("deathworm_yellow", new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, new MemorizeSupplier<>(() -> Ingredient.of(IafItems.DEATH_WORM_CHITIN_YELLOW.get())));
    public static final RegistrySupplier<ArmorMaterial> DEATHWORM_WHITE = register("deathworm_white", new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, new MemorizeSupplier<>(() -> Ingredient.of(IafItems.DEATH_WORM_CHITIN_RED.get())));
    public static final RegistrySupplier<ArmorMaterial> DEATHWORM_RED = register("deathworm_red", new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, new MemorizeSupplier<>(() -> Ingredient.of(IafItems.DEATH_WORM_CHITIN_WHITE.get())));
    public static final RegistrySupplier<ArmorMaterial> TROLL_MOUNTAIN = register("troll_mountain", new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, new MemorizeSupplier<>(() -> Ingredient.of(IafTrollTypes.MOUNTAIN.leather.get())));
    public static final RegistrySupplier<ArmorMaterial> TROLL_FOREST = register("troll_forest", new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, new MemorizeSupplier<>(() -> Ingredient.of(IafTrollTypes.FOREST.leather.get())));
    public static final RegistrySupplier<ArmorMaterial> TROLL_FROST = register("troll_frost", new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, new MemorizeSupplier<>(() -> Ingredient.of(IafTrollTypes.FROST.leather.get())));
    public static final RegistrySupplier<ArmorMaterial> DRAGONSTEEL_FIRE = register(
            "dragonsteel_fire",
            new int[]{
                    IafCommonConfig.INSTANCE.armors.dragonsteelBootsArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelLeggingsArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelChestplateArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelHelmetArmor.getValue()
            },
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorEnchantability.getValue(),
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorToughness.getValue().floatValue(),
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorKnockbackResistance.getValue().floatValue(),
            new MemorizeSupplier<>(() -> Ingredient.of(IafItems.DRAGONSTEEL_FIRE_INGOT.get()))
    );
    public static final RegistrySupplier<ArmorMaterial> DRAGONSTEEL_ICE = register(
            "dragonsteel_ice",
            new int[]{
                    IafCommonConfig.INSTANCE.armors.dragonsteelBootsArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelLeggingsArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelChestplateArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelHelmetArmor.getValue()
            },
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorEnchantability.getValue(),
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorToughness.getValue().floatValue(),
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorKnockbackResistance.getValue().floatValue(),
            new MemorizeSupplier<>(() -> Ingredient.of(IafItems.DRAGONSTEEL_ICE_INGOT.get()))
    );
    public static final RegistrySupplier<ArmorMaterial> DRAGONSTEEL_LIGHTNING = register(
            "dragonsteel_lightning",
            new int[]{
                    IafCommonConfig.INSTANCE.armors.dragonsteelBootsArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelLeggingsArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelChestplateArmor.getValue(),
                    IafCommonConfig.INSTANCE.armors.dragonsteelHelmetArmor.getValue()
            },
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorEnchantability.getValue(),
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorToughness.getValue().floatValue(),
            IafCommonConfig.INSTANCE.armors.dragonsteelArmorKnockbackResistance.getValue().floatValue(),
            new MemorizeSupplier<>(() -> Ingredient.of(IafItems.DRAGONSTEEL_ICE_INGOT.get()))
    );

    public static RegistrySupplier<ArmorMaterial> register(String name, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness, Supplier<Ingredient> repairIngredients) {
        return register(name, damageReduction, enchantability, sound, toughness, 0, repairIngredients);
    }

    public static RegistrySupplier<ArmorMaterial> register(String name, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness, float knockBackResistance, Supplier<Ingredient> repairIngredients) {
        return REGISTRY.register(name, () -> createMaterial(name, damageReduction, enchantability, sound, toughness, knockBackResistance, repairIngredients));
    }

    public static ArmorMaterial createMaterial(String name, int[] protection, int enchantAbility, Holder<SoundEvent> equipSound, float toughness, float knockBackResistance, Supplier<Ingredient> repairIngredients) {
        return new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                    map.put(ArmorItem.Type.HELMET, protection[3]);
                    map.put(ArmorItem.Type.CHESTPLATE, protection[2]);
                    map.put(ArmorItem.Type.LEGGINGS, protection[1]);
                    map.put(ArmorItem.Type.BOOTS, protection[0]);
                }
        ), enchantAbility, equipSound, repairIngredients, List.of(new ArmorMaterial.Layer(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name))), toughness, knockBackResistance);
    }
}
