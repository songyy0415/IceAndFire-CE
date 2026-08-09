package com.iafenvoy.iceandfire.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DreadPortalParticle extends SingleQuadParticle {
    protected DreadPortalParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.first());
        this.setPos(x, y, z);
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new DreadPortalParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
    }

    @Override
    public float getQuadSize(float a) {
        float size = 0.125F * (this.lifetime - this.age);
        size = size * 0.09F;
        return size;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.OPAQUE; }

    @Override
    protected int getLightCoords(float partialTick) { return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT; }
}
