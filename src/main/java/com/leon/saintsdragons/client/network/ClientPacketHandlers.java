package com.leon.saintsdragons.client.network;

import com.leon.saintsdragons.client.camera.ClientCameraImpulse;
import com.leon.saintsdragons.common.network.MessageCameraImpulse;
import com.leon.saintsdragons.common.network.MessageDragonMovingSound;
import com.leon.saintsdragons.sound.client.DragonSoundRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** Client-side handlers for server-originated visual feedback packets. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleDragonMovingSound(MessageDragonMovingSound message) {
        DragonSoundRuntime.playMoving(
                message.playbackId(),
                message.entityId(),
                message.entityUuid(),
                message.soundId(),
                message.volume(),
                message.pitch(),
                message.durationTicks()
        );
    }

    public static void handleCameraImpulse(MessageCameraImpulse message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || message.radius() <= 0.0F) {
            return;
        }

        double distance = minecraft.player.position().distanceTo(message.origin());
        if (distance > message.radius()) {
            return;
        }

        float proximity = 1.0F - Mth.clamp((float) (distance / message.radius()), 0.0F, 1.0F);
        ClientCameraImpulse.trigger(
                message.intensity() * (0.35F + proximity * 0.65F),
                message.durationTicks()
        );
    }
}
