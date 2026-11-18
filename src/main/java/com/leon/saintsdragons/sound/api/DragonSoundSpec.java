package com.leon.saintsdragons.sound.api;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Description of a dragon sound playback request.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/sound/api/DragonSoundSpec.java
 */
public record DragonSoundSpec(
        DragonSoundMode mode,
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch,
        int durationTicks
) {
    public static DragonSoundSpec world(SoundEvent sound, SoundSource source, float volume, float pitch) {
        return new DragonSoundSpec(DragonSoundMode.WORLD, sound, source, volume, pitch, 0);
    }

    public static DragonSoundSpec moving(SoundEvent sound, SoundSource source, float volume, float pitch, int durationTicks) {
        return new DragonSoundSpec(DragonSoundMode.MOVING, sound, source, volume, pitch, Math.max(1, durationTicks));
    }
}
