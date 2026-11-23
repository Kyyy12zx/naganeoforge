/*
 * Copyright (c) 2025 Leon Saint
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */
package com.leon.saintsdragons.common.network;

import com.leon.saintsdragons.client.network.ClientPacketHandlers;
import com.leon.saintsdragons.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;

/** Server-to-client debug overlay packet for dragon ability hitboxes. */
public record MessageDragonAbilityDebugBox(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ,
        int colorRgb,
        int lifetimeTicks
) {
    private static final int MAX_LIFETIME_TICKS = 12_000;

    public MessageDragonAbilityDebugBox(AABB box, int colorRgb, int lifetimeTicks) {
        this(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorRgb, lifetimeTicks);
    }

    public boolean isValid() {
        return Double.isFinite(minX) && Double.isFinite(minY) && Double.isFinite(minZ)
                && Double.isFinite(maxX) && Double.isFinite(maxY) && Double.isFinite(maxZ)
                && minX <= maxX && minY <= maxY && minZ <= maxZ
                && lifetimeTicks > 0 && lifetimeTicks <= MAX_LIFETIME_TICKS;
    }

    public AABB box() {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static void encode(MessageDragonAbilityDebugBox message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.minX());
        buffer.writeDouble(message.minY());
        buffer.writeDouble(message.minZ());
        buffer.writeDouble(message.maxX());
        buffer.writeDouble(message.maxY());
        buffer.writeDouble(message.maxZ());
        buffer.writeInt(message.colorRgb());
        buffer.writeVarInt(message.lifetimeTicks());
    }

    public static MessageDragonAbilityDebugBox decode(FriendlyByteBuf buffer) {
        return new MessageDragonAbilityDebugBox(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readInt(),
                buffer.readVarInt()
        );
    }

    public static void handle(MessageDragonAbilityDebugBox message) {
        Services.PLATFORM.runOnClient(() -> ClientPacketHandlers.handleAbilityDebugBox(message));
    }
}
