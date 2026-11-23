package com.leon.saintsdragons.neoforge.client;

import com.leon.saintsdragons.neoforge.SaintsDragonsNeoForge;
import com.leon.saintsdragons.client.debug.DragonAbilityDebugClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.platform.InputConstants;
import com.leon.saintsdragons.sound.client.DragonSoundRuntime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Client-only lifecycle hooks for migrated runtime systems. */
@EventBusSubscriber(modid = SaintsDragonsNeoForge.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private static final String KEY_CATEGORY = "key.categories.saintsdragons.debug";
    public static final KeyMapping TOGGLE_ABILITY_DEBUG = new KeyMapping(
            "key.saintsdragons.toggle_ability_debug",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F7,
            KEY_CATEGORY
    );
    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        DragonSoundRuntime.tick(minecraft);
        DragonAbilityDebugClient.tick();
        if (minecraft.player == null) {
            return;
        }

        while (TOGGLE_ABILITY_DEBUG.consumeClick()) {
            boolean enabled = DragonAbilityDebugClient.toggle();
            minecraft.player.displayClientMessage(
                    Component.literal("Dragon ability debug: " + (enabled ? "ON" : "OFF")),
                    true
            );
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!DragonAbilityDebugClient.isEnabled()
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        var boxes = DragonAbilityDebugClient.getBoxes();
        if (boxes.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (var box : boxes) {
            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    box.box(),
                    box.red(),
                    box.green(),
                    box.blue(),
                    1.0F
            );
        }
        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
    }
}
