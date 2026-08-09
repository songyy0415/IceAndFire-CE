package com.iafenvoy.iceandfire.render.model;

import com.iafenvoy.iceandfire.render.entity.state.AnimatedBipedRenderState;
import com.iafenvoy.uranus.animation.Animation;

abstract class DreadBaseModel<T extends AnimatedBipedRenderState> extends BipedBaseModel<T> {
    DreadBaseModel() {
        super();
    }

    public abstract Animation getSpawnAnimation();

    @Override
    public void setupAnim(T state) {
        super.setupAnim(state);
        float limbAngle = state.walkAnimationPos;
        float limbDistance = state.walkAnimationSpeed;
        float animationProgress = state.ageInTicks;
        float headYaw = state.yRot;
        float headPitch = state.xRot;
        this.setRotationAnglesSpawn(state, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
    }

    public void setRotationAnglesSpawn(T state, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (state.getAnimation() == this.getSpawnAnimation())
            if (state.getAnimationTick() < 30) {
                this.flap(this.armRight, 0.5F, 0.5F, false, 2, -0.7F, state.ageInTicks, 1);
                this.flap(this.armLeft, 0.5F, 0.5F, true, 2, -0.7F, state.ageInTicks, 1);
                this.walk(this.armRight, 0.5F, 0.5F, true, 1, 0, state.ageInTicks, 1);
                this.walk(this.armLeft, 0.5F, 0.5F, true, 1, 0, state.ageInTicks, 1);
            }
    }

    @Override
    public void animate(T state, float f, float f1, float f2, float f3, float f4, float f5) {
        this.animator.startAnimate(state);
        if (this.animator.setAnimation(this.getSpawnAnimation())) {
            this.animator.startKeyframe(0);
            this.animator.move(this.body, 0, 35, 0);
            this.rotate(this.animator, this.armLeft, -180, 0, 0);
            this.rotate(this.animator, this.armRight, -180, 0, 0);
            this.animator.endKeyframe();
            this.animator.startKeyframe(30);
            this.animator.move(this.body, 0, 0, 0);
            this.rotate(this.animator, this.armLeft, -180, 0, 0);
            this.rotate(this.animator, this.armRight, -180, 0, 0);
            this.animator.endKeyframe();
            this.animator.resetKeyframe(5);
        }
        this.animator.endAnimate();
    }
}
