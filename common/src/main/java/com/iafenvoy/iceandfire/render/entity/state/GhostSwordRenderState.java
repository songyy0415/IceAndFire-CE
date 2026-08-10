package com.iafenvoy.iceandfire.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class GhostSwordRenderState extends EntityRenderState {
    public int lightCoords;
    public float yRot;
    public float xRot;
    public float animProgress;
    public final ItemStackRenderState item = new ItemStackRenderState();
}
