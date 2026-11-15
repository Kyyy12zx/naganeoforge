package com.leon.saintsdragons.server.entity.dragons.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Shared dragon block-griefing checks.
 *
 * The config value is supplied by the platform/config layer so this class
 * remains independent of Forge and NeoForge configuration APIs.
 */
public final class DragonGriefingRules {
    private DragonGriefingRules() {
    }

    public static boolean canDestroyBlocks(Level level, boolean configEnabled) {
        return level != null
                && configEnabled
                && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public static boolean canSetBlocksOnFire(Level level, boolean griefingEnabled,
                                             boolean fireIgnitionEnabled) {
        return canDestroyBlocks(level, griefingEnabled) && fireIgnitionEnabled;
    }

    public static boolean isProtectedFromPassiveTreeDestruction(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return true;
        }
        for (Structure structure : level.structureManager().getAllStructuresAt(pos).keySet()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structure);
            if (start != null && start.isValid()) {
                return true;
            }
        }
        return false;
    }
}
