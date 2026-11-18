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
        Entity parent = getDragonParent();
        if (!(this instanceof Entity part)) {
            return false;
        }
        float multiplier = getDamageMultiplier();
        if (source == null || parent == null || !parent.isAlive() || parent.isRemoved() || part.isRemoved()
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
