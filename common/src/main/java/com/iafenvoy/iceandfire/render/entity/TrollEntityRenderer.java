package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.entity.GorgonEntity;
import com.iafenvoy.iceandfire.entity.TrollEntity;
import com.iafenvoy.iceandfire.render.entity.feature.TrollEyesFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.TrollWeaponFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.TrollRenderState;
import com.iafenvoy.iceandfire.render.model.TrollModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class TrollEntityRenderer extends AdvancedEntityRendererBase<TrollEntity, TrollRenderState, TrollModel> {
    public TrollEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TrollModel(), 0.9F);
        this.layers.add(new TrollWeaponFeatureRenderer(this));
        this.layers.add(new TrollEyesFeatureRenderer(this));
    }

    @Override
    public TrollRenderState createRenderState() {
        return new TrollRenderState();
    }

    @Override
    public void extractRenderState(TrollEntity entity, TrollRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.trollType = entity.getTrollType();
        state.weaponType = entity.getWeaponType();
        state.isStone = GorgonEntity.isStoneMob(entity);
        state.stoneProgress = entity.stoneProgress;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
    }

    @Override
    public Identifier getTextureLocation(TrollRenderState state) {
        return state.trollType.getTextureLocation();
    }
}
