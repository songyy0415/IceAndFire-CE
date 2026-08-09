package com.iafenvoy.iceandfire.event;

import com.iafenvoy.uranus.event.Event;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;

public final class CommonEvents {
    public static final Event<Consumer<LivingEntity>> LIVING_TICK = new Event<>(listeners -> living -> listeners.forEach(x -> x.accept(living)));
}
