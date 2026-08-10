package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.iceandfire.render.entity.TideTridentEntityRenderer;
import com.iafenvoy.iceandfire.render.model.TideTridentModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;

public class TideTridentSpecialModelRenderer extends AdvancedSpecialModelRenderer<Boolean> {
    public TideTridentSpecialModelRenderer() {
        super(new TideTridentModel());
    }

    @Override
    public Boolean extractArgument(ItemStack stack) {
        return stack.hasFoil();
    }

    @Override
    public void submit(Boolean foil, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color) {
        this.renderModel(poseStack, submitNodeCollector, RenderTypes.entitySolid(TideTridentEntityRenderer.TRIDENT), light, overlay, color);
        if (foil)
            this.renderModel(poseStack, submitNodeCollector, RenderTypes.entityGlint(), light, overlay, color);
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<Boolean> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Boolean> bake(SpecialModelRenderer.BakingContext context) {
            return new TideTridentSpecialModelRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Boolean>> type() {
            return MAP_CODEC;
        }
    }
}
