package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.IceAndFire;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

//FIXME::Fix this f**king thing after port to NeoForge only
public class IafAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(IceAndFire.MOD_ID, Registries.ATTRIBUTE);

    public static final RegistrySupplier<Attribute> DRAGON_FORGE_SPEED = REGISTRY.register("generic.dragon_forge_speed", () -> new RangedAttribute("attribute.name.generic.dragon_forge_speed", 0.025, 0, 1024).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));
}
