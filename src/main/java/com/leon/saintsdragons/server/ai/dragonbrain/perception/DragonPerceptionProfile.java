package com.leon.saintsdragons.server.ai.dragonbrain.perception;

import net.minecraft.util.Mth;

/**
 * Tunable perception and investigation timings shared by dragon AI.
 *
 * <p>The profile is independent of DragonEntity so it can be used by
 * common AI code before entity implementations are registered.</p>
 */
public record DragonPerceptionProfile(
        int hearingRange,
        int targetMemoryTicks,
        int soundMemoryTicks,
        int searchTicks,
        double investigationSpeed,
        double arrivalDistance
) {
    private static final int MIN_HEARING_RANGE = 24;
    private static final int MAX_HEARING_RANGE = 48;
    private static final int MIN_INVESTIGATION_TICKS = 20 * 8;
    private static final int MAX_INVESTIGATION_TICKS = 20 * 20;
    private static final int ROUTE_MARGIN_TICKS = 20 * 2;
    private static final double ESTIMATED_TICKS_PER_BLOCK = 8.0D;

    public static DragonPerceptionProfile standard() {
        return forAttributes(32.0D, 2.0D);
    }

    public static DragonPerceptionProfile forAttributes(double followRange, double width) {
        int hearingRange = Mth.clamp(
                (int) Math.round(Math.max(MIN_HEARING_RANGE, followRange * 0.9D)),
                MIN_HEARING_RANGE,
                MAX_HEARING_RANGE
        );
        return new DragonPerceptionProfile(
                hearingRange,
                20 * 6,
                20 * 4,
                20 * 3,
                0.85D,
                Math.max(2.0D, width * 0.75D)
        );
    }

    public int investigationMemoryTicks(double distanceToDestination) {
        double travelDistance = Math.max(0.0D, distanceToDestination - arrivalDistance);
        int estimatedTravelTicks = Mth.ceil(travelDistance * ESTIMATED_TICKS_PER_BLOCK);
        return Mth.clamp(
                estimatedTravelTicks + searchTicks + ROUTE_MARGIN_TICKS,
                MIN_INVESTIGATION_TICKS,
                MAX_INVESTIGATION_TICKS
        );
    }
}
