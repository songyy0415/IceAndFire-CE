package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class HippocampusRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public int variant;
    public boolean isBlinking;
    public boolean isSaddled;
    public boolean hasPassenger;
    public boolean isChested;
    public int armorValue;
    public boolean isRainbow;
    public int rainbowColor = -1;
    public float onLandProgress;
    public float sitProgress;
    public boolean onGround;
    public com.iafenvoy.iceandfire.entity.util.ChainBuffer tail_buffer;
    public Animation animation = IAnimatedEntity.NO_ANIMATION;
    public int animationTick;
    public Animation[] animations = new Animation[0];

    @Override public int getAnimationTick() { return this.animationTick; }
    @Override public void setAnimationTick(int tick) { this.animationTick = tick; }
    @Override public Animation getAnimation() { return this.animation; }
    @Override public void setAnimation(Animation animation) { this.animation = animation; }
    @Override public Animation[] getAnimations() { return this.animations; }
}
