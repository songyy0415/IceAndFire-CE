package com.iafenvoy.iceandfire.render.block.state;

import com.iafenvoy.iceandfire.data.DragonColor;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class PodiumRenderState extends BlockEntityRenderState {
    public boolean hasItem;
    public DragonColor eggType;
    public float itemBob;
    public float itemAngle;
    public final ItemStackRenderState item = new ItemStackRenderState();
}
