package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.iceandfire.data.TrollType;
import com.iafenvoy.iceandfire.item.tool.TrollWeaponItem;
import com.iafenvoy.iceandfire.render.model.TrollWeaponModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;

public class TrollWeaponSpecialModelRenderer extends AdvancedSpecialModelRenderer<TrollType.ITrollWeapon> {
    public TrollWeaponSpecialModelRenderer() {
        super(new TrollWeaponModel());
    }

    @Override
    public TrollType.ITrollWeapon extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof TrollWeaponItem trollWeapon)
            return trollWeapon.weapon;
        return TrollType.BuiltinWeapon.AXE;
    }

    @Override
    public void submit(TrollType.ITrollWeapon weapon, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color) {
        poseStack.pushPose();
        poseStack.translate(0.5F, -0.75F, 0.5F);
        this.renderModel(poseStack, submitNodeCollector, RenderTypes.entityCutout(weapon.getTextureLocation()), light, overlay, color);
        poseStack.popPose();
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<TrollType.ITrollWeapon> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<TrollType.ITrollWeapon> bake(SpecialModelRenderer.BakingContext context) {
            return new TrollWeaponSpecialModelRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<TrollType.ITrollWeapon>> type() {
            return MAP_CODEC;
        }
    }
}
