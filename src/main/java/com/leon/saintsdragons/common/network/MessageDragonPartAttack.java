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

import com.leon.saintsdragons.platform.Services;
import com.leon.saintsdragons.server.entity.base.DragonPartEntity;
import com.leon.saintsdragons.server.entity.part.DragonPartProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;

/**
 * Server-validated melee interaction with a multipart dragon hitbox.
 *
 * The packet deliberately resolves the parent and re-traces the player's view on
 * the server; client-provided entity and part identifiers are never trusted alone.
 */
public record MessageDragonPartAttack(int dragonId, UUID dragonUuid, int partIndex) {
    private static final Map<ServerPlayer, Integer> LAST_ATTACK_TICK = new WeakHashMap<>();
    private static final double PART_HITBOX_INFLATION = 0.15D;
    private static final double OCCLUSION_EPSILON_SQR = 1.0E-6D;

    public static void encode(MessageDragonPartAttack message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.dragonId());
        buffer.writeUUID(message.dragonUuid());
        buffer.writeVarInt(message.partIndex());
    }

    public static MessageDragonPartAttack decode(FriendlyByteBuf buffer) {
        return new MessageDragonPartAttack(buffer.readVarInt(), buffer.readUUID(), buffer.readVarInt());
    }

    public static void handle(MessageDragonPartAttack message, ServerPlayer player) {
        if (message == null || player == null || !player.isAlive() || player.isSpectator()
                || message.dragonId() < 0 || message.dragonUuid() == null || message.partIndex() < 0) {
            return;
        }

        Entity parent = player.serverLevel().getEntity(message.dragonId());
        if (parent == null || !parent.isAlive() || parent.isRemoved()
                || !parent.getUUID().equals(message.dragonUuid())
                || !(parent instanceof DragonPartProvider provider)) {
            return;
        }

        Entity[] parts = provider.dragonParts();
        if (parts == null || message.partIndex() >= parts.length) {
            return;
        }

        double reach = Services.PLATFORM.getPlayerAttackReach(player);
        if (!Double.isFinite(reach) || reach <= 0.0D) {
            return;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(reach));
        Vec3 wallHit = player.level().clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        )).getLocation();
        double nearestAllowedDistanceSqr = eye.distanceToSqr(wallHit) + OCCLUSION_EPSILON_SQR;

        Entity selectedPart = null;
        double nearestPartDistanceSqr = Double.POSITIVE_INFINITY;
        for (Entity part : parts) {
            if (!(part instanceof DragonPartEntity region)
                    || region.getDragonParent() != parent
                    || part.level() != player.level()
                    || part.isRemoved() || !part.isAlive() || !part.isPickable()) {
                continue;
            }

            var hitBox = part.getBoundingBox().inflate(PART_HITBOX_INFLATION);
            Vec3 hit = hitBox.contains(eye) ? eye : hitBox.clip(eye, end).orElse(null);
            if (hit == null) {
                continue;
            }

            double distanceSqr = eye.distanceToSqr(hit);
            if (distanceSqr < nearestPartDistanceSqr) {
                nearestPartDistanceSqr = distanceSqr;
                selectedPart = part;
            }
        }

        if (selectedPart == null || nearestPartDistanceSqr > nearestAllowedDistanceSqr
                || !player.level().getWorldBorder().isWithinBounds(selectedPart.blockPosition())) {
            return;
        }

        synchronized (LAST_ATTACK_TICK) {
            Integer lastAttackTick = LAST_ATTACK_TICK.get(player);
            if (lastAttackTick != null && lastAttackTick == player.tickCount) {
                return;
            }
            LAST_ATTACK_TICK.put(player, player.tickCount);
        }

        player.resetLastActionTime();
        player.attack(selectedPart);
    }
}
