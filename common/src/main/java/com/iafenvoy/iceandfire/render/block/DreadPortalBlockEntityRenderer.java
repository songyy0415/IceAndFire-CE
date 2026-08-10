package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.item.block.entity.DreadPortalBlockEntity;
import com.iafenvoy.iceandfire.registry.IafRenderLayers;
import com.iafenvoy.iceandfire.render.block.state.DreadPortalRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DreadPortalBlockEntityRenderer implements BlockEntityRenderer<DreadPortalBlockEntity, DreadPortalRenderState> {
    public static final Identifier DREAD_PORTAL_BACKGROUND = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/environment/dread_portal_background.png");
    public static final Identifier DREAD_PORTAL = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/environment/dread_portal.png");

    public DreadPortalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public DreadPortalRenderState createRenderState() {
        return new DreadPortalRenderState();
    }

    @Override
    public void extractRenderState(DreadPortalBlockEntity entity, DreadPortalRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
    }

    @Override
    public void submit(DreadPortalRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        RenderType renderType = this.renderType();
        if (renderType != null) {
            submitNodeCollector.submitCustomGeometry(matrices, renderType, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                Matrix4f matrix4f = fresh.last().pose();
                VertexConsumer consumer = buffer;
                // z = 1
                this.vertex(consumer, matrix4f, pose, 0, 0, 1, state);
                this.vertex(consumer, matrix4f, pose, 1, 0, 1, state);
                this.vertex(consumer, matrix4f, pose, 1, 1, 1, state);
                this.vertex(consumer, matrix4f, pose, 0, 1, 1, state);
                // z = 0
                this.vertex(consumer, matrix4f, pose, 0, 0, 0, state);
                this.vertex(consumer, matrix4f, pose, 0, 1, 0, state);
                this.vertex(consumer, matrix4f, pose, 1, 1, 0, state);
                this.vertex(consumer, matrix4f, pose, 1, 0, 0, state);
                // x = 0
                this.vertex(consumer, matrix4f, pose, 0, 0, 0, state);
                this.vertex(consumer, matrix4f, pose, 0, 0, 1, state);
                this.vertex(consumer, matrix4f, pose, 0, 1, 1, state);
                this.vertex(consumer, matrix4f, pose, 0, 1, 0, state);
                // x = 1
                this.vertex(consumer, matrix4f, pose, 1, 0, 1, state);
                this.vertex(consumer, matrix4f, pose, 1, 0, 0, state);
                this.vertex(consumer, matrix4f, pose, 1, 1, 0, state);
                this.vertex(consumer, matrix4f, pose, 1, 1, 1, state);
                // y = 1
                this.vertex(consumer, matrix4f, pose, 0, 1, 0, state);
                this.vertex(consumer, matrix4f, pose, 0, 1, 1, state);
                this.vertex(consumer, matrix4f, pose, 1, 1, 1, state);
                this.vertex(consumer, matrix4f, pose, 1, 1, 0, state);
                // y = 0
                this.vertex(consumer, matrix4f, pose, 0, 0, 0, state);
                this.vertex(consumer, matrix4f, pose, 1, 0, 0, state);
                this.vertex(consumer, matrix4f, pose, 1, 0, 1, state);
                this.vertex(consumer, matrix4f, pose, 0, 0, 1, state);
            });
        }
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix4f, PoseStack.Pose pose, float x, float y, float z, DreadPortalRenderState state) {
        consumer.addVertex(matrix4f, x, y, z).setColor(-1).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    protected RenderType renderType() {
        return IafRenderLayers.getDreadlandsPortal();
    }
}
