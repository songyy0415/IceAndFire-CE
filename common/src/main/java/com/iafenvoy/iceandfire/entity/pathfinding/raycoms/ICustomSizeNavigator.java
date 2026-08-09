package com.iafenvoy.iceandfire.entity.pathfinding.raycoms;

public interface ICustomSizeNavigator {
    boolean isSmallerThanBlock();

    float getXZNavSize();

    int getYNavSize();
}
