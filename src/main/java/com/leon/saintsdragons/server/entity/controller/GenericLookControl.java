package com.leon.saintsdragons.server.entity.controller;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

/**
 * Look controller that caps head yaw and pitch changes per tick.
 */
public class GenericLookControl extends LookControl {
    private final Mob entity;
    private final float maxYRotSpeed;
    private final float maxXRotSpeed;

    public GenericLookControl(Mob entity) {
        this(entity, 10.0F, 9.0F);
    }

    public GenericLookControl(Mob entity, float maxYRotSpeed, float maxXRotSpeed) {
        super(entity);
        this.entity = entity;
        this.maxYRotSpeed = maxYRotSpeed;
        this.maxXRotSpeed = maxXRotSpeed;
    }

    @Override
    public void tick() {
        if (entity.isVehicle()) {
            super.tick();
            return;
        }

        float oldYaw = entity.yHeadRot;
        float oldPitch = entity.getXRot();

        super.tick();

        float yawChange = Mth.wrapDegrees(entity.yHeadRot - oldYaw);
        float pitchChange = entity.getXRot() - oldPitch;

        yawChange = Mth.clamp(yawChange, -maxYRotSpeed, maxYRotSpeed);
        pitchChange = Mth.clamp(pitchChange, -maxXRotSpeed, maxXRotSpeed);

        entity.setYHeadRot(oldYaw + yawChange);
        entity.setXRot(oldPitch + pitchChange);
    }
}
