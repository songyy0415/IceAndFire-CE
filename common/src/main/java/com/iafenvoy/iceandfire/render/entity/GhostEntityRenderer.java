package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.GhostEntity;
import com.iafenvoy.iceandfire.render.entity.state.GhostRenderState;
import com.iafenvoy.iceandfire.render.model.GhostModel;
import com.iafenvoy.iceandfire.util.Color4i;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class GhostEntityRenderer extends AdvancedEntityRendererBase<GhostEntity, GhostRenderState, GhostModel> {

    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/ghost/ghost_white.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/ghost/ghost_blue.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/ghost/ghost_green.png");
    public static final Identifier TEXTURE_SHOPPING_LIST = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/ghost/haunted_shopping_list.png");

    public GhostEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GhostModel(0.0F), 0.55F);
    }

    public static Identifier getGhostOverlayForType(int ghost) {
        return switch (ghost) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case -1 -> TEXTURE_SHOPPING_LIST;
            default -> TEXTURE_0;
        };
    }

    @Override
    public GhostRenderState createRenderState() {
        return new GhostRenderState();
    }

    @Override
    public void extractRenderState(GhostEntity entity, GhostRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isPassenger = entity.isPassenger();
        state.mainArm = entity.getMainArm();
        state.swingingArm = entity.swingingArm;
        state.attackTime = entity.getAttackAnim(partialTicks);
        state.isSneak = entity.isCrouching();
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.color = entity.getColor();
        state.isDaytimeMode = entity.isDaytimeMode();
        state.isHauntedShoppingList = entity.isHauntedShoppingList();
        if (entity.isDaytimeMode())
            state.alphaForRender = Mth.clamp((101 - Math.min(entity.getDaytimeCounter(), 100)) / 100F, 0, 1);
        else
            state.alphaForRender = Mth.clamp((Mth.sin((entity.tickCount + partialTicks) * 0.1F) + 1F) * 0.5F + 0.1F, 0F, 1F);
    }

    @Override
    protected RenderType getRenderType(GhostRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearedGlowing) {
        // The haunted shopping list replaces the model with a billboard quad (submitExtra).
        if (state.isHauntedShoppingList)
            return null;
        return RenderTypes.entityTranslucent(this.getTextureLocation(state));
    }

    @Override
    protected int getModelTint(GhostRenderState state) {
        return new Color4i(1.0F, 1.0F, 1.0F, state.alphaForRender).getIntValue();
    }

    @Override
    protected void submitExtra(GhostRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        if (state.isHauntedShoppingList) {
            poseStack.pushPose();
            poseStack.translate(0, 0.8F + Mth.sin(state.ageInTicks * 0.15F) * 0.1F, 0);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            int overlayCoords = getOverlayCoords(state, this.getWhiteOverlayProgress(state));
            RenderType renderType = RenderTypes.entityCutout(TEXTURE_SHOPPING_LIST);
            submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                Matrix4f matrix4f = fresh.last().pose();
                PoseStack.Pose entry = fresh.last();
                int alphaInt = (int) (state.alphaForRender * 255);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, -1, -2, 0, 1.0F, 0.0F, 0, 1, 0, 240);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, 1, -2, 0, 0.5F, 0.0F, 0, 1, 0, 240);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, 1, 2, 0, 0.5F, 1, 0, 1, 0, 240);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, -1, 2, 0, 1.0F, 1, 0, 1, 0, 240);
                fresh.mulPose(Axis.YP.rotationDegrees(180.0F));
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, -1, -2, 0, 0.0F, 0.0F, 0, 1, 0, 240);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, 1, -2, 0, 0.5F, 0.0F, 0, 1, 0, 240);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, 1, 2, 0, 0.5F, 1, 0, 1, 0, 240);
                this.drawVertex(matrix4f, entry, buffer, overlayCoords, alphaInt, -1, 2, 0, 0.0F, 1, 0, 1, 0, 240);
            });
            poseStack.popPose();
        }
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public Identifier getTextureLocation(GhostRenderState state) {
        return switch (state.color) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case -1 -> TEXTURE_SHOPPING_LIST;
            default -> TEXTURE_0;
        };
    }

    public void drawVertex(Matrix4f stack, PoseStack.Pose entry, VertexConsumer builder, int packedRed, int alphaInt, int x, int y, int z, float u, float v, int lightmap, int lightmap3, int lightmap2, int lightmap4) {
        builder.addVertex(stack, (float) x, (float) y, (float) z).setColor(255, 255, 255, alphaInt).setUv(u, v).setOverlay(packedRed).setLight(lightmap4).setNormal(entry, lightmap, lightmap2, lightmap3);
    }
}
