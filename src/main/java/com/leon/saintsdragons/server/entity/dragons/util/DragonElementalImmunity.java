package com.leon.saintsdragons.server.entity.dragons.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Shared elemental-immunity checks backed by datapack entity-type tags.
 */
public final class DragonElementalImmunity {
    private static final TagKey<EntityType<?>> IMMUNE_TO_ELECTRICITY = tag("immune_to/electricity");
    private static final TagKey<EntityType<?>> IMMUNE_TO_FIRE = tag("immune_to/fire");
    private static final TagKey<EntityType<?>> IMMUNE_TO_POISON = tag("immune_to/poison");

    private DragonElementalImmunity() {
    }

    public static boolean isElectricityImmune(LivingEntity target) {
        return target != null && target.getType().is(IMMUNE_TO_ELECTRICITY);
    }

    public static boolean isFireImmune(LivingEntity target) {
        return target != null && target.getType().is(IMMUNE_TO_FIRE);
    }

    public static boolean isPoisonImmune(LivingEntity target) {
        return target != null && target.getType().is(IMMUNE_TO_POISON);
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath("saintsdragons", path)
        );
    }
}
