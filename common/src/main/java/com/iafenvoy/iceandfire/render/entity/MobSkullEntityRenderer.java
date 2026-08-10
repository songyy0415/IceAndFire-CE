package com.iafenvoy.iceandfire.render.entity;

import com.google.common.collect.Maps;
import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.IafSkullType;
import com.iafenvoy.iceandfire.entity.MobSkullEntity;
import com.iafenvoy.iceandfire.registry.IafRenderers;
import com.iafenvoy.iceandfire.render.entity.state.MobSkullRenderState;
import com.iafenvoy.iceandfire.render.entity.state.SeaSerpentRenderState;
import com.iafenvoy.iceandfire.render.model.*;
import com.iafenvoy.iceandfire.render.model.animator.SeaSerpentTabulaModelAnimator;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.iafenvoy.uranus.client.model.basic.BasicModelPart;
import com.iafenvoy.uranus.client.model.util.TabulaModelHandlerHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class MobSkullEntityRenderer extends EntityRenderer<MobSkullEntity, MobSkullRenderState> {
    private static final Map<String, Identifier> SKULL_TEXTURE_CACHE = Maps.newHashMap();
    private final HippogryphModel hippogryphModel;
    private final CyclopsModel cyclopsModel;
    private final CockatriceModel cockatriceModel;
    private final StymphalianBirdModel stymphalianBirdModel;
    private final TrollModel trollModel;
    private final AmphithereModel amphithereModel;
    private final HydraHeadModel hydraModel;
    private final TabulaModel<SeaSerpentRenderState> seaSerpentModel;

    public MobSkullEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.hippogryphModel = new HippogryphModel();
        this.cyclopsModel = new CyclopsModel();
        this.cockatriceModel = new CockatriceModel();
        this.stymphalianBirdModel = new StymphalianBirdModel();
        this.trollModel = new TrollModel();
        this.amphithereModel = new AmphithereModel();
        this.seaSerpentModel = TabulaModelHandlerHelper.getModel(IafRenderers.SEA_SERPENT, SeaSerpentTabulaModelAnimator::new);
        this.hydraModel = new HydraHeadModel(0);
    }

    private static void setRotationAngles(BasicModelPart cube, float rotX) {
        cube.rotateAngleX = rotX;
        cube.rotateAngleY = (float) 0;
        cube.rotateAngleZ = (float) 0;
    }

    @Override
    public MobSkullRenderState createRenderState() {
        return new MobSkullRenderState();
    }

    @Override
    public void extractRenderState(MobSkullEntity entity, MobSkullRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lightCoords = this.getPackedLightCoords(entity, partialTicks);
        state.yRot = entity.getYRot();
        state.isOnWall = entity.isOnWall();
        state.skullType = entity.getSkullType();
        state.texture = this.getSkullTexture(state.skullType);
    }

    @Override
    public void submit(MobSkullRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-180.0F));
        matrixStackIn.mulPose(Axis.YN.rotationDegrees(180.0F - state.yRot));
        matrixStackIn.scale(1.0F, 1.0F, 1.0F);
        matrixStackIn.translate(0, state.isOnWall ? -0.24F : -0.12F, 0.5F);
        this.renderForEnum(state.skullType, state.isOnWall, state.texture, state.lightCoords, matrixStackIn, submitNodeCollector);
        matrixStackIn.popPose();
    }

    private void renderForEnum(IafSkullType skull, boolean onWall, Identifier texture, int lightCoords, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector) {
        switch (skull) {
            case HIPPOGRYPH -> {
                matrixStackIn.translate(0, -0.0F, -0.2F);
                matrixStackIn.scale(1.2F, 1.2F, 1.2F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.hippogryphModel.resetToDefaultPose();
                    setRotationAngles(this.hippogryphModel.Head, onWall ? (float) Math.toRadians(50F) : (float) Math.toRadians(-5));
                    this.hippogryphModel.Head.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case CYCLOPS -> {
                matrixStackIn.translate(0, 1.8F, -0.5F);
                matrixStackIn.scale(2.25F, 2.25F, 2.25F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.cyclopsModel.resetToDefaultPose();
                    setRotationAngles(this.cyclopsModel.Head, onWall ? (float) Math.toRadians(50F) : 0F);
                    this.cyclopsModel.Head.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case COCKATRICE -> {
                if (onWall) matrixStackIn.translate(0, 0F, 0.35F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.cockatriceModel.resetToDefaultPose();
                    setRotationAngles(this.cockatriceModel.head, onWall ? (float) Math.toRadians(50F) : 0F);
                    this.cockatriceModel.head.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case STYMPHALIAN -> {
                if (!onWall) matrixStackIn.translate(0, 0F, -0.35F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.stymphalianBirdModel.resetToDefaultPose();
                    setRotationAngles(this.stymphalianBirdModel.HeadBase, onWall ? (float) Math.toRadians(50F) : 0F);
                    this.stymphalianBirdModel.HeadBase.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case TROLL -> {
                matrixStackIn.translate(0, 1F, -0.35F);
                if (onWall) matrixStackIn.translate(0, 0F, 0.35F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.trollModel.resetToDefaultPose();
                    setRotationAngles(this.trollModel.head, onWall ? (float) Math.toRadians(50F) : (float) Math.toRadians(-20));
                    this.trollModel.head.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case AMPHITHERE -> {
                matrixStackIn.translate(0, -0.2F, 0.7F);
                matrixStackIn.scale(2.0F, 2.0F, 2.0F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.amphithereModel.resetToDefaultPose();
                    setRotationAngles(this.amphithereModel.Head, onWall ? (float) Math.toRadians(50F) : 0F);
                    this.amphithereModel.Head.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case SEASERPENT -> {
                matrixStackIn.translate(0, -0.35F, 0.8F);
                matrixStackIn.scale(2.5F, 2.5F, 2.5F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.seaSerpentModel.resetToDefaultPose();
                    setRotationAngles(this.seaSerpentModel.getCube("Head"), onWall ? (float) Math.toRadians(50F) : 0F);
                    this.seaSerpentModel.getCube("Head").render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
            case HYDRA -> {
                matrixStackIn.translate(0, -0.2F, -0.1F);
                matrixStackIn.scale(2.0F, 2.0F, 2.0F);
                this.renderSkullCube(submitNodeCollector, texture, lightCoords, matrixStackIn, (fresh, buffer) -> {
                    this.hydraModel.resetToDefaultPose();
                    setRotationAngles(this.hydraModel.Head1, onWall ? (float) Math.toRadians(50F) : 0F);
                    this.hydraModel.Head1.render(fresh, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                });
            }
        }
    }

    private void renderSkullCube(SubmitNodeCollector submitNodeCollector, Identifier texture, int lightCoords, PoseStack matrixStackIn, BiConsumer<PoseStack, VertexConsumer> cube) {
        submitNodeCollector.submitCustomGeometry(matrixStackIn, RenderTypes.entityTranslucent(texture), (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            cube.accept(fresh, buffer);
        });
    }

    public Identifier getTextureLocation(MobSkullRenderState state) {
        return state.texture;
    }

    public Identifier getSkullTexture(IafSkullType skull) {
        Identifier id = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/skulls/skull_" + skull.name().toLowerCase(Locale.ROOT) + ".png");
        return SKULL_TEXTURE_CACHE.computeIfAbsent(id.toString(), k -> id);
    }

}
