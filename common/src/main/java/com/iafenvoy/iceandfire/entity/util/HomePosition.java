package com.iafenvoy.iceandfire.entity.util;

import com.iafenvoy.iceandfire.entity.util.dragon.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HomePosition {
    int x;
    int y;
    int z;
    BlockPos pos;
    String dimension;

    public HomePosition(ValueInput compound) {
        this.read(compound);
    }

    public HomePosition(ValueInput compound, Level world) {
        this.read(compound, world);
    }

    public HomePosition(BlockPos pos, Level world) {
        this(pos.getX(), pos.getY(), pos.getZ(), world);
    }

    public HomePosition(int x, int y, int z, Level world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pos = new BlockPos(x, y, z);
        this.dimension = DragonUtils.getDimensionName(world);
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    public String getDimension() {
        return this.dimension == null ? "" : this.dimension;
    }

    public void write(ValueOutput compound) {
        compound.putInt("HomeAreaX", this.x);
        compound.putInt("HomeAreaY", this.y);
        compound.putInt("HomeAreaZ", this.z);
        if (this.dimension != null)
            compound.putString("HomeDimension", this.dimension);
    }

    public void read(ValueInput compound, Level world) {
        this.read(compound);
        if (this.dimension == null)
            this.dimension = DragonUtils.getDimensionName(world);
    }

    public void read(ValueInput compound) {
        if (compound.getInt("HomeAreaX").isPresent())
            this.x = compound.getInt("HomeAreaX").orElse(0);
        if (compound.getInt("HomeAreaY").isPresent())
            this.y = compound.getInt("HomeAreaY").orElse(0);
        if (compound.getInt("HomeAreaZ").isPresent())
            this.z = compound.getInt("HomeAreaZ").orElse(0);
        this.pos = new BlockPos(this.x, this.y, this.z);
        if (compound.getString("HomeDimension").isPresent())
            this.dimension = compound.getString("HomeDimension").orElse("");
    }
}
