package com.leon.saintsdragons.server.entity.controller;

/**
 * Exposes a temporary body-yaw constraint while a dragon performs a combat action.
 */
public interface CombatBodyFacingLock {
    boolean isCombatBodyFacingLocked();

    float getCombatBodyFacingYaw();

    default float getCombatBodyFacingTurnSpeed() {
        return 18.0F;
    }
}
