package com.iafenvoy.iceandfire.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class BloodParticle extends SingleQuadParticle {
    protected BloodParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider) {
        super(world, x, y, z, 0, Math.random() * (double) 0.2F + 0.1, 0, spriteProvider.first());
        this.setPos(x, y, z);
        this.yd += 0.01D;
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new BloodParticle(world, x, y, z, spriteProvider);
    }

    @Override
    protected int getLightCoords(float partialTick) { return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT; }

    @Override
    public SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.OPAQUE; }
}
