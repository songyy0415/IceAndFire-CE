package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadKnightEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.DreadKnightRenderState;
import com.iafenvoy.iceandfire.render.model.DreadKnightModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DreadKnightEntityRenderer extends AdvancedEntityRendererBase<DreadKnightEntity, DreadKnightRenderState, DreadKnightModel> {
    public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_knight_eyes.png");
    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_knight_1.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_knight_2.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_knight_3.png");

    public DreadKnightEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadKnightModel(0.0F), 0.6F);
        this.layers.add(new GenericGlowingFeatureRenderer<>(this, TEXTURE_EYES));
    }

    @Override
    public DreadKnightRenderState createRenderState() {
        return new DreadKnightRenderState();
    }

    @Override
    public void extractRenderState(DreadKnightEntity entity, DreadKnightRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armorVariant = entity.getArmorVariant();
        state.mainHandItem = entity.getMainHandItem();
        state.swinging = entity.swinging;
        state.isPassenger = entity.isPassenger();
        state.mainArm = entity.getMainArm();
        state.swingingArm = entity.swingingArm;
        state.attackTime = entity.getAttackAnim(partialTicks);
        state.isSneak = entity.isCrouching();
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    protected void scale(DreadKnightRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(0.95F, 0.95F, 0.95F);
    }

    @Override
    public Identifier getTextureLocation(DreadKnightRenderState state) {
        return switch (state.armorVariant) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            default -> TEXTURE_0;
        };
    }
}
