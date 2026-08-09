package com.iafenvoy.iceandfire.network.payload;

import com.iafenvoy.iceandfire.IceAndFire;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StartRidingMobS2CPayload(int dragonId, boolean ride, boolean baby) implements CustomPacketPayload {
    private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "start_riding_mob_s2c");
    public static final Type<StartRidingMobS2CPayload> ID = new Type<>(IDENTIFIER);
    public static final StreamCodec<ByteBuf, StartRidingMobS2CPayload> CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("dragonId").forGetter(StartRidingMobS2CPayload::dragonId),
            Codec.BOOL.fieldOf("ride").forGetter(StartRidingMobS2CPayload::ride),
            Codec.BOOL.fieldOf("baby").forGetter(StartRidingMobS2CPayload::baby)
    ).apply(i, StartRidingMobS2CPayload::new)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
