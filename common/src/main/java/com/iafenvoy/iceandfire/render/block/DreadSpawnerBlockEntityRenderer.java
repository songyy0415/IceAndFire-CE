package com.iafenvoy.iceandfire.render.block;

import com.iafenvoy.iceandfire.item.block.entity.DreadSpawnerBlockEntity;
import com.iafenvoy.iceandfire.render.block.state.DreadSpawnerRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.phys.Vec3;

public class DreadSpawnerBlockEntityRenderer implements BlockEntityRenderer<DreadSpawnerBlockEntity, DreadSpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public DreadSpawnerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public DreadSpawnerRenderState createRenderState() {
        return new DreadSpawnerRenderState();
    }

    @Override
    public void extractRenderState(DreadSpawnerBlockEntity entity, DreadSpawnerRenderState state, float partialTicks, Vec3 pos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        BaseSpawner spawnerLogic = entity.getLogic();
        Entity displayEntity = spawnerLogic.getOrCreateDisplayEntity(entity.getLevel(), entity.getBlockPos());
        if (displayEntity != null) {
            TrialSpawnerRenderer.extractSpawnerData(state, partialTicks, displayEntity, this.entityRenderer, spawnerLogic.getOSpin(), spawnerLogic.getSpin());
        }
    }

    @Override
    public void submit(DreadSpawnerRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(matrixStackIn, submitNodeCollector, state.displayEntity, this.entityRenderer, state.scale, state.spin, camera);
        }
    }
}
