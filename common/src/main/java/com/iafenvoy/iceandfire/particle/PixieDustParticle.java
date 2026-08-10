package com.iafenvoy.iceandfire.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class PixieDustParticle extends SingleQuadParticle {
    private final float newScale;

    protected PixieDustParticle(ClientLevel world, double x, double y, double z, float scale, float red, float green, float blue, SpriteSet spriteProvider) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D, spriteProvider.first());
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;
        float f = (float) Math.random() * 0.4F + 0.6F;
        this.rCol = ((float) (Math.random() * 0.2) + 0.8F) * red * f;
        this.gCol = ((float) (Math.random() * 0.2) + 0.8F) * green * f;
        this.bCol = ((float) (Math.random() * 0.2) + 0.8F) * blue * f;
        this.quadSize *= scale;
        this.newScale = this.quadSize;
        this.lifetime = (int) (this.lifetime * scale);
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new PixieDustParticle(world, x, y, z, 1, 1, 1, 1, spriteProvider);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float tickDelta) {
        float scaley = ((float) this.age + tickDelta) / (float) this.lifetime * 32.0F;
        scaley = Mth.clamp(scaley, 0.0F, 1.0F);
        this.quadSize = this.newScale * scaley;
        super.extract(state, camera, tickDelta);
    }

    @Override
    protected int getLightCoords(float partialTick) { return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT; }

    @Override
    public SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.OPAQUE; }
}
