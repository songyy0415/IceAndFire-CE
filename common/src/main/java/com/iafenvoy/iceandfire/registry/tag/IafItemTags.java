package com.iafenvoy.iceandfire.registry.tag;

import com.iafenvoy.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class IafItemTags {
    public static final TagKey<Item> SUMMON_GHOST_SWORD = create("summon_ghost_sword");
    public static final TagKey<Item> DRAGON_ARROWS = create("dragon_arrows");
    public static final TagKey<Item> DRAGON_BLOODS = create("dragon_bloods");
    public static final TagKey<Item> DRAGON_HEARTS = create("dragon_hearts");
    public static final TagKey<Item> BREED_AMPITHERE = create("breed_ampithere");
    public static final TagKey<Item> BREED_HIPPOCAMPUS = create("breed_hippocampus");
    public static final TagKey<Item> BREED_HIPPOGRYPH = create("breed_hippogryph");
    public static final TagKey<Item> HEAL_AMPITHERE = create("heal_ampithere");
    public static final TagKey<Item> HEAL_COCKATRICE = create("heal_cockatrice");
    public static final TagKey<Item> HEAL_HIPPOCAMPUS = create("heal_hippocampus");
    public static final TagKey<Item> HEAL_PIXIE = create("heal_pixie");
    public static final TagKey<Item> TAME_HIPPOGRYPH = create("tame_hippogryph");
    public static final TagKey<Item> TAME_PIXIE = create("tame_pixie");
    public static final TagKey<Item> TEMPT_DRAGON = create("tempt_dragon");
    public static final TagKey<Item> TEMPT_HIPPOCAMPUS = create("tempt_hippocampus");
    public static final TagKey<Item> TEMPT_HIPPOGRYPH = create("tempt_hippogryph");
    public static final TagKey<Item> PIXIE_STOLEN_BLACKLIST = create("pixie_stolen_blacklist");
    // Tool repair tags (26.2 ToolMaterial.repairItems requires TagKey<Item>)
    public static final TagKey<Item> DRAGON_BONE = create("dragon_bone");
    public static final TagKey<Item> HIPPOGRYPH_TALON = create("hippogryph_talon");
    public static final TagKey<Item> SHINY_SCALES = create("shiny_scales");
    public static final TagKey<Item> AMPHITHERE_FEATHER = create("amphithere_feather");
    public static final TagKey<Item> STYMPHALIAN_BIRD_FEATHER = create("stymphalian_bird_feather");
    public static final TagKey<Item> DREAD_SHARD = create("dread_shard");
    public static final TagKey<Item> COPPER_INGOTS = create("copper_ingots");
    public static final TagKey<Item> DRAGON_STEELS = create("dragon_steels");

    private static TagKey<Item> create(final String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
    }
}
