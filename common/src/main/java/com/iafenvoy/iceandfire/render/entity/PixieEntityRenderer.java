package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.PixieEntity;
import com.iafenvoy.iceandfire.render.entity.feature.PixieGlowFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.PixieItemFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.PixieRenderState;
import com.iafenvoy.iceandfire.render.model.PixieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;

public class PixieEntityRenderer extends MobRenderer<PixieEntity, PixieRenderState, PixieModel> {
    public static final Identifier TEXTURE_0 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/pixie_0.png");
    public static final Identifier TEXTURE_1 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/pixie_1.png");
    public static final Identifier TEXTURE_2 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/pixie_2.png");
    public static final Identifier TEXTURE_3 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/pixie_3.png");
    public static final Identifier TEXTURE_4 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/pixie_4.png");
    public static final Identifier TEXTURE_5 = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/pixie/pixie_5.png");

    public PixieEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PixieModel(), 0.2F);
        this.addLayer(new PixieItemFeatureRenderer(this));
        this.addLayer(new PixieGlowFeatureRenderer(this));
    }

    @Override
    public PixieRenderState createRenderState() {
        return new PixieRenderState();
    }

    @Override
    public void extractRenderState(PixieEntity entity, PixieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.sitting = entity.isPixieSitting();
        state.hasItemInHand = !entity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
        state.texture = switch (entity.getColor()) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            case 4 -> TEXTURE_4;
            case 5 -> TEXTURE_5;
            default -> TEXTURE_0;
        };
        this.itemModelResolver.updateForLiving(state.headItem, entity.getItemInHand(InteractionHand.MAIN_HAND), ItemDisplayContext.FIXED, entity);
    }

    @Override
    protected void scale(PixieRenderState state, PoseStack stack) {
        stack.scale(0.55F, 0.55F, 0.55F);
        if (state.sitting) {
            stack.translate(0F, 0.5F, 0F);
        }
    }

    @Override
    public Identifier getTextureLocation(PixieRenderState state) {
        return state.texture;
    }
}
