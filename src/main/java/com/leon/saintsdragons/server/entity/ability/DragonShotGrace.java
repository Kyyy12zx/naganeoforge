package com.leon.saintsdragons.server.entity.ability;

/**
 * Tracks short alignment and obstruction grace windows for dragon attacks.
 *
 * Adapted from Saint's Dragons (LilRicefield), upstream branch 1.20.1.
 */
public final class DragonShotGrace {
    private int lastTick = Integer.MIN_VALUE;
    private int lostTicks;
    private int alignmentTicks;
    private int blockedTicks;

    /**
     * Returns whether an attack may continue through a brief loss of alignment.
     *
     * @param shot current shot assessment
     * @param tick current game tick
     * @param alignmentGrace maximum consecutive alignment ticks tolerated
     * @param obstructionGrace maximum consecutive blocked ticks tolerated
     */
    public boolean allows(DragonAbilityShot shot, int tick, int alignmentGrace, int obstructionGrace) {
        if (shot == null || alignmentGrace < 0 || obstructionGrace < 0) {
            reset();
            return false;
        }
        if (tick != lastTick) {
            lostTicks = shot == DragonAbilityShot.ALIGNED ? 0 : lostTicks + 1;
            alignmentTicks = shot.needsAlignment() ? alignmentTicks + 1 : 0;
            blockedTicks = shot == DragonAbilityShot.ALIGNED || shot.needsAlignment() ? 0 : blockedTicks + 1;
            lastTick = tick;
        }
        return alignmentTicks < alignmentGrace
                && blockedTicks < obstructionGrace
                && lostTicks < alignmentGrace + obstructionGrace;
    }

    public void reset() {
        lastTick = Integer.MIN_VALUE;
        lostTicks = alignmentTicks = blockedTicks = 0;
    }

    public enum DragonAbilityShot {
        ALIGNED,
        ALIGNING,
        OUT_OF_ARC,
        BLOCKED,
        OUT_OF_RANGE,
        NO_TARGET;

        public boolean needsAlignment() {
            return this == ALIGNING || this == OUT_OF_ARC;
        }
    }
}
