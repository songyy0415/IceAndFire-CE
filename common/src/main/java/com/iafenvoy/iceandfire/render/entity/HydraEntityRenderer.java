package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.entity.HydraEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.HydraHeadFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.HydraRenderState;
import com.iafenvoy.iceandfire.render.model.HydraBodyModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class HydraEntityRenderer extends AdvancedEntityRendererBase<HydraEntity, HydraRenderState, HydraBodyModel> {
    public static final Identifier TEXUTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hydra/hydra_0.png");
    public static final Identifier TEXUTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hydra/hydra_1.png");
    public static final Identifier TEXUTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hydra/hydra_2.png");
    public static final Identifier TEXUTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/hydra/hydra_eyes.png");

    public HydraEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HydraBodyModel(), 1.2F);
        this.addLayer(new HydraHeadFeatureRenderer(this));
        this.addLayer(new GenericGlowingFeatureRenderer<>(this, TEXUTURE_EYES));
    }

    @Override
    public HydraRenderState createRenderState() {
        return new HydraRenderState();
    }

    @Override
    public void extractRenderState(HydraEntity entity, HydraRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.headCount = entity.getHeadCount();
        state.severedHead = entity.getSeveredHead();
        state.isAlive = entity.isAlive();
        state.isStone = GorgonEntity.isStoneMob(entity);
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        int heads = state.headCount;
        state.speakProgress = new float[heads];
        state.strikeProgress = new float[heads];
        state.breathProgress = new float[heads];
        for (int i = 0; i < heads; i++) {
            state.speakProgress[i] = entity.prevSpeakingProgress[i] + partialTicks * (entity.speakingProgress[i] - entity.prevSpeakingProgress[i]);
            state.strikeProgress[i] = entity.prevStrikeProgress[i] + partialTicks * (entity.strikingProgress[i] - entity.prevStrikeProgress[i]);
            state.breathProgress[i] = entity.prevBreathProgress[i] + partialTicks * (entity.breathProgress[i] - entity.prevBreathProgress[i]);
        }
    }

    @Override
    protected void scale(HydraRenderState state, PoseStack stack) {
        stack.scale(1.75F, 1.75F, 1.75F);
    }

    @Override
    public Identifier getTextureLocation(HydraRenderState state) {
        return switch (state.variant) {
            case 1 -> TEXUTURE_1;
            case 2 -> TEXUTURE_2;
            default -> TEXUTURE_0;
        };
    }
}
