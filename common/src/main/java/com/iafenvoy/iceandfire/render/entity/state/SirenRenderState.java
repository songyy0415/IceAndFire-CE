package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.iceandfire.entity.util.ChainBuffer;
import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SirenRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public Identifier texture;
    public float swimProgress;
    public boolean swimming;
    public boolean singing;
    public int singingPose;
    public float singProgress;
    public boolean onGround;
    public ChainBuffer tailBuffer;
    public Animation animation = IAnimatedEntity.NO_ANIMATION;
    public int animationTick;
    public Animation[] animations = new Animation[0];

    @Override
    public int getAnimationTick() {
        return this.animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        this.animationTick = tick;
    }

    @Override
    public Animation getAnimation() {
        return this.animation;
    }

    @Override
    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return this.animations;
    }
}
