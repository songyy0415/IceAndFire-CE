package com.iafenvoy.iceandfire.render.entity.state;

import com.iafenvoy.iceandfire.data.DragonType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class DragonSkullRenderState extends EntityRenderState {
    public int lightCoords;
    public float yRot;
    public float size;
    public boolean isOnWall;
    public Identifier texture;
    public DragonType dragonType;
}
