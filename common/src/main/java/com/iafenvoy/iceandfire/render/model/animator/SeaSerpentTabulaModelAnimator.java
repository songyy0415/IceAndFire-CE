package com.iafenvoy.iceandfire.render.model.animator;

import com.iafenvoy.iceandfire.entity.SeaSerpentEntity;
import com.iafenvoy.iceandfire.render.entity.state.SeaSerpentRenderState;import com.iafenvoy.iceandfire.render.model.util.SeaSerpentAnimations;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.iafenvoy.uranus.client.model.ITabulaModelAnimator;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.iafenvoy.uranus.client.model.util.TabulaModelHandlerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class SeaSerpentTabulaModelAnimator extends IceAndFireTabulaModelAnimator<SeaSerpentRenderState> implements ITabulaModelAnimator<SeaSerpentRenderState> {
    public final SeaSerpentAnimations[] swimPose = {SeaSerpentAnimations.SWIM1, SeaSerpentAnimations.SWIM3, SeaSerpentAnimations.SWIM4, SeaSerpentAnimations.SWIM6};

    public SeaSerpentTabulaModelAnimator() {
        super(resolve(SeaSerpentAnimations.T_POSE.getModelId()));
    }

    @Override
    public void setRotationAngles(TabulaModel<SeaSerpentRenderState> model, SeaSerpentRenderState state, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        model.resetToDefaultPose();
        model.getCube("BodyUpper").rotationPointY += 9;//model was made too high
        model.animator.startAnimate(state);
        this.animate(model, state, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale);
        int currentIndex = state.swimCycle / 10;
        int prevIndex = currentIndex - 1;
        if (prevIndex < 0) prevIndex = 3;
        TabulaModel<SeaSerpentRenderState> prevPosition = resolve(this.swimPose[prevIndex].getModelId());
        TabulaModel<SeaSerpentRenderState> currentPosition = resolve(this.swimPose[currentIndex].getModelId());
        if (prevPosition == null || currentPosition == null) return;
        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float delta = ((state.swimCycle) / 10.0F) % 1.0F + (partialTicks / 10.0F);

        for (AdvancedModelBox cube : model.getCubes().values()) {
            if (state.jumpProgress > 0.0F)
                if (!this.isRotationEqual(cube, resolve(SeaSerpentAnimations.JUMPING2.getModelId()).getCube(cube.boxName)))
                    this.transitionTo(cube, resolve(SeaSerpentAnimations.JUMPING2.getModelId()).getCube(cube.boxName), state.jumpProgress, 5, false);
            if (state.wantJumpProgress > 0.0F)
                if (!this.isRotationEqual(cube, resolve(SeaSerpentAnimations.JUMPING1.getModelId()).getCube(cube.boxName)))
                    this.transitionTo(cube, resolve(SeaSerpentAnimations.JUMPING1.getModelId()).getCube(cube.boxName), state.wantJumpProgress, 10, false);
            AdvancedModelBox prevPositionCube = prevPosition.getCube(cube.boxName);
            AdvancedModelBox currPositionCube = currentPosition.getCube(cube.boxName);
            float prevX = prevPositionCube.rotateAngleX;
            float prevY = prevPositionCube.rotateAngleY;
            float prevZ = prevPositionCube.rotateAngleZ;
            float x = currPositionCube.rotateAngleX;
            float y = currPositionCube.rotateAngleY;
            float z = currPositionCube.rotateAngleZ;
            this.addToRotateAngle(cube, limbSwingAmount, prevX + delta * this.distance(prevX, x), prevY + delta * this.distance(prevY, y), prevZ + delta * this.distance(prevZ, z));

        }
        if (state.breathProgress > 0.0F) {
            this.progressRotation(model.getCube("Head"), state.breathProgress, (float) Math.toRadians(-15F), 0, 0);
            this.progressRotation(model.getCube("HeadFront"), state.breathProgress, (float) Math.toRadians(-20F), 0, 0);
            this.progressRotation(model.getCube("Jaw"), state.breathProgress, (float) Math.toRadians(60F), 0, 0);
        }
        if (state.jumpRot > 0.0F) {
            float jumpRot = state.prevJumpRot + (state.jumpRot - state.prevJumpRot) * partialTicks;
            float turn = state.deltaMovementY * -4F;
            model.getCube("BodyUpper").rotateAngleX += (float) Math.toRadians(22.5F * turn) * jumpRot;
            model.getCube("Tail1").rotateAngleX -= (float) Math.toRadians(turn) * jumpRot;
            model.getCube("Tail2").rotateAngleX -= (float) Math.toRadians(turn) * jumpRot;
            model.getCube("Tail3").rotateAngleX -= (float) Math.toRadians(turn) * jumpRot;
            model.getCube("Tail4").rotateAngleX -= (float) Math.toRadians(turn) * jumpRot;
        }
        float prevRenderOffset = state.yBodyRotO + (state.yBodyRot - state.yBodyRotO) * partialTicks;

        model.getCube("Tail1").rotateAngleY += (state.pieceYaw[0] - prevRenderOffset) * ((float) Math.PI / 180F);
        model.getCube("Tail2").rotateAngleY += (state.pieceYaw[1] - prevRenderOffset) * ((float) Math.PI / 180F);
        model.getCube("Tail3").rotateAngleY += (state.pieceYaw[2] - prevRenderOffset) * ((float) Math.PI / 180F);
        model.getCube("Tail4").rotateAngleY += (state.pieceYaw[3] - prevRenderOffset) * ((float) Math.PI / 180F);
        model.getCube("BodyUpper").rotateAngleX -= rotationPitch * ((float) Math.PI / 180F);
        if (!state.isJumpingOutOfWater || state.isInWater) {
            model.getCube("Tail1").rotateAngleX -= (state.piecePitch[0] - 0) * ((float) Math.PI / 180F);
            model.getCube("Tail2").rotateAngleX -= (state.piecePitch[1] - 0) * ((float) Math.PI / 180F);
            model.getCube("Tail3").rotateAngleX -= (state.piecePitch[2] - 0) * ((float) Math.PI / 180F);
            model.getCube("Tail4").rotateAngleX -= (state.piecePitch[3] - 0) * ((float) Math.PI / 180F);
        }
        model.animator.endAnimate();
    }

    public void progressRotation(AdvancedModelBox model, float progress, float rotX, float rotY, float rotZ) {
        model.rotateAngleX += progress * (rotX - model.defaultRotationX) / 20.0F;
        model.rotateAngleY += progress * (rotY - model.defaultRotationY) / 20.0F;
        model.rotateAngleZ += progress * (rotZ - model.defaultRotationZ) / 20.0F;
    }

    private void animate(TabulaModel<SeaSerpentRenderState> model, SeaSerpentRenderState state, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        if (model.animator.setAnimation(SeaSerpentEntity.ANIMATION_SPEAK)) {
            model.animator.startKeyframe(5);
            this.rotate(model.animator, model.getCube("Jaw"), 25, 0, 0);
            model.animator.endKeyframe();
            model.animator.setStaticKeyframe(5);
            model.animator.resetKeyframe(5);
        }
        if (model.animator.setAnimation(SeaSerpentEntity.ANIMATION_BITE)) {
            model.animator.startKeyframe(5);
            this.moveToPose(model, resolve(SeaSerpentAnimations.BITE1.getModelId()));
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, resolve(SeaSerpentAnimations.BITE2.getModelId()));
            model.animator.endKeyframe();
            model.animator.setStaticKeyframe(2);
            model.animator.resetKeyframe(3);
        }
        if (model.animator.setAnimation(SeaSerpentEntity.ANIMATION_ROAR)) {
            model.animator.startKeyframe(10);
            this.moveToPose(model, resolve(SeaSerpentAnimations.ROAR1.getModelId()));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, resolve(SeaSerpentAnimations.ROAR2.getModelId()));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, resolve(SeaSerpentAnimations.ROAR3.getModelId()));
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
    }

    private static TabulaModel<SeaSerpentRenderState> resolve(Identifier id) {
        return TabulaModelHandlerHelper.getModel(id);
    }
}
