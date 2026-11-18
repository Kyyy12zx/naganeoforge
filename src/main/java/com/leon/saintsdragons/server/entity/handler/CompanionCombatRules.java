package com.leon.saintsdragons.server.entity.handler;

import com.leon.saintsdragons.server.data.GlobalDragonAllySavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Shared trust checks for players and owned companions. */
public final class CompanionCombatRules {
    private CompanionCombatRules() {
    }

    public static boolean isTrusted(@Nullable Entity entity, @Nullable UUID ownerUuid, @Nullable Level level) {
        if (entity == null || ownerUuid == null || level == null || entity.isRemoved()) {
            return false;
        }
        if (entity instanceof Player player) {
            return isTrustedUuid(player.getUUID(), ownerUuid, level);
        }
        UUID companionOwnerUuid = getCompanionOwnerUuid(entity);
        return companionOwnerUuid != null && isTrustedUuid(companionOwnerUuid, ownerUuid, level);
    }

    @Nullable
    private static UUID getCompanionOwnerUuid(Entity entity) {
        if (entity instanceof TamableAnimal tamable && !tamable.isTame()) {
            return null;
        }
        return entity instanceof OwnableEntity ownable ? ownable.getOwnerUUID() : null;
    }

    private static boolean isTrustedUuid(UUID candidateUuid, UUID ownerUuid, Level level) {
        if (candidateUuid == null || ownerUuid == null || level == null) return false;
        if (ownerUuid.equals(candidateUuid)) return true;
        return level instanceof ServerLevel serverLevel
                && GlobalDragonAllySavedData.get(serverLevel).isAlly(ownerUuid, candidateUuid);
    }
}
