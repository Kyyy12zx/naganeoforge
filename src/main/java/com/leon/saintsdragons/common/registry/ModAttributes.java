package com.leon.saintsdragons.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge registry for Saint's Dragons gameplay attributes. */
public final class ModAttributes {
    private static final DeferredRegister<Attribute> REGISTER =
            DeferredRegister.create(Registries.ATTRIBUTE, "saintsdragons");

    public static final DeferredHolder<Attribute, Attribute> DOUBLE_JUMP =
            REGISTER.register("double_jump",
                    () -> new RangedAttribute("attribute.name.saintsdragons.double_jump", 0.0D, 0.0D, 1.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> FIRE_RESISTANCE =
            REGISTER.register("fire_resistance",
                    () -> new RangedAttribute("attribute.name.saintsdragons.fire_resistance", 0.0D, 0.0D, 100.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BLAST_RESISTANCE =
            REGISTER.register("blast_resistance",
                    () -> new RangedAttribute("attribute.name.saintsdragons.blast_resistance", 0.0D, 0.0D, 100.0D)
                            .setSyncable(true));

    private ModAttributes() {
    }

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
