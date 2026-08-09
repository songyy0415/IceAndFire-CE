package com.iafenvoy.iceandfire.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SerpentBubbleParticle extends SingleQuadParticle {
    protected SerpentBubbleParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.first());
        this.setPos(x, y, z);
        this.quadSize = 0.3F;
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new SerpentBubbleParticle(world, x, y, z, 1, 1, 1, spriteProvider);
    }

    @Override
    protected int getLightCoords(float partialTick) { return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT; }

    @Override
    public SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.OPAQUE; }
}
