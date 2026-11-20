package com.leon.saintsdragons.common.registry;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.server.entity.ability.DragonAbility;
import com.leon.saintsdragons.server.entity.ability.DragonAbilityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for the mod's ability types.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/common/registry/AbilityRegistry.java
 */
public final class AbilityRegistry {
    private static final Map<ResourceLocation, DragonAbilityType<?, ?>> BY_NAME = new HashMap<>();
    private static final Map<DragonAbilityType<?, ?>, ResourceLocation> BY_TYPE = new IdentityHashMap<>();

    private AbilityRegistry() {
    }

    public static synchronized <M extends LivingEntity, T extends DragonAbility<M>>
    DragonAbilityType<M, T> register(DragonAbilityType<M, T> type) {
        Objects.requireNonNull(type, "type");
        return register(type.getName(), type);
    }

    public static synchronized <M extends LivingEntity, T extends DragonAbility<M>>
    DragonAbilityType<M, T> register(String name, DragonAbilityType<M, T> type) {
        Objects.requireNonNull(type, "type");
        ResourceLocation key = resolveKey(name);
        if (key == null) {
            throw new IllegalArgumentException("Ability name must be a valid resource location: " + name);
        }

        DragonAbilityType<?, ?> existing = BY_NAME.get(key);
        if (existing != null && existing != type) {
            throw new IllegalStateException("Duplicate ability name: " + key);
        }

        ResourceLocation registeredKey = BY_TYPE.get(type);
        if (registeredKey != null && !registeredKey.equals(key)) {
            throw new IllegalStateException(
                    "Ability type is already registered as " + registeredKey + " and cannot also be registered as " + key
            );
        }

        BY_NAME.putIfAbsent(key, type);
        BY_TYPE.putIfAbsent(type, key);
        return type;
    }

    public static synchronized DragonAbilityType<?, ?> get(String name) {
        ResourceLocation key = resolveKey(name);
        return key == null ? null : BY_NAME.get(key);
    }

    public static synchronized String getName(DragonAbilityType<?, ?> type) {
        if (type == null) {
            return null;
        }
        ResourceLocation key = BY_TYPE.get(type);
        return key != null ? key.toString() : null;
    }

    private static ResourceLocation resolveKey(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.indexOf(':') >= 0) {
            return ResourceLocation.tryParse(name);
        }
        return ResourceLocation.fromNamespaceAndPath(SaintsDragonsCommon.MOD_ID, name);
    }
}
