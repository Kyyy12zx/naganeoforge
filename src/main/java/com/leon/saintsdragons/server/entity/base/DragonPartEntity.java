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
package com.leon.saintsdragons.server.entity.base;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Contract for a multipart hitbox belonging to a dragon entity.
 * Damage is forwarded to the parent after validating the relationship and source.
 */
public interface DragonPartEntity {
    @Nullable Entity getDragonParent();

    int getPartIndex();

    float getDamageMultiplier();

    void updateBounds(AABB bounds);

    default boolean hurtParent(DamageSource source, float amount) {
        if (!(this instanceof Entity part)) {
            return false;
        }
        Entity parent = getDragonParent();
        float multiplier = getDamageMultiplier();
        if (source == null || parent == null || parent == part
                || !parent.isAlive() || parent.isRemoved() || part.isRemoved()
                || parent.level() != part.level() || !Float.isFinite(amount) || amount <= 0
                || !Float.isFinite(multiplier) || multiplier <= 0
                || part.isInvulnerableTo(source)) {
            return false;
        }
        if (part.level().isClientSide) {
            return !parent.isInvulnerableTo(source);
        }
        float scaledAmount = amount * multiplier;
        if (!Float.isFinite(scaledAmount) || scaledAmount <= 0) {
            return false;
        }
        return parent.hurt(source, scaledAmount);
    }
}
