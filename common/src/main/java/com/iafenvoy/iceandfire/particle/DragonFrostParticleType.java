package com.iafenvoy.iceandfire.particle;

import com.iafenvoy.iceandfire.registry.IafParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DragonFrostParticleType extends DragonParticleType<DragonFrostParticleType> {
    private static final MapCodec<DragonFrostParticleType> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("scale").forGetter(DragonFrostParticleType::getScale)
    ).apply(i, DragonFrostParticleType::new));

    public DragonFrostParticleType() {
        this(1);
    }

    public DragonFrostParticleType(float scale) {
        super(scale);
    }

    @Override
    public ParticleType<?> getType() {
        return IafParticles.DRAGON_FROST.get();
    }

    @Override
    public MapCodec<DragonFrostParticleType> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonFrostParticleType> streamCodec() {
        return StreamCodec.composite(ByteBufCodecs.FLOAT, DragonFrostParticleType::getScale, DragonFrostParticleType::new);
    }
}
