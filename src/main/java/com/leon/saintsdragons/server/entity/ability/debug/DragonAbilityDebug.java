/*
 * Copyright (c) 2025 Leon Saint
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */
package com.leon.saintsdragons.server.entity.ability.debug;

import com.leon.saintsdragons.common.network.MessageDragonAbilityDebugBox;
import com.leon.saintsdragons.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/** Sends temporary ability debug bounds only to players near the source entity. */
public final class DragonAbilityDebug {
    private static final double DEFAULT_SEND_RADIUS = 96.0D;
    private static final double DEFAULT_SEND_RADIUS_SQR = DEFAULT_SEND_RADIUS * DEFAULT_SEND_RADIUS;

    private DragonAbilityDebug() {
    }

    public static void sendBox(Entity source, AABB box, int colorRgb, int lifetimeTicks) {
        if (source == null || source.isRemoved() || box == null
                || !(source.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        MessageDragonAbilityDebugBox message = new MessageDragonAbilityDebugBox(box, colorRgb, lifetimeTicks);
        if (!message.isValid()) {
            return;
        }

        for (ServerPlayer player : serverLevel.players()) {
            if (player.isAlive() && player.distanceToSqr(source) <= DEFAULT_SEND_RADIUS_SQR) {
                Services.PLATFORM.getNetworkHelper().sendToPlayer(player, message);
            }
        }
    }
}
