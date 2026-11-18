package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent per-player ally registry shared across all dragons. */
public class GlobalDragonAllySavedData extends SavedData {
    private static final String DATA_NAME = "saintsdragons_global_allies";
    private static final SavedData.Factory<GlobalDragonAllySavedData> FACTORY =
            new SavedData.Factory<>(GlobalDragonAllySavedData::new, GlobalDragonAllySavedData::load, null);

    private final Map<UUID, Map<UUID, String>> alliesByOwner = new HashMap<>();

    public static GlobalDragonAllySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Map<UUID, String> getAllies(UUID ownerId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        return allies == null ? Map.of() : new HashMap<>(allies);
    }

    public boolean isAlly(UUID ownerId, UUID allyId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        return allies != null && allies.containsKey(allyId);
    }

    public int getAllyCount(UUID ownerId) {
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        return allies == null ? 0 : allies.size();
    }

    public void addAlly(UUID ownerId, UUID allyId, String username) {
        if (ownerId == null || allyId == null || ownerId.equals(allyId) || username == null) {
            return;
        }
        alliesByOwner.computeIfAbsent(ownerId, id -> new HashMap<>()).put(allyId, username);
        setDirty();
    }

    public boolean removeAlly(UUID ownerId, UUID allyId) {
        if (ownerId == null || allyId == null) return false;
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        if (allies == null) return false;
        boolean removed = allies.remove(allyId) != null;
        if (removed) setDirty();
        if (allies.isEmpty()) alliesByOwner.remove(ownerId);
        return removed;
    }

    public void clearAllies(UUID ownerId) {
        if (ownerId == null) return;
        Map<UUID, String> allies = alliesByOwner.get(ownerId);
        if (allies != null && !allies.isEmpty()) {
            allies.clear();
            alliesByOwner.remove(ownerId);
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, Map<UUID, String>> entry : alliesByOwner.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Owner", entry.getKey());
            ListTag allyList = new ListTag();
            for (Map.Entry<UUID, String> allyEntry : entry.getValue().entrySet()) {
                CompoundTag allyTag = new CompoundTag();
                allyTag.putUUID("UUID", allyEntry.getKey());
                allyTag.putString("Username", allyEntry.getValue());
                allyList.add(allyTag);
            }
            playerTag.put("Allies", allyList);
            playerList.add(playerTag);
        }
        tag.put("Players", playerList);
        return tag;
    }

    public static GlobalDragonAllySavedData load(CompoundTag tag) {
        GlobalDragonAllySavedData data = new GlobalDragonAllySavedData();
        if (tag.contains("Players", Tag.TAG_LIST)) {
            ListTag playerList = tag.getList("Players", Tag.TAG_COMPOUND);
            for (int i = 0; i < playerList.size(); i++) {
                CompoundTag playerTag = playerList.getCompound(i);
                if (!playerTag.hasUUID("Owner")) continue;
                UUID ownerId = playerTag.getUUID("Owner");
                Map<UUID, String> allies = new HashMap<>();
                if (playerTag.contains("Allies", Tag.TAG_LIST)) {
                    ListTag allyList = playerTag.getList("Allies", Tag.TAG_COMPOUND);
                    for (int j = 0; j < allyList.size(); j++) {
                        CompoundTag allyTag = allyList.getCompound(j);
                        if (allyTag.hasUUID("UUID") && allyTag.contains("Username", Tag.TAG_STRING)) {
                            UUID allyId = allyTag.getUUID("UUID");
                            String username = allyTag.getString("Username");
                            if (!ownerId.equals(allyId) && !username.isBlank()) allies.put(allyId, username);
                        }
                    }
                }
                if (!allies.isEmpty()) data.alliesByOwner.put(ownerId, allies);
            }
        }
        return data;
    }
}
