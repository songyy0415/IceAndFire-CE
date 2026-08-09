package com.iafenvoy.iceandfire.render.entity.state;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.item.ItemStack;

public class DreadKnightRenderState extends AnimatedBipedRenderState {
    public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
    public ItemStack mainHandItem = ItemStack.EMPTY;
    public boolean swinging;
    public int armorVariant;
}
