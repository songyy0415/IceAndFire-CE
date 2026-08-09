package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadScuttlerEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.DreadScuttlerRenderState;
import com.iafenvoy.iceandfire.render.model.DreadScuttlerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DreadScuttlerEntityRenderer extends AdvancedEntityRendererBase<DreadScuttlerEntity, DreadScuttlerRenderState, DreadScuttlerModel> {
public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_scuttler_eyes.png");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_scuttler.png");

    public DreadScuttlerEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadScuttlerModel(), 0.75F);
        this.layers.add(new GenericGlowingFeatureRenderer<>(this, TEXTURE_EYES));
    }

    @Override
    public DreadScuttlerRenderState createRenderState() {
        return new DreadScuttlerRenderState();
    }

    @Override
    public void extractRenderState(DreadScuttlerEntity entity, DreadScuttlerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.getSize();
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    public void scale(DreadScuttlerRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(state.size, state.size, state.size);
    }

    @Override
    public Identifier getTextureLocation(DreadScuttlerRenderState state) {
        return TEXTURE;
    }
}
