/*
 * Copyright (c) 2025 Leon Saint
 * SPDX-License-Identifier: MIT
 */
package com.leon.saintsdragons.client.input;

import com.leon.saintsdragons.server.entity.base.DragonPartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Resolves a click on a multipart hitbox to the entity behind it.
 *
 * <p>Dragon part entities are used for precise damage targeting, but ordinary
 * interactions should still reach the main entity. The temporary picking guard
 * prevents the second pick pass from selecting the same multipart hitbox again.</p>
 */
public final class DragonPartInteractionTargeting {
    private static boolean pickingInteraction;

    private DragonPartInteractionTargeting() {
    }

    public static @Nullable HitResult pick(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        HitResult originalHit = minecraft.hitResult;
        if (!(originalHit instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof DragonPartEntity)
                || minecraft.level == null
                || minecraft.player == null
                || minecraft.gameMode == null
                || minecraft.getCameraEntity() == null
                || pickingInteraction) {
            return originalHit;
        }

        Entity originalCrosshairEntity = minecraft.crosshairPickEntity;
        try {
            pickingInteraction = true;
            minecraft.gameRenderer.pick(1.0F);
            return minecraft.hitResult;
        } finally {
            pickingInteraction = false;
            minecraft.hitResult = originalHit;
            minecraft.crosshairPickEntity = originalCrosshairEntity;
        }
    }

    public static Predicate<Entity> filter(Predicate<Entity> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return pickingInteraction
                ? predicate.and(entity -> !(entity instanceof DragonPartEntity))
                : predicate;
    }
}
