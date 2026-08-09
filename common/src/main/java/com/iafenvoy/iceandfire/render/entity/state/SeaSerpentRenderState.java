package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SeaSerpentRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public Identifier texture;
    public boolean blinking;
    public boolean isAncient;
    public float seaSerpentScale = 1.0F;
    public int swimCycle;
    public float jumpProgress;
    public float wantJumpProgress;
    public float breathProgress;
    public float jumpRot;
    public float prevJumpRot;
    public float yBodyRot;
    public float yBodyRotO;
    public float deltaMovementY;
    public boolean isInWater;
    public boolean isJumpingOutOfWater;
    public float[] pieceYaw = new float[4];
    public float[] piecePitch = new float[4];
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
