package com.leon.saintsdragons.common.network;

import com.leon.saintsdragons.platform.Services;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.UUID;

/**
 * Server-to-client request for a short sound that follows a tracked entity.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/common/network/MessageDragonMovingSound.java
 */
public record MessageDragonMovingSound(
        UUID playbackId,
        int entityId,
        UUID entityUuid,
        String soundId,
        float volume,
        float pitch,
        int durationTicks
) {
    private static final int MAX_SOUND_ID_LENGTH = 256;

    public MessageDragonMovingSound {
        Objects.requireNonNull(playbackId, "playbackId");
        Objects.requireNonNull(entityUuid, "entityUuid");
        if (soundId == null || soundId.isBlank() || soundId.length() > MAX_SOUND_ID_LENGTH) {
            throw new IllegalArgumentException("Sound ID must contain 1-" + MAX_SOUND_ID_LENGTH + " characters");
        }
        if (!Float.isFinite(volume) || volume < 0.0F) {
            throw new IllegalArgumentException("Sound volume must be finite and non-negative");
        }
        if (!Float.isFinite(pitch) || pitch <= 0.0F) {
            throw new IllegalArgumentException("Sound pitch must be finite and positive");
        }
        durationTicks = Math.max(1, durationTicks);
    }

    public static void encode(MessageDragonMovingSound message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playbackId());
        buffer.writeVarInt(message.entityId());
        buffer.writeUUID(message.entityUuid());
        buffer.writeUtf(message.soundId(), MAX_SOUND_ID_LENGTH);
        buffer.writeFloat(message.volume());
        buffer.writeFloat(message.pitch());
        buffer.writeVarInt(message.durationTicks());
    }

    public static MessageDragonMovingSound decode(FriendlyByteBuf buffer) {
        return new MessageDragonMovingSound(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readUUID(),
                buffer.readUtf(MAX_SOUND_ID_LENGTH),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readVarInt()
        );
    }

    public static void handle(MessageDragonMovingSound message) {
        Services.PLATFORM.runOnClient(() ->
                com.leon.saintsdragons.client.network.ClientPacketHandlers.handleDragonMovingSound(message));
    }
}
