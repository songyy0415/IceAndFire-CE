package com.iafenvoy.iceandfire.render.model;

import com.iafenvoy.iceandfire.render.entity.state.ChainTieRenderState;

import java.util.Map;

import java.util.List;

import net.minecraft.client.model.geom.ModelPart;

import com.google.common.collect.ImmutableList;
import com.iafenvoy.iceandfire.entity.ChainTieEntity;
import com.iafenvoy.uranus.client.model.basic.BasicEntityModel;
import com.iafenvoy.uranus.client.model.basic.BasicModelPart;

public class ChainTieModel extends BasicEntityModel<ChainTieRenderState> {
    public final BasicModelPart knotRenderer;

    public ChainTieModel() {
        this(0, 0, 32, 32);
    }

    public ChainTieModel(int width, int height, int texWidth, int texHeight) {
        super(new ModelPart(List.of(), Map.of()));
        this.knotRenderer = new BasicModelPart(this, width, height);
        this.knotRenderer.addBox(-4.0F, 2.0F, -4.0F, 8, 12, 8, 1.0F);
        this.knotRenderer.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void setupAnim(ChainTieRenderState state) {
        this.knotRenderer.rotateAngleY = state.yRot * 0.017453292F;
        this.knotRenderer.rotateAngleX = state.xRot * 0.017453292F;
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(this.knotRenderer);
    }
}
