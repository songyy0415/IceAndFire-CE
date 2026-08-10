package com.iafenvoy.iceandfire.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;

public class DreadTorchParticle extends SingleQuadParticle {
    protected DreadTorchParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.first());
        this.setPos(x, y, z);
        this.yd += 0.01D;
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new DreadTorchParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float tickDelta) {
        this.quadSize = 0.125F * (this.lifetime - this.age) * 0.09F;
        this.xd *= 0.75D;
        this.yd *= 0.75D;
        this.zd *= 0.75D;
        super.extract(state, camera, tickDelta);
    }

    @Override
    protected int getLightCoords(float partialTick) { return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT; }

    @Override
    public SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.OPAQUE; }
}
