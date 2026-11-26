/*
 * Copyright (c) 2025 Leon Saint
 * Licensed under the MIT License. See LICENSE.md in the project root.
 */
package com.leon.saintsdragons.server.entity.dragons.util;

import com.leon.saintsdragons.common.config.SaintsDragonsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Shared server-side gate for dragon breeding interactions. */
public final class DragonBreedingRules {
    private DragonBreedingRules() {
    }

    public static boolean isEnabled() {
        return SaintsDragonsConfig.isDragonBreedingEnabled();
    }

    public static boolean checkEnabled(Player player) {
        if (isEnabled()) {
            return true;
        }
        if (player != null && !player.level().isClientSide) {
            player.displayClientMessage(
                    Component.translatable("entity.saintsdragons.dragon.breeding_disabled"), true);
        }
        return false;
    }
}
