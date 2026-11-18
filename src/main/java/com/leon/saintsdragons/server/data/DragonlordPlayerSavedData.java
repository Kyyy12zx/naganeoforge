package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Persists player health while temporarily transformed into a Dragonlord. */
public class DragonlordPlayerSavedData extends SavedData {
    private static final String DATA_NAME = "saintsdragons_dragonlord_players";
    private static final SavedData.Factory<DragonlordPlayerSavedData> FACTORY =
            new SavedData.Factory<>(DragonlordPlayerSavedData::new, DragonlordPlayerSavedData::load, null);
    private final Map<UUID, Float> savedHealth = new HashMap<>();

    public static DragonlordPlayerSavedData get(ServerLevel level) {
        if (level == null || level.getServer() == null) {
            throw new IllegalArgumentException("A server level is required");
        }
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public void saveHealth(UUID playerId, float health) {
        if (playerId == null || !Float.isFinite(health) || health <= 0.0F) return;
        savedHealth.put(playerId, health);
        setDirty();
    }

    public Optional<Float> consumeHealth(UUID playerId) {
        if (playerId == null) return Optional.empty();
        Float health = savedHealth.remove(playerId);
        if (health != null) setDirty();
        return Optional.ofNullable(health);
    }

    public Optional<Float> getHealth(UUID playerId) {
        return playerId == null ? Optional.empty() : Optional.ofNullable(savedHealth.get(playerId));
    }

    public void clearHealth(UUID playerId) {
        if (playerId != null && savedHealth.remove(playerId) != null) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, Float> entry : savedHealth.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());
            playerTag.putFloat("Health", entry.getValue());
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public static DragonlordPlayerSavedData load(CompoundTag tag) {
        DragonlordPlayerSavedData data = new DragonlordPlayerSavedData();
        if (tag != null && tag.contains("Players", Tag.TAG_LIST)) {
            ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
            for (int i = 0; i < players.size(); i++) {
                CompoundTag playerTag = players.getCompound(i);
                if (playerTag.hasUUID("UUID") && playerTag.contains("Health", Tag.TAG_FLOAT)) {
                    float health = playerTag.getFloat("Health");
                    if (Float.isFinite(health) && health > 0.0F) data.savedHealth.put(playerTag.getUUID("UUID"), health);
                }
            }
        }
        return data;
    }
}