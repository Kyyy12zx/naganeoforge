package com.leon.saintsdragons.server.ai.dragonbrain.tactical;

/**
 * Immutable water-combat tuning for a dragon species.
 * Runtime decision helpers are implemented by the dragon AI integration.
 */
public record DragonWaterCombatProfile(boolean aquatic, boolean favorsRanged,
                                       double attackRadius, double attackHeight,
                                       double biteRange, double passLength,
                                       int biteIntervalTicks) {
}
