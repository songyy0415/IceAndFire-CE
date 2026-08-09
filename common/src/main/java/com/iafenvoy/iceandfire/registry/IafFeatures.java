package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.registry.tag.IafBiomeTags;
import com.iafenvoy.iceandfire.world.feature.*;
import dev.architectury.platform.Platform;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class IafFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.FEATURE);

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_DEATH_WORM = feature("spawn_death_worm", () -> new DeathWormSpawnFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_DRAGON_SKELETON_L = feature("spawn_dragon_skeleton_lightning", () -> new DragonSkeletonSpawnFeature(IafEntities.LIGHTNING_DRAGON.get(), NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_DRAGON_SKELETON_F = feature("spawn_dragon_skeleton_fire", () -> new DragonSkeletonSpawnFeature(IafEntities.FIRE_DRAGON.get(), NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_DRAGON_SKELETON_I = feature("spawn_dragon_skeleton_ice", () -> new DragonSkeletonSpawnFeature(IafEntities.ICE_DRAGON.get(), NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_HIPPOCAMPUS = feature("spawn_hippocampus", () -> new HippocampusSpawnFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_SEA_SERPENT = feature("spawn_sea_serpent", () -> new SeaSerpentSpawnFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_STYMPHALIAN_BIRD = feature("spawn_stymphalian_bird", () -> new StymphalianBirdSpawnFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> SPAWN_WANDERING_CYCLOPS = feature("spawn_wandering_cyclops", () -> new WanderingCyclopsSpawnFeature(NoneFeatureConfiguration.CODEC));

    private static <F extends Feature<? extends FeatureConfiguration>> RegistrySupplier<F> feature(String name, Supplier<F> feature) {
        return REGISTRY.register(name, feature);
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> DREADWOOD = configuredFeature("dreadwood");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DREADWOOD_LARGE = configuredFeature("dreadwood_large");

    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DEATH_WORM = placeFeature("spawn_death_worm");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DRAGON_SKELETON_L = placeFeature("spawn_dragon_skeleton_lightning");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DRAGON_SKELETON_F = placeFeature("spawn_dragon_skeleton_fire");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DRAGON_SKELETON_I = placeFeature("spawn_dragon_skeleton_ice");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_HIPPOCAMPUS = placeFeature("spawn_hippocampus");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_SEA_SERPENT = placeFeature("spawn_sea_serpent");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_STYMPHALIAN_BIRD = placeFeature("spawn_stymphalian_bird");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_WANDERING_CYCLOPS = placeFeature("spawn_wandering_cyclops");
    public static final ResourceKey<PlacedFeature> PLACED_SILVER_ORE = placeFeature("silver_ore");
    public static final ResourceKey<PlacedFeature> PLACED_SAPPHIRE_ORE = placeFeature("sapphire_ore");
    public static final ResourceKey<PlacedFeature> PLACED_FIRE_LILY = placeFeature("fire_lily");
    public static final ResourceKey<PlacedFeature> PLACED_LIGHTNING_LILY = placeFeature("lightning_lily");
    public static final ResourceKey<PlacedFeature> PLACED_FROST_LILY = placeFeature("frost_lily");

    public static ResourceKey<ConfiguredFeature<?, ?>> configuredFeature(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
    }

    public static ResourceKey<PlacedFeature> placeFeature(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
    }

    public static void init() {
        if (Platform.isNeoForge()) return;

        addFeatureToBiome(IafBiomeTags.FIRE, PLACED_FIRE_LILY, GenerationStep.Decoration.VEGETAL_DECORATION);
        addFeatureToBiome(IafBiomeTags.ICE, PLACED_FROST_LILY, GenerationStep.Decoration.VEGETAL_DECORATION);
        addFeatureToBiome(IafBiomeTags.LIGHTNING, PLACED_LIGHTNING_LILY, GenerationStep.Decoration.VEGETAL_DECORATION);
        addFeatureToBiome(IafBiomeTags.FIRE, PLACED_SPAWN_DRAGON_SKELETON_F, GenerationStep.Decoration.SURFACE_STRUCTURES);
        addFeatureToBiome(IafBiomeTags.ICE, PLACED_SPAWN_DRAGON_SKELETON_I, GenerationStep.Decoration.SURFACE_STRUCTURES);
        addFeatureToBiome(IafBiomeTags.LIGHTNING, PLACED_SPAWN_DRAGON_SKELETON_L, GenerationStep.Decoration.SURFACE_STRUCTURES);

        addFeatureToBiome(IafBiomeTags.SILVER_ORE, PLACED_SILVER_ORE, GenerationStep.Decoration.UNDERGROUND_ORES);
        addFeatureToBiome(IafBiomeTags.SAPPHIRE_ORE, PLACED_SAPPHIRE_ORE, GenerationStep.Decoration.UNDERGROUND_ORES);

        addFeatureToBiome(IafBiomeTags.DEATHWORM, PLACED_SPAWN_DEATH_WORM, GenerationStep.Decoration.SURFACE_STRUCTURES);
        addFeatureToBiome(IafBiomeTags.WANDERING_CYCLOPS, PLACED_SPAWN_WANDERING_CYCLOPS, GenerationStep.Decoration.SURFACE_STRUCTURES);
        addFeatureToBiome(IafBiomeTags.HIPPOCAMPUS, PLACED_SPAWN_HIPPOCAMPUS, GenerationStep.Decoration.SURFACE_STRUCTURES);
        addFeatureToBiome(IafBiomeTags.SEA_SERPENT, PLACED_SPAWN_SEA_SERPENT, GenerationStep.Decoration.SURFACE_STRUCTURES);
        addFeatureToBiome(IafBiomeTags.STYMPHALIAN_BIRD, PLACED_SPAWN_STYMPHALIAN_BIRD, GenerationStep.Decoration.SURFACE_STRUCTURES);
    }

    private static void addFeatureToBiome(TagKey<Biome> biomeTag, ResourceKey<PlacedFeature> featureResource, GenerationStep.Decoration step) {
        BiomeModifications.addProperties(context -> context.hasTag(biomeTag), (context, mutable) -> mutable.getGenerationProperties().addFeature(step, featureResource));
    }
}
