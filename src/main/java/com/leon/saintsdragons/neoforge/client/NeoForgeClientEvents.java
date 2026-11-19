package com.leon.saintsdragons.neoforge.client;

import com.leon.saintsdragons.neoforge.SaintsDragonsNeoForge;
import com.leon.saintsdragons.sound.client.DragonSoundRuntime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client-only lifecycle hooks for migrated runtime systems. */
@EventBusSubscriber(modid = SaintsDragonsNeoForge.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        DragonSoundRuntime.tick(Minecraft.getInstance());
    }
}
