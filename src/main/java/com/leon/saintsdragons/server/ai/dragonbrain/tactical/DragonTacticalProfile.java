package com.leon.saintsdragons.server.ai.dragonbrain.tactical;

/**
 * Tuning values for selecting and retaining a dragon's current tactical mode.
 *
 * <p>The profile is deliberately independent of the dragon entity so it can
 * be used by the brain and decision logic without introducing entity cycles.</p>
 */
public record DragonTacticalProfile(
        int evaluationIntervalTicks,
        int switchMargin,
        float retreatHealthRatio
) {
    private static final DragonTacticalProfile STANDARD =
            new DragonTacticalProfile(10, 15, 0.25F);

    public static DragonTacticalProfile standard() {
        return STANDARD;
    }

    public int minimumTicks(DragonTactic tactic) {
        return switch (tactic) {
            case NONE -> 20;
            case GUARD -> 60;
            case INVESTIGATE -> 40;
            case GROUND_PURSUIT, WATER_PURSUIT -> 60;
            case AERIAL_PURSUIT, LANDING_APPROACH -> 80;
            case RETREAT -> 100;
        };
    }

    public int maximumTicks(DragonTactic tactic) {
        return switch (tactic) {
            case NONE -> 100;
            case GUARD -> 200;
            case INVESTIGATE -> 160;
            case GROUND_PURSUIT, WATER_PURSUIT -> 200;
            case AERIAL_PURSUIT, LANDING_APPROACH -> 240;
            case RETREAT -> 160;
        };
    }
}
