package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadLichSkullEntity;
import com.iafenvoy.iceandfire.render.entity.state.DreadLichSkullRenderState;
import com.iafenvoy.iceandfire.render.model.DreadLichSkullModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DreadLichSkullEntityRenderer extends EntityRenderer<DreadLichSkullEntity, DreadLichSkullRenderState> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_skull.png");
    private static final DreadLichSkullModel MODEL_SPIRIT = new DreadLichSkullModel();

    public DreadLichSkullEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DreadLichSkullRenderState createRenderState() {
        return new DreadLichSkullRenderState();
    }

    @Override
    public void extractRenderState(DreadLichSkullEntity entity, DreadLichSkullRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tickCount = entity.tickCount;
        state.yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
    }

    @Override
    public void submit(DreadLichSkullRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.tickCount > 3) {
            matrixStackIn.pushPose();
            matrixStackIn.scale(1.5F, -1.5F, 1.5F);
            matrixStackIn.translate(0F, 0F, 0F);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yaw - 180.0F));
            RenderType renderType = RenderTypes.eyes(TEXTURE);
            if (renderType != null) {
                submitNodeCollector.submitCustomGeometry(matrixStackIn, renderType, (pose, buffer) -> {
                    PoseStack fresh = new PoseStack();
                    fresh.last().pose().set(pose.pose());
                    fresh.last().normal().set(pose.normal());
                    MODEL_SPIRIT.renderPartsToBuffer(fresh, buffer, 240, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            matrixStackIn.popPose();
        }
        super.submit(state, matrixStackIn, submitNodeCollector, camera);
    }

    public Identifier getTextureLocation(DreadLichSkullRenderState state) {
        return TEXTURE;
    }
}
