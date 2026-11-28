package com.leon.saintsdragons.server.entity.handler;

import com.leon.saintsdragons.server.entity.interfaces.HumanSoundProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimationController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Client-side animation sound dispatcher for humanoid entities. */
public class HumanSoundHandler {
    private final Mob entity;
    private final HumanSoundProfile profile;
    private final Map<String, Integer> soundCooldowns = new HashMap<>();

    public HumanSoundHandler(Mob entity, HumanSoundProfile profile) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        this.entity = entity;
        this.profile = profile != null ? profile : HumanSoundProfile.EMPTY;
    }

    public void handleAnimationSound(Object keyframeData, AnimationController<?> controller) {
        if (!entity.level().isClientSide || keyframeData == null) return;
        String raw = extractSoundString(keyframeData);
        if (raw == null || raw.isBlank()) return;

        String[] parts = raw.toLowerCase(Locale.ROOT).split("\\|", -1);
        String soundKey = parts[0].trim();
        if (soundKey.isEmpty()) return;
        float volume = parts.length > 1 ? parseFloat(parts[1], 1.0F) : 1.0F;
        float pitch = parts.length > 2 ? parseFloat(parts[2], 1.0F) : 1.0F;
        if (!Float.isFinite(volume) || !Float.isFinite(pitch) || volume < 0 || pitch <= 0) return;

        String locator = extractLocator(keyframeData);
        if (soundKey.contains(":")) {
            playDirectSound(soundKey, locator, volume, pitch);
            return;
        }
        profile.handleSound(this, entity, soundKey, locator, volume, pitch);
    }

    public boolean playSound(String soundKey, SoundEvent sound, Vec3 position,
                             float volume, float pitch, int cooldownTicks) {
        if (sound == null || position == null || soundKey == null || soundKey.isBlank()
                || !Float.isFinite(volume) || !Float.isFinite(pitch) || volume < 0 || pitch <= 0
                || isOnCooldown(soundKey)) return false;
        if (cooldownTicks > 0) setCooldown(soundKey, cooldownTicks);
        Level level = entity.level();
        level.playLocalSound(position.x, position.y, position.z, sound, SoundSource.NEUTRAL,
                volume, pitch, false);
        return true;
    }

    public boolean playSound(String soundKey, SoundEvent sound, float volume, float pitch, int cooldownTicks) {
        return playSound(soundKey, sound, entity.position(), volume, pitch, cooldownTicks);
    }

    public void playSoundImmediate(SoundEvent sound, Vec3 position, float volume, float pitch) {
        if (sound == null || position == null || !Float.isFinite(volume) || !Float.isFinite(pitch)
                || volume < 0 || pitch <= 0) return;
        entity.level().playLocalSound(position.x, position.y, position.z, sound,
                SoundSource.NEUTRAL, volume, pitch, false);
    }

    public Vec3 resolveLocator(String locator) {
        if (locator == null || locator.isBlank()) return entity.position();
        Vec3 locatorPos = getClientLocatorPosition(locator);
        return locatorPos != null ? locatorPos : profile.resolveLocator(this, entity, locator);
    }

    public boolean isOnCooldown(String soundKey) {
        if (soundKey == null) return false;
        Integer cooldownEnd = soundCooldowns.get(soundKey);
        if (cooldownEnd == null) return false;
        if (entity.tickCount >= cooldownEnd) {
            soundCooldowns.remove(soundKey);
            return false;
        }
        return true;
    }

    public void setCooldown(String soundKey, int ticks) {
        if (soundKey == null || soundKey.isBlank() || ticks <= 0) return;
        soundCooldowns.put(soundKey, entity.tickCount + ticks);
    }

    public Mob getEntity() {
        return entity;
    }

    public HumanSoundProfile getProfile() {
        return profile;
    }

    private void playDirectSound(String soundId, String locator, float volume, float pitch) {
        ResourceLocation id = ResourceLocation.tryParse(soundId);
        if (id == null) return;
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(id);
        if (sound != null) playSoundImmediate(sound, resolveLocator(locator), volume, pitch);
    }

    private String extractSoundString(Object keyframeData) {
        try {
            Object value = keyframeData.getClass().getMethod("getSound").invoke(keyframeData);
            return value instanceof String string ? string : null;
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    private String extractLocator(Object keyframeData) {
        try {
            Object value = keyframeData.getClass().getMethod("getLocator").invoke(keyframeData);
            return value instanceof String string ? string : null;
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private Vec3 getClientLocatorPosition(String locator) {
        if (entity instanceof GeoEntity) {
            // Locator transforms are supplied by species profiles when available.
            return null;
        }
        return null;
    }
}
