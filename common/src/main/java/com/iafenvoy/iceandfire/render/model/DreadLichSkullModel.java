package com.iafenvoy.iceandfire.render.model;

import com.iafenvoy.iceandfire.render.entity.state.DreadLichSkullRenderState;

import java.util.Map;

import java.util.List;

import net.minecraft.client.model.geom.ModelPart;

import com.google.common.collect.ImmutableList;
import com.iafenvoy.iceandfire.entity.DreadLichSkullEntity;
import com.iafenvoy.uranus.client.model.AdvancedEntityModel;
import com.iafenvoy.uranus.client.model.AdvancedModelBox;
import com.iafenvoy.uranus.client.model.basic.BasicModelPart;
import com.iafenvoy.uranus.client.model.util.HideableModelRenderer;

public class DreadLichSkullModel extends AdvancedEntityModel<DreadLichSkullRenderState> {
    public final HideableModelRenderer bipedHead;
    public final HideableModelRenderer bipedHeadwear;

    public DreadLichSkullModel() {
        this(0.0F);
    }

    public DreadLichSkullModel(float modelSize) {
        super(new ModelPart(List.of(), Map.of()));
        this.texHeight = 32;
        this.texWidth = 64;
        this.bipedHead = new HideableModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize - 0.5F);
        this.bipedHead.setPos(0.0F, 0.0F, 0.0F);
        this.bipedHeadwear = new HideableModelRenderer(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
        this.bipedHeadwear.setPos(0.0F, 0.0F, 0.0F);
        this.updateDefaultPose();
    }

    @Override
    public void setupAnim(DreadLichSkullRenderState state) {
        this.resetToDefaultPose();
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(this.bipedHead, this.bipedHeadwear);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(this.bipedHead, this.bipedHeadwear);
    }

}