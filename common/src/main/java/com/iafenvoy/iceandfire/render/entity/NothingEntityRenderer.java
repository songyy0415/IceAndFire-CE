package com.iafenvoy.iceandfire.render.entity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class NothingEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public NothingEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(T livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        if (!this.entityRenderDispatcher.shouldRenderHitBoxes()) return false;
        return super.shouldRender(livingEntityIn, camera, camX, camY, camZ);
    }

    @Override
    public Identifier getTextureLocation(Entity entity) {
        return null;
    }
}
