package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.StoneStatueEntity;
import com.iafenvoy.iceandfire.registry.IafRenderLayers;
import com.iafenvoy.iceandfire.render.entity.state.StoneStatueRenderState;
import com.iafenvoy.iceandfire.render.model.ICustomStatueModel;
import com.iafenvoy.iceandfire.render.model.StonePlayerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

public class StoneStatueEntityRenderer extends EntityRenderer<StoneStatueEntity, StoneStatueRenderState> {
    protected static final Identifier[] DESTROY_STAGES = new Identifier[]{
            Identifier.withDefaultNamespace("textures/block/destroy_stage_0.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_1.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_2.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_3.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_4.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_5.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_6.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_7.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_8.png"),
            Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/destroy_stage_9.png")};
    private final Map<String, Model<?>> modelMap = new HashMap<>();
    private final EntityRendererProvider.Context context;

    public StoneStatueEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public StoneStatueRenderState createRenderState() {
        return new StoneStatueRenderState();
    }

    @Override
    public void extractRenderState(StoneStatueEntity entity, StoneStatueRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.trappedEntityTypeString = entity.getTrappedEntityTypeString();
        state.ageScale = entity.getAgeScale() < 0.01F ? 1F : entity.getAgeScale();
        state.crackAmount = entity.getCrackAmount();
        state.yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
    }

    @Override
    public void submit(StoneStatueRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Model<?> model = this.getStatueModel(state);
        if (model != null) {
            matrixStackIn.pushPose();
            matrixStackIn.scale(state.ageScale, state.ageScale, state.ageScale);
            matrixStackIn.translate(0, 1.5F, 0);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(state.yaw));
            RenderType stoneTex = IafRenderLayers.getStoneMobRenderType(200, 200);
            submitNodeCollector.submitCustomGeometry(matrixStackIn, stoneTex, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                this.renderStoneModel(model, fresh, buffer, state.lightCoords);
            });
            if (state.crackAmount >= 1) {
                int i = Mth.clamp(state.crackAmount - 1, 0, DESTROY_STAGES.length - 1);
                RenderType crackTex = IafRenderLayers.getStoneCrackRenderType(DESTROY_STAGES[i]);
                submitNodeCollector.submitCustomGeometry(matrixStackIn, crackTex, (pose, buffer) -> {
                    PoseStack fresh = new PoseStack();
                    fresh.last().pose().set(pose.pose());
                    fresh.last().normal().set(pose.normal());
                    this.renderStoneModel(model, fresh, buffer, state.lightCoords);
                });
            }
            matrixStackIn.popPose();
        }
        super.submit(state, matrixStackIn, submitNodeCollector, camera);
    }

    private void renderStoneModel(Model<?> model, PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        if (model instanceof ICustomStatueModel statueModel)
            statueModel.renderStatue(poseStack, buffer, packedLight, null);
        else
            model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }

    private Model<?> getStatueModel(StoneStatueRenderState state) {
        Model<?> cached = this.modelMap.get(state.trappedEntityTypeString);
        if (cached != null) return cached;
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(state.trappedEntityTypeString)).orElse(BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("pig")));
        Model<?> model = null;
        Entity tempEntity = type.create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);
        if (tempEntity != null) {
            EntityRenderer<?, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(tempEntity);
            if (renderer instanceof RenderLayerParent<?, ?> rlp)
                model = rlp.getModel();
        }
        if (model == null && type == BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("player")))
            model = new StonePlayerModel(this.context.bakeLayer(ModelLayers.PLAYER));
        if (model == null)
            model = new PigModel(this.context.bakeLayer(ModelLayers.PIG));
        this.modelMap.put(state.trappedEntityTypeString, model);
        return model;
    }
}
