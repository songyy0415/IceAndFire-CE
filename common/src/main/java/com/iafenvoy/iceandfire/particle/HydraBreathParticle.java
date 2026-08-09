package com.iafenvoy.iceandfire.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class HydraBreathParticle extends SingleQuadParticle {
    private final float newScale;

    protected HydraBreathParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider) {
        super(world, x, y, z, 0, 0, 0, spriteProvider.first());
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;
        this.newScale = this.quadSize;
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new HydraBreathParticle(world, x, y, z, spriteProvider);
    }

    @Override
    public float getQuadSize(float a) {
        float scaley = ((float) this.age + a) / (float) this.lifetime * 32.0F;
        scaley = Mth.clamp(scaley, 0.0F, 1.0F);
        return this.newScale * scaley;
    }

    @Override
    protected int getLightCoords(float partialTick) { return super.getLightCoords(partialTick); }

    @Override
    public SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.OPAQUE; }
}
