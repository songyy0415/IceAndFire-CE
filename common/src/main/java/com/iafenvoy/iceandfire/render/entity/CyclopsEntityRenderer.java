package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.CyclopsEntity;
import com.iafenvoy.iceandfire.render.entity.state.CyclopsRenderState;
import com.iafenvoy.iceandfire.render.model.CyclopsModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class CyclopsEntityRenderer extends AdvancedEntityRendererBase<CyclopsEntity, CyclopsRenderState, CyclopsModel> {
    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_0.png");
    public static final Identifier BLINK_0_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_0_blink.png");
    public static final Identifier BLINDED_0_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_0_injured.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_1.png");
    public static final Identifier BLINK_1_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_1_blink.png");
    public static final Identifier BLINDED_1_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_1_injured.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_2.png");
    public static final Identifier BLINK_2_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_2_blink.png");
    public static final Identifier BLINDED_2_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_2_injured.png");
    public static final Identifier TEXTURE_3 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_3.png");
    public static final Identifier BLINK_3_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_3_blink.png");
    public static final Identifier BLINDED_3_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cyclops/cyclops_3_injured.png");

    public CyclopsEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CyclopsModel(), 1.6F);
    }

    @Override
    public CyclopsRenderState createRenderState() {
        return new CyclopsRenderState();
    }

    @Override
    public void extractRenderState(CyclopsEntity entity, CyclopsRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.blinded = entity.isBlinded();
        state.blinking = entity.isBlinking();
        state.eatingPlayer = entity.getAnimation() == CyclopsEntity.ANIMATION_EATPLAYER;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    protected void scale(CyclopsRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(2.25F, 2.25F, 2.25F);
    }

    @Override
    public Identifier getTextureLocation(CyclopsRenderState state) {
        switch (state.variant) {
            case 0 -> {
                if (state.blinded) return BLINDED_0_TEXTURE;
                else if (state.blinking) return BLINK_0_TEXTURE;
                else return TEXTURE_0;
            }
            case 1 -> {
                if (state.blinded) return BLINDED_1_TEXTURE;
                else if (state.blinking) return BLINK_1_TEXTURE;
                else return TEXTURE_1;
            }
            case 2 -> {
                if (state.blinded) return BLINDED_2_TEXTURE;
                else if (state.blinking) return BLINK_2_TEXTURE;
                else return TEXTURE_2;
            }
            case 3 -> {
                if (state.blinded) return BLINDED_3_TEXTURE;
                else if (state.blinking) return BLINK_3_TEXTURE;
                else return TEXTURE_3;
            }
        }
        return TEXTURE_0;
    }
}
