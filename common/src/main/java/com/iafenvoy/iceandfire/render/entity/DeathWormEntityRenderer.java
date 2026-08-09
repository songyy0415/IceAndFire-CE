package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DeathWormEntity;
import com.iafenvoy.iceandfire.render.entity.state.DeathWormRenderState;
import com.iafenvoy.iceandfire.render.model.DeathWormModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class DeathWormEntityRenderer extends MobRenderer<DeathWormEntity, DeathWormRenderState, DeathWormModel> {
    public static final Identifier TEXTURE_RED = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/deathworm/deathworm_red.png");
    public static final Identifier TEXTURE_WHITE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/deathworm/deathworm_white.png");
    public static final Identifier TEXTURE_YELLOW = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/deathworm/deathworm_yellow.png");

    public DeathWormEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DeathWormModel(), 0);
    }

    @Override
    public DeathWormRenderState createRenderState() {
        return new DeathWormRenderState();
    }

    @Override
    public void extractRenderState(DeathWormEntity entity, DeathWormRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ageScale = entity.getAgeScale();
        state.jumpProgress = entity.jumpProgress;
        state.prevJumpProgress = entity.prevJumpProgress;
        state.wormJumping = entity.getWormJumping();
        state.tickCount = entity.tickCount;
        state.tailBuffer = entity.tail_buffer;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.texture = entity.getVariant() == 2 ? TEXTURE_WHITE : entity.getVariant() == 1 ? TEXTURE_RED : TEXTURE_YELLOW;
    }

    @Override
    protected void scale(DeathWormRenderState state, PoseStack matrixStackIn) {
        this.shadowRadius = state.ageScale / 3;
        matrixStackIn.scale(state.ageScale, state.ageScale, state.ageScale);
    }

    @Override
    protected int getBlockLightLevel(DeathWormEntity entityIn, BlockPos partialTicks) {
        return entityIn.isOnFire() ? 15 : entityIn.getWormBrightness(false);
    }

    @Override
    protected int getSkyLightLevel(DeathWormEntity entity, BlockPos pos) {
        return entity.getWormBrightness(true);
    }

    @Override
    public Identifier getTextureLocation(DeathWormRenderState state) {
        return state.texture;
    }
}
