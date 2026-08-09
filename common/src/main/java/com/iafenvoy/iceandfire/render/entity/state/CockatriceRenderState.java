package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class CockatriceRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public boolean isHen;
    public float stareProgress;
    public float sitProgress;
    public Animation animation = IAnimatedEntity.NO_ANIMATION;
    public int animationTick;
    public Animation[] animations = new Animation[0];
    public boolean beamActive;
    public Vec3 startPos;
    public Vec3 targetPos;
    public float beamGameTime;
    public float attackAnimationScale;

    @Override public int getAnimationTick() { return this.animationTick; }
    @Override public void setAnimationTick(int tick) { this.animationTick = tick; }
    @Override public Animation getAnimation() { return this.animation; }
    @Override public void setAnimation(Animation animation) { this.animation = animation; }
    @Override public Animation[] getAnimations() { return this.animations; }
}
