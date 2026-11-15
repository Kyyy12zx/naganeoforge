package com.leon.saintsdragons.server.ai.dragonbrain.perception;

import net.minecraft.util.Mth;

/**
 * Distance and timing parameters used when a dragon assesses scent clues.
 */
public record DragonScentProfile(
        double horizontalRange,
        double verticalRange,
        int minAssessmentTicks,
        int maxAssessmentTicks,
        int minCooldownTicks,
        int maxCooldownTicks
) {
    /**
     * Builds a profile from an entity's dimensions, avoiding a dependency on
     * the not-yet-ported DragonEntity implementation.
     */
    public static DragonScentProfile forDimensions(double width, double height) {
        double horizontalRange = Mth.clamp(18.0D + width * 2.0D, 20.0D, 30.0D);
        double verticalRange = Mth.clamp(6.0D + height * 0.75D, 8.0D, 14.0D);
        return new DragonScentProfile(horizontalRange, verticalRange, 24, 36, 20 * 12, 20 * 20);
    }

    public double uncertainty(double distance) {
        double rangeFactor = Mth.clamp(distance / horizontalRange, 0.0D, 1.0D);
        return 1.5D + rangeFactor * 4.5D;
    }

    public float confidence(double distance) {
        double rangeFactor = Mth.clamp(distance / horizontalRange, 0.0D, 1.0D);
        return (float) (0.85D - rangeFactor * 0.5D);
    }
}
