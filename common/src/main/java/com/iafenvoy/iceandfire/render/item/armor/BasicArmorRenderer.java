package com.iafenvoy.iceandfire.render.item.armor;

import com.iafenvoy.uranus.client.render.armor.IArmorRendererBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class BasicArmorRenderer implements IArmorRendererBase<HumanoidRenderState> {
    private final Boolean2ObjectFunction<HumanoidModel<HumanoidRenderState>> modelProvider;

    public BasicArmorRenderer(Boolean2ObjectFunction<HumanoidModel<HumanoidRenderState>> modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    public HumanoidModel<HumanoidRenderState> getHumanoidArmorModel(HumanoidRenderState state, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<HumanoidRenderState> bipedEntityModel) {
        return this.modelProvider.get(armorSlot == EquipmentSlot.LEGS || armorSlot == EquipmentSlot.HEAD);
    }
}
