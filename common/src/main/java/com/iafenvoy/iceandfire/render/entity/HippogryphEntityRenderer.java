package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.HippogryphEntity;
import com.iafenvoy.iceandfire.registry.IafHippogryphTypes;
import com.iafenvoy.iceandfire.render.entity.state.HippogryphRenderState;
import com.iafenvoy.iceandfire.render.model.HippogryphModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class HippogryphEntityRenderer extends MobRenderer<HippogryphEntity, HippogryphRenderState, HippogryphModel> {
    public HippogryphEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HippogryphModel(), 0.8F);
        this.layers.add(new LayerHippogriffSaddle(this));
    }

    @Override
    public HippogryphRenderState createRenderState() {
        return new HippogryphRenderState();
    }

    @Override
    public void extractRenderState(HippogryphEntity entity, HippogryphRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armorValue = entity.getArmorValue();
        state.saddled = entity.isSaddled();
        state.hasPassenger = entity.getControllingPassenger() != null;
        state.chested = entity.isChested();
        state.sitProgress = entity.sitProgress;
        state.hoverProgress = entity.hoverProgress;
        state.flyProgress = entity.flyProgress;
        state.isDodo = entity.getEnumVariant() == IafHippogryphTypes.DODO;
        state.flying = entity.isFlying();
        state.hovering = entity.isHovering();
        state.airBorneCounter = entity.airBorneCounter;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.texture = entity.getEnumVariant().getTextureLocation(entity.isBlinking());
    }

    @Override
    protected void scale(HippogryphRenderState state, PoseStack matrix) {
        matrix.scale(1.2F, 1.2F, 1.2F);
    }

    @Override
    public Identifier getTextureLocation(HippogryphRenderState state) {
        return state.texture;
    }

    private static class LayerHippogriffSaddle extends RenderLayer<HippogryphRenderState, HippogryphModel> {
        private final RenderType SADDLE_TEXTURE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/saddle.png"), false);
        private final RenderType BRIDLE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/bridle.png"), false);
        private final RenderType CHEST = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/chest.png"));
        private final RenderType TEXTURE_IRON = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/armor_iron.png"));
        private final RenderType TEXTURE_GOLD = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/armor_gold.png"));
        private final RenderType TEXTURE_DIAMOND = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/armor_diamond.png"));
        private final RenderType TEXTURE_NETHERITE = RenderTypes.entityCutout(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hippogryph/armor_netherite.png"));

        public LayerHippogriffSaddle(HippogryphEntityRenderer renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int lightCoords, HippogryphRenderState state, float yRot, float xRot) {
            this.getParentModel().setupAnim(state);
            if (state.armorValue != 0) {
                RenderType type = switch (state.armorValue) {
                    case 1 -> this.TEXTURE_IRON;
                    case 2 -> this.TEXTURE_GOLD;
                    case 3 -> this.TEXTURE_DIAMOND;
                    case 4 -> this.TEXTURE_NETHERITE;
                    default -> null;
                };
                if (type != null) {
                    submitNodeCollector.order(1)
                        .submitModel(this.getParentModel(), state, matrixStackIn, type, lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
                }
            }
            if (state.saddled) {
                submitNodeCollector.order(1)
                    .submitModel(this.getParentModel(), state, matrixStackIn, this.SADDLE_TEXTURE, lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            }
            if (state.saddled && state.hasPassenger) {
                submitNodeCollector.order(1)
                    .submitModel(this.getParentModel(), state, matrixStackIn, this.BRIDLE, lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            }
            if (state.chested) {
                submitNodeCollector.order(1)
                    .submitModel(this.getParentModel(), state, matrixStackIn, this.CHEST, lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            }
        }
    }
}
