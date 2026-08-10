package com.iafenvoy.iceandfire.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class ParticleProviderHolder<T extends ParticleOptions> {
    private final ParticleType<T> type;
    @Nullable
    private final ParticleProviderRegistry.PendingParticleProvider<T> commonFactory;

    public ParticleProviderHolder(ParticleType<T> type, @NotNull ParticleProviderRegistry.PendingParticleProvider<T> factory) {
        this.type = type;
        this.commonFactory = factory;
    }

    public void applyRegister(BiConsumer<ParticleType<T>, ParticleProviderRegistry.PendingParticleProvider<T>> common) {
        if (this.commonFactory != null) common.accept(this.type, this.commonFactory);
    }
}
