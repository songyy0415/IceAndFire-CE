package com.iafenvoy.iceandfire.util;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public final class IafEntityDataSerializers {
    // mc26.2 removed EntityDataSerializers.OPTIONAL_UUID; rebuilt it from a string-backed stream codec.
    // 26.2 requires custom serializers to be registered before use, else defineId() gets an
    // unregistered id ("Unregistered serializer ... for N").
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString)));

    static {
        EntityDataSerializers.registerSerializer(OPTIONAL_UUID);
    }

    private IafEntityDataSerializers() {
    }
}
