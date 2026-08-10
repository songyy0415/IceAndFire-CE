package com.iafenvoy.iceandfire.particle;

import com.iafenvoy.iceandfire.registry.IafParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DragonFlameParticleType extends DragonParticleType<DragonFlameParticleType> {
    private static final MapCodec<DragonFlameParticleType> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.fieldOf("scale").forGetter(DragonFlameParticleType::getScale)
    ).apply(i, DragonFlameParticleType::new));

    public DragonFlameParticleType() {
        this(1);
    }

    public DragonFlameParticleType(float scale) {
        super(scale);
    }

    @Override
    public ParticleType<?> getType() {
        return IafParticles.DRAGON_FLAME.get();
    }

    @Override
    public MapCodec<DragonFlameParticleType> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonFlameParticleType> streamCodec() {
        return StreamCodec.composite(ByteBufCodecs.FLOAT, DragonFlameParticleType::getScale, DragonFlameParticleType::new);
    }
}
