package com.leon.saintsdragons.server.entity.dragons.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Tracks redstone wire positions temporarily powered by dragon abilities.
 */
public final class DragonRedstonePowerManager {
    private static final Map<Level, Map<UUID, Set<BlockPos>>> POWERED_WIRES = new WeakHashMap<>();

    private DragonRedstonePowerManager() {
    }

    public static void update(ServerLevel level, UUID source, Set<BlockPos> positions) {
        if (level == null || source == null) {
            return;
        }
        if (positions == null || positions.isEmpty()) {
            clear(level, source);
            return;
        }

        Set<BlockPos> storedPositions = new HashSet<>();
        for (BlockPos pos : positions) {
            if (pos != null) {
                storedPositions.add(pos.immutable());
            }
        }
        if (storedPositions.isEmpty()) {
            clear(level, source);
            return;
        }

        POWERED_WIRES.computeIfAbsent(level, ignored -> new HashMap<>())
                .put(source, storedPositions);
    }

    public static void clear(ServerLevel level, UUID source) {
        if (level == null || source == null) {
            return;
        }
        Map<UUID, Set<BlockPos>> levelSources = POWERED_WIRES.get(level);
        if (levelSources == null) {
            return;
        }
        levelSources.remove(source);
        if (levelSources.isEmpty()) {
            POWERED_WIRES.remove(level);
        }
    }

    public static boolean isBeamPowered(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        Map<UUID, Set<BlockPos>> levelSources = POWERED_WIRES.get(level);
        if (levelSources == null) {
            return false;
        }
        for (Set<BlockPos> positions : levelSources.values()) {
            if (positions.contains(pos)) {
                return true;
            }
        }
        return false;
    }
}
