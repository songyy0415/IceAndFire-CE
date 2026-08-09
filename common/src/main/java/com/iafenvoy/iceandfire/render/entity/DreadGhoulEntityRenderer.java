package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadGhoulEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.DreadGhoulRenderState;
import com.iafenvoy.iceandfire.render.model.DreadGhoulModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DreadGhoulEntityRenderer extends AdvancedEntityRendererBase<DreadGhoulEntity, DreadGhoulRenderState, DreadGhoulModel> {
    public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_eyes.png");

    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_closed_1.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_closed_2.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_closed_3.png");
    public static final Identifier TEXTURE_0_MID = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_mid_1.png");
    public static final Identifier TEXTURE_1_MID = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_mid_2.png");
    public static final Identifier TEXTURE_2_MID = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_mid_3.png");
    public static final Identifier TEXTURE_0_OPEN = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_open_1.png");
    public static final Identifier TEXTURE_1_OPEN = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_open_2.png");
    public static final Identifier TEXTURE_2_OPEN = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_ghoul_open_3.png");

    public DreadGhoulEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadGhoulModel(0.0F), 0.5F);
        this.layers.add(new GenericGlowingFeatureRenderer<>(this, TEXTURE_EYES));
    }

    @Override
    public DreadGhoulRenderState createRenderState() {
        return new DreadGhoulRenderState();
    }

    @Override
    public void extractRenderState(DreadGhoulEntity entity, DreadGhoulRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.screamStage = entity.getScreamStage();
        state.variant = entity.getVariant();
        state.size = entity.getSize();
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
    protected void scale(DreadGhoulRenderState state, PoseStack matrixStackIn) {
        float scale = state.size < 0.01F ? 1F : state.size;
        matrixStackIn.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(DreadGhoulRenderState state) {
        return switch (state.screamStage) {
            case 2 -> switch (state.variant) {
                case 1 -> TEXTURE_1_OPEN;
                case 2 -> TEXTURE_2_OPEN;
                default -> TEXTURE_0_OPEN;
            };
            case 1 -> switch (state.variant) {
                case 1 -> TEXTURE_1_MID;
                case 2 -> TEXTURE_2_MID;
                default -> TEXTURE_0_MID;
            };
            default -> switch (state.variant) {
                case 1 -> TEXTURE_1;
                case 2 -> TEXTURE_2;
                default -> TEXTURE_0;
            };
        };
    }
}
