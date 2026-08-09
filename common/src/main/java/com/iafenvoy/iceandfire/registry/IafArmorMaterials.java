package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.registry.tag.CommonItemTags;
import com.iafenvoy.iceandfire.registry.tag.IafItemTags;
import java.util.EnumMap;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class IafArmorMaterials {
    // 26.2 ArmorMaterial gained an `int durability` field (used as `type.getDurability(material.durability())`
    // inside humanoidArmor). The 1.21.1 material had no durability — every IAF armor item sets its own via
    // `.durability(...)` after `humanoidArmor(...)`, which overrides this base. Kept as a sane base value.
    private static final int BASE_DURABILITY = 15;

    public static final ArmorMaterial COPPER = create("copper", new int[]{1, 3, 4, 2}, 15, SoundEvents.ARMOR_EQUIP_GOLD, 0, 0, IafItemTags.COPPER_INGOTS);
    public static final ArmorMaterial SILVER = create("silver", new int[]{1, 4, 5, 2}, 20, SoundEvents.ARMOR_EQUIP_CHAIN, 0, 0, CommonItemTags.INGOTS_SILVER);
    public static final ArmorMaterial BLINDFOLD = create("blindfold", new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, CommonItemTags.STRINGS);
    public static final ArmorMaterial SHEEP = create("sheep", new int[]{1, 3, 2, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, ItemTags.WOOL);
    public static final ArmorMaterial EARPLUGS = create("earplugs", new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, ItemTags.WOODEN_BUTTONS);
    public static final ArmorMaterial DEATHWORM_YELLOW = create("deathworm_yellow", new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, 0, IafItemTags.DEATHWORM_CHITIN_YELLOW);
    public static final ArmorMaterial DEATHWORM_WHITE = create("deathworm_white", new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, 0, IafItemTags.DEATHWORM_CHITIN_RED);
    public static final ArmorMaterial DEATHWORM_RED = create("deathworm_red", new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, 0, IafItemTags.DEATHWORM_CHITIN_WHITE);
    public static final ArmorMaterial TROLL_MOUNTAIN = create("troll_mountain", new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, 0, IafItemTags.TROLL_LEATHER_MOUNTAIN);
    public static final ArmorMaterial TROLL_FOREST = create("troll_forest", new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, 0, IafItemTags.TROLL_LEATHER_FOREST);
    public static final ArmorMaterial TROLL_FROST = create("troll_frost", new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, 0, IafItemTags.TROLL_LEATHER_FROST);
    public static final ArmorMaterial DRAGONSTEEL_FIRE = create(
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
            IafItemTags.DRAGON_STEELS
    );
    public static final ArmorMaterial DRAGONSTEEL_ICE = create(
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
            IafItemTags.DRAGON_STEELS
    );
    public static final ArmorMaterial DRAGONSTEEL_LIGHTNING = create(
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
            IafItemTags.DRAGON_STEELS
    );

    public static ArmorMaterial create(String name, int[] protection, int enchantAbility, Holder<SoundEvent> equipSound, float toughness, float knockBackResistance, TagKey<Item> repairItems) {
        return new ArmorMaterial(
                BASE_DURABILITY,
                Util.make(new EnumMap<>(ArmorType.class), map -> {
                    map.put(ArmorType.HELMET, protection[3]);
                    map.put(ArmorType.CHESTPLATE, protection[2]);
                    map.put(ArmorType.LEGGINGS, protection[1]);
                    map.put(ArmorType.BOOTS, protection[0]);
                }),
                enchantAbility,
                equipSound,
                toughness,
                knockBackResistance,
                repairItems,
                createAssetId(name)
        );
    }

    private static ResourceKey<EquipmentAsset> createAssetId(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
    }
}
