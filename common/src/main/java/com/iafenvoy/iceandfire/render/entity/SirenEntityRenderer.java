package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.SirenEntity;
import com.iafenvoy.iceandfire.render.entity.state.SirenRenderState;
import com.iafenvoy.iceandfire.render.model.SirenModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class SirenEntityRenderer extends MobRenderer<SirenEntity, SirenRenderState, SirenModel> {
    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/siren/siren_0.png");
    public static final Identifier TEXTURE_0_AGGRESSIVE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/siren/siren_0_aggressive.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/siren/siren_1.png");
    public static final Identifier TEXTURE_1_AGGRESSIVE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/siren/siren_1_aggressive.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/siren/siren_2.png");
    public static final Identifier TEXTURE_2_AGGRESSIVE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/siren/siren_2_aggressive.png");

    public SirenEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SirenModel(), 0.8F);
    }

    public static Identifier getSirenOverlayTexture(int siren) {
        return switch (siren) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            default -> TEXTURE_0;
        };
    }

    @Override
    public SirenRenderState createRenderState() {
        return new SirenRenderState();
    }

    @Override
    public void extractRenderState(SirenEntity entity, SirenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swimProgress = entity.swimProgress;
        state.swimming = entity.isSwimming();
        state.singing = entity.isSinging();
        state.singingPose = entity.getSingingPose();
        state.singProgress = entity.singProgress;
        state.onGround = entity.onGround();
        state.tailBuffer = entity.tail_buffer;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.texture = switch (entity.getHairColor()) {
            case 1 -> entity.isAgressive() ? TEXTURE_1_AGGRESSIVE : TEXTURE_1;
            case 2 -> entity.isAgressive() ? TEXTURE_2_AGGRESSIVE : TEXTURE_2;
            default -> entity.isAgressive() ? TEXTURE_0_AGGRESSIVE : TEXTURE_0;
        };
    }

    @Override
    protected void scale(SirenRenderState state, PoseStack stack) {
        stack.translate(0, 0, -0.5F);
    }

    @Override
    public Identifier getTextureLocation(SirenRenderState state) {
        return state.texture;
    }
}
