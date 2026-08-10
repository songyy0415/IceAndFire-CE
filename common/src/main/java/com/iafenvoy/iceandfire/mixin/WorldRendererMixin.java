package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.event.ClientEvents;
import com.iafenvoy.iceandfire.render.misc.LightningBoltData;
import com.iafenvoy.iceandfire.render.misc.LightningRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC26.2 port of the 1.21.1 world lightning render hook.
 *
 * <p>Old hook: {@code LevelRenderer.renderLevel} at the
 * {@code entitiesForRendering()} invoke — drew pending {@link ClientEvents#LIGHTNINGS}
 * bolts into the world {@code MultiBufferSource} before entities were rendered.
 *
 * <p>26.2 lifecycle: {@code LevelRenderer.render} builds the frame graph; entities are
 * submitted by {@code submitEntities(PoseStack, LevelRenderState, SubmitNodeCollector)}
 * (called from {@code submitFeatures} once per frame, right before entity render states
 * are drawn). The old "before entities" seam maps to the HEAD of {@code submitEntities}:
 * <ul>
 *   <li>camera position comes from {@link LevelRenderState#cameraRenderState}{@code .pos}
 *       (no more {@code Camera.getPosition()} — the camera is fixed during extract).</li>
 *   <li>geometry is submitted through the {@link SubmitNodeCollector} passed into
 *       {@code submitEntities} ({@code OrderedSubmitNodeCollector.submitCustomGeometry}),
 *       not a {@code MultiBufferSource}.</li>
 *   <li>partial ticks come from {@code Minecraft.getInstance().getDeltaTracker()
 *       .getGameTimeDeltaPartialTick(false)}.</li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Unique
    private final LightningRenderer iceandfire$lightningRenderer = new LightningRenderer();

    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void renderBolts(PoseStack matrices, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 pos = levelRenderState.cameraRenderState.pos;
        matrices.pushPose();
        matrices.translate(-pos.x, -pos.y, -pos.z);
        for (Pair<Vec3, Vec3> pair : ClientEvents.LIGHTNINGS) {
            LightningBoltData bolt = new LightningBoltData(LightningBoltData.BoltRenderInfo.ELECTRICITY, pair.getLeft(), pair.getRight(), 4)
                    .size(0.05F)
                    .lifespan(10)
                    .fade(LightningBoltData.FadeFunction.fade(0.1F))
                    .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
            this.iceandfire$lightningRenderer.update(null, bolt, tickDelta);
        }
        ClientEvents.LIGHTNINGS.clear();
        this.iceandfire$lightningRenderer.render(tickDelta, matrices, submitNodeCollector);
        matrices.popPose();
    }
}
