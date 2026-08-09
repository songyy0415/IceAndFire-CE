package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadThrallEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.IHasArmorVariantResource;
import com.iafenvoy.iceandfire.render.entity.state.DreadThrallRenderState;
import com.iafenvoy.iceandfire.render.model.DreadThrallModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DreadThrallEntityRenderer extends AdvancedEntityRendererBase<DreadThrallEntity, DreadThrallRenderState, DreadThrallModel> implements IHasArmorVariantResource {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_thrall.png");
    public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_thrall_eyes.png");
    public static final Identifier TEXTURE_LEG_ARMOR = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_legs.png");
    public static final Identifier TEXTURE_ARMOR_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_1.png");
    public static final Identifier TEXTURE_ARMOR_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_2.png");
    public static final Identifier TEXTURE_ARMOR_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_3.png");
    public static final Identifier TEXTURE_ARMOR_3 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_4.png");
    public static final Identifier TEXTURE_ARMOR_4 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_5.png");
    public static final Identifier TEXTURE_ARMOR_5 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_6.png");
    public static final Identifier TEXTURE_ARMOR_6 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_7.png");
    public static final Identifier TEXTURE_ARMOR_7 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/thrall_chest_8.png");

    public DreadThrallEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadThrallModel(0.0F, false), 0.6F);
        this.layers.add(new GenericGlowingFeatureRenderer<>(this, TEXTURE_EYES));
    }

    @Override
    public DreadThrallRenderState createRenderState() {
        return new DreadThrallRenderState();
    }

    @Override
    public void extractRenderState(DreadThrallEntity entity, DreadThrallRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
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
    public void scale(DreadThrallRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(0.95F, 0.95F, 0.95F);
    }

    @Override
    public Identifier getTextureLocation(DreadThrallRenderState state) {
        return TEXTURE;
    }

    @Override
    public Identifier getArmorResource(int variant, net.minecraft.world.entity.EquipmentSlot equipmentSlotType) {
        if (equipmentSlotType == net.minecraft.world.entity.EquipmentSlot.LEGS) return TEXTURE_LEG_ARMOR;
        return switch (variant) {
            case 1 -> TEXTURE_ARMOR_1;
            case 2 -> TEXTURE_ARMOR_2;
            case 3 -> TEXTURE_ARMOR_3;
            case 4 -> TEXTURE_ARMOR_4;
            case 5 -> TEXTURE_ARMOR_5;
            case 6 -> TEXTURE_ARMOR_6;
            case 7 -> TEXTURE_ARMOR_7;
            default -> TEXTURE_ARMOR_0;
        };
    }
}
