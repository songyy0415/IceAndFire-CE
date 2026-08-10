package com.iafenvoy.iceandfire.particle;

import com.iafenvoy.iceandfire.entity.GhostEntity;
import com.iafenvoy.iceandfire.registry.IafRenderLayers;
import com.iafenvoy.iceandfire.render.entity.GhostEntityRenderer;
import com.iafenvoy.iceandfire.render.entity.state.GhostRenderState;
import com.iafenvoy.iceandfire.render.model.GhostModel;
import com.iafenvoy.iceandfire.util.Color4i;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class GhostAppearanceParticle extends Particle {
    public static final ParticleRenderType PARTICLE_TYPE = new ParticleRenderType("ghost_appearance", "ghost_appearance");

    private final GhostModel model = new GhostModel(0.0F);
    private final int ghost;
    private final boolean fromLeft;

    protected GhostAppearanceParticle(ClientLevel level, double x, double y, double z, int ghost) {
        super(level, x, y, z);
        this.gravity = 0.0F;
        this.lifetime = 15;
        this.ghost = ghost;
        this.fromLeft = level.getRandom().nextBoolean();
    }

    public static ParticleProvider<SimpleParticleType> factory() {
        return (parameters, level, x, y, z, velocityX, velocityY, velocityZ, random) -> new GhostAppearanceParticle(level, x, y, z, 1);
    }

    @Override
    public ParticleRenderType getGroup() {
        return PARTICLE_TYPE;
    }

    // Called by the particle group during extraction; builds the model pose, pose stack and
    // render type for this particle. Returns null when the ghost cannot be rendered this frame.
    Group.State createState(Camera camera, float partialTick) {
        float f = (this.age + partialTick) / this.lifetime;
        float f1 = 0.05F + 0.5F * Mth.sin(f * (float) Math.PI);
        Entity entity = this.level.getEntity(this.ghost);
        if (entity instanceof GhostEntity ghostEntity && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(camera.rotation());
            if (this.fromLeft) {
                poseStack.mulPose(Axis.YN.rotationDegrees(150 * f - 60));
                poseStack.mulPose(Axis.ZN.rotationDegrees(150 * f - 60));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(150 * f - 60));
                poseStack.mulPose(Axis.ZP.rotationDegrees(150 * f - 60));
            }
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0D, 0.3F, 1.25D);

            GhostRenderState state = new GhostRenderState();
            GhostEntityRenderer renderer = (GhostEntityRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(ghostEntity);
            renderer.extractRenderState(ghostEntity, state, partialTick);
            state.alphaForRender = f1;
            this.model.setupAnim(state);

            RenderType renderType = IafRenderLayers.getGhost(GhostEntityRenderer.getGhostOverlayForType(ghostEntity.getColor()));
            int color = new Color4i(1.0F, 1.0F, 1.0F, f1).getIntValue();
            return new Group.State(this.model, state, poseStack, renderType, color);
        }
        return null;
    }

    public static class Group extends ParticleGroup<GhostAppearanceParticle> {
        public Group(ParticleEngine engine) {
            super(engine);
        }

        @Override
        public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
            List<State> states = new ArrayList<>();
            for (GhostAppearanceParticle particle : this.particles) {
                State state = particle.createState(camera, partialTick);
                if (state != null) states.add(state);
            }
            return new GroupState(states);
        }

        record State(GhostModel model, GhostRenderState renderState, PoseStack poseStack, RenderType renderType, int color) {
        }

        record GroupState(List<State> states) implements ParticleGroupRenderState {
            @Override
            public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
                for (State state : this.states) {
                    submitNodeCollector.submitModel(state.model, state.renderState, state.poseStack, state.renderType, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, state.color, null, 0, null);
                }
            }
        }
    }
}
