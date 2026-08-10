package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.SeaSerpentEntity;
import com.iafenvoy.iceandfire.registry.IafRegistries;
import com.iafenvoy.iceandfire.registry.IafRenderers;
import com.iafenvoy.iceandfire.render.entity.feature.SeaSerpentAncientFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.SeaSerpentRenderState;
import com.iafenvoy.iceandfire.render.model.animator.SeaSerpentTabulaModelAnimator;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.iafenvoy.uranus.client.model.util.TabulaModelHandlerHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class SeaSerpentEntityRenderer extends AdvancedEntityRendererBase<SeaSerpentEntity, SeaSerpentRenderState, TabulaModel<SeaSerpentRenderState>> {
    public SeaSerpentEntityRenderer(EntityRendererProvider.Context context) {
        super(context, TabulaModelHandlerHelper.getModel(IafRenderers.SEA_SERPENT, SeaSerpentTabulaModelAnimator::new), 1.6F);
        this.layers.add(new SeaSerpentAncientFeatureRenderer(this));
    }

    @Override
    public SeaSerpentRenderState createRenderState() {
        return new SeaSerpentRenderState();
    }

    @Override
    public void extractRenderState(SeaSerpentEntity entity, SeaSerpentRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.blinking = entity.isBlinking();
        state.isAncient = entity.isAncient();
        state.seaSerpentScale = entity.getSeaSerpentScale();
        state.swimCycle = entity.swimCycle;
        state.jumpProgress = entity.jumpProgress;
        state.wantJumpProgress = entity.wantJumpProgress;
        state.breathProgress = entity.breathProgress;
        state.jumpRot = entity.jumpRot;
        state.prevJumpRot = entity.prevJumpRot;
        state.yBodyRot = entity.yBodyRot;
        state.yBodyRotO = entity.yBodyRotO;
        state.deltaMovementY = (float) entity.getDeltaMovement().y;
        state.isInWater = entity.isInWater();
        state.isJumpingOutOfWater = entity.isJumpingOutOfWater();
        for (int i = 1; i <= 4; i++) {
            state.pieceYaw[i - 1] = entity.getPieceYaw(i, partialTicks);
            state.piecePitch[i - 1] = entity.getPiecePitch(i, partialTicks);
        }
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.texture = IafRegistries.SEA_SERPENT_TYPE.get(IceAndFire.id(entity.getVariant())).orElseThrow().value().getTextureLocation(entity.isBlinking());
    }

    @Override
    protected void scale(SeaSerpentRenderState state, PoseStack matrixStackIn) {
        this.shadowRadius = state.seaSerpentScale;
        matrixStackIn.scale(this.shadowRadius, this.shadowRadius, this.shadowRadius);
    }

    @Override
    public Identifier getTextureLocation(SeaSerpentRenderState state) {
        return state.texture;
    }
}
