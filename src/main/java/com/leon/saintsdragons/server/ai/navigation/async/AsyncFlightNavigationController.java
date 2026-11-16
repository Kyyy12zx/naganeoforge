package com.leon.saintsdragons.server.ai.navigation.async;

import net.minecraft.world.phys.Vec3;

/**
 * Minimal contract consumed by {@link AsyncFlyingPathNavigation}.
 */
public interface AsyncFlightNavigationController {
    void setWaypoint(Vec3 target, double speedModifier);

    void clearAllWaypoints();

    boolean isIdle();
}
