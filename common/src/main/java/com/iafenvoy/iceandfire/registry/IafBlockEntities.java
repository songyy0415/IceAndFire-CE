package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.item.block.entity.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class IafBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<EggInIceBlockEntity>> EGG_IN_ICE = register("egginice", () -> new BlockEntityType<>(EggInIceBlockEntity::new, Set.of(IafBlocks.EGG_IN_ICE.get())));
    public static final RegistrySupplier<BlockEntityType<PixieHouseBlockEntity>> PIXIE_HOUSE = register("pixie_house", () -> new BlockEntityType<>(PixieHouseBlockEntity::new, Set.of(IafBlocks.PIXIE_HOUSE_MUSHROOM_RED.get(), IafBlocks.PIXIE_HOUSE_MUSHROOM_BROWN.get(), IafBlocks.PIXIE_HOUSE_OAK.get(), IafBlocks.PIXIE_HOUSE_BIRCH.get(), IafBlocks.PIXIE_HOUSE_SPRUCE.get(), IafBlocks.PIXIE_HOUSE_DARK_OAK.get())));
    public static final RegistrySupplier<BlockEntityType<JarBlockEntity>> PIXIE_JAR = register("pixie_jar", () -> new BlockEntityType<>(JarBlockEntity::new, Set.of(IafBlocks.JAR_EMPTY.get(), IafBlocks.JAR_PIXIE_0.get(), IafBlocks.JAR_PIXIE_1.get(), IafBlocks.JAR_PIXIE_2.get(), IafBlocks.JAR_PIXIE_3.get(), IafBlocks.JAR_PIXIE_4.get())));
    public static final RegistrySupplier<BlockEntityType<DragonForgeBlockEntity>> DRAGONFORGE_CORE = register("dragonforge_core", () -> new BlockEntityType<>(DragonForgeBlockEntity::new, Set.of(IafBlocks.DRAGONFORGE_FIRE_CORE.get(), IafBlocks.DRAGONFORGE_ICE_CORE.get(), IafBlocks.DRAGONFORGE_FIRE_CORE_DISABLED.get(), IafBlocks.DRAGONFORGE_ICE_CORE_DISABLED.get(), IafBlocks.DRAGONFORGE_LIGHTNING_CORE.get(), IafBlocks.DRAGONFORGE_LIGHTNING_CORE_DISABLED.get())));
    public static final RegistrySupplier<BlockEntityType<DragonForgeBrickBlockEntity>> DRAGONFORGE_BRICK = register("dragonforge_brick", () -> new BlockEntityType<>(DragonForgeBrickBlockEntity::new, Set.of(IafBlocks.DRAGONFORGE_FIRE_BRICK.get(), IafBlocks.DRAGONFORGE_ICE_BRICK.get(), IafBlocks.DRAGONFORGE_LIGHTNING_BRICK.get())));
    public static final RegistrySupplier<BlockEntityType<DragonForgeInputBlockEntity>> DRAGONFORGE_INPUT = register("dragonforge_input", () -> new BlockEntityType<>(DragonForgeInputBlockEntity::new, Set.of(IafBlocks.DRAGONFORGE_FIRE_INPUT.get(), IafBlocks.DRAGONFORGE_ICE_INPUT.get(), IafBlocks.DRAGONFORGE_LIGHTNING_INPUT.get())));
    public static final RegistrySupplier<BlockEntityType<DreadPortalBlockEntity>> DREAD_PORTAL = register("dread_portal", () -> new BlockEntityType<>(DreadPortalBlockEntity::new, Set.of(IafBlocks.DREAD_PORTAL.get())));
    public static final RegistrySupplier<BlockEntityType<DreadSpawnerBlockEntity>> DREAD_SPAWNER = register("dread_spawner", () -> new BlockEntityType<>(DreadSpawnerBlockEntity::new, Set.of(IafBlocks.DREAD_SPAWNER.get())));
    public static final RegistrySupplier<BlockEntityType<GhostChestBlockEntity>> GHOST_CHEST = register("ghost_chest", () -> new BlockEntityType<>(GhostChestBlockEntity::new, Set.of(IafBlocks.GHOST_CHEST.get())));
    public static final RegistrySupplier<BlockEntityType<LecternBlockEntity>> IAF_LECTERN = register("lectern", () -> new BlockEntityType<>(LecternBlockEntity::new, Set.of(IafBlocks.LECTERN.get())));
    public static final RegistrySupplier<BlockEntityType<PodiumBlockEntity>> PODIUM = register("podium", () -> new BlockEntityType<>(PodiumBlockEntity::new, Set.of(IafBlocks.PODIUM_OAK.get(), IafBlocks.PODIUM_BIRCH.get(), IafBlocks.PODIUM_SPRUCE.get(), IafBlocks.PODIUM_JUNGLE.get(), IafBlocks.PODIUM_DARK_OAK.get(), IafBlocks.PODIUM_ACACIA.get())));

    private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String entityName, Supplier<BlockEntityType<T>> builder) {
        return REGISTRY.register(entityName, builder);
    }
}
