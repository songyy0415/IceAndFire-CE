package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DreadBeastEntity;
import com.iafenvoy.iceandfire.render.entity.feature.GenericGlowingFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.DreadBeastRenderState;
import com.iafenvoy.iceandfire.render.model.DreadBeastModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class DreadBeastEntityRenderer extends AdvancedEntityRendererBase<DreadBeastEntity, DreadBeastRenderState, DreadBeastModel> {
public static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_beast_eyes.png");
    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_beast_1.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/dread/dread_beast_2.png");

    public DreadBeastEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DreadBeastModel(), 0.5F);
        this.layers.add(new GenericGlowingFeatureRenderer<>(this, TEXTURE_EYES));
    }

    @Override
    public DreadBeastRenderState createRenderState() {
        return new DreadBeastRenderState();
    }

    @Override
    public void extractRenderState(DreadBeastEntity entity, DreadBeastRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.getSize();
        state.variant = entity.getVariant();
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    public void scale(DreadBeastRenderState state, PoseStack matrixStackIn) {
        matrixStackIn.scale(state.size, state.size, state.size);
    }

    @Override
    public Identifier getTextureLocation(DreadBeastRenderState state) {
        return state.variant == 1 ? TEXTURE_1 : TEXTURE_0;
    }
}
