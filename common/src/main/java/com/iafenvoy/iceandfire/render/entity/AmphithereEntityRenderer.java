package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.AmphithereEntity;
import com.iafenvoy.iceandfire.render.entity.state.AmphithereRenderState;
import com.iafenvoy.iceandfire.render.model.AmphithereModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class AmphithereEntityRenderer extends AdvancedEntityRendererBase<AmphithereEntity, AmphithereRenderState, AmphithereModel> {
    public static final Identifier TEXTURE_BLUE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_blue.png");
    public static final Identifier TEXTURE_BLUE_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_blue_blink.png");
    public static final Identifier TEXTURE_GREEN = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_green.png");
    public static final Identifier TEXTURE_GREEN_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_green_blink.png");
    public static final Identifier TEXTURE_OLIVE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_olive.png");
    public static final Identifier TEXTURE_OLIVE_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_olive_blink.png");
    public static final Identifier TEXTURE_RED = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_red.png");
    public static final Identifier TEXTURE_RED_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_red_blink.png");
    public static final Identifier TEXTURE_YELLOW = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_yellow.png");
    public static final Identifier TEXTURE_YELLOW_BLINK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/amphithere/amphithere_yellow_blink.png");

    public AmphithereEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new AmphithereModel(), 1.6F);
    }

    @Override
    public AmphithereRenderState createRenderState() {
        return new AmphithereRenderState();
    }

    @Override
    public void extractRenderState(AmphithereEntity entity, AmphithereRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flapProgress = entity.flapProgress;
        state.groundProgress = entity.groundProgress;
        state.sitProgress = entity.sitProgress;
        state.diveProgress = entity.diveProgress;
        state.onGround = entity.onGround();
        state.variant = entity.getVariant();
        state.isBlinking = entity.isBlinking();
        state.roll_buffer = entity.roll_buffer;
        state.pitch_buffer = entity.pitch_buffer;
        state.tail_buffer = entity.tail_buffer;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    protected void scale(AmphithereRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public Identifier getTextureLocation(AmphithereRenderState state) {
        switch (state.variant) {
            case 0 -> {
                if (state.isBlinking) return TEXTURE_BLUE_BLINK;
                else return TEXTURE_BLUE;
            }
            case 1 -> {
                if (state.isBlinking) return TEXTURE_GREEN_BLINK;
                else return TEXTURE_GREEN;
            }
            case 2 -> {
                if (state.isBlinking) return TEXTURE_OLIVE_BLINK;
                else return TEXTURE_OLIVE;
            }
            case 3 -> {
                if (state.isBlinking) return TEXTURE_RED_BLINK;
                else return TEXTURE_RED;
            }
            case 4 -> {
                if (state.isBlinking) return TEXTURE_YELLOW_BLINK;
                else return TEXTURE_YELLOW;
            }
        }
        return TEXTURE_GREEN;
    }
}
