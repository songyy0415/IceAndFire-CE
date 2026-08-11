package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.uranus.client.model.AdvancedEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;

/**
 * Base renderer for mobs whose model is a hand-built {@link AdvancedEntityModel} tree.
 *
 * <p>26.2 {@code Model.renderToBuffer} is final and draws only the single
 * {@code ModelPart root}, so an {@code AdvancedModelBox} hierarchy (which carries the
 * geometry in its own cube lists) would render nothing through the vanilla model path.
 * This replicates the vanilla {@code LivingEntityRenderer.submit} flow but feeds the
 * {@code AdvancedModelBox} tree into the deferred render graph via
 * {@code SubmitNodeCollector.submitCustomGeometry} + {@code model.renderPartsToBuffer()}.
 * The animation input semantics are preserved unchanged (setupAnim runs before the
 * model node is drawn, so the deferred draw sees the animated pose).
 */
public abstract class AdvancedEntityRendererBase<E extends Mob, S extends LivingEntityRenderState, M extends AdvancedEntityModel<S>> extends MobRenderer<E, S, M> {
    public AdvancedEntityRendererBase(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    /**
     * Hook for renderers whose body is drawn by different model classes depending on the
     * entity (e.g. Cockatrice swaps adult/chick model by {@code state.isBaby}). Defaults to
     * the renderer's main model; both the {@code setupAnim} call and the deferred
     * {@code renderPartsToBuffer} draw use whichever model this returns.
     */
    protected M getModel(S state) {
        return this.model;
    }

    /**
     * Hook for extra deferred geometry drawn in the entity's local pose space after the body
     * and layers (e.g. the Cockatrice beam). Subclasses submit it via
     * {@code submitNodeCollector.submitCustomGeometry(...)}; the callback receives the pose
     * captured at submit time and rebuilds a fresh {@code PoseStack} from it.
     */
    protected void submitExtra(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.hasPose(Pose.SLEEPING)) {
            Direction bedOrientation = state.bedOrientation;
            if (bedOrientation != null) {
                float headOffset = state.eyeHeight - 0.1F;
                poseStack.translate(-bedOrientation.getStepX() * headOffset, 0.0F, -bedOrientation.getStepZ() * headOffset);
            }
        }
        float scale = state.scale;
        poseStack.scale(scale, scale, scale);
        this.setupRotations(state, poseStack, state.bodyRot, scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(state, poseStack);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        boolean isBodyVisible = this.isBodyVisible(state);
        boolean forceTransparent = !isBodyVisible && !state.isInvisibleToPlayer;
        RenderType renderType = this.getRenderType(state, isBodyVisible, forceTransparent, state.appearsGlowing());
        if (renderType != null) {
            int overlayCoords = getOverlayCoords(state, this.getWhiteOverlayProgress(state));
            int baseColor = forceTransparent ? 654311423 : -1;
            int tintedColor = ARGB.multiply(baseColor, this.getModelTint(state));
            M model = this.getModel(state);
            model.setupAnim(state);
            // Body: skipped when getRenderType chose an outline render type (invisible-but-glowing
            // entity renders its outline only) — matches vanilla submitModel's !renderType.isOutline().
            if (!renderType.isOutline()) {
                submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                    PoseStack fresh = new PoseStack();
                    fresh.last().pose().set(pose.pose());
                    fresh.last().normal().set(pose.normal());
                    // Re-run setupAnim at deferred-draw time so each node draws the pose of ITS
                    // render state. The per-renderer model is shared: without this, every visible
                    // same-type entity renders the pose of whichever entity was submitted last this
                    // frame, so multiple dragons all animate in sync with the last one and snap when
                    // its animation state changes (visible jerk). IAF models are idempotent
                    // (resetToDefaultPose first), so re-running is safe.
                    model.setupAnim(state);
                    model.renderPartsToBuffer(fresh, buffer, state.lightCoords, overlayCoords, tintedColor);
                });
            }
            // Glowing outline (spectral arrow, team color, spectator): vanilla submitModel submits a
            // second node to the outline phase with the outline render type and the outline color as
            // the vertex tint. submitCustomGeometry routes to the outline phase when the render type
            // isOutline(), so submit the same geometry again with that render type and full-bright
            // light — otherwise a glowing dragon/sea-serpent loses its outline entirely.
            if (state.outlineColor != 0) {
                RenderType outlineRenderType = renderType.isOutline() ? renderType : renderType.outline().orElse(null);
                if (outlineRenderType != null) {
                    submitNodeCollector.submitCustomGeometry(poseStack, outlineRenderType, (pose, buffer) -> {
                        PoseStack fresh = new PoseStack();
                        fresh.last().pose().set(pose.pose());
                        fresh.last().normal().set(pose.normal());
                        model.setupAnim(state);
                        model.renderPartsToBuffer(fresh, buffer, 15728880, OverlayTexture.NO_OVERLAY, state.outlineColor);
                    });
                }
            }
        }
        if (this.shouldRenderLayers(state) && !this.layers.isEmpty()) {
            for (RenderLayer<S, M> layer : this.layers) {
                layer.submit(poseStack, submitNodeCollector, state.lightCoords, state, state.yRot, state.xRot);
            }
        }
        this.submitExtra(state, poseStack, submitNodeCollector, state.lightCoords);
        poseStack.popPose();
        // Tail: leash + name display. This replicates EntityRenderer.submit instead of
        // delegating to super. AdvancedEntityRendererBase extends MobRenderer (no submit
        // override) so super.submit resolves to LivingEntityRenderer.submit, which re-renders
        // the body and every feature layer on top of the geometry already submitted above.
        // AdvancedEntityModel mirrors its geometry into a real 26.2 ModelPart render tree, so
        // that second pass actually draws an overlapping duplicate model (z-fighting ghost,
        // doubled translucent layers, an extra setupAnim per frame). Its only unique work is
        // the leash + name display tail, replicated here.
        if (state.leashStates != null) {
            for (EntityRenderState.LeashState leashState : state.leashStates) {
                submitNodeCollector.submitLeash(poseStack, leashState);
            }
        }
        this.submitNameDisplay(state, poseStack, submitNodeCollector, camera);
    }
}
