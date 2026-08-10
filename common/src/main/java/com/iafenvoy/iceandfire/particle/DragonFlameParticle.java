package com.iafenvoy.iceandfire.particle;

import com.iafenvoy.uranus.object.VecUtil;
import com.iafenvoy.uranus.util.RandomHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DragonFlameParticle extends SingleQuadParticle {
    protected DragonFlameParticle(DragonFlameParticleType parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, spriteProvider.first());
        float size = parameters.getScale();
        this.quadSize *= (float) RandomHelper.nextDouble(size, size * 2);
        this.lifetime = 30;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.setParticleSpeed(RandomHelper.randomize(velocityX, 0.5), RandomHelper.randomize(velocityY, 0.5), RandomHelper.randomize(velocityZ, 0.5));
        this.setSprite(spriteProvider.get(this.random));
    }

    public static ParticleProvider<DragonFlameParticleType> factory(SpriteSet spriteProvider) {
        return (parameters, world, x, y, z, velocityX, velocityY, velocityZ, random) -> new DragonFlameParticle(parameters, world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
    }

    @Override
    protected int getLightCoords(float partialTick) { return net.minecraft.util.LightCoordsUtil.FULL_BRIGHT; }

    @SuppressWarnings("deprecation")
    @Override
    public void tick() {
        super.tick();
        BlockState state = this.level.getBlockState(VecUtil.createBlockPos(this.x, this.y, this.z));
        if (state != null && state.isSolid())
            this.remove();
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }
}
