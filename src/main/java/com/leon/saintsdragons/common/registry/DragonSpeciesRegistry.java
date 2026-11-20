package com.leon.saintsdragons.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Central lookup for dragon species metadata.
 *
 * <p>This is a dependency-light NeoForge port of the species registry from
 * Saint's Dragons 1.20.1 by LilRicefield. The target's DragonEntity hierarchy
 * is not yet present, so the entity supplier intentionally remains typed to
 * EntityType<?> until that hierarchy is migrated.</p>
 *
 * <p>Upstream reference:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/common/registry/DragonSpeciesRegistry.java
 * </p>
 */
public final class DragonSpeciesRegistry {
    private static final Map<ResourceLocation, Definition> ENTRIES = new LinkedHashMap<>();

    private DragonSpeciesRegistry() {
    }

    public static Definition register(ResourceLocation id,
                                      Supplier<? extends EntityType<?>> entityType) {
        return register(id, id, entityType);
    }

    public static synchronized Definition register(ResourceLocation id,
                                                   ResourceLocation attributesId,
                                                   Supplier<? extends EntityType<?>> entityType) {
        Definition definition = new Definition(id, attributesId, entityType);
        if (ENTRIES.putIfAbsent(id, definition) != null) {
            throw new IllegalArgumentException("Duplicate dragon species: " + id);
        }
        return definition;
    }

    @Nullable
    public static synchronized Definition get(ResourceLocation id) {
        return ENTRIES.get(id);
    }

    public static synchronized List<Definition> getAll() {
        return List.copyOf(ENTRIES.values());
    }

    /**
     * Resolves the registered species key from an entity's runtime type.
     * Returns null when the entity type has not been registered in Minecraft's
     * built-in entity registry.
     */
    @Nullable
    public static ResourceLocation speciesId(EntityType<?> entityType) {
        Objects.requireNonNull(entityType, "entityType");
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    }

    public record Definition(ResourceLocation id,
                             ResourceLocation attributesId,
                             Supplier<? extends EntityType<?>> entityType) {
        public Definition {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(attributesId, "attributesId");
            Objects.requireNonNull(entityType, "entityType");
        }
    }
}
