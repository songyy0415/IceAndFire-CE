package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.StymphalianBirdEntity;
import com.iafenvoy.iceandfire.render.entity.state.StymphalianBirdRenderState;
import com.iafenvoy.iceandfire.render.model.StymphalianBirdModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class StymphalianBirdEntityRenderer extends AdvancedEntityRendererBase<StymphalianBirdEntity, StymphalianBirdRenderState, StymphalianBirdModel> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/stymphalianbird/stymphalian_bird.png");

    public StymphalianBirdEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new StymphalianBirdModel(), 0.6F);
    }

    @Override
    public StymphalianBirdRenderState createRenderState() {
        return new StymphalianBirdRenderState();
    }

    @Override
    public void extractRenderState(StymphalianBirdEntity entity, StymphalianBirdRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flyProgress = entity.flyProgress;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    protected void scale(StymphalianBirdRenderState state, PoseStack stack) {
        stack.scale(0.75F, 0.75F, 0.75F);
    }

    @Override
    public Identifier getTextureLocation(StymphalianBirdRenderState state) {
        return TEXTURE;
    }
}
