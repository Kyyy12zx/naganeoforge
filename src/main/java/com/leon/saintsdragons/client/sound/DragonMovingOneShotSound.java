package com.leon.saintsdragons.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/**
 * A finite sound instance whose position follows one entity.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/client/sound/DragonMovingOneShotSound.java
 */
public final class DragonMovingOneShotSound extends AbstractTickableSoundInstance {
    private final int entityId;
    private final UUID entityUuid;
    private final int maxLifeTicks;
    private int lifeTicks;

    public DragonMovingOneShotSound(
            int entityId,
            UUID entityUuid,
            SoundEvent sound,
            float volume,
            float pitch,
            int durationTicks
    ) {
        super(sound, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.entityId = entityId;
        this.entityUuid = entityUuid;
        this.maxLifeTicks = Math.max(1, durationTicks);
        this.looping = false;
        this.delay = 0;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuation = Attenuation.LINEAR;
        updatePosition();
    }

    @Override
    public boolean canPlaySound() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        return isExpectedEntity(entity) && entity.isAlive() && !entity.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (++lifeTicks >= maxLifeTicks || !updatePosition()) {
            stop();
        }
    }

    private boolean updatePosition() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (!isExpectedEntity(entity) || entity.isRemoved() || !entity.isAlive()) {
            return false;
        }
        this.x = entity.getX();
        this.y = entity.getY() + entity.getBbHeight() * 0.65D;
        this.z = entity.getZ();
        return true;
    }

    private boolean isExpectedEntity(Entity entity) {
        return entity != null && entityUuid.equals(entity.getUUID());
    }

    public void updateMix(float volume, float pitch) {
        if (Float.isFinite(volume) && volume >= 0.0F && Float.isFinite(pitch) && pitch > 0.0F) {
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
