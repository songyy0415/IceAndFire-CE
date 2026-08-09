package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class DragonRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public Identifier texture;
    public Identifier eyesTexture;
    public Identifier maleOverlayTexture;
    public Identifier armorHead;
    public Identifier armorChest;
    public Identifier armorLegs;
    public Identifier armorFeet;
    public float renderSize = 1.0F;
    public float dragonPitch;
    public float prevDragonPitch;
    public boolean shouldRenderEyes;
    public boolean isMale;
    public boolean isSkeletal;
    public int variant;
    public int dragonStage;
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
