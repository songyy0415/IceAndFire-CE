package com.iafenvoy.iceandfire.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class DragonPosWorldData extends SavedData {
    private static final Factory<DragonPosWorldData> TYPE = new Factory<>(DragonPosWorldData::new, DragonPosWorldData::fromNbt, DataFixTypes.CHUNK);
    private static final String IDENTIFIER = "iceandfire_dragonPositions";
    protected final Map<UUID, BlockPos> lastDragonPositions = new HashMap<>();

    private static DragonPosWorldData fromNbt(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        DragonPosWorldData data = new DragonPosWorldData();
        ListTag list = nbt.getListOrEmpty("DragonMap");
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag obj = list.getCompound(i);
            UUID uuid = obj.read("DragonUUID", UUIDUtil.CODEC).orElse(null);
            BlockPos pos = new BlockPos(obj.getInt("DragonPosX").orElse(0), obj.getInt("DragonPosY").orElse(0), obj.getInt("DragonPosZ").orElse(0));
            data.lastDragonPositions.put(uuid, pos);
        }
        return data;
    }

    public static DragonPosWorldData get(Level world) {
        if (world instanceof ServerLevel serverWorld) {
            DimensionDataStorage storage = serverWorld.getDataStorage();
            DragonPosWorldData data = storage.computeIfAbsent(TYPE, IDENTIFIER);
            if (data != null) data.setDirty();
            return data;
        }
        return null;
    }

    public void addDragon(UUID uuid, BlockPos pos) {
        this.lastDragonPositions.put(uuid, pos);
        this.setDirty();
    }

    public void removeDragon(UUID uuid) {
        this.lastDragonPositions.remove(uuid);
        this.setDirty();
    }

    public BlockPos getDragonPos(UUID uuid) {
        return this.lastDragonPositions.get(uuid);
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, BlockPos> pair : this.lastDragonPositions.entrySet()) {
            CompoundTag obj = new CompoundTag();
            obj.putIntArray("DragonUUID", UUIDUtil.uuidToIntArray(pair.getKey()));
            obj.putInt("DragonPosX", pair.getValue().getX());
            obj.putInt("DragonPosY", pair.getValue().getY());
            obj.putInt("DragonPosZ", pair.getValue().getZ());
            list.add(obj);
        }
        nbt.put("DragonMap", list);
        return nbt;
    }
}
