package com.leon.saintsdragons.sound.server;

import com.leon.saintsdragons.common.network.MessageDragonMovingSound;
import com.leon.saintsdragons.platform.Services;
import com.leon.saintsdragons.sound.api.DragonSoundMode;
import com.leon.saintsdragons.sound.api.DragonSoundSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Routes world and entity-following sound requests to the appropriate side.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield and adapted to the
 * NeoForge platform network abstraction:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/sound/server/DragonSoundOrchestrator.java
 */
public final class DragonSoundOrchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DragonSoundOrchestrator.class);

    private DragonSoundOrchestrator() {
    }

    public static void play(Entity entity, DragonSoundSpec spec) {
        if (entity == null || spec == null || spec.mode() == null || spec.sound() == null
                || spec.source() == null || !Float.isFinite(spec.volume()) || spec.volume() < 0.0F
                || !Float.isFinite(spec.pitch()) || spec.pitch() <= 0.0F) {
            return;
        }
        if (spec.mode() == DragonSoundMode.MOVING) {
            playMoving(entity, spec);
            return;
        }
        playWorld(entity, spec);
    }

    private static void playWorld(Entity entity, DragonSoundSpec spec) {
        if (entity.level().isClientSide) {
            entity.level().playLocalSound(
                    entity.getX(), entity.getY(), entity.getZ(),
                    spec.sound(), spec.source(), spec.volume(), spec.pitch(), false
            );
            return;
        }
        entity.level().playSound(
                null, entity.getX(), entity.getY(), entity.getZ(),
                spec.sound(), spec.source(), spec.volume(), spec.pitch()
        );
    }

    private static void playMoving(Entity entity, DragonSoundSpec spec) {
        if (entity.level().isClientSide || entity.isRemoved() || !entity.isAlive()) {
            return;
        }
        var soundId = BuiltInRegistries.SOUND_EVENT.getKey(spec.sound());
        if (soundId == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sound event not registered for entity {}: {}", entity.getId(), spec.sound());
            }
            return;
        }
        MessageDragonMovingSound packet = new MessageDragonMovingSound(
                UUID.randomUUID(),
                entity.getId(),
                entity.getUUID(),
                soundId.toString(),
                spec.volume(),
                spec.pitch(),
                spec.durationTicks()
        );
        Services.PLATFORM.getNetworkHelper().sendToTracking(entity, packet);
    }
}
