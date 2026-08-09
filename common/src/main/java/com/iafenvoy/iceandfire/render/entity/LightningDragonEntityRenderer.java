package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.entity.LightningDragonEntity;
import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.iceandfire.render.misc.LightningBoltData;
import com.iafenvoy.iceandfire.render.misc.LightningRenderer;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LightningDragonEntityRenderer extends DragonBaseEntityRenderer<LightningDragonEntity> {
    private final LightningRenderer lightningRenderer = new LightningRenderer();

    public LightningDragonEntityRenderer(EntityRendererProvider.Context context, TabulaModel<DragonRenderState> modelSupplier) {
        super(context, modelSupplier);
    }

    private static float getBoundedScale(float scale) {
        return (float) 0.5 + scale * ((float) 2 - (float) 0.5);
    }

    @Override
    public boolean shouldRender(LightningDragonEntity livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntityIn, camera, camX, camY, camZ)) return true;
        else {
            if (livingEntityIn.hasLightningTarget()) {
                Vec3 head = livingEntityIn.getHeadPosition();
                Vec3 target = new Vec3(livingEntityIn.getLightningTargetX(), livingEntityIn.getLightningTargetY(), livingEntityIn.getLightningTargetZ());
                return camera.isVisible(new AABB(head.x, head.y, head.z, target.x, target.y, target.z));
            }
            return false;
        }
    }

    @Override
    public void extractRenderState(LightningDragonEntity entity, DragonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hasLightningTarget = entity.hasLightningTarget();
        if (state.hasLightningTarget) {
            Minecraft client = Minecraft.getInstance();
            assert client.player != null;
            state.lightningDist = client.player.distanceTo(entity);
            Vec3 head = entity.getHeadPosition();
            Vec3 target = new Vec3(entity.getLightningTargetX(), entity.getLightningTargetY(), entity.getLightningTargetZ());
            float energyScale = 0.4F * entity.getAgeScale();
            state.lightningBolt = new LightningBoltData(LightningBoltData.BoltRenderInfo.ELECTRICITY, head, target, 15)
                .size(0.05F * getBoundedScale(energyScale))
                .lifespan(4)
                .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
        }
    }

    @Override
    public void submit(DragonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        if (state.hasLightningTarget && state.lightningBolt != null
            && state.lightningDist <= Math.max(256, Minecraft.getInstance().options.renderDistance().get() * 16F)) {
            poseStack.pushPose();
            this.lightningRenderer.update(null, state.lightningBolt, state.partialTicks);
            poseStack.translate(-state.x, -state.y, -state.z);
            this.lightningRenderer.render(state.partialTicks, poseStack, submitNodeCollector);
            poseStack.popPose();
        }
    }
}
