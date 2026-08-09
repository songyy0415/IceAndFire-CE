package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.HydraArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class HydraArrowEntityRenderer extends ArrowRenderer<HydraArrowEntity> {
    private static final Identifier TEXTURES = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/misc/hydra_arrow.png");

    public HydraArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(HydraArrowEntity entity) {
        return TEXTURES;
    }
}