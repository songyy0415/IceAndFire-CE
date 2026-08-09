package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.registry.tag.CommonItemTags;
import com.iafenvoy.iceandfire.registry.tag.IafItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public enum IafToolMaterials {
    SILVER_TOOL_MATERIAL("silver", 460, 1.0F, 11.0F, 18, BlockTags.INCORRECT_FOR_IRON_TOOL),
    COPPER_TOOL_MATERIAL("copper", 300, 0.0F, 3.0F, 10, BlockTags.INCORRECT_FOR_IRON_TOOL),
    DRAGONBONE_TOOL_MATERIAL("dragon_bone", 1660, 4.0F, 10.0F, 22, BlockTags.INCORRECT_FOR_IRON_TOOL),
    BLOODED_DRAGONBONE_TOOL_MATERIAL("blooded_dragon_bone", 2000, 5.5F, 10F, 22, BlockTags.INCORRECT_FOR_IRON_TOOL),
    TROLL_WEAPON_TOOL_MATERIAL("troll_weapon", 300, 1F, 10F, 1, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    HIPPOGRYPH_SWORD_TOOL_MATERIAL("hippogryph_sword", 500, 2.5F, 10F, 10, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    STYMHALIAN_SWORD_TOOL_MATERIAL("stymphalian_sword", 500, 2, 10.0F, 10, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    AMPHITHERE_SWORD_TOOL_MATERIAL("amphithere_sword", 500, 1F, 10F, 10, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    HIPPOCAMPUS_SWORD_TOOL_MATERIAL("hippocampus_sword", 500, -2F, 0F, 50, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    DREAD_SWORD_TOOL_MATERIAL("dread_sword", 100, 1F, 10F, 0, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    DREAD_KNIGHT_TOOL_MATERIAL("dread_knight_sword", 1200, 13F, 0F, 10, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    GHOST_SWORD_TOOL_MATERIAL("ghost_sword", 3000, 5, 10.0F, 25, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    DRAGONSTEEL_FIRE("dragon_steel_fire", IafCommonConfig.INSTANCE.armors.dragonSteelBaseDurability.getValue(), IafCommonConfig.INSTANCE.armors.dragonSteelBaseDamage.getValue().floatValue() - 1, 10F, 21, BlockTags.INCORRECT_FOR_NETHERITE_TOOL),
    DRAGONSTEEL_ICE("dragon_steel_ice", IafCommonConfig.INSTANCE.armors.dragonSteelBaseDurability.getValue(), IafCommonConfig.INSTANCE.armors.dragonSteelBaseDamage.getValue().floatValue() - 1, 10F, 21, BlockTags.INCORRECT_FOR_NETHERITE_TOOL),
    DRAGONSTEEL_LIGHTNING("dragon_steel_lightning", IafCommonConfig.INSTANCE.armors.dragonSteelBaseDurability.getValue(), IafCommonConfig.INSTANCE.armors.dragonSteelBaseDamage.getValue().floatValue() - 1, 10F, 21, BlockTags.INCORRECT_FOR_NETHERITE_TOOL),
    DREAD_QUEEN("dread_queen", 4000, 4F, 10F, 21, BlockTags.INCORRECT_FOR_WOODEN_TOOL);

    private final String name;
    private final int durability;
    private final float damage;
    private final float speed;
    private final int enchantability;
    private final TagKey<Block> inverted;
    private TagKey<Item> repairItems = ItemTags.AIR;

    IafToolMaterials(String name, int durability, float damage, float speed, int enchantability, TagKey<Block> inverted) {
        this.name = name;
        this.durability = durability;
        this.damage = damage;
        this.speed = speed;
        this.enchantability = enchantability;
        this.inverted = inverted;
    }

    public String getName() {
        return this.name;
    }

    // 26.2: Tier interface removed — expose the ToolMaterial record instead.
    public ToolMaterial toolMaterial() {
        return new ToolMaterial(this.inverted, this.durability, this.speed, this.damage, this.enchantability, this.repairItems);
    }

    // 26.2: repair is a TagKey<Item>, not an Ingredient.
    public void setRepairItems(TagKey<Item> repairItems) {
        this.repairItems = repairItems;
    }

    public static void init() {
        SILVER_TOOL_MATERIAL.setRepairItems(CommonItemTags.INGOTS_SILVER);
        DRAGONBONE_TOOL_MATERIAL.setRepairItems(IafItemTags.DRAGON_BONE);
        BLOODED_DRAGONBONE_TOOL_MATERIAL.setRepairItems(IafItemTags.DRAGON_BONE);
        TROLL_WEAPON_TOOL_MATERIAL.setRepairItems(ItemTags.STONE_CRAFTING_MATERIALS);
        HIPPOGRYPH_SWORD_TOOL_MATERIAL.setRepairItems(IafItemTags.HIPPOGRYPH_TALON);
        HIPPOCAMPUS_SWORD_TOOL_MATERIAL.setRepairItems(IafItemTags.SHINY_SCALES);
        AMPHITHERE_SWORD_TOOL_MATERIAL.setRepairItems(IafItemTags.AMPHITHERE_FEATHER);
        STYMHALIAN_SWORD_TOOL_MATERIAL.setRepairItems(IafItemTags.STYMPHALIAN_BIRD_FEATHER);
        DREAD_SWORD_TOOL_MATERIAL.setRepairItems(IafItemTags.DREAD_SHARD);
        DREAD_KNIGHT_TOOL_MATERIAL.setRepairItems(IafItemTags.DREAD_SHARD);
        COPPER_TOOL_MATERIAL.setRepairItems(IafItemTags.COPPER_INGOTS);
        DRAGONSTEEL_FIRE.setRepairItems(IafItemTags.DRAGON_STEELS);
        DRAGONSTEEL_ICE.setRepairItems(IafItemTags.DRAGON_STEELS);
        DRAGONSTEEL_LIGHTNING.setRepairItems(IafItemTags.DRAGON_STEELS);
    }
}
