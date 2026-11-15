package com.leon.saintsdragons.server.entity.part;

import net.minecraft.world.entity.Entity;

/** Exposes the multipart entities owned by a dragon entity. */
public interface DragonPartProvider {
    Entity[] dragonParts();

    void updateDragonParts();
}
