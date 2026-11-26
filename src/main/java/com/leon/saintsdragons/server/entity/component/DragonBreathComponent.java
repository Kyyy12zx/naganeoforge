package com.leon.saintsdragons.server.entity.component;

import net.minecraft.util.Mth;

public final class DragonBreathComponent {
    private DragonBreathComponent() {
    }

    public interface Gauge {
        float getEnergy();

        void setEnergyRaw(float energy);

        boolean isDepleted();

        void setDepleted(boolean depleted);
    }

    public static void setEnergy(Gauge gauge, float energy, float rearmThreshold) {
        if (gauge == null) {
            return;
        }
        float safeEnergy = Float.isFinite(energy) ? energy : 0.0F;
        float threshold = Float.isFinite(rearmThreshold) ? Mth.clamp(rearmThreshold, 0.0F, 1.0F) : 1.0F;
        float clamped = Mth.clamp(safeEnergy, 0.0F, 1.0F);
        gauge.setEnergyRaw(clamped);
        if (clamped >= threshold && gauge.isDepleted()) {
            gauge.setDepleted(false);
        }
    }

    public static boolean canUse(Gauge gauge, float depletedThreshold) {
        if (gauge == null) {
            return false;
        }
        float energy = gauge.getEnergy();
        float threshold = Float.isFinite(depletedThreshold)
                ? Mth.clamp(depletedThreshold, 0.0F, 1.0F) : 1.0F;
        return Float.isFinite(energy) && energy > threshold && !gauge.isDepleted();
    }

    public static boolean drain(Gauge gauge, float amount, float depletedThreshold, float rearmThreshold) {
        if (gauge == null) {
            return false;
        }
        float drain = Float.isFinite(amount) ? Math.max(0.0F, amount) : 0.0F;
        float energy = gauge.getEnergy();
        if (!Float.isFinite(energy)) {
            energy = 0.0F;
        }
        if (drain > 0.0F) {
            setEnergy(gauge, energy - drain, rearmThreshold);
        }
        float threshold = Float.isFinite(depletedThreshold)
                ? Mth.clamp(depletedThreshold, 0.0F, 1.0F) : 1.0F;
        if (gauge.getEnergy() <= threshold) {
            gauge.setDepleted(true);
            return false;
        }
        return true;
    }

    public static void regen(Gauge gauge, float amount, float rearmThreshold) {
        if (gauge == null) {
            return;
        }
        float energy = gauge.getEnergy();
        if (!Float.isFinite(energy)) {
            energy = 0.0F;
        }
        if (energy >= 1.0F) {
            return;
        }
        float regen = Float.isFinite(amount) ? Math.max(0.0F, amount) : 0.0F;
        if (regen > 0.0F) {
            setEnergy(gauge, energy + regen, rearmThreshold);
        }
    }
}
