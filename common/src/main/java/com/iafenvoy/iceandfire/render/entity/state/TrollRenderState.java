package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.iceandfire.data.TrollType;
import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class TrollRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public TrollType trollType;
    public TrollType.ITrollWeapon weaponType;
    public boolean isStone;
    public float stoneProgress;
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
