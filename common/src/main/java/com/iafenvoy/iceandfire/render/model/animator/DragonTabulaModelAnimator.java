package com.iafenvoy.iceandfire.render.model.animator;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.iceandfire.render.model.util.DragonPoses;
import com.iafenvoy.iceandfire.render.model.util.IEnumDragonModelTypes;
import com.iafenvoy.iceandfire.render.model.util.IEnumDragonPoses;
import com.iafenvoy.iceandfire.render.model.util.LegArticulator;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.iafenvoy.uranus.client.model.ITabulaModelAnimator;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.iafenvoy.uranus.client.model.util.TabulaModelHandlerHelper;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public abstract class DragonTabulaModelAnimator<T extends DragonRenderState> extends IceAndFireTabulaModelAnimator<T> implements ITabulaModelAnimator<T> {
    //FIXME::Will not reload
    private final Map<DragonPoses, TabulaModel<T>> modelCache = new LinkedHashMap<>();
    protected final IEnumDragonModelTypes modelType;
    protected TabulaModel<T>[] walkPoses;
    protected TabulaModel<T>[] flyPoses;
    protected TabulaModel<T>[] swimPoses;
    protected AdvancedModelBox[] neckParts;
    protected AdvancedModelBox[] tailParts;
    protected AdvancedModelBox[] tailPartsWBody;
    protected AdvancedModelBox[] toesPartsL;
    protected AdvancedModelBox[] toesPartsR;
    protected AdvancedModelBox[] clawL;
    protected AdvancedModelBox[] clawR;

    public DragonTabulaModelAnimator(IEnumDragonPoses pose, IEnumDragonModelTypes modelType) {
        super(TabulaModelHandlerHelper.getModel(buildId(pose, modelType)));
        this.modelType = modelType;
    }

    public static Identifier buildId(IEnumDragonPoses pose, IEnumDragonModelTypes modelType) {
        return Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, modelType.getModelType() + "dragon/" + modelType.getModelType() + "dragon_" + pose.getPose());
    }

    public void init(TabulaModel<T> model) {
        this.neckParts = new AdvancedModelBox[]{model.getCube("Neck1"), model.getCube("Neck2"), model.getCube("Neck3"), model.getCube("Head")};
        this.tailParts = new AdvancedModelBox[]{model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4")};
        this.tailPartsWBody = new AdvancedModelBox[]{model.getCube("BodyLower"), model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4")};
        this.toesPartsL = new AdvancedModelBox[]{model.getCube("ToeL1"), model.getCube("ToeL2"), model.getCube("ToeL3")};
        this.toesPartsR = new AdvancedModelBox[]{model.getCube("ToeR1"), model.getCube("ToeR2"), model.getCube("ToeR3")};
        this.clawL = new AdvancedModelBox[]{model.getCube("ClawL")};
        this.clawR = new AdvancedModelBox[]{model.getCube("ClawR")};
    }

    @Override
    public void setRotationAngles(TabulaModel<T> model, T state, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        model.resetToDefaultPose();
        if (this.neckParts == null) this.init(model);
        this.animate(model, state);

        boolean walking = !state.isHovering && !state.isFlying && state.hoverProgress <= 0 && state.flyProgress <= 0;
        boolean swimming = state.isInWater && state.swimProgress > 0;

        int currentIndex = walking ? (state.walkCycle / 10) : (state.flightCycle / 10);
        if (swimming) currentIndex = state.swimCycle / 10;
        int prevIndex = currentIndex - 1;
        if (prevIndex < 0) prevIndex = swimming ? 4 : walking ? 3 : 5;

        TabulaModel<T> currentPosition = swimming ? this.swimPoses[currentIndex] : walking ? this.walkPoses[currentIndex] : this.flyPoses[currentIndex];
        TabulaModel<T> prevPosition = swimming ? this.swimPoses[prevIndex] : walking ? this.walkPoses[prevIndex] : this.flyPoses[prevIndex];
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float cyclePos = swimming ? state.swimCycle : walking ? state.walkCycle : state.flightCycle;
        float tickAdvance = (walking || swimming) ? 1.0F : 2.0F;
        float smoothCycle = cyclePos + partialTick * tickAdvance;
        float deltaTicks = (smoothCycle / 10.0F) % 1.0F;

        float speed_walk = 0.2F;
        float speed_idle = state.isSleeping ? 0.025F : 0.05F;
        float speed_fly = 0.2F;
        float degree_walk = 0.5F;
        float degree_idle = state.isSleeping ? 0.25F : 0.5F;
        float degree_fly = 0.5F;

        if (state.isModelDead) {
            for (AdvancedModelBox cube : model.getCubes().values())
                this.setRotationsLoopDeath(state, partialTick, cube);
            return;
        }

        if (state.isNoAi) return;

        for (AdvancedModelBox cube : model.getCubes().values())
            this.setRotationsLoop(model, state, limbSwingAmount, walking, currentPosition, prevPosition, partialTick, deltaTicks, cube);

        if (state.getAnimation() != DragonBaseEntity.ANIMATION_SHAKEPREY || state.getAnimation() != DragonBaseEntity.ANIMATION_ROAR)
            model.faceTarget(rotationYaw, rotationPitch, 2, this.neckParts);
        if (!walking) {
            model.bob(model.getCube("BodyUpper"), -speed_fly, degree_fly * 5, false, ageInTicks, 1);
            model.walk(model.getCube("BodyUpper"), -speed_fly, degree_fly * 0.1F, false, 0, 0, ageInTicks, 1);
            model.chainWave(this.tailPartsWBody, speed_fly, degree_fly * -0.1F, 0, ageInTicks, 1);
            model.chainWave(this.neckParts, speed_fly, degree_fly * 0.2F, -4, ageInTicks, 1);
            model.chainWave(this.toesPartsL, speed_fly, degree_fly * 0.2F, -2, ageInTicks, 1);
            model.chainWave(this.toesPartsR, speed_fly, degree_fly * 0.2F, -2, ageInTicks, 1);
            model.walk(model.getCube("ThighR"), -speed_fly, degree_fly * 0.1F, false, 0, 0, ageInTicks, 1);
            model.walk(model.getCube("ThighL"), -speed_fly, degree_fly * 0.1F, true, 0, 0, ageInTicks, 1);
        } else {
            model.bob(model.getCube("BodyUpper"), speed_walk * 2, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.bob(model.getCube("ThighR"), speed_walk, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.bob(model.getCube("ThighL"), speed_walk, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.chainSwing(this.tailParts, speed_walk, degree_walk * 0.25F, -2, limbSwing, limbSwingAmount);
            model.chainWave(this.tailParts, speed_walk, degree_walk * 0.15F, 2, limbSwing, limbSwingAmount);
            model.chainSwing(this.neckParts, speed_walk, degree_walk * 0.15F, 2, limbSwing, limbSwingAmount);
            model.chainWave(this.neckParts, speed_walk, degree_walk * 0.05F, -2, limbSwing, limbSwingAmount);
            model.chainSwing(this.tailParts, speed_idle, degree_idle * 0.25F, -2, ageInTicks, 1);
            model.chainWave(this.tailParts, speed_idle, degree_idle * 0.15F, -2, ageInTicks, 1);
            model.chainWave(this.neckParts, speed_idle, degree_idle * -0.15F, -3, ageInTicks, 1);
            model.walk(model.getCube("Neck1"), speed_idle, degree_idle * 0.05F, false, 0, 0, ageInTicks, 1);
        }
        model.bob(model.getCube("BodyUpper"), speed_idle, degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(model.getCube("ThighR"), speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(model.getCube("ThighL"), speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(model.getCube("armR1"), speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(model.getCube("armL1"), speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);

        if (state.isActuallyBreathingFire) {
            float speed_shake = 0.7F;
            float degree_shake = 0.1F;
            model.chainFlap(this.neckParts, speed_shake, degree_shake, 2, ageInTicks, 1);
            model.chainSwing(this.neckParts, speed_shake * 0.65F, degree_shake * 0.1F, 1, ageInTicks, 1);
        }

        if (state.turnBuffer != null && !state.isVehicle && !state.isPassenger && state.isBreathingFire)
            state.turnBuffer.applyChainSwingBuffer(this.neckParts);
        if (state.tailBuffer != null && !state.isPassenger)
            state.tailBuffer.applyChainSwingBuffer(this.tailPartsWBody);
        if (state.rollBuffer != null && state.pitchBufferBody != null && state.pitchBuffer != null)
            if (state.flyProgress > 0 || state.hoverProgress > 0) {
                state.rollBuffer.applyChainFlapBuffer(model.getCube("BodyUpper"));
                state.pitchBufferBody.applyChainWaveBuffer(model.getCube("BodyUpper"));
                state.pitchBuffer.applyChainWaveBufferReverse(this.tailPartsWBody);
            }
        if (state.bbWidth >= 2 && state.flyProgress == 0 && state.hoverProgress == 0)
            LegArticulator.articulateQuadruped(state.renderSize, state.legSolver, model.getCube("BodyUpper"), model.getCube("BodyLower"), model.getCube("Neck1"),
                    model.getCube("ThighL"), model.getCube("LegL"), this.toesPartsL,
                    model.getCube("ThighR"), model.getCube("LegR"), this.toesPartsR,
                    model.getCube("armL1"), model.getCube("armL2"), this.clawL,
                    model.getCube("armR1"), model.getCube("armR2"), this.clawR,
                    1.0F, 0.5F, 0.5F, -0.15F, -0.15F, 0F,
                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
            );
    }


    private void setRotationsLoop(TabulaModel<T> model, T state, float limbSwingAmount, boolean walking, TabulaModel<T> currentPosition, TabulaModel<T> prevPosition, float partialTick, float deltaTicks, AdvancedModelBox cube) {
        this.genderMob(state, cube);
        if (walking && state.flyProgress <= 0.0F && state.hoverProgress <= 0.0F && state.modelDeadProgress <= 0.0F) {
            AdvancedModelBox walkPart = this.getModel(DragonPoses.GROUND_POSE).getCube(cube.boxName);
            AdvancedModelBox prevPositionCube = prevPosition.getCube(cube.boxName);
            AdvancedModelBox currPositionCube = currentPosition.getCube(cube.boxName);

            if (prevPositionCube == null || currPositionCube == null) return;

            float prevX = prevPositionCube.rotateAngleX;
            float prevY = prevPositionCube.rotateAngleY;
            float prevZ = prevPositionCube.rotateAngleZ;
            float x = currPositionCube.rotateAngleX;
            float y = currPositionCube.rotateAngleY;
            float z = currPositionCube.rotateAngleZ;
            if (this.isHorn(cube) || this.isWing(model, cube) && (state.getAnimation() == DragonBaseEntity.ANIMATION_WINGBLAST || state.getAnimation() == DragonBaseEntity.ANIMATION_EPIC_ROAR))
                this.addToRotateAngle(cube, limbSwingAmount, walkPart.rotateAngleX, walkPart.rotateAngleY, walkPart.rotateAngleZ);
            else
                this.addToRotateAngle(cube, limbSwingAmount, prevX + deltaTicks * this.distance(prevX, x), prevY + deltaTicks * this.distance(prevY, y), prevZ + deltaTicks * this.distance(prevZ, z));
        }
        if (state.sleepProgress > 0.0F)
            if (!this.isRotationEqual(cube, this.getModel(DragonPoses.SLEEPING_POSE).getCube(cube.boxName)))
                this.transitionTo(cube, this.getModel(DragonPoses.SLEEPING_POSE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevAnimationProgresses[1], state.sleepProgress), 20, false);
        if (state.hoverProgress > 0.0F)
            if (!this.isRotationEqual(cube, this.getModel(DragonPoses.HOVERING_POSE).getCube(cube.boxName)) && !this.isWing(model, cube) && !cube.boxName.contains("Tail"))
                this.transitionTo(cube, this.getModel(DragonPoses.HOVERING_POSE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevAnimationProgresses[2], state.hoverProgress), 20, false);
        if (state.flyProgress > 0.0F)
            if (!this.isRotationEqual(cube, this.getModel(DragonPoses.FLYING_POSE).getCube(cube.boxName)))
                this.transitionTo(cube, this.getModel(DragonPoses.FLYING_POSE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevAnimationProgresses[3], state.flyProgress) - (Mth.lerp(partialTick, state.prevDiveProgress, state.diveProgress)) * 2, 20, false);
        if (state.sitProgress > 0.0F)
            if (!state.isPassenger)
                if (!this.isRotationEqual(cube, this.getModel(DragonPoses.SITTING_POSE).getCube(cube.boxName)))
                    this.transitionTo(cube, this.getModel(DragonPoses.SITTING_POSE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevAnimationProgresses[0], state.sitProgress), 20, false);
        if (state.ridingProgress > 0.0F)
            if (!this.isHorn(cube) && !this.isRotationEqual(cube, this.getModel(DragonPoses.SIT_ON_PLAYER_POSE).getCube(cube.boxName))) {
                this.transitionTo(cube, this.getModel(DragonPoses.SIT_ON_PLAYER_POSE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevAnimationProgresses[5], state.ridingProgress), 20, false);
                if (cube.boxName.equals("BodyUpper"))
                    cube.offsetZ += ((-12F - cube.offsetZ) / 20) * Mth.lerp(partialTick, state.prevAnimationProgresses[5], state.ridingProgress);
            }
        if (state.tackleProgress > 0.0F)
            if (!this.isRotationEqual(this.getModel(DragonPoses.TACKLE).getCube(cube.boxName), this.getModel(DragonPoses.FLYING_POSE).getCube(cube.boxName)) && !this.isWing(model, cube))
                this.transitionTo(cube, this.getModel(DragonPoses.TACKLE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevAnimationProgresses[6], state.tackleProgress), 5, false);
        if (state.diveProgress > 0.0F)
            if (!this.isRotationEqual(cube, this.getModel(DragonPoses.DIVING_POSE).getCube(cube.boxName)))
                this.transitionTo(cube, this.getModel(DragonPoses.DIVING_POSE).getCube(cube.boxName), Mth.lerp(partialTick, state.prevDiveProgress, state.diveProgress), 10, false);
        if (state.fireBreathProgress > 0.0F)
            if (!this.isRotationEqual(cube, this.getModel(DragonPoses.STREAM_BREATH).getCube(cube.boxName)) && !this.isWing(model, cube) && !cube.boxName.contains("Finger")) {
                if (state.prevFireBreathProgress <= state.fireBreathProgress)
                    this.transitionTo(cube, this.getModel(DragonPoses.BLAST_CHARGE3).getCube(cube.boxName), Mth.clamp(Mth.lerp(partialTick, state.prevFireBreathProgress, state.fireBreathProgress), 0, 5), 5, false);
                this.transitionTo(cube, this.getModel(DragonPoses.STREAM_BREATH).getCube(cube.boxName), Mth.clamp(Mth.lerp(partialTick, state.prevFireBreathProgress, state.fireBreathProgress) - 5, 0, 5), 5, false);
            }
        if (!walking) {
            AdvancedModelBox flightPart = this.getModel(DragonPoses.FLYING_POSE).getCube(cube.boxName);
            AdvancedModelBox prevPositionCube = prevPosition.getCube(cube.boxName);
            AdvancedModelBox currPositionCube = currentPosition.getCube(cube.boxName);
            float prevX = prevPositionCube.rotateAngleX;
            float prevY = prevPositionCube.rotateAngleY;
            float prevZ = prevPositionCube.rotateAngleZ;
            float x = currPositionCube.rotateAngleX;
            float y = currPositionCube.rotateAngleY;
            float z = currPositionCube.rotateAngleZ;
            if (x != flightPart.rotateAngleX || y != flightPart.rotateAngleY || z != flightPart.rotateAngleZ)
                this.setRotateAngle(cube, 1F, prevX + deltaTicks * this.distance(prevX, x), prevY + deltaTicks * this.distance(prevY, y), prevZ + deltaTicks * this.distance(prevZ, z));
        }
    }

    public void setRotationsLoopDeath(T state, float partialTick, AdvancedModelBox cube) {
        if (state.modelDeadProgress > 0.0F) {
            // TODO: Figure out what's up with custom poses
            // DON'T use this in it's current state since it heavily effects render performance due to the fact that
            // custom poses aren't being used right now
            // TabulaModel customPose = customPose(state);
            TabulaModel<T> pose = this.getModel(DragonPoses.DEAD);
            if (!this.isRotationEqual(cube, pose.getCube(cube.boxName)))
                this.transitionTo(cube, pose.getCube(cube.boxName), state.prevModelDeadProgress + (state.modelDeadProgress - state.prevModelDeadProgress) * Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), 20, cube.boxName.equals("ThighR") || cube.boxName.equals("ThighL"));
            //Ugly hack to make sure ice dragon models are touching the ground when dead
            if (this instanceof IceDragonTabulaModelAnimator)
                if (cube.boxName.equals("BodyUpper"))
                    cube.rotationPointY += 0.35F * Mth.lerp(partialTick, state.prevModelDeadProgress, state.modelDeadProgress);
        }
    }

    protected boolean isWing(TabulaModel<T> model, AdvancedModelBox modelRenderer) {
        return model.getCube("armL1") == modelRenderer || model.getCube("armR1") == modelRenderer || model.getCube("armL1").childModels.contains(modelRenderer) || model.getCube("armR1").childModels.contains(modelRenderer);
    }

    protected boolean isHorn(AdvancedModelBox modelRenderer) {
        return modelRenderer.boxName.contains("Horn");
    }

    protected void genderMob(T state, AdvancedModelBox cube) {
        if (!state.isMale) {
            TabulaModel<T> maleModel = this.getModel(DragonPoses.MALE);
            TabulaModel<T> femaleModel = this.getModel(DragonPoses.FEMALE);
            AdvancedModelBox femaleModelCube = femaleModel.getCube(cube.boxName);
            AdvancedModelBox maleModelCube = maleModel.getCube(cube.boxName);
            if (maleModelCube == null || femaleModelCube == null) return;
            float x = femaleModelCube.rotateAngleX;
            float y = femaleModelCube.rotateAngleY;
            float z = femaleModelCube.rotateAngleZ;
            if (x != maleModelCube.rotateAngleX || y != maleModelCube.rotateAngleY || z != maleModelCube.rotateAngleZ)
                this.setRotateAngle(cube, 1F, x, y, z);
        }
    }

    protected TabulaModel<T> getModel(DragonPoses pose) {
        if (this.modelCache.containsKey(pose)) return this.modelCache.get(pose);
        TabulaModel<T> model = TabulaModelHandlerHelper.getModel(buildId(pose, this.modelType));
        this.modelCache.put(pose, model);
        return model;
    }

    protected void animate(TabulaModel<T> model, T state) {
        AdvancedModelBox modelCubeJaw = model.getCube("Jaw");
        AdvancedModelBox modelCubeBodyUpper = model.getCube("BodyUpper");
        model.animator.startAnimate(state);
        //Firecharge
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_FIRECHARGE)) {
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.BLAST_CHARGE1));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.BLAST_CHARGE2));
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.BLAST_CHARGE3));
            model.animator.endKeyframe();
            model.animator.resetKeyframe(5);
        }
        //Speak
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_SPEAK)) {
            model.animator.startKeyframe(5);
            this.rotate(model.animator, modelCubeJaw, 18, 0, 0);
            model.animator.move(modelCubeJaw, 0, 0, 0.2F);
            model.animator.endKeyframe();
            model.animator.setStaticKeyframe(5);
            model.animator.startKeyframe(5);
            this.rotate(model.animator, modelCubeJaw, 18, 0, 0);
            model.animator.move(modelCubeJaw, 0, 0, 0.2F);
            model.animator.endKeyframe();
            model.animator.resetKeyframe(5);
        }
        //Bite
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_BITE)) {
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.BITE1));
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.BITE2));
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.BITE3));
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
        //Shakeprey
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_SHAKEPREY)) {
            model.animator.startKeyframe(15);
            this.moveToPose(model, this.getModel(DragonPoses.GRAB1));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.GRAB2));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.GRAB_SHAKE1));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.GRAB_SHAKE2));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.GRAB_SHAKE3));
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
        //Tailwhack
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_TAILWHACK)) {
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.TAIL_WHIP1));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.TAIL_WHIP2));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.TAIL_WHIP3));
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
        //Wingblast
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_WINGBLAST)) {
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST1));
            model.animator.move(modelCubeBodyUpper, 0, 0, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST2));
            model.animator.move(modelCubeBodyUpper, 0, -2F, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST3));
            model.animator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST4));
            model.animator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST5));
            model.animator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST6));
            model.animator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.moveToPose(model, this.getModel(DragonPoses.WING_BLAST7));
            model.animator.move(modelCubeBodyUpper, 0, -2F, 0);
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
        //Roar
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_ROAR)) {
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.ROAR1));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.ROAR2));
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.ROAR3));
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
        //Epicroar
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_EPIC_ROAR)) {
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.EPIC_ROAR1));
            model.animator.rotate(modelCubeBodyUpper, -0.1F, 0, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.EPIC_ROAR2));
            model.animator.rotate(modelCubeBodyUpper, -0.2F, 0, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.EPIC_ROAR3));
            model.animator.rotate(modelCubeBodyUpper, -0.2F, 0, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.EPIC_ROAR2));
            model.animator.rotate(modelCubeBodyUpper, -0.2F, 0, 0);
            model.animator.endKeyframe();
            model.animator.startKeyframe(10);
            this.moveToPose(model, this.getModel(DragonPoses.EPIC_ROAR3));
            model.animator.rotate(modelCubeBodyUpper, -0.1F, 0, 0);
            model.animator.endKeyframe();
            model.animator.resetKeyframe(10);
        }
        // EATING
        if (model.animator.setAnimation(DragonBaseEntity.ANIMATION_EAT)) {
            model.animator.startKeyframe(5);
            this.rotate(model.animator, model.getCube("Neck1"), 18, 0, 0);
            this.rotate(model.animator, model.getCube("Neck2"), 18, 0, 0);
            model.animator.endKeyframe();
            //CODE from speak
            model.animator.startKeyframe(5);
            this.rotate(model.animator, modelCubeJaw, 18, 0, 0);
            model.animator.move(modelCubeJaw, 0, 0, 0.2F);
            model.animator.endKeyframe();
            model.animator.setStaticKeyframe(5);
            model.animator.startKeyframe(5);
            this.rotate(model.animator, modelCubeJaw, 18, 0, 0);
            model.animator.move(modelCubeJaw, 0, 0, 0.2F);
            model.animator.endKeyframe();
            model.animator.startKeyframe(5);
            this.rotate(model.animator, model.getCube("Neck1"), -18, 0, 0);
            this.rotate(model.animator, model.getCube("Neck2"), -18, 0, 0);
            model.animator.endKeyframe();
        }
        model.animator.endAnimate();
    }
}
