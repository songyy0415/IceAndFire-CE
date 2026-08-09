package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GorgonEyesFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.GorgonRenderState;
import com.iafenvoy.iceandfire.render.model.GorgonModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class GorgonEntityRenderer extends AdvancedEntityRendererBase<GorgonEntity, GorgonRenderState, GorgonModel> {
    public static final Identifier PASSIVE_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_passive.png");
    public static final Identifier AGRESSIVE_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_active.png");
    public static final Identifier DEAD_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/gorgon/gorgon_decapitated.png");

    public GorgonEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GorgonModel(), 0.4F);
        this.layers.add(new GorgonEyesFeatureRenderer(this));
    }

    @Override
    public GorgonRenderState createRenderState() {
        return new GorgonRenderState();
    }

    @Override
    public void extractRenderState(GorgonEntity entity, GorgonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    protected void scale(GorgonRenderState state, PoseStack stack) {
        stack.scale(0.85F, 0.85F, 0.85F);
    }

    @Override
    public Identifier getTextureLocation(GorgonRenderState state) {
        if (state.getAnimation() == GorgonEntity.ANIMATION_SCARE) return AGRESSIVE_TEXTURE;
        else if (state.deathTime > 0) return DEAD_TEXTURE;
        else return PASSIVE_TEXTURE;
    }
}
