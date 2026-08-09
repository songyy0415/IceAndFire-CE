package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.entity.DragonEggEntity;
import com.iafenvoy.iceandfire.render.entity.state.DragonEggRenderState;
import com.iafenvoy.iceandfire.render.model.DragonEggModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.Identifier;

public class DragonEggEntityRenderer extends LivingEntityRenderer<DragonEggEntity, DragonEggRenderState, DragonEggModel> {
    public DragonEggEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonEggModel(), 0.3F);
    }

    @Override
    public DragonEggRenderState createRenderState() {
        return new DragonEggRenderState();
    }

    @Override
    public void extractRenderState(DragonEggEntity entity, DragonEggRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.texture = entity.getEggType().getTextureProvider().getEggTexture();
        state.locationValid = entity.isLocationValid();
    }

    @Override
    public Identifier getTextureLocation(DragonEggRenderState state) {
        return state.texture;
    }
}
