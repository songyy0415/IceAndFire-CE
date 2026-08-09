package com.iafenvoy.iceandfire.network.payload;

import com.iafenvoy.iceandfire.IceAndFire;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StartRidingMobC2SPayload(int dragonId, boolean ride, boolean baby) implements CustomPacketPayload {
    private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "start_riding_mob_c2s");
    public static final Type<StartRidingMobC2SPayload> ID = new Type<>(IDENTIFIER);
    public static final StreamCodec<ByteBuf, StartRidingMobC2SPayload> CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("dragonId").forGetter(StartRidingMobC2SPayload::dragonId),
            Codec.BOOL.fieldOf("ride").forGetter(StartRidingMobC2SPayload::ride),
            Codec.BOOL.fieldOf("baby").forGetter(StartRidingMobC2SPayload::baby)
    ).apply(i, StartRidingMobC2SPayload::new)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
