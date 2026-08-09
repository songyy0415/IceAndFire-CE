package com.iafenvoy.iceandfire.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class ParticleProviderHolder<T extends ParticleOptions> {
    private final ParticleType<T> type;
    @Nullable
    private final ParticleProvider<T> commonFactory;

    public ParticleProviderHolder(ParticleType<T> type, @NotNull ParticleProvider<T> factory) {
        this.type = type;
        this.commonFactory = factory;
    }

    public void applyRegister(BiConsumer<ParticleType<T>, ParticleProvider<T>> common) {
        if (this.commonFactory != null) common.accept(this.type, this.commonFactory);
    }
}
