package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.registry.IafDataComponents;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.render.model.GorgonHeadActiveModel;
import com.iafenvoy.iceandfire.render.model.GorgonHeadModel;
import com.iafenvoy.uranus.client.model.AdvancedEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class GorgonHeadSpecialModelRenderer extends AdvancedSpecialModelRenderer<Boolean> {
    private static final RenderType ACTIVE_TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/head_active.png"));
    private static final RenderType INACTIVE_TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/head_inactive.png"));
    private static final AdvancedEntityModel<?> ACTIVE_MODEL = new GorgonHeadActiveModel();
    private static final AdvancedEntityModel<?> INACTIVE_MODEL = new GorgonHeadModel();

    public GorgonHeadSpecialModelRenderer() {
        super(INACTIVE_MODEL);
    }

    @Override
    public Boolean extractArgument(ItemStack stack) {
        return stack.getItem() == IafItems.GORGON_HEAD.get() && stack.has(IafDataComponents.ACTIVE.get());
    }

    @Override
    public void submit(Boolean active, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color) {
        poseStack.pushPose();
        poseStack.translate(0.5F, active ? 1.5F : 1.25F, 0.5F);
        this.renderModel(active ? ACTIVE_MODEL : INACTIVE_MODEL, poseStack, submitNodeCollector, active ? ACTIVE_TEXTURE : INACTIVE_TEXTURE, light, overlay, color);
        poseStack.popPose();
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<Boolean> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Boolean> bake(SpecialModelRenderer.BakingContext context) {
            return new GorgonHeadSpecialModelRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Boolean>> type() {
            return MAP_CODEC;
        }
    }
}
