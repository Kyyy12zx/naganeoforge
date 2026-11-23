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
package com.leon.saintsdragons.client.debug;

import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Client-side lifetime store for server-authored dragon ability debug boxes. */
public final class DragonAbilityDebugClient {
    public record DebugBox(AABB box, float red, float green, float blue, int alpha) {
    }

    private static final int MAX_ACTIVE_BOXES = 512;
    private static final int MAX_LIFETIME_TICKS = 12_000;
    private static final List<Entry> ACTIVE_BOXES = new ArrayList<>();
    private static boolean enabled;

    private DragonAbilityDebugClient() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        if (!enabled) {
            ACTIVE_BOXES.clear();
        }
        return enabled;
    }

    public static void addBox(AABB box, int colorRgb, int lifetimeTicks) {
        if (!enabled || box == null || lifetimeTicks <= 0 || lifetimeTicks > MAX_LIFETIME_TICKS
                || !Double.isFinite(box.minX) || !Double.isFinite(box.minY) || !Double.isFinite(box.minZ)
                || !Double.isFinite(box.maxX) || !Double.isFinite(box.maxY) || !Double.isFinite(box.maxZ)
                || box.minX > box.maxX || box.minY > box.maxY || box.minZ > box.maxZ) {
            return;
        }

        if (ACTIVE_BOXES.size() >= MAX_ACTIVE_BOXES) {
            ACTIVE_BOXES.remove(0);
        }

        float red = ((colorRgb >> 16) & 0xFF) / 255.0F;
        float green = ((colorRgb >> 8) & 0xFF) / 255.0F;
        float blue = (colorRgb & 0xFF) / 255.0F;
        ACTIVE_BOXES.add(new Entry(box, red, green, blue, lifetimeTicks));
    }

    public static List<DebugBox> getBoxes() {
        List<DebugBox> boxes = new ArrayList<>(ACTIVE_BOXES.size());
        for (Entry entry : ACTIVE_BOXES) {
            boxes.add(new DebugBox(entry.box, entry.red, entry.green, entry.blue, 255));
        }
        return List.copyOf(boxes);
    }

    public static void tick() {
        if (ACTIVE_BOXES.isEmpty()) {
            return;
        }

        Iterator<Entry> iterator = ACTIVE_BOXES.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            entry.ticksRemaining--;
            if (entry.ticksRemaining <= 0) {
                iterator.remove();
            }
        }
    }

    private static final class Entry {
        private final AABB box;
        private final float red;
        private final float green;
        private final float blue;
        private int ticksRemaining;

        private Entry(AABB box, float red, float green, float blue, int ticksRemaining) {
            this.box = box;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
