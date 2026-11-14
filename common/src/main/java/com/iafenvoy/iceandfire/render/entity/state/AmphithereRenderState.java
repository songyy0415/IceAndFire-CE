package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.iceandfire.render.model.IFChainBuffer;
import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class AmphithereRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public float flapProgress;
    public float groundProgress;
    public float sitProgress;
    public float diveProgress;
    public boolean onGround;
    public int variant;
    public boolean isBlinking;
    public IFChainBuffer roll_buffer;
    public IFChainBuffer tail_buffer;
    public IFChainBuffer pitch_buffer;
    public boolean isFlying;
    public boolean isInWater;
    public boolean isInLava;
    /** Flight rework: the player drawn by the amphithere rider layer, null when there is no controlling rider. */
    public EntityRenderState riderRenderState;
    public Animation animation = IAnimatedEntity.NO_ANIMATION;
    public int animationTick;
    public Animation[] animations = new Animation[0];

    @Override public int getAnimationTick() { return this.animationTick; }
    @Override public void setAnimationTick(int tick) { this.animationTick = tick; }
    @Override public Animation getAnimation() { return this.animation; }
    @Override public void setAnimation(Animation animation) { this.animation = animation; }
    @Override public Animation[] getAnimations() { return this.animations; }
}
