package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class HydraRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public int variant;
    public int headCount = 1;
    public int severedHead = -1;
    public boolean isAlive;
    public boolean isStone;
    public float[] speakProgress = new float[0];
    public float[] strikeProgress = new float[0];
    public float[] breathProgress = new float[0];
    public Animation animation = IAnimatedEntity.NO_ANIMATION;
    public int animationTick;
    public Animation[] animations = new Animation[0];

    @Override public int getAnimationTick() { return this.animationTick; }
    @Override public void setAnimationTick(int tick) { this.animationTick = tick; }
    @Override public Animation getAnimation() { return this.animation; }
    @Override public void setAnimation(Animation animation) { this.animation = animation; }
    @Override public Animation[] getAnimations() { return this.animations; }
}
