package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.world.processor.DreadPortalProcessor;
import com.iafenvoy.iceandfire.world.processor.DreadRuinProcessor;
import com.iafenvoy.iceandfire.world.processor.GraveyardProcessor;
import com.iafenvoy.iceandfire.world.processor.VillageHouseProcessor;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public final class IafProcessors {
    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.STRUCTURE_PROCESSOR);

    public static final DeferredSupplier<MapCodec<? extends StructureProcessor>> GRAVEYARD_PROCESSOR = registerProcessor("graveyard_processor", () -> GraveyardProcessor.CODEC);
    public static final DeferredSupplier<MapCodec<? extends StructureProcessor>> VILLAGE_HOUSE_PROCESSOR = registerProcessor("village_house_processor", () -> VillageHouseProcessor.CODEC);
    public static final DeferredSupplier<MapCodec<? extends StructureProcessor>> DREAD_MAUSOLEUM_PROCESSOR = registerProcessor("dread_mausoleum_processor", () -> DreadRuinProcessor.CODEC);
    public static final DeferredSupplier<MapCodec<? extends StructureProcessor>> DREAD_PORTAL_PROCESSOR = registerProcessor("dread_portal_processor", () -> DreadPortalProcessor.CODEC);

    private static <P extends StructureProcessor> DeferredSupplier<MapCodec<? extends StructureProcessor>> registerProcessor(String name, Supplier<MapCodec<? extends StructureProcessor>> processor) {
        return REGISTRY.register(name, processor);
    }
}
