package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.compat.delight.DelightFoodItem;
import com.iafenvoy.iceandfire.data.DragonArmorMaterial;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.item.*;
import com.iafenvoy.iceandfire.item.ability.BuiltinAbilities;
import com.iafenvoy.iceandfire.item.armor.BlindfoldItem;
import com.iafenvoy.iceandfire.item.armor.DragonSteelArmorItem;
import com.iafenvoy.iceandfire.item.armor.EarPlugsArmorItem;
import com.iafenvoy.iceandfire.item.tool.*;
import com.iafenvoy.iceandfire.registry.tag.IafBannerPatternTags;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import java.util.function.Function;


@SuppressWarnings("unused")
public final class IafItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.ITEM);

    //Items
    public static final RegistrySupplier<Item> BESTIARY = registerItem("bestiary", BestiaryItem::new);
    public static final RegistrySupplier<Item> MANUSCRIPT = registerItem("manuscript", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> SAPPHIRE_GEM = registerItem("sapphire_gem", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> SILVER_INGOT = registerItem("silver_ingot", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> SILVER_NUGGET = registerItem("silver_nugget", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> RAW_SILVER = registerItem("raw_silver", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> COPPER_NUGGET = registerItem("copper_nugget", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> FIRE_STEW = registerItem("fire_stew", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> FROST_STEW = registerItem("frost_stew", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> LIGHTNING_STEW = registerItem("lightning_stew", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGONEGG_RED = registerItem("dragonegg_red", key -> new DragonEggItem(key, IafDragonColors.RED));
    public static final RegistrySupplier<Item> DRAGONEGG_GREEN = registerItem("dragonegg_green", key -> new DragonEggItem(key, IafDragonColors.GREEN));
    public static final RegistrySupplier<Item> DRAGONEGG_BRONZE = registerItem("dragonegg_bronze", key -> new DragonEggItem(key, IafDragonColors.BRONZE));
    public static final RegistrySupplier<Item> DRAGONEGG_GRAY = registerItem("dragonegg_gray", key -> new DragonEggItem(key, IafDragonColors.GRAY));
    public static final RegistrySupplier<Item> DRAGONEGG_BLUE = registerItem("dragonegg_blue", key -> new DragonEggItem(key, IafDragonColors.BLUE));
    public static final RegistrySupplier<Item> DRAGONEGG_WHITE = registerItem("dragonegg_white", key -> new DragonEggItem(key, IafDragonColors.WHITE));
    public static final RegistrySupplier<Item> DRAGONEGG_SAPPHIRE = registerItem("dragonegg_sapphire", key -> new DragonEggItem(key, IafDragonColors.SAPPHIRE));
    public static final RegistrySupplier<Item> DRAGONEGG_SILVER = registerItem("dragonegg_silver", key -> new DragonEggItem(key, IafDragonColors.SILVER));
    public static final RegistrySupplier<Item> DRAGONEGG_ELECTRIC = registerItem("dragonegg_electric", key -> new DragonEggItem(key, IafDragonColors.ELECTRIC));
    public static final RegistrySupplier<Item> DRAGONEGG_AMETHYST = registerItem("dragonegg_amethyst", key -> new DragonEggItem(key, IafDragonColors.AMETHYST));
    public static final RegistrySupplier<Item> DRAGONEGG_COPPER = registerItem("dragonegg_copper", key -> new DragonEggItem(key, IafDragonColors.COPPER));
    public static final RegistrySupplier<Item> DRAGONEGG_BLACK = registerItem("dragonegg_black", key -> new DragonEggItem(key, IafDragonColors.BLACK));
    public static final RegistrySupplier<Item> DRAGONSCALES_RED = registerItem("dragonscales_red", key -> new DragonScalesItem(key, IafDragonColors.RED));
    public static final RegistrySupplier<Item> DRAGONSCALES_GREEN = registerItem("dragonscales_green", key -> new DragonScalesItem(key, IafDragonColors.GREEN));
    public static final RegistrySupplier<Item> DRAGONSCALES_BRONZE = registerItem("dragonscales_bronze", key -> new DragonScalesItem(key, IafDragonColors.BRONZE));
    public static final RegistrySupplier<Item> DRAGONSCALES_GRAY = registerItem("dragonscales_gray", key -> new DragonScalesItem(key, IafDragonColors.GRAY));
    public static final RegistrySupplier<Item> DRAGONSCALES_BLUE = registerItem("dragonscales_blue", key -> new DragonScalesItem(key, IafDragonColors.BLUE));
    public static final RegistrySupplier<Item> DRAGONSCALES_WHITE = registerItem("dragonscales_white", key -> new DragonScalesItem(key, IafDragonColors.WHITE));
    public static final RegistrySupplier<Item> DRAGONSCALES_SAPPHIRE = registerItem("dragonscales_sapphire", key -> new DragonScalesItem(key, IafDragonColors.SAPPHIRE));
    public static final RegistrySupplier<Item> DRAGONSCALES_SILVER = registerItem("dragonscales_silver", key -> new DragonScalesItem(key, IafDragonColors.SILVER));
    public static final RegistrySupplier<Item> DRAGONSCALES_ELECTRIC = registerItem("dragonscales_electric", key -> new DragonScalesItem(key, IafDragonColors.ELECTRIC));
    public static final RegistrySupplier<Item> DRAGONSCALES_AMETHYST = registerItem("dragonscales_amethyst", key -> new DragonScalesItem(key, IafDragonColors.AMETHYST));
    public static final RegistrySupplier<Item> DRAGONSCALES_COPPER = registerItem("dragonscales_copper", key -> new DragonScalesItem(key, IafDragonColors.COPPER));
    public static final RegistrySupplier<Item> DRAGONSCALES_BLACK = registerItem("dragonscales_black", key -> new DragonScalesItem(key, IafDragonColors.BLACK));
    public static final RegistrySupplier<Item> DRAGON_BONE = registerItem("dragonbone", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> WITHERBONE = registerItem("witherbone", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> WITHER_SHARD = registerItem("wither_shard", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGON_SKULL_FIRE = registerItem("dragon_skull_fire", key -> new DragonSkullItem(key, IafDragonTypes.FIRE));
    public static final RegistrySupplier<Item> DRAGON_SKULL_ICE = registerItem("dragon_skull_ice", key -> new DragonSkullItem(key, IafDragonTypes.ICE));
    public static final RegistrySupplier<Item> DRAGON_SKULL_LIGHTNING = registerItem("dragon_skull_lightning", key -> new DragonSkullItem(key, IafDragonTypes.LIGHTNING));
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_IRON_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.IRON);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_IRON_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.IRON);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_IRON_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.IRON);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_IRON_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.IRON);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_COPPER_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.COPPER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_COPPER_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.COPPER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_COPPER_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.COPPER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_COPPER_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.COPPER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_SILVER_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.SILVER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_SILVER_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.SILVER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_SILVER_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.SILVER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_SILVER_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.SILVER);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_GOLD_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.GOLD);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_GOLD_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.GOLD);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_GOLD_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.GOLD);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_GOLD_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.GOLD);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DIAMOND_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.DIAMOND);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DIAMOND_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.DIAMOND);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DIAMOND_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.DIAMOND);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DIAMOND_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.DIAMOND);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_NETHERITE_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.NETHERITE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_NETHERITE_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.NETHERITE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_NETHERITE_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.NETHERITE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_NETHERITE_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.NETHERITE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_FIRE_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.DRAGON_STEEL_FIRE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_FIRE_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.DRAGON_STEEL_FIRE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_FIRE_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.DRAGON_STEEL_FIRE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_FIRE_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.DRAGON_STEEL_FIRE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_ICE_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.DRAGON_STEEL_ICE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_ICE_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.DRAGON_STEEL_ICE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_ICE_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.DRAGON_STEEL_ICE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_ICE_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.DRAGON_STEEL_ICE);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_HEAD = buildDragonArmor(DragonArmorPart.HEAD, DragonArmorMaterial.DRAGON_STEEL_LIGHTNING);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_NECK = buildDragonArmor(DragonArmorPart.NECK, DragonArmorMaterial.DRAGON_STEEL_LIGHTNING);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_BODY = buildDragonArmor(DragonArmorPart.BODY, DragonArmorMaterial.DRAGON_STEEL_LIGHTNING);
    public static final RegistrySupplier<DragonArmorItem> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_TAIL = buildDragonArmor(DragonArmorPart.TAIL, DragonArmorMaterial.DRAGON_STEEL_LIGHTNING);
    public static final RegistrySupplier<Item> DRAGON_MEAL = registerItem("dragon_meal", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> SICKLY_DRAGON_MEAL = registerItem("sickly_dragon_meal", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> CREATIVE_DRAGON_MEAL = registerItem("creative_dragon_meal", key -> new GenericItem(key, 2));
    public static final RegistrySupplier<Item> FIRE_DRAGON_FLESH = registerItem("fire_dragon_flesh", key -> new DragonFleshItem(key, IafDragonTypes.FIRE));
    public static final RegistrySupplier<Item> ICE_DRAGON_FLESH = registerItem("ice_dragon_flesh", key -> new DragonFleshItem(key, IafDragonTypes.ICE));
    public static final RegistrySupplier<Item> LIGHTNING_DRAGON_FLESH = registerItem("lightning_dragon_flesh", key -> new DragonFleshItem(key, IafDragonTypes.LIGHTNING));
    public static final RegistrySupplier<Item> FIRE_DRAGON_HEART = registerItem("fire_dragon_heart", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> ICE_DRAGON_HEART = registerItem("ice_dragon_heart", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> LIGHTNING_DRAGON_HEART = registerItem("lightning_dragon_heart", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> FIRE_DRAGON_BLOOD = registerItem("fire_dragon_blood", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> ICE_DRAGON_BLOOD = registerItem("ice_dragon_blood", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> LIGHTNING_DRAGON_BLOOD = registerItem("lightning_dragon_blood", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGON_STAFF = registerItem("dragon_stick", key -> new Item(new Item.Properties().setId(key).stacksTo(1)));
    public static final RegistrySupplier<Item> DRAGON_HORN = registerItem("dragon_horn", DragonHornItem::new);
    public static final RegistrySupplier<Item> DRAGON_FLUTE = registerItem("dragon_flute", DragonFluteItem::new);
    public static final RegistrySupplier<Item> SUMMONING_CRYSTAL_FIRE = registerItem("summoning_crystal_fire", SummoningCrystalItem::new);
    public static final RegistrySupplier<Item> SUMMONING_CRYSTAL_ICE = registerItem("summoning_crystal_ice", SummoningCrystalItem::new);
    public static final RegistrySupplier<Item> SUMMONING_CRYSTAL_LIGHTNING = registerItem("summoning_crystal_lightning", SummoningCrystalItem::new);
    public static final RegistrySupplier<Item> HIPPOGRYPH_EGG = registerItem("hippogryph_egg", HippogryphEggItem::new);
    public static final RegistrySupplier<Item> IRON_HIPPOGRYPH_ARMOR = registerItem("iron_hippogryph_armor", key -> new Item(new Item.Properties().setId(key).stacksTo(1)));
    public static final RegistrySupplier<Item> GOLD_HIPPOGRYPH_ARMOR = registerItem("gold_hippogryph_armor", key -> new Item(new Item.Properties().setId(key).stacksTo(1)));
    public static final RegistrySupplier<Item> DIAMOND_HIPPOGRYPH_ARMOR = registerItem("diamond_hippogryph_armor", key -> new Item(new Item.Properties().setId(key).stacksTo(1)));
    public static final RegistrySupplier<Item> NETHERITE_HIPPOGRYPH_ARMOR = registerItem("netherite_hippogryph_armor", key -> new Item(new Item.Properties().setId(key).stacksTo(1).fireResistant()));
    public static final RegistrySupplier<Item> HIPPOGRYPH_TALON = registerItem("hippogryph_talon", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> STONE_STATUE = registerItem("stone_statue", StoneStatueItem::new);
    public static final RegistrySupplier<Item> BLINDFOLD = registerItem("blindfold", BlindfoldItem::new);
    public static final RegistrySupplier<Item> PIXIE_DUST = registerItem("pixie_dust", key -> new Item(new Item.Properties().setId(key).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).alwaysEdible().build(), foodConsumable(new MobEffectInstance(MobEffects.LEVITATION, 100, 1), new MobEffectInstance(MobEffects.GLOWING, 100, 1)))));
    public static final RegistrySupplier<Item> PIXIE_WINGS = registerItem("pixie_wings", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> AMBROSIA = registerItem("ambrosia", key -> new Item(new Item.Properties().setId(key).stacksTo(1).usingConvertsTo(Items.BOWL).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).alwaysEdible().build(), foodConsumable(new MobEffectInstance(MobEffects.STRENGTH, 3600, 2), new MobEffectInstance(MobEffects.ABSORPTION, 3600, 2), new MobEffectInstance(MobEffects.JUMP_BOOST, 3600, 2), new MobEffectInstance(MobEffects.LUCK, 3600, 2)))));
    public static final RegistrySupplier<Item> SHINY_SCALES = registerItem("shiny_scales", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> SIREN_TEAR = registerItem("siren_tear", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> HIPPOCAMPUS_FIN = registerItem("hippocampus_fin", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> EARPLUGS = registerItem("earplugs", EarPlugsArmorItem::new);
    public static final RegistrySupplier<Item> DEATH_WORM_CHITIN_YELLOW = registerItem("deathworm_chitin_yellow", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DEATH_WORM_CHITIN_WHITE = registerItem("deathworm_chitin_white", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DEATH_WORM_CHITIN_RED = registerItem("deathworm_chitin_red", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DEATHWORM_EGG = registerItem("deathworm_egg", key -> new DeathwormEggItem(key, false));
    public static final RegistrySupplier<Item> DEATHWORM_EGG_GIGANTIC = registerItem("deathworm_egg_giant", key -> new DeathwormEggItem(key, true));
    public static final RegistrySupplier<Item> DEATHWORM_TOUNGE = registerItem("deathworm_tounge", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> ROTTEN_EGG = registerItem("rotten_egg", RottenEggItem::new);
    public static final RegistrySupplier<Item> COCKATRICE_EYE = registerItem("cockatrice_eye", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> STYMPHALIAN_BIRD_FEATHER = registerItem("stymphalian_bird_feather", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> TROLL_TUSK = registerItem("troll_tusk", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> AMPHITHERE_FEATHER = registerItem("amphithere_feather", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> AMPHITHERE_ARROW = registerItem("amphithere_arrow", AmphithereArrowItem::new);
    public static final RegistrySupplier<Item> SERPENT_FANG = registerItem("sea_serpent_fang", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> SEA_SERPENT_ARROW = registerItem("sea_serpent_arrow", SeaSerpentArrowItem::new);
    public static final RegistrySupplier<Item> CHAIN = registerItem("chain", key -> new ChainItem(key, false));
    public static final RegistrySupplier<Item> CHAIN_STICKY = registerItem("chain_sticky", key -> new ChainItem(key, true));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_INGOT = registerItem("dragonsteel_fire_ingot", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_INGOT = registerItem("dragonsteel_ice_ingot", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_INGOT = registerItem("dragonsteel_lightning_ingot", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DREAD_SHARD = registerItem("dread_shard", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DREAD_KEY = registerItem("dread_key", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> HYDRA_FANG = registerItem("hydra_fang", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> HYDRA_HEART = registerItem("hydra_heart", HydraHeartItem::new);
    public static final RegistrySupplier<Item> HYDRA_ARROW = registerItem("hydra_arrow", HydraArrowItem::new);
    public static final RegistrySupplier<Item> CANNOLI = registerItem("cannoli", CannoliItem::new);
    public static final RegistrySupplier<Item> ECTOPLASM = registerItem("ectoplasm", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> GHOST_INGOT = registerItem("ghost_ingot", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> DRAGON_SEEKER = registerItem("dragon_seeker", key -> new DragonSeekerItem(key, DragonSeekerItem.SeekerType.NORMAL));
    public static final RegistrySupplier<Item> EPIC_DRAGON_SEEKER = registerItem("epic_dragon_seeker", key -> new DragonSeekerItem(key, DragonSeekerItem.SeekerType.EPIC));
    public static final RegistrySupplier<Item> LEGENDARY_DRAGON_SEEKER = registerItem("legendary_dragon_seeker", key -> new DragonSeekerItem(key, DragonSeekerItem.SeekerType.LEGENDARY));
    public static final RegistrySupplier<Item> GODLY_DRAGON_SEEKER = registerItem("godly_dragon_seeker", key -> new DragonSeekerItem(key, DragonSeekerItem.SeekerType.GODLY));
    public static final RegistrySupplier<Item> PATTERN_FIRE = registerItem("banner_pattern_fire", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.FIRE_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_ICE = registerItem("banner_pattern_ice", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.ICE_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_LIGHTNING = registerItem("banner_pattern_lightning", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.LIGHTNING_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_FIRE_HEAD = registerItem("banner_pattern_fire_head", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.FIRE_HEAD_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_ICE_HEAD = registerItem("banner_pattern_ice_head", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.ICE_HEAD_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_LIGHTNING_HEAD = registerItem("banner_pattern_lightning_head", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.LIGHTNING_HEAD_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_AMPHITHERE = registerItem("banner_pattern_amphithere", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.AMPHITHERE_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_BIRD = registerItem("banner_pattern_bird", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.BIRD_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_EYE = registerItem("banner_pattern_eye", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.EYE_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_FAE = registerItem("banner_pattern_fae", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.FAE_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_FEATHER = registerItem("banner_pattern_feather", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.FEATHER_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_GORGON = registerItem("banner_pattern_gorgon", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.GORGON_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_HIPPOCAMPUS = registerItem("banner_pattern_hippocampus", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.HIPPOCAMPUS_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_HIPPOGRYPH_HEAD = registerItem("banner_pattern_hippogryph_head", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.HIPPOGRYPH_HEAD_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_MERMAID = registerItem("banner_pattern_mermaid", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.MERMAID_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_SEA_SERPENT = registerItem("banner_pattern_sea_serpent", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.SEA_SERPENT_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_TROLL = registerItem("banner_pattern_troll", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.TROLL_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_WEEZER = registerItem("banner_pattern_weezer", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.WEEZER_BANNER_PATTERN))));
    public static final RegistrySupplier<Item> PATTERN_DREAD = registerItem("banner_pattern_dread", key -> new Item(new Item.Properties().setId(key).stacksTo(1).delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS, context -> context.getOrThrow(IafBannerPatternTags.DREAD_BANNER_PATTERN))));
    public static final RegistrySupplier<DelightFoodItem> COOKED_RICE_WITH_FIRE_DRAGON_MEAT = registerItem("cooked_rice_with_fire_dragon_meat", key -> new DelightFoodItem(new Item.Properties().setId(key).stacksTo(1).usingConvertsTo(Items.BOWL).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build(), foodConsumable(new MobEffectInstance(MobEffects.SATURATION, 20 * 5), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60 * 2)))));
    public static final RegistrySupplier<DelightFoodItem> COOKED_RICE_WITH_ICE_DRAGON_MEAT = registerItem("cooked_rice_with_ice_dragon_meat", key -> new DelightFoodItem(new Item.Properties().setId(key).stacksTo(1).usingConvertsTo(Items.BOWL).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build(), foodConsumable(new MobEffectInstance(MobEffects.SATURATION, 20 * 5), new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 60 * 2, 2)))));
    public static final RegistrySupplier<DelightFoodItem> COOKED_RICE_WITH_LIGHTNING_DRAGON_MEAT = registerItem("cooked_rice_with_lightning_dragon_meat", key -> new DelightFoodItem(new Item.Properties().setId(key).stacksTo(1).usingConvertsTo(Items.BOWL).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build(), foodConsumable(new MobEffectInstance(MobEffects.SATURATION, 20 * 5), new MobEffectInstance(MobEffects.SPEED, 20 * 60 * 2, 2)))));
    public static final RegistrySupplier<DelightFoodItem> GHOST_CREAM = registerItem("ghost_cream", key -> new DelightFoodItem(new Item.Properties().setId(key).stacksTo(1).usingConvertsTo(Items.GLASS_BOTTLE).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build(), fastFoodConsumable(new MobEffectInstance(MobEffects.LEVITATION, 20 * 20)))));
    public static final RegistrySupplier<DelightFoodItem> PIXIE_DUST_MILKY_TEA = registerItem("pixie_dust_milky_tea", key -> new DelightFoodItem(new Item.Properties().setId(key).stacksTo(1).usingConvertsTo(Items.GLASS_BOTTLE).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build(), fastFoodConsumable(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60 * 2)))));

    //spawn Eggs
    static {
        registerItem("spawn_egg_fire_dragon", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.FIRE_DRAGON.get())));
        registerItem("spawn_egg_ice_dragon", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.ICE_DRAGON.get())));
        registerItem("spawn_egg_lightning_dragon", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.LIGHTNING_DRAGON.get())));
        registerItem("spawn_egg_hippogryph", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.HIPPOGRYPH.get())));
        registerItem("spawn_egg_gorgon", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.GORGON.get())));
        registerItem("spawn_egg_pixie", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.PIXIE.get())));
        registerItem("spawn_egg_cyclops", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.CYCLOPS.get())));
        registerItem("spawn_egg_siren", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.SIREN.get())));
        registerItem("spawn_egg_hippocampus", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.HIPPOCAMPUS.get())));
        registerItem("spawn_egg_death_worm", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DEATH_WORM.get())));
        registerItem("spawn_egg_cockatrice", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.COCKATRICE.get())));
        registerItem("spawn_egg_stymphalian_bird", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.STYMPHALIAN_BIRD.get())));
        registerItem("spawn_egg_troll", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.TROLL.get())));
        registerItem("spawn_egg_amphithere", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.AMPHITHERE.get())));
        registerItem("spawn_egg_sea_serpent", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.SEA_SERPENT.get())));
        registerItem("spawn_egg_dread_thrall", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_THRALL.get())));
        registerItem("spawn_egg_dread_ghoul", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_GHOUL.get())));
        registerItem("spawn_egg_dread_beast", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_BEAST.get())));
        registerItem("spawn_egg_dread_scuttler", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_SCUTTLER.get())));
        registerItem("spawn_egg_lich", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_LICH.get())));
        registerItem("spawn_egg_dread_knight", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_KNIGHT.get())));
        registerItem("spawn_egg_dread_horse", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.DREAD_HORSE.get())));
        registerItem("spawn_egg_hydra", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.HYDRA.get())));
        registerItem("spawn_egg_ghost", key -> new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(IafEntities.GHOST.get())));
    }

    //Hidden
    public static final RegistrySupplier<Item> TIDE_TRIDENT_INVENTORY = register("tide_trident_inventory", key -> new Item(new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> WEEZER_BLUE_ALBUM = register("weezer_blue_album", key -> new GenericItem(key, 1));
    public static final RegistrySupplier<Item> DRAGON_DEBUG_STICK = register("dragon_debug_stick", key -> new GenericItem(key, 1));

    //Armors
    public static final RegistrySupplier<Item> SILVER_HELMET = registerArmor("armor_silver_metal_helmet", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SILVER, ArmorType.HELMET).durability(165)));
    public static final RegistrySupplier<Item> SILVER_CHESTPLATE = registerArmor("armor_silver_metal_chestplate", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SILVER, ArmorType.CHESTPLATE).durability(240)));
    public static final RegistrySupplier<Item> SILVER_LEGGINGS = registerArmor("armor_silver_metal_leggings", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SILVER, ArmorType.LEGGINGS).durability(225)));
    public static final RegistrySupplier<Item> SILVER_BOOTS = registerArmor("armor_silver_metal_boots", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SILVER, ArmorType.BOOTS).durability(195)));
    public static final RegistrySupplier<Item> COPPER_HELMET = registerArmor("armor_copper_metal_helmet", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.COPPER, ArmorType.HELMET).durability(111)));
    public static final RegistrySupplier<Item> COPPER_CHESTPLATE = registerArmor("armor_copper_metal_chestplate", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.COPPER, ArmorType.CHESTPLATE).durability(161)));
    public static final RegistrySupplier<Item> COPPER_LEGGINGS = registerArmor("armor_copper_metal_leggings", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.COPPER, ArmorType.LEGGINGS).durability(151)));
    public static final RegistrySupplier<Item> COPPER_BOOTS = registerArmor("armor_copper_metal_boots", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.COPPER, ArmorType.BOOTS).durability(131)));
    public static final RegistrySupplier<Item> SHEEP_HELMET = registerArmor("sheep_helmet", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SHEEP, ArmorType.HELMET).durability(55)));
    public static final RegistrySupplier<Item> SHEEP_CHESTPLATE = registerArmor("sheep_chestplate", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SHEEP, ArmorType.CHESTPLATE).durability(80)));
    public static final RegistrySupplier<Item> SHEEP_LEGGINGS = registerArmor("sheep_leggings", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SHEEP, ArmorType.LEGGINGS).durability(75)));
    public static final RegistrySupplier<Item> SHEEP_BOOTS = registerArmor("sheep_boots", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.SHEEP, ArmorType.BOOTS).durability(65)));
    public static final RegistrySupplier<Item> DEATHWORM_YELLOW_HELMET = registerArmor("deathworm_yellow_helmet", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_YELLOW, ArmorType.HELMET).durability(165)));
    public static final RegistrySupplier<Item> DEATHWORM_YELLOW_CHESTPLATE = registerArmor("deathworm_yellow_chestplate", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_YELLOW, ArmorType.CHESTPLATE).durability(240)));
    public static final RegistrySupplier<Item> DEATHWORM_YELLOW_LEGGINGS = registerArmor("deathworm_yellow_leggings", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_YELLOW, ArmorType.LEGGINGS).durability(225)));
    public static final RegistrySupplier<Item> DEATHWORM_YELLOW_BOOTS = registerArmor("deathworm_yellow_boots", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_YELLOW, ArmorType.BOOTS).durability(195)));
    public static final RegistrySupplier<Item> DEATHWORM_WHITE_HELMET = registerArmor("deathworm_white_helmet", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_WHITE, ArmorType.HELMET).durability(165)));
    public static final RegistrySupplier<Item> DEATHWORM_WHITE_CHESTPLATE = registerArmor("deathworm_white_chestplate", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_WHITE, ArmorType.CHESTPLATE).durability(240)));
    public static final RegistrySupplier<Item> DEATHWORM_WHITE_LEGGINGS = registerArmor("deathworm_white_leggings", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_WHITE, ArmorType.LEGGINGS).durability(225)));
    public static final RegistrySupplier<Item> DEATHWORM_WHITE_BOOTS = registerArmor("deathworm_white_boots", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_WHITE, ArmorType.BOOTS).durability(165)));
    public static final RegistrySupplier<Item> DEATHWORM_RED_HELMET = registerArmor("deathworm_red_helmet", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_RED, ArmorType.HELMET).durability(165)));
    public static final RegistrySupplier<Item> DEATHWORM_RED_CHESTPLATE = registerArmor("deathworm_red_chestplate", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_RED, ArmorType.CHESTPLATE).durability(240)));
    public static final RegistrySupplier<Item> DEATHWORM_RED_LEGGINGS = registerArmor("deathworm_red_leggings", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_RED, ArmorType.LEGGINGS).durability(225)));
    public static final RegistrySupplier<Item> DEATHWORM_RED_BOOTS = registerArmor("deathworm_red_boots", key -> new Item(new Item.Properties().setId(key).humanoidArmor(IafArmorMaterials.DEATHWORM_RED, ArmorType.BOOTS).durability(165)));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_HELMET = registerArmor("dragonsteel_fire_helmet", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_FIRE, ArmorType.HELMET));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_CHESTPLATE = registerArmor("dragonsteel_fire_chestplate", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_FIRE, ArmorType.CHESTPLATE));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_LEGGINGS = registerArmor("dragonsteel_fire_leggings", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_FIRE, ArmorType.LEGGINGS));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_BOOTS = registerArmor("dragonsteel_fire_boots", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_FIRE, ArmorType.BOOTS));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_HELMET = registerArmor("dragonsteel_ice_helmet", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_ICE, ArmorType.HELMET));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_CHESTPLATE = registerArmor("dragonsteel_ice_chestplate", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_ICE, ArmorType.CHESTPLATE));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_LEGGINGS = registerArmor("dragonsteel_ice_leggings", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_ICE, ArmorType.LEGGINGS));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_BOOTS = registerArmor("dragonsteel_ice_boots", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_ICE, ArmorType.BOOTS));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_HELMET = registerArmor("dragonsteel_lightning_helmet", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_LIGHTNING, ArmorType.HELMET));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_CHESTPLATE = registerArmor("dragonsteel_lightning_chestplate", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_LIGHTNING, ArmorType.CHESTPLATE));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_LEGGINGS = registerArmor("dragonsteel_lightning_leggings", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_LIGHTNING, ArmorType.LEGGINGS));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_BOOTS = registerArmor("dragonsteel_lightning_boots", key -> new DragonSteelArmorItem(key, IafArmorMaterials.DRAGONSTEEL_LIGHTNING, ArmorType.BOOTS));

    //Tools&Weapons
    public static final RegistrySupplier<Item> SILVER_SWORD = registerToolOrWeapon("silver_sword", key -> new ActivePostHitSwordItem( IafToolMaterials.SILVER_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.UNDEAD_DAMAGE_BONUS));
    public static final RegistrySupplier<Item> SILVER_SHOVEL = registerToolOrWeapon("silver_shovel", key -> new ActivePostHitShovelItem( IafToolMaterials.SILVER_TOOL_MATERIAL.toolMaterial(), 1.5F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.UNDEAD_DAMAGE_BONUS));
    public static final RegistrySupplier<Item> SILVER_PICKAXE = registerToolOrWeapon("silver_pickaxe", key -> new ActivePostHitPickaxeItem( IafToolMaterials.SILVER_TOOL_MATERIAL.toolMaterial(), 1.0F, -2.8F, new Item.Properties().setId(key), BuiltinAbilities.UNDEAD_DAMAGE_BONUS));
    public static final RegistrySupplier<Item> SILVER_AXE = registerToolOrWeapon("silver_axe", key -> new ActivePostHitAxeItem( IafToolMaterials.SILVER_TOOL_MATERIAL.toolMaterial(), 6.0F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.UNDEAD_DAMAGE_BONUS));
    public static final RegistrySupplier<Item> SILVER_HOE = registerToolOrWeapon("silver_hoe", key -> new ActivePostHitHoeItem( IafToolMaterials.SILVER_TOOL_MATERIAL.toolMaterial(), 0.0F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.UNDEAD_DAMAGE_BONUS));
    public static final RegistrySupplier<Item> COPPER_SWORD = registerToolOrWeapon("copper_sword", key -> new Item(new Item.Properties().setId(key).sword(IafToolMaterials.COPPER_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F)));
    public static final RegistrySupplier<Item> COPPER_SHOVEL = registerToolOrWeapon("copper_shovel", key -> new ShovelItem( IafToolMaterials.COPPER_TOOL_MATERIAL.toolMaterial(), 1.5F, -2.4F, new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> COPPER_PICKAXE = registerToolOrWeapon("copper_pickaxe", key -> new Item(new Item.Properties().setId(key).pickaxe(IafToolMaterials.COPPER_TOOL_MATERIAL.toolMaterial(), 1.0F, -2.8F)));
    public static final RegistrySupplier<Item> COPPER_AXE = registerToolOrWeapon("copper_axe", key -> new AxeItem( IafToolMaterials.COPPER_TOOL_MATERIAL.toolMaterial(), 6.0F, -3.0F, new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> COPPER_HOE = registerToolOrWeapon("copper_hoe", key -> new HoeItem( IafToolMaterials.COPPER_TOOL_MATERIAL.toolMaterial(), 0.0F, -1.0F, new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> FISHING_SPEAR = registerToolOrWeapon("fishing_spear", key -> new Item(new Item.Properties().setId(key).durability(64)));
    public static final RegistrySupplier<Item> DRAGONBONE_SWORD = registerToolOrWeapon("dragonbone_sword", key -> new Item(new Item.Properties().setId(key).sword(IafToolMaterials.DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F)));
    public static final RegistrySupplier<Item> DRAGONBONE_SHOVEL = registerToolOrWeapon("dragonbone_shovel", key -> new ShovelItem( IafToolMaterials.DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 1.5F, -2.8F, new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGONBONE_PICKAXE = registerToolOrWeapon("dragonbone_pickaxe", key -> new Item(new Item.Properties().setId(key).pickaxe(IafToolMaterials.DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 1.0F, -2.8F)));
    public static final RegistrySupplier<Item> DRAGONBONE_AXE = registerToolOrWeapon("dragonbone_axe", key -> new AxeItem( IafToolMaterials.DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 5.0F, -3.0F, new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGONBONE_HOE = registerToolOrWeapon("dragonbone_hoe", key -> new HoeItem( IafToolMaterials.DRAGONBONE_TOOL_MATERIAL.toolMaterial(), -4.0F, 0.0F, new Item.Properties().setId(key)));
    public static final RegistrySupplier<Item> DRAGONBONE_ARROW = registerToolOrWeapon("dragonbone_arrow", DragonArrowItem::new);
    public static final RegistrySupplier<Item> DRAGON_BOW = registerToolOrWeapon("dragonbone_bow", DragonBowItem::new);
    public static final RegistrySupplier<Item> STYMPHALIAN_ARROW = registerToolOrWeapon("stymphalian_arrow", StymphalianArrowItem::new);
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_SWORD = registerToolOrWeapon("dragonsteel_fire_sword", key -> new ActivePostHitSwordItem( IafToolMaterials.DRAGONSTEEL_FIRE.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_FIRE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_PICKAXE = registerToolOrWeapon("dragonsteel_fire_pickaxe", key -> new ActivePostHitPickaxeItem( IafToolMaterials.DRAGONSTEEL_FIRE.toolMaterial(), 1.0F, -2.8F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_FIRE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_AXE = registerToolOrWeapon("dragonsteel_fire_axe", key -> new ActivePostHitAxeItem( IafToolMaterials.DRAGONSTEEL_FIRE.toolMaterial(), 5.0F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_FIRE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_SHOVEL = registerToolOrWeapon("dragonsteel_fire_shovel", key -> new ActivePostHitShovelItem( IafToolMaterials.DRAGONSTEEL_FIRE.toolMaterial(), 1.5F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_FIRE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_FIRE_HOE = registerToolOrWeapon("dragonsteel_fire_hoe", key -> new ActivePostHitHoeItem( IafToolMaterials.DRAGONSTEEL_FIRE.toolMaterial(), -4.0F, 0.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_FIRE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_SWORD = registerToolOrWeapon("dragonsteel_ice_sword", key -> new ActivePostHitSwordItem( IafToolMaterials.DRAGONSTEEL_ICE.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_ICE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_PICKAXE = registerToolOrWeapon("dragonsteel_ice_pickaxe", key -> new ActivePostHitPickaxeItem( IafToolMaterials.DRAGONSTEEL_ICE.toolMaterial(), 1.0F, -2.8F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_ICE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_AXE = registerToolOrWeapon("dragonsteel_ice_axe", key -> new ActivePostHitAxeItem( IafToolMaterials.DRAGONSTEEL_ICE.toolMaterial(), 5.0F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_ICE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_SHOVEL = registerToolOrWeapon("dragonsteel_ice_shovel", key -> new ActivePostHitShovelItem( IafToolMaterials.DRAGONSTEEL_ICE.toolMaterial(), 1.5F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_ICE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_ICE_HOE = registerToolOrWeapon("dragonsteel_ice_hoe", key -> new ActivePostHitHoeItem( IafToolMaterials.DRAGONSTEEL_ICE.toolMaterial(), -4.0F, 0.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_ICE_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_SWORD = registerToolOrWeapon("dragonsteel_lightning_sword", key -> new ActivePostHitSwordItem( IafToolMaterials.DRAGONSTEEL_LIGHTNING.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_LIGHTNING_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_PICKAXE = registerToolOrWeapon("dragonsteel_lightning_pickaxe", key -> new ActivePostHitPickaxeItem( IafToolMaterials.DRAGONSTEEL_LIGHTNING.toolMaterial(), 1.0F, -2.8F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_LIGHTNING_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_AXE = registerToolOrWeapon("dragonsteel_lightning_axe", key -> new ActivePostHitAxeItem( IafToolMaterials.DRAGONSTEEL_LIGHTNING.toolMaterial(), 5.0F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_LIGHTNING_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_SHOVEL = registerToolOrWeapon("dragonsteel_lightning_shovel", key -> new ActivePostHitShovelItem( IafToolMaterials.DRAGONSTEEL_LIGHTNING.toolMaterial(), 1.5F, -3.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_LIGHTNING_TOOL));
    public static final RegistrySupplier<Item> DRAGONSTEEL_LIGHTNING_HOE = registerToolOrWeapon("dragonsteel_lightning_hoe", key -> new ActivePostHitHoeItem( IafToolMaterials.DRAGONSTEEL_LIGHTNING.toolMaterial(), -4.0F, 0.0F, new Item.Properties().setId(key), BuiltinAbilities.DRAGONSTEEL_LIGHTNING_TOOL));
    public static final RegistrySupplier<Item> DREAD_SWORD = registerToolOrWeapon("dread_sword", key -> new Item(new Item.Properties().setId(key).sword(IafToolMaterials.DREAD_SWORD_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F)));
    public static final RegistrySupplier<Item> DREAD_KNIGHT_SWORD = registerToolOrWeapon("dread_knight_sword", key -> new Item(new Item.Properties().setId(key).sword(IafToolMaterials.DREAD_KNIGHT_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F)));
    public static final RegistrySupplier<Item> LICH_STAFF = registerToolOrWeapon("lich_staff", LichStaffItem::new);
    public static final RegistrySupplier<Item> DREAD_QUEEN_SWORD = registerToolOrWeapon("dread_queen_sword", key -> new Item(new Item.Properties().setId(key).sword(IafToolMaterials.DREAD_QUEEN.toolMaterial(), 3.0F, -2.4F)));
    public static final RegistrySupplier<Item> DREAD_QUEEN_STAFF = registerToolOrWeapon("dread_queen_staff", DreadQueenStaffItem::new);
    //--Legendary
    public static final RegistrySupplier<Item> DRAGONBONE_SWORD_FIRE = registerToolOrWeapon("dragonbone_sword_fire", key -> new ActivePostHitSwordItem( IafToolMaterials.BLOODED_DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.FIRE_DRAGON_BLOOD_TOOL));
    public static final RegistrySupplier<Item> DRAGONBONE_SWORD_ICE = registerToolOrWeapon("dragonbone_sword_ice", key -> new ActivePostHitSwordItem( IafToolMaterials.BLOODED_DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.ICE_DRAGON_BLOOD_TOOL));
    public static final RegistrySupplier<Item> DRAGONBONE_SWORD_LIGHTNING = registerToolOrWeapon("dragonbone_sword_lightning", key -> new ActivePostHitSwordItem( IafToolMaterials.BLOODED_DRAGONBONE_TOOL_MATERIAL.toolMaterial(), 3.0F, -2.4F, new Item.Properties().setId(key), BuiltinAbilities.LIGHTNING_DRAGON_BLOOD_TOOL));
    public static final RegistrySupplier<Item> HIPPOGRYPH_SWORD = registerToolOrWeapon("hippogryph_sword", HippogryphSwordItem::new);
    public static final RegistrySupplier<Item> GORGON_HEAD = registerToolOrWeapon("gorgon_head", GorgonHeadItem::new);
    public static final RegistrySupplier<Item> PIXIE_WAND = registerToolOrWeapon("pixie_wand", PixieWandItem::new);
    public static final RegistrySupplier<Item> CYCLOPS_EYE = registerToolOrWeapon("cyclops_eye", CyclopsEyeItem::new);
    public static final RegistrySupplier<Item> SIREN_FLUTE = registerToolOrWeapon("siren_flute", SirenFluteItem::new);
    public static final RegistrySupplier<Item> HIPPOCAMPUS_SLAPPER = registerToolOrWeapon("hippocampus_slapper", HippocampusSlapperItem::new);
    public static final RegistrySupplier<Item> DEATHWORM_GAUNTLET_YELLOW = registerToolOrWeapon("deathworm_gauntlet_yellow", DeathwormGauntletItem::new);
    public static final RegistrySupplier<Item> DEATHWORM_GAUNTLET_WHITE = registerToolOrWeapon("deathworm_gauntlet_white", DeathwormGauntletItem::new);
    public static final RegistrySupplier<Item> DEATHWORM_GAUNTLET_RED = registerToolOrWeapon("deathworm_gauntlet_red", DeathwormGauntletItem::new);
    public static final RegistrySupplier<Item> COCKATRICE_SCEPTER = registerToolOrWeapon("cockatrice_scepter", CockatriceScepterItem::new);
    public static final RegistrySupplier<Item> STYMPHALIAN_FEATHER_BUNDLE = registerToolOrWeapon("stymphalian_feather_bundle", StymphalianFeatherBundleItem::new);
    public static final RegistrySupplier<Item> STYMPHALIAN_DAGGER = registerToolOrWeapon("stymphalian_bird_dagger", StymphalianDaggerItem::new);
    public static final RegistrySupplier<Item> AMPHITHERE_MACUAHUITL = registerToolOrWeapon("amphithere_macuahuitl", AmphithereMacuahuitlItem::new);
    public static final RegistrySupplier<Item> TIDE_TRIDENT = registerToolOrWeapon("tide_trident", TideTridentItem::new);
    public static final RegistrySupplier<Item> GHOST_SWORD = registerToolOrWeapon("ghost_sword", GhostSwordItem::new);

    private static Consumable foodConsumable(MobEffectInstance... effects) {
        Consumable.Builder builder = Consumable.builder();
        for (MobEffectInstance effect : effects)
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect, 1.0F));
        return builder.build();
    }

    private static Consumable fastFoodConsumable(MobEffectInstance... effects) {
        Consumable.Builder builder = Consumable.builder().consumeSeconds(0.6F);
        for (MobEffectInstance effect : effects)
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect, 1.0F));
        return builder.build();
    }

    public static RegistrySupplier<DragonArmorItem> buildDragonArmor(DragonArmorPart type, DragonArmorMaterial material) {
        return registerItem(String.format("dragonarmor_%s_%s", material.name(), type.getId()), key -> new DragonArmorItem(key, material, type));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T extends Item> RegistrySupplier<T> registerBlock(String name, Function<ResourceKey<Item>, T> item) {
        RegistrySupplier<T> r = register(name, item);
        r.listen(o -> CreativeTabRegistry.append(IafItemGroups.BLOCKS, o));
        return r;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T extends Item> RegistrySupplier<T> registerItem(String name, Function<ResourceKey<Item>, T> item) {
        RegistrySupplier<T> r = register(name, item);
        r.listen(o -> CreativeTabRegistry.append(IafItemGroups.ITEMS, o));
        return r;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T extends Item> RegistrySupplier<T> registerToolOrWeapon(String name, Function<ResourceKey<Item>, T> item) {
        RegistrySupplier<T> r = register(name, item);
        r.listen(o -> CreativeTabRegistry.append(IafItemGroups.TOOLS_WEAPONS, o));
        return r;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T extends Item> RegistrySupplier<T> registerArmor(String name, Function<ResourceKey<Item>, T> item) {
        RegistrySupplier<T> r = register(name, item);
        r.listen(o -> CreativeTabRegistry.append(IafItemGroups.ARMORS, o));
        return r;
    }

    static <T extends Item> RegistrySupplier<T> register(String name, Function<ResourceKey<Item>, T> item) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
        return REGISTRY.register(name, () -> item.apply(key));
    }
}
