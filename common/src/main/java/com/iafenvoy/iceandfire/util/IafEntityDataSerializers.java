package com.iafenvoy.iceandfire.util;

import com.iafenvoy.iceandfire.IceAndFire;
import dev.architectury.registry.level.entity.EntityDataSerializerRegistry;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class IafEntityDataSerializers {
    // mc26.2 removed EntityDataSerializers.OPTIONAL_UUID; rebuilt it from a string-backed stream codec.
    // 26.2 requires custom serializers to be registered before use, else defineId() gets an
    // unregistered id ("Unregistered serializer ... for N"). Register through Architectury's
    // cross-loader registry (Fabric delegates to FabricEntityDataRegistry, NeoForge to the
    // native registry) — calling EntityDataSerializers.registerSerializer directly is blocked
    // on Fabric.
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString)));

    static {
        EntityDataSerializerRegistry.register(IceAndFire.id("optional_uuid"), OPTIONAL_UUID);
    }

    private IafEntityDataSerializers() {
    }
}
