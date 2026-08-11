package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.render.entity.TideTridentEntityRenderer;
import com.iafenvoy.iceandfire.render.model.TideTridentModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class TideTridentSpecialModelRenderer extends AdvancedSpecialModelRenderer<Boolean> {
    private static final RenderType FLAT_ICON = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/item/tide_trident.png"));

    public TideTridentSpecialModelRenderer() {
        super(new TideTridentModel());
    }

    @Override
    public Boolean extractArgument(ItemStack stack) {
        return stack.hasFoil();
    }

    @Override
    public void submit(Boolean foil, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color) {
        ItemDisplayContext context = ItemDisplayContextHolder.CURRENT.get();
        // Match the pre-migration TideTridentItemRenderer: GUI/FIXED/NONE/GROUND show the flat
        // item icon, hand-held contexts show the 3D model. The display context is captured by
        // ItemStackRenderStateMixin (26.2 does not pass it into SpecialModelRenderer.submit).
        if (context == ItemDisplayContext.GUI || context == ItemDisplayContext.FIXED
                || context == ItemDisplayContext.NONE || context == ItemDisplayContext.GROUND) {
            this.submitFlatIcon(poseStack, submitNodeCollector, light, overlay, FLAT_ICON);
            if (foil)
                this.submitFlatIcon(poseStack, submitNodeCollector, light, overlay, RenderTypes.entityGlint());
        } else {
            poseStack.pushPose();
            // The TideTridentModel is built in entity space (a ~1.5-block tall spear) and its
            // base model (models/item/tide_trident.json) carries no display transform, so it must
            // be centered and shrunk to fit the 0..1 item space — otherwise the item renders as a
            // thin sliver at the slot's origin.
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            // entityCutout (not entitySolid): tide_trident.png has large fully-transparent regions
            // (792/1024 texels alpha 0); entitySolid draws those as BLACK rectangles.
            this.renderModel(poseStack, submitNodeCollector, RenderTypes.entityCutout(TideTridentEntityRenderer.TRIDENT), light, overlay, color);
            if (foil)
                this.renderModel(poseStack, submitNodeCollector, RenderTypes.entityGlint(), light, overlay, color);
            poseStack.popPose();
        }
    }

    /**
     * Draws the flat 16x16 item icon (textures/item/tide_trident.png) filling the 0..1 item
     * space, replicating what the pre-migration renderer produced via the
     * {@code tide_trident_inventory} flat model for GUI/FIXED/NONE/GROUND contexts.
     */
    private void submitFlatIcon(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, RenderType renderType) {
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            Matrix4f matrix4f = fresh.last().pose();
            VertexConsumer consumer = buffer;
            consumer.addVertex(matrix4f, 0.0F, 0.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(fresh.last(), 0.0F, 0.0F, 1.0F);
            consumer.addVertex(matrix4f, 1.0F, 0.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setOverlay(overlay).setLight(light).setNormal(fresh.last(), 0.0F, 0.0F, 1.0F);
            consumer.addVertex(matrix4f, 1.0F, 1.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(fresh.last(), 0.0F, 0.0F, 1.0F);
            consumer.addVertex(matrix4f, 0.0F, 1.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(fresh.last(), 0.0F, 0.0F, 1.0F);
        });
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
