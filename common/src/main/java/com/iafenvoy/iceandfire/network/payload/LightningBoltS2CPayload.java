package com.iafenvoy.iceandfire.network.payload;

import com.iafenvoy.iceandfire.IceAndFire;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.world.phys.Vec3;

public record LightningBoltS2CPayload(List<Pair<Vec3, Vec3>> lightnings) implements CustomPacketPayload {
    private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "lightning_bolt_s2c");
    public static final Type<LightningBoltS2CPayload> ID = new Type<>(IDENTIFIER);
    public static final StreamCodec<ByteBuf, LightningBoltS2CPayload> CODEC = ByteBufCodecs.fromCodec(RecordCodecBuilder.create(i -> i.group(
            RecordCodecBuilder.<Pair<Vec3, Vec3>>create(i1 -> i1.group(
                    Vec3.CODEC.fieldOf("left").forGetter(Pair::getLeft),
                    Vec3.CODEC.fieldOf("right").forGetter(Pair::getRight)
            ).apply(i1, Pair::of)).listOf().fieldOf("lightnings").forGetter(LightningBoltS2CPayload::lightnings)
    ).apply(i, LightningBoltS2CPayload::new)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
