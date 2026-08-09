package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.iceandfire.entity.util.ChainBuffer;
import com.iafenvoy.iceandfire.render.misc.LightningBoltData;
import com.iafenvoy.iceandfire.entity.util.ReversedBuffer;
import com.iafenvoy.iceandfire.render.model.IFChainBuffer;
import com.iafenvoy.iceandfire.render.model.util.LegSolverQuadruped;
import com.iafenvoy.uranus.animation.Animation;
import com.iafenvoy.uranus.animation.IAnimatedEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;

public class DragonRenderState extends LivingEntityRenderState implements IAnimatedEntity {
    public Identifier texture;
    public Identifier eyesTexture;
    public Identifier maleOverlayTexture;
    public Identifier armorHead;
    public Identifier armorChest;
    public Identifier armorLegs;
    public Identifier armorFeet;
    public final ItemStackRenderState bannerItem = new ItemStackRenderState();
    public float renderSize = 1.0F;
    public float dragonPitch;
    public float prevDragonPitch;
    public boolean shouldRenderEyes;
    public boolean isMale;
    public boolean isSkeletal;
    public String variant;
    public int dragonStage;
    public boolean isHovering;
    public boolean isFlying;
    public boolean isInWater;
    public boolean isSleeping;
    public boolean isModelDead;
    public boolean isNoAi;
    public boolean isActuallyBreathingFire;
    public boolean isBreathingFire;
    public boolean isVehicle;
    public boolean isPassenger;
    public float sitProgress;
    public float sleepProgress;
    public float hoverProgress;
    public float flyProgress;
    public float fireBreathProgress;
    public float diveProgress;
    public float prevDiveProgress;
    public float prevFireBreathProgress;
    public float modelDeadProgress;
    public float prevModelDeadProgress;
    public float ridingProgress;
    public float tackleProgress;
    public float swimProgress;
    public int walkCycle;
    public int flightCycle;
    public int swimCycle;
    public float[] prevAnimationProgresses = new float[10];
    public float bbWidth;
    public ReversedBuffer turnBuffer;
    public ChainBuffer tailBuffer;
    public IFChainBuffer rollBuffer;
    public IFChainBuffer pitchBuffer;
    public IFChainBuffer pitchBufferBody;
    public LegSolverQuadruped legSolver;
    public final List<EntityRenderState> preyRenderStates = new ArrayList<>();
    public final List<Byte> preyModelTypes = new ArrayList<>();
    public float partialTicks;
    public float dragonScale = 1.0F;
    public boolean shakingPrey;
    public boolean hasLightningTarget;
    public LightningBoltData lightningBolt;
    public double lightningDist;
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
