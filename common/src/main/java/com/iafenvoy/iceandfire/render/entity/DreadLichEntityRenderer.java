package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadLichEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.DreadLichRenderState;
import com.iafenvoy.iceandfire.render.model.DreadLichModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DreadLichEntityRenderer extends AdvancedEntityRendererBase<DreadLichEntity, DreadLichRenderState, DreadLichModel> {
    public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_eyes.png");
    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_0.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_1.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_2.png");
    public static final Identifier TEXTURE_3 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_3.png");
    public static final Identifier TEXTURE_4 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_lich_4.png");

    public DreadLichEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadLichModel(0.0F), 0.6F);
        this.layers.add(new GenericGlowingFeatureRenderer<>(this, TEXTURE_EYES));
    }

    @Override
    public DreadLichRenderState createRenderState() {
        return new DreadLichRenderState();
    }

    @Override
    public void extractRenderState(DreadLichEntity entity, DreadLichRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
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
    protected void scale(DreadLichRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(0.95F, 0.95F, 0.95F);
    }

    @Override
    public Identifier getTextureLocation(DreadLichRenderState state) {
        return switch (state.variant) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            case 4 -> TEXTURE_4;
            default -> TEXTURE_0;
        };
    }
}
