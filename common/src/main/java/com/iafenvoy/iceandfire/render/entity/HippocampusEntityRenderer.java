package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.HippocampusEntity;
import com.iafenvoy.iceandfire.render.entity.state.HippocampusRenderState;
import com.iafenvoy.iceandfire.render.model.HippocampusModel;
import com.iafenvoy.iceandfire.util.Color4i;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Locale;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class HippocampusEntityRenderer extends AdvancedEntityRendererBase<HippocampusEntity, HippocampusRenderState, HippocampusModel> {
    private static final Identifier VARIANT_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_0.png");
    private static final Identifier VARIANT_0_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_0_blinking.png");
    private static final Identifier VARIANT_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_1.png");
    private static final Identifier VARIANT_1_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_1_blinking.png");
    private static final Identifier VARIANT_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_2.png");
    private static final Identifier VARIANT_2_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_2_blinking.png");
    private static final Identifier VARIANT_3 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_3.png");
    private static final Identifier VARIANT_3_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_3_blinking.png");
    private static final Identifier VARIANT_4 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_4.png");
    private static final Identifier VARIANT_4_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_4_blinking.png");
    private static final Identifier VARIANT_5 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_5.png");
    private static final Identifier VARIANT_5_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/hippocampus_5_blinking.png");

    public HippocampusEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HippocampusModel(), 0.8F);
        this.layers.add(new LayerHippocampusRainbow(this));
        this.layers.add(new LayerHippocampusSaddle(this));
    }

    @Override
    public HippocampusRenderState createRenderState() {
        return new HippocampusRenderState();
    }

    @Override
    public void extractRenderState(HippocampusEntity entity, HippocampusRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.isBlinking = entity.isBlinking();
        state.isSaddled = entity.isSaddled();
        state.hasPassenger = entity.getControllingPassenger() != null;
        state.isChested = entity.isChested();
        state.armorValue = entity.getArmorValue();
        state.onLandProgress = entity.onLandProgress;
        state.sitProgress = entity.sitProgress;
        state.onGround = entity.onGround();
        state.tail_buffer = entity.tail_buffer;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.isRainbow = entity.hasCustomName() && entity.getCustomName().toString().toLowerCase(Locale.ROOT).contains("rainbow");
        if (state.isRainbow) {
            int i = entity.tickCount / 25 + entity.getId();
            int j = DyeColor.values().length;
            int k = i % j;
            int l = (i + 1) % j;
            float f = ((float) (entity.tickCount % 25) + partialTicks) / 25.0F;
            Color4i afloat1 = new Color4i(DyeColor.byId(k).getTextureDiffuseColor());
            Color4i afloat2 = new Color4i(DyeColor.byId(l).getTextureDiffuseColor());
            state.rainbowColor = new Color4i(afloat1.r * (1.0F - f) + afloat2.r * f, afloat1.g * (1.0F - f) + afloat2.g * f, afloat1.b * (1.0F - f) + afloat2.b * f, 1.0F).getIntValue();
        }
    }

    @Override
    public Identifier getTextureLocation(HippocampusRenderState state) {
        return switch (state.variant) {
            case 1 -> state.isBlinking ? VARIANT_1_BLINK : VARIANT_1;
            case 2 -> state.isBlinking ? VARIANT_2_BLINK : VARIANT_2;
            case 3 -> state.isBlinking ? VARIANT_3_BLINK : VARIANT_3;
            case 4 -> state.isBlinking ? VARIANT_4_BLINK : VARIANT_4;
            case 5 -> state.isBlinking ? VARIANT_5_BLINK : VARIANT_5;
            default -> state.isBlinking ? VARIANT_0_BLINK : VARIANT_0;
        };
    }

    private static class LayerHippocampusSaddle extends RenderLayer<HippocampusRenderState, HippocampusModel> {
        private final RenderType SADDLE_TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/saddle.png"));
        private final RenderType BRIDLE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/bridle.png"));
        private final RenderType CHEST = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/chest.png"));
        private final RenderType TEXTURE_DIAMOND = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/armor_diamond.png"));
        private final RenderType TEXTURE_GOLD = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/armor_gold.png"));
        private final RenderType TEXTURE_IRON = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/armor_iron.png"));

        public LayerHippocampusSaddle(HippocampusEntityRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int packedLightIn, HippocampusRenderState state, float yRot, float xRot) {
            if (state.isSaddled) {
                this.submitModel(matrixStackIn, submitNodeCollector, this.SADDLE_TEXTURE, packedLightIn, state);
            }
            if (state.isSaddled && state.hasPassenger) {
                this.submitModel(matrixStackIn, submitNodeCollector, this.BRIDLE, packedLightIn, state);
            }
            if (state.isChested) {
                this.submitModel(matrixStackIn, submitNodeCollector, this.CHEST, packedLightIn, state);
            }
            if (state.armorValue != 0) {
                RenderType type = switch (state.armorValue) {
                    case 1 -> this.TEXTURE_IRON;
                    case 2 -> this.TEXTURE_GOLD;
                    case 3 -> this.TEXTURE_DIAMOND;
                    default -> null;
                };
                if (type != null) this.submitModel(matrixStackIn, submitNodeCollector, type, packedLightIn, state);
            }
        }

        private void submitModel(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, RenderType renderType, int packedLightIn, HippocampusRenderState state) {
            submitNodeCollector.submitCustomGeometry(matrixStackIn, renderType, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                // Re-run setupAnim at deferred-draw time (see AdvancedEntityRendererBase).
                this.getParentModel().setupAnim(state);
                this.getParentModel().renderPartsToBuffer(fresh, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
            });
        }
    }

    private static class LayerHippocampusRainbow extends RenderLayer<HippocampusRenderState, HippocampusModel> {
        private final RenderType TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/rainbow.png"));
        private final RenderType TEXTURE_BLINK = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippocampus/rainbow_blink.png"));

        public LayerHippocampusRainbow(HippocampusEntityRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int packedLightIn, HippocampusRenderState state, float yRot, float xRot) {
            if (state.isRainbow) {
                RenderType renderType = state.isBlinking ? this.TEXTURE_BLINK : this.TEXTURE;
                submitNodeCollector.submitCustomGeometry(matrixStackIn, renderType, (pose, buffer) -> {
                    PoseStack fresh = new PoseStack();
                    fresh.last().pose().set(pose.pose());
                    fresh.last().normal().set(pose.normal());
                    // Re-run setupAnim at deferred-draw time (see AdvancedEntityRendererBase).
                    this.getParentModel().setupAnim(state);
                    this.getParentModel().renderPartsToBuffer(fresh, buffer, packedLightIn, getOverlayCoords(state, 0.0F), state.rainbowColor);
                });
            }
        }
    }
}
