package com.iafenvoy.iceandfire.registry;

import com.google.common.collect.ImmutableSet;
import com.iafenvoy.iceandfire.IceAndFire;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class IafTrades {
    public static final DeferredRegister<PoiType> POI_REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.POINT_OF_INTEREST_TYPE);
    public static final DeferredRegister<VillagerProfession> PROFESSION_REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.VILLAGER_PROFESSION);

    private static final String SCRIBE = "scribe";
    private static final RegistrySupplier<Block> SCRIBE_BLOCK = IafBlocks.LECTERN;
    public static final Function<Block, Set<BlockState>> SCRIBE_WORKSTATION = block -> new HashSet<>(block.getStateDefinition().getPossibleStates());
    public static final DeferredSupplier<PoiType> SCRIBE_POI = POI_REGISTRY.register(SCRIBE, () -> new PoiType(SCRIBE_WORKSTATION.apply(SCRIBE_BLOCK.get()), 1, 1));
    public static final RegistrySupplier<VillagerProfession> SCRIBE_PROFESSION = PROFESSION_REGISTRY.register(SCRIBE, () -> new VillagerProfession(
            Component.translatable(SCRIBE),
            e -> e.is(SCRIBE_POI.getKey()),
            e -> e.is(SCRIBE_POI.getKey()),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN,
            scribeTradeSetsByLevel()));

    // Scribe trade content is data-driven: data/iceandfire/villager_trade/scribe/<level>/,
    // data/iceandfire/tags/villager_trade/scribe_level_<n> and data/iceandfire/trade_set/scribe/level_<n>.
    private static Int2ObjectMap<ResourceKey<TradeSet>> scribeTradeSetsByLevel() {
        Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++)
            tradeSets.put(level, ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, SCRIBE + "/level_" + level)));
        return tradeSets;
    }

    public static void init() {
        for (BlockState state : SCRIBE_WORKSTATION.apply(SCRIBE_BLOCK.get()))
            PoiTypes.TYPE_BY_STATE.put(state, BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(SCRIBE_POI.get()));
    }
}
