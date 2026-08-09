package com.iafenvoy.iceandfire.render.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class BipedRenderState extends LivingEntityRenderState {
    public boolean isPassenger;
    public HumanoidArm mainArm = HumanoidArm.RIGHT;
    public InteractionHand swingingArm = InteractionHand.MAIN_HAND;
    public float attackTime;
    public boolean isSneak;
}
