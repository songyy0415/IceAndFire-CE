package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.DragonType;
import com.iafenvoy.iceandfire.item.block.*;
import com.iafenvoy.iceandfire.item.block.util.WallBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class IafBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> LECTERN = register("lectern", LecternBlock::new);
    public static final RegistrySupplier<Block> PODIUM_OAK = register("podium_oak", PodiumBlock::new);
    public static final RegistrySupplier<Block> PODIUM_BIRCH = register("podium_birch", PodiumBlock::new);
    public static final RegistrySupplier<Block> PODIUM_SPRUCE = register("podium_spruce", PodiumBlock::new);
    public static final RegistrySupplier<Block> PODIUM_JUNGLE = register("podium_jungle", PodiumBlock::new);
    public static final RegistrySupplier<Block> PODIUM_DARK_OAK = register("podium_dark_oak", PodiumBlock::new);
    public static final RegistrySupplier<Block> PODIUM_ACACIA = register("podium_acacia", PodiumBlock::new);
    public static final RegistrySupplier<Block> FIRE_LILY = register("fire_lily", ElementalFlowerBlock::new);
    public static final RegistrySupplier<Block> FROST_LILY = register("frost_lily", ElementalFlowerBlock::new);
    public static final RegistrySupplier<Block> LIGHTNING_LILY = register("lightning_lily", ElementalFlowerBlock::new);
    public static final RegistrySupplier<Block> GOLD_PILE = register("gold_pile", PileBlock::new);
    public static final RegistrySupplier<Block> SILVER_PILE = register("silver_pile", PileBlock::new);
    public static final RegistrySupplier<Block> COPPER_PILE = register("copper_pile", PileBlock::new);
    public static final RegistrySupplier<Block> SILVER_ORE = register("silver_ore", key -> new DropExperienceBlock(ConstantInt.of(2), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3, 3).requiresCorrectToolForDrops().setId(key)));
    public static final RegistrySupplier<Block> DEEPSLATE_SILVER_ORE = register("deepslate_silver_ore", key -> new DropExperienceBlock(ConstantInt.of(2), BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(3, 3).requiresCorrectToolForDrops().setId(key)));
    public static final RegistrySupplier<Block> SILVER_BLOCK = register("silver_block", key -> GenericBlock.builder(key, 3.0F, 5.0F, SoundType.METAL, MapColor.METAL, null, null, false));
    public static final RegistrySupplier<Block> SAPPHIRE_ORE = register("sapphire_ore", key -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4, 3).requiresCorrectToolForDrops().setId(key)));
    public static final RegistrySupplier<Block> SAPPHIRE_BLOCK = register("sapphire_block", key -> GenericBlock.builder(key, 3.0F, 6.0F, SoundType.METAL, MapColor.METAL, null, null, false));
    public static final RegistrySupplier<Block> RAW_SILVER_BLOCK = register("raw_silver_block", key -> GenericBlock.builder(key, 3.0F, 5.0F, SoundType.STONE, MapColor.METAL, NoteBlockInstrument.BASEDRUM, null, false));
    public static final RegistrySupplier<Block> CHARRED_DIRT = register("chared_dirt", key -> ReturningStateBlock.builder(key, 0.5F, 0.0F, SoundType.GRAVEL, MapColor.DIRT, null, null, false, Blocks.DIRT.defaultBlockState()));
    public static final RegistrySupplier<Block> CHARRED_GRASS = register("chared_grass", key -> ReturningStateBlock.builder(key, 0.6F, 0.0F, SoundType.GRAVEL, MapColor.GRASS, null, null, false, Blocks.GRASS_BLOCK.defaultBlockState()));
    public static final RegistrySupplier<Block> CHARRED_STONE = register("chared_stone", key -> ReturningStateBlock.builder(key, 1.5F, 10.0F, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, false, Blocks.STONE.defaultBlockState()));
    public static final RegistrySupplier<Block> CHARRED_COBBLESTONE = register("chared_cobblestone", key -> ReturningStateBlock.builder(key, 2F, 10.0F, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, false, Blocks.COBBLESTONE.defaultBlockState()));
    public static final RegistrySupplier<Block> CHARRED_GRAVEL = register("chared_gravel", key -> new FallingReturningStateBlock(key, 0.6F, 0F, SoundType.GRAVEL, MapColor.DIRT, Blocks.GRAVEL.defaultBlockState()));
    public static final RegistrySupplier<Block> CHARRED_DIRT_PATH = register(CharedPathBlock.getNameFromType(0), key -> new CharedPathBlock(key, 0));
    public static final RegistrySupplier<Block> ASH = register("ash", key -> FallingGenericBlock.builder(key, 0.5F, 0F, SoundType.SAND, MapColor.SAND, NoteBlockInstrument.SNARE));
    public static final RegistrySupplier<Block> FROZEN_DIRT = register("frozen_dirt", key -> ReturningStateBlock.builder(key, 0.5F, 0.0F, SoundType.GLASS, true, MapColor.DIRT, null, null, false, Blocks.DIRT.defaultBlockState()));
    public static final RegistrySupplier<Block> FROZEN_GRASS = register("frozen_grass", key -> ReturningStateBlock.builder(key, 0.6F, 0.0F, SoundType.GLASS, true, MapColor.GRASS, null, null, false, Blocks.GRASS_BLOCK.defaultBlockState()));
    public static final RegistrySupplier<Block> FROZEN_STONE = register("frozen_stone", key -> ReturningStateBlock.builder(key, 1.5F, 1.0F, SoundType.GLASS, true, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, false, Blocks.STONE.defaultBlockState()));
    public static final RegistrySupplier<Block> FROZEN_COBBLESTONE = register("frozen_cobblestone", key -> ReturningStateBlock.builder(key, 2F, 2.0F, SoundType.GLASS, true, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, false, Blocks.COBBLESTONE.defaultBlockState()));
    public static final RegistrySupplier<Block> FROZEN_GRAVEL = register("frozen_gravel", key -> new FallingReturningStateBlock(key, 0.6F, 0F, SoundType.GLASS, true, MapColor.DIRT, Blocks.GRAVEL.defaultBlockState()));
    public static final RegistrySupplier<Block> FROZEN_DIRT_PATH = register(CharedPathBlock.getNameFromType(1), key -> new CharedPathBlock(key, 1));
    public static final RegistrySupplier<Block> FROZEN_SPLINTERS = register("frozen_splinters", key -> GenericBlock.builder(key, 2.0F, 1.0F, SoundType.GLASS, true, MapColor.WOOD, NoteBlockInstrument.BASS, null, true));
    public static final RegistrySupplier<Block> DRAGON_ICE = register("dragon_ice", key -> GenericBlock.builder(key, 0.5F, 0F, SoundType.GLASS, true, MapColor.ICE, null, null, false));
    public static final RegistrySupplier<Block> DRAGON_ICE_SPIKES = register("dragon_ice_spikes", IceSpikesBlock::new);
    public static final RegistrySupplier<Block> CRACKLED_DIRT = register("crackled_dirt", key -> ReturningStateBlock.builder(key, 0.5F, 0.0F, SoundType.GRAVEL, MapColor.DIRT, null, null, false, Blocks.DIRT.defaultBlockState()));
    public static final RegistrySupplier<Block> CRACKLED_GRASS = register("crackled_grass", key -> ReturningStateBlock.builder(key, 0.6F, 0.0F, SoundType.GRAVEL, MapColor.GRASS, null, null, false, Blocks.GRASS_BLOCK.defaultBlockState()));
    public static final RegistrySupplier<Block> CRACKLED_STONE = register("crackled_stone", key -> ReturningStateBlock.builder(key, 1.5F, 1.0F, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, false, Blocks.STONE.defaultBlockState()));
    public static final RegistrySupplier<Block> CRACKLED_COBBLESTONE = register("crackled_cobblestone", key -> ReturningStateBlock.builder(key, 2F, 2F, SoundType.STONE, MapColor.STONE, NoteBlockInstrument.BASEDRUM, null, false, Blocks.COBBLESTONE.defaultBlockState()));
    public static final RegistrySupplier<Block> CRACKLED_GRAVEL = register("crackled_gravel", key -> new FallingReturningStateBlock(key, 0.6F, 0F, SoundType.GRAVEL, MapColor.DIRT, Blocks.GRAVEL.defaultBlockState()));
    public static final RegistrySupplier<Block> CRACKLED_DIRT_PATH = register(CharedPathBlock.getNameFromType(2), key -> new CharedPathBlock(key, 2));

    public static final RegistrySupplier<Block> NEST = register("nest", key -> GenericBlock.builder(key, 0.5F, 0F, SoundType.GRAVEL, false, MapColor.PLANT, null, PushReaction.DESTROY, false));

    public static final RegistrySupplier<Block> DRAGON_SCALE_RED = register("dragonscale_red", key -> new DragonScalesBlock(key, IafDragonColors.RED));
    public static final RegistrySupplier<Block> DRAGON_SCALE_GREEN = register("dragonscale_green", key -> new DragonScalesBlock(key, IafDragonColors.GREEN));
    public static final RegistrySupplier<Block> DRAGON_SCALE_BRONZE = register("dragonscale_bronze", key -> new DragonScalesBlock(key, IafDragonColors.BRONZE));
    public static final RegistrySupplier<Block> DRAGON_SCALE_GRAY = register("dragonscale_gray", key -> new DragonScalesBlock(key, IafDragonColors.GRAY));
    public static final RegistrySupplier<Block> DRAGON_SCALE_BLUE = register("dragonscale_blue", key -> new DragonScalesBlock(key, IafDragonColors.BLUE));
    public static final RegistrySupplier<Block> DRAGON_SCALE_WHITE = register("dragonscale_white", key -> new DragonScalesBlock(key, IafDragonColors.WHITE));
    public static final RegistrySupplier<Block> DRAGON_SCALE_SAPPHIRE = register("dragonscale_sapphire", key -> new DragonScalesBlock(key, IafDragonColors.SAPPHIRE));
    public static final RegistrySupplier<Block> DRAGON_SCALE_SILVER = register("dragonscale_silver", key -> new DragonScalesBlock(key, IafDragonColors.SILVER));
    public static final RegistrySupplier<Block> DRAGON_SCALE_ELECTRIC = register("dragonscale_electric", key -> new DragonScalesBlock(key, IafDragonColors.ELECTRIC));
    public static final RegistrySupplier<Block> DRAGON_SCALE_amethyst = register("dragonscale_amethyst", key -> new DragonScalesBlock(key, IafDragonColors.AMETHYST));
    public static final RegistrySupplier<Block> DRAGON_SCALE_COPPER = register("dragonscale_copper", key -> new DragonScalesBlock(key, IafDragonColors.COPPER));
    public static final RegistrySupplier<Block> DRAGON_SCALE_BLACK = register("dragonscale_black", key -> new DragonScalesBlock(key, IafDragonColors.BLACK));

    public static final RegistrySupplier<Block> DRAGON_BONE_BLOCK = register("dragon_bone_block", DragonBoneBlock::new);
    public static final RegistrySupplier<Block> DRAGON_BONE_BLOCK_WALL = register("dragon_bone_wall", key -> new DragonBoneWallBlock(BlockBehaviour.Properties.ofFullCopy(IafBlocks.DRAGON_BONE_BLOCK.get()).setId(key)));
    public static final RegistrySupplier<Block> DRAGONFORGE_FIRE_BRICK = forgeBrick(IafDragonTypes.FIRE);
    public static final RegistrySupplier<Block> DRAGONFORGE_ICE_BRICK = forgeBrick(IafDragonTypes.ICE);
    public static final RegistrySupplier<Block> DRAGONFORGE_LIGHTNING_BRICK = forgeBrick(IafDragonTypes.LIGHTNING);
    public static final RegistrySupplier<Block> DRAGONFORGE_FIRE_INPUT = forgeInput(IafDragonTypes.FIRE);
    public static final RegistrySupplier<Block> DRAGONFORGE_ICE_INPUT = forgeInput(IafDragonTypes.ICE);
    public static final RegistrySupplier<Block> DRAGONFORGE_LIGHTNING_INPUT = forgeInput(IafDragonTypes.LIGHTNING);
    public static final RegistrySupplier<Block> DRAGONFORGE_FIRE_CORE = forgeCore(IafDragonTypes.FIRE, true);
    public static final RegistrySupplier<Block> DRAGONFORGE_ICE_CORE = forgeCore(IafDragonTypes.ICE, true);
    public static final RegistrySupplier<Block> DRAGONFORGE_LIGHTNING_CORE = forgeCore(IafDragonTypes.LIGHTNING, true);
    public static final RegistrySupplier<Block> DRAGONFORGE_FIRE_CORE_DISABLED = forgeCore(IafDragonTypes.FIRE, false);
    public static final RegistrySupplier<Block> DRAGONFORGE_ICE_CORE_DISABLED = forgeCore(IafDragonTypes.ICE, false);
    public static final RegistrySupplier<Block> DRAGONFORGE_LIGHTNING_CORE_DISABLED = forgeCore(IafDragonTypes.LIGHTNING, false);
    public static final RegistrySupplier<Block> EGG_IN_ICE = register("egginice", EggInIceBlock::new);
    public static final RegistrySupplier<Block> PIXIE_HOUSE_MUSHROOM_RED = register(PixieHouseBlock.name("mushroom_red"), PixieHouseBlock::new);
    public static final RegistrySupplier<Block> PIXIE_HOUSE_MUSHROOM_BROWN = register(PixieHouseBlock.name("mushroom_brown"), PixieHouseBlock::new);
    public static final RegistrySupplier<Block> PIXIE_HOUSE_OAK = register(PixieHouseBlock.name("oak"), PixieHouseBlock::new);
    public static final RegistrySupplier<Block> PIXIE_HOUSE_BIRCH = register(PixieHouseBlock.name("birch"), PixieHouseBlock::new);
    public static final RegistrySupplier<Block> PIXIE_HOUSE_SPRUCE = register(PixieHouseBlock.name("spruce"), PixieHouseBlock::new);
    public static final RegistrySupplier<Block> PIXIE_HOUSE_DARK_OAK = register(PixieHouseBlock.name("dark_oak"), PixieHouseBlock::new);
    public static final RegistrySupplier<Block> JAR_EMPTY = register(JarBlock.name(-1), key -> new JarBlock(key, -1));
    public static final RegistrySupplier<Block> JAR_PIXIE_0 = register(JarBlock.name(0), key -> new JarBlock(key, 0));
    public static final RegistrySupplier<Block> JAR_PIXIE_1 = register(JarBlock.name(1), key -> new JarBlock(key, 1));
    public static final RegistrySupplier<Block> JAR_PIXIE_2 = register(JarBlock.name(2), key -> new JarBlock(key, 2));
    public static final RegistrySupplier<Block> JAR_PIXIE_3 = register(JarBlock.name(3), key -> new JarBlock(key, 3));
    public static final RegistrySupplier<Block> JAR_PIXIE_4 = register(JarBlock.name(4), key -> new JarBlock(key, 4));
    public static final RegistrySupplier<Block> DRAGONSTEEL_FIRE_BLOCK = register("dragonsteel_fire_block", key -> GenericBlock.builder(key, 10.0F, 1000.0F, SoundType.METAL, MapColor.METAL, null, null, false));
    public static final RegistrySupplier<Block> DRAGONSTEEL_ICE_BLOCK = register("dragonsteel_ice_block", key -> GenericBlock.builder(key, 10.0F, 1000.0F, SoundType.METAL, MapColor.METAL, null, null, false));
    public static final RegistrySupplier<Block> DRAGONSTEEL_LIGHTNING_BLOCK = register("dragonsteel_lightning_block", key -> GenericBlock.builder(key, 10.0F, 1000.0F, SoundType.METAL, MapColor.METAL, null, null, false));
    public static final RegistrySupplier<DreadBaseBlock> DREAD_STONE = register("dread_stone", key -> new DreadBaseBlock(key, false));
    public static final RegistrySupplier<DreadBaseBlock> DREAD_STONE_BRICKS = register("dread_stone_bricks", key -> new DreadBaseBlock(key, false));
    public static final RegistrySupplier<Block> DREAD_STONE_BRICKS_STAIRS = register("dread_stone_stairs", key -> new DreadStairsBlock(DREAD_STONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).setId(key)));
    public static final RegistrySupplier<DreadBaseBlock> DREAD_STONE_BRICKS_CHISELED = register("dread_stone_bricks_chiseled", key -> new DreadBaseBlock(key, false));
    public static final RegistrySupplier<DreadBaseBlock> DREAD_STONE_BRICKS_CRACKED = register("dread_stone_bricks_cracked", key -> new DreadBaseBlock(key, false));
    public static final RegistrySupplier<DreadBaseBlock> DREAD_STONE_BRICKS_MOSSY = register("dread_stone_bricks_mossy", key -> new DreadBaseBlock(key, false));
    public static final RegistrySupplier<DreadBaseBlock> DREAD_STONE_TILE = register("dread_stone_tile", key -> new DreadBaseBlock(key, false));
    public static final RegistrySupplier<Block> DREAD_STONE_FACE = register("dread_stone_face", DreadStoneFaceBlock::new);
    public static final RegistrySupplier<DreadTorchWallBlock> DREAD_TORCH_WALL = registerWallTorch("dread_torch_wall", DreadTorchWallBlock::new);
    public static final RegistrySupplier<TorchBlock> DREAD_TORCH = registerWallBlock("dread_torch", DreadTorchBlock::new);
    public static final RegistrySupplier<Block> DREAD_STONE_BRICKS_SLAB = register("dread_stone_slab", key -> new DreadSlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(10F, 10000F).setId(key)));
    public static final RegistrySupplier<Block> DREADWOOD_LOG = register("dreadwood_log", DreadWoodLogBlock::new);
    public static final RegistrySupplier<DreadBaseBlock> DREADWOOD_PLANKS = register("dreadwood_planks", key -> new DreadBaseBlock(key, true));
    public static final RegistrySupplier<Block> DREADWOOD_LEAVES = register("dreadwood_leaves", key -> new UntintedParticleLeavesBlock(0.0F, ParticleTypes.ASH, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().setId(key)));
    public static final RegistrySupplier<Block> DREADWOOD_SAPLING = register("dreadwood_sapling", DreadwoodSaplingBlock::new);
    public static final RegistrySupplier<Block> DREADWOOD_PLANKS_LOCK = register("dreadwood_planks_lock", DreadWoodLockBlock::new);
    public static final RegistrySupplier<Block> DREAD_PORTAL = register("dread_portal", DreadPortalBlock::new);
    public static final RegistrySupplier<Block> DREAD_SPAWNER = register("dread_spawner", DreadSpawnerBlock::new);
    public static final RegistrySupplier<BurntTorchWallBlock> BURNT_TORCH_WALL = registerWallTorch("burnt_torch_wall", BurntTorchWallBlock::new);
    public static final RegistrySupplier<TorchBlock> BURNT_TORCH = registerWallBlock("burnt_torch", BurntTorchBlock::new);
    public static final RegistrySupplier<Block> GHOST_CHEST = register("ghost_chest", GhostChestBlock::new);
    public static final RegistrySupplier<Block> GRAVEYARD_SOIL = register("graveyard_soil", GraveyardSoilBlock::new);

    public static RegistrySupplier<Block> forgeBrick(DragonType type) {
        return register(DragonForgeBrickBlock.name(type), key -> new DragonForgeBrickBlock(key, type));
    }

    public static RegistrySupplier<Block> forgeInput(DragonType type) {
        return register(DragonForgeInputBlock.name(type), key -> new DragonForgeInputBlock(key, type));
    }

    public static RegistrySupplier<Block> forgeCore(DragonType type, boolean activated) {
        return register(DragonForgeCoreBlock.name(type, activated), key -> new DragonForgeCoreBlock(key, type, activated));
    }

    public static <T extends Block> RegistrySupplier<T> register(String name, Function<ResourceKey<Block>, T> block) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
        RegistrySupplier<T> r = REGISTRY.register(name, () -> block.apply(key));
        IafItems.registerBlock(name, key2 -> new BlockItem(r.get(), new Item.Properties().setId(key2).useBlockDescriptionPrefix()));
        return r;
    }

    private static <T extends TorchBlock> RegistrySupplier<T> registerWallBlock(String name, Function<ResourceKey<Block>, T> block) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
        RegistrySupplier<T> r = REGISTRY.register(name, () -> block.apply(key));
        IafItems.registerBlock(name, key2 -> new StandingAndWallBlockItem(r.get(), ((WallBlock) r.get()).wallBlock(), Direction.DOWN, new Item.Properties().setId(key2).useBlockDescriptionPrefix()));
        return r;
    }

    private static <T extends WallTorchBlock> RegistrySupplier<T> registerWallTorch(String name, Function<ResourceKey<Block>, T> block) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, name));
        return REGISTRY.register(name, () -> block.apply(key));
    }
}
