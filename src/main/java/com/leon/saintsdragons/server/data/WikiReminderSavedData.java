package com.leon.saintsdragons.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Tracks which players have already received the in-game wiki reminder. */
public class WikiReminderSavedData extends SavedData {
    private static final String DATA_NAME = "saintsdragons_wiki_reminders";
    private static final SavedData.Factory<WikiReminderSavedData> FACTORY =
            new SavedData.Factory<>(WikiReminderSavedData::new, WikiReminderSavedData::load, null);

    private final Set<UUID> shownPlayers = new HashSet<>();

    public static WikiReminderSavedData get(ServerLevel level) {
        if (level == null || level.getServer() == null) {
            throw new IllegalArgumentException("A server level is required to access wiki reminder data");
        }
        ServerLevel storageLevel = level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public boolean markShownIfFirst(UUID playerId) {
        if (playerId == null) return false;
        boolean added = shownPlayers.add(playerId);
        if (added) setDirty();
        return added;
    }

    public boolean hasBeenShown(UUID playerId) {
        return playerId != null && shownPlayers.contains(playerId);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        for (UUID playerId : shownPlayers) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", playerId);
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }

    public static WikiReminderSavedData load(CompoundTag tag) {
        WikiReminderSavedData data = new WikiReminderSavedData();
        if (tag != null && tag.contains("Players", Tag.TAG_LIST)) {
            ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
            for (int i = 0; i < players.size(); i++) {
                CompoundTag playerTag = players.getCompound(i);
                if (playerTag.hasUUID("UUID")) data.shownPlayers.add(playerTag.getUUID("UUID"));
            }
        }
        return data;
    }
}
