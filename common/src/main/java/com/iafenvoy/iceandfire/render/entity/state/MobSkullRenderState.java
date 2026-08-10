package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.iceandfire.data.IafSkullType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class MobSkullRenderState extends EntityRenderState {
    public int lightCoords;
    public float yRot;
    public boolean isOnWall;
    public IafSkullType skullType;
    public Identifier texture;
}
