package com.iafenvoy.iceandfire.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TagValueUtil {
    private TagValueUtil() {
    }

    /** Wrap a CompoundTag as a ValueInput for entity/block-entity loading. */
    public static ValueInput asInput(CompoundTag tag, HolderLookup.Provider lookup) {
        return TagValueInput.create(ProblemReporter.DISCARDING, lookup, tag);
    }

    /** Create a ValueOutput that writes back into a fresh CompoundTag. */
    public static TagValueOutput output() {
        return TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    }
}
