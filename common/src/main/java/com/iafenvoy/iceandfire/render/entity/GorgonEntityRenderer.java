package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GorgonEyesFeatureRenderer;
import com.iafenvoy.iceandfire.render.model.GorgonModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class GorgonEntityRenderer extends MobRenderer<GorgonEntity, GorgonModel> {
    public static final Identifier PASSIVE_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_passive.png");
    public static final Identifier AGRESSIVE_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_active.png");
    public static final Identifier DEAD_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_decapitated.png");

    public GorgonEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GorgonModel(), 0.4F);
        this.layers.add(new GorgonEyesFeatureRenderer(this));
    }

    @Override
    public void scale(GorgonEntity LivingEntityIn, PoseStack stack, float partialTickTime) {
        stack.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    public Identifier getTextureLocation(GorgonEntity gorgon) {
        if (gorgon.getAnimation() == GorgonEntity.ANIMATION_SCARE) return AGRESSIVE_TEXTURE;
        else if (gorgon.deathTime > 0) return DEAD_TEXTURE;
        else return PASSIVE_TEXTURE;
    }
}
