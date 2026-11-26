/*
 * Copyright (c) 2025 Leon Saint
 * Licensed under the MIT License. See LICENSE.md in the project root.
 */
package com.leon.saintsdragons.server.entity.dragons.util;

import com.leon.saintsdragons.common.registry.ModTags;
import net.minecraft.world.entity.LivingEntity;

/** Shared tag-based checks for elemental damage immunity. */
public final class DragonElementalImmunity {
    private DragonElementalImmunity() {
    }

    public static boolean isElectricityImmune(LivingEntity target) {
        return target != null && target.getType().is(ModTags.EntityTypes.IMMUNE_TO_ELECTRICITY);
    }

    public static boolean isFireImmune(LivingEntity target) {
        return target != null && target.getType().is(ModTags.EntityTypes.IMMUNE_TO_FIRE);
    }

    public static boolean isPoisonImmune(LivingEntity target) {
        return target != null && target.getType().is(ModTags.EntityTypes.IMMUNE_TO_POISON);
    }
}
