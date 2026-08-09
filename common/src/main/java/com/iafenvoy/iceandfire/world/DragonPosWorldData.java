package com.iafenvoy.iceandfire.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

public class DragonPosWorldData extends SavedData {
    private static final Codec<DragonPosWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, BlockPos.CODEC).fieldOf("DragonMap").forGetter(data -> data.lastDragonPositions)
    ).apply(instance, map -> {
        DragonPosWorldData data = new DragonPosWorldData();
        data.lastDragonPositions.putAll(map);
        return data;
    }));
    private static final SavedDataType<DragonPosWorldData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("iceandfire", "dragonPositions"), DragonPosWorldData::new, CODEC, DataFixTypes.CHUNK);
    protected final Map<UUID, BlockPos> lastDragonPositions = new HashMap<>();

    public static DragonPosWorldData get(Level world) {
        if (world instanceof ServerLevel serverWorld) {
            SavedDataStorage storage = serverWorld.getDataStorage();
            DragonPosWorldData data = storage.computeIfAbsent(TYPE);
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
}
