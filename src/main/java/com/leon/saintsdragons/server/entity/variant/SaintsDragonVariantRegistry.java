package com.leon.saintsdragons.server.entity.variant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SaintsDragonVariantRegistry {
    public static final String SAINTS_DRAGONS = SaintsDragonsCommon.MOD_ID;
    public static final ResourceLocation DEFAULT_VARIANT_ID = SaintsDragonsCommon.rl("default");

    private static volatile Map<ResourceLocation, List<DragonVariantDefinition>> defaults = buildDefaults();
    private static Map<ResourceLocation, List<DragonVariantDefinition>> datapackVariants = Map.of();
    private static volatile Map<ResourceLocation, List<DragonVariantDefinition>> variantsByDragon = defaults;
    private static volatile Map<ResourceLocation, DragonVariantDefinition> variantsById = index(defaults);

    private SaintsDragonVariantRegistry() {
    }

    public static void bootstrap() {
    }

    public static synchronized void registerDefaults(ResourceLocation dragonId, List<DragonVariantDefinition> definitions) {
        Objects.requireNonNull(dragonId, "dragonId");
        if (defaults.containsKey(dragonId)) {
            throw new IllegalArgumentException("Duplicate dragon variant defaults: " + dragonId);
        }
        Map<ResourceLocation, DragonVariantDefinition> byId = new LinkedHashMap<>();
        for (DragonVariantDefinition definition : List.copyOf(definitions)) {
            if (!dragonId.equals(definition.dragon())) {
                throw new IllegalArgumentException("Variant belongs to a different species: " + definition.id());
            }
            if (byId.putIfAbsent(definition.id(), definition) != null) {
                throw new IllegalArgumentException("Duplicate dragon variant: " + definition.id());
            }
        }
        byId.putIfAbsent(DEFAULT_VARIANT_ID, defaultDefinition(dragonId));
        Map<ResourceLocation, List<DragonVariantDefinition>> updated = new LinkedHashMap<>(defaults);
        updated.put(dragonId, List.copyOf(byId.values()));
        defaults = ImmutableMap.copyOf(updated);
        rebuildVariants();
    }

    public static ResourceLocation defaultVariantId(ResourceLocation dragonId) {
        return DEFAULT_VARIANT_ID;
    }

    public static ResourceLocation legacyToVariantId(ResourceLocation dragonId, int legacyId) {
        for (DragonVariantDefinition definition : getVariants(dragonId)) {
            if (definition.legacyId() == legacyId) {
                return definition.id();
            }
        }
        return defaultVariantId(dragonId);
    }

    public static int variantIdToLegacy(ResourceLocation dragonId, ResourceLocation variantId) {
        DragonVariantDefinition definition = get(dragonId, variantId);
        return definition != null && definition.dragon().equals(dragonId) && definition.hasLegacyId()
                ? definition.legacyId() : 0;
    }

    public static boolean isLegacyVariant(ResourceLocation dragonId, ResourceLocation variantId) {
        DragonVariantDefinition definition = get(dragonId, variantId);
        return definition != null && definition.dragon().equals(dragonId) && definition.hasLegacyId();
    }

    public static boolean isDefault(ResourceLocation variantId) {
        return DEFAULT_VARIANT_ID.equals(variantId);
    }

    public static DragonVariantDefinition get(ResourceLocation variantId) {
        return variantsById.get(variantId);
    }

    public static DragonVariantDefinition get(ResourceLocation dragonId, ResourceLocation variantId) {
        for (DragonVariantDefinition definition : getVariants(dragonId)) {
            if (definition.id().equals(variantId)) return definition;
        }
        return null;
    }

    public static List<DragonVariantDefinition> getVariants(ResourceLocation dragonId) {
        List<DragonVariantDefinition> variants = variantsByDragon.get(dragonId);
        return variants != null ? variants : List.of(defaultDefinition(dragonId));
    }

    public static boolean hasDefaults(ResourceLocation dragonId) {
        return defaults.containsKey(dragonId);
    }

    private static DragonVariantDefinition defaultDefinition(ResourceLocation dragonId) {
        return new DragonVariantDefinition(DEFAULT_VARIANT_ID, dragonId, "default", 100, 0,
                null, null, DragonVariantDefinition.AltitudeRestriction.ANY);
    }

    public static Map<String, Integer> legacyNameMap(ResourceLocation dragonId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (DragonVariantDefinition definition : getVariants(dragonId)) {
            if (definition.hasLegacyId()) result.put(definition.name(), definition.legacyId());
        }
        result.putIfAbsent("default", 0);
        return result;
    }

    public static Map<String, ResourceLocation> variantNameMap(ResourceLocation dragonId) {
        Map<String, ResourceLocation> result = new LinkedHashMap<>();
        for (DragonVariantDefinition definition : getVariants(dragonId)) {
            result.put(definition.name(), definition.id());
            result.put(definition.id().toString(), definition.id());
        }
        return result;
    }

    public static List<String> commandSuggestions(ResourceLocation dragonId) {
        List<String> result = new ArrayList<>();
        for (DragonVariantDefinition definition : getVariants(dragonId)) {
            result.add(SAINTS_DRAGONS.equals(definition.id().getNamespace())
                    ? definition.name() : definition.id().toString());
        }
        return result;
    }

    public static ResourceLocation chooseSpawnVariant(ServerLevelAccessor levelAccessor,
                                                        net.minecraft.world.entity.Entity entity,
                                                        ResourceLocation dragonId) {
        List<DragonVariantDefinition> allowed = new ArrayList<>();
        BlockPos pos = entity.blockPosition();
        for (DragonVariantDefinition definition : getVariants(dragonId)) {
            if (definition.weight() <= 0 || !definition.altitudeRestriction().allows(pos.getY())) continue;
            if (definition.hasBannedBiomes() && matchesBiome(definition.bannedBiomes(), levelAccessor, pos)) continue;
            if (!definition.hasAllowedBiomes() || matchesBiome(definition.allowedBiomes(), levelAccessor, pos)) {
                allowed.add(definition);
            }
        }
        return roll(entity.getRandom(), allowed, defaultVariantId(dragonId));
    }

    public static ResourceLocation normalize(ResourceLocation dragonId, @Nullable ResourceLocation variantId) {
        if (variantId == null) return defaultVariantId(dragonId);
        DragonVariantDefinition definition = get(dragonId, variantId);
        if (definition == null || definition.dragon().equals(dragonId)) return variantId;
        return defaultVariantId(dragonId);
    }

    public static synchronized void replaceDatapackVariants(
            Map<ResourceLocation, List<DragonVariantDefinition>> replacements) {
        Map<ResourceLocation, List<DragonVariantDefinition>> copied = new LinkedHashMap<>();
        replacements.forEach((id, definitions) -> {
            for (DragonVariantDefinition definition : definitions) {
                if (!id.equals(definition.dragon())) {
                    throw new IllegalArgumentException("Variant belongs to a different species: " + definition.id());
                }
            }
            copied.put(id, List.copyOf(definitions));
        });
        datapackVariants = ImmutableMap.copyOf(copied);
        rebuildVariants();
    }

    private static void rebuildVariants() {
        Map<ResourceLocation, LinkedHashMap<ResourceLocation, DragonVariantDefinition>> merged = new LinkedHashMap<>();
        defaults.forEach((id, definitions) -> {
            LinkedHashMap<ResourceLocation, DragonVariantDefinition> byId = new LinkedHashMap<>();
            definitions.forEach(definition -> byId.put(definition.id(), definition));
            merged.put(id, byId);
        });
        datapackVariants.forEach((id, definitions) -> {
            LinkedHashMap<ResourceLocation, DragonVariantDefinition> byId = merged.computeIfAbsent(id, key -> {
                LinkedHashMap<ResourceLocation, DragonVariantDefinition> values = new LinkedHashMap<>();
                values.put(DEFAULT_VARIANT_ID, defaultDefinition(key));
                return values;
            });
            for (DragonVariantDefinition definition : definitions) {
                byId.put(definition.id(), preserveLegacyId(byId.get(definition.id()), definition));
            }
        });
        Map<ResourceLocation, List<DragonVariantDefinition>> next = new LinkedHashMap<>();
        merged.forEach((id, definitions) -> next.put(id, ImmutableList.copyOf(definitions.values())));
        variantsByDragon = ImmutableMap.copyOf(next);
        variantsById = index(variantsByDragon);
        SaintsDragonsCommon.LOGGER.info("Loaded {} dragon variant definition(s)", variantsById.size());
    }

    public static ResourceLocation adultTexture(ResourceLocation dragonId, ResourceLocation variantId, boolean female) {
        String suffix = female ? "_female" : "";
        return ResourceLocation.fromNamespaceAndPath(variantId.getNamespace(),
                "textures/entity/" + dragonId.getPath() + "/" + variantId.getPath() + suffix + ".png");
    }

    private static ResourceLocation roll(RandomSource random, List<DragonVariantDefinition> variants,
                                          ResourceLocation fallback) {
        long total = 0;
        for (DragonVariantDefinition definition : variants) total += definition.weight();
        if (total <= 0) return fallback;
        if (total > Integer.MAX_VALUE) {
            throw new IllegalStateException("Dragon variant weights exceed the supported random bound");
        }
        int roll = random.nextInt((int) total);
        for (DragonVariantDefinition definition : variants) {
            roll -= definition.weight();
            if (roll < 0) return definition.id();
        }
        return fallback;
    }

    private static DragonVariantDefinition preserveLegacyId(@Nullable DragonVariantDefinition existing,
                                                              DragonVariantDefinition replacement) {
        if (existing == null || replacement.hasLegacyId() || !existing.hasLegacyId()) return replacement;
        return new DragonVariantDefinition(replacement.id(), replacement.dragon(), replacement.name(),
                replacement.weight(), existing.legacyId(), replacement.allowedBiomes(),
                replacement.bannedBiomes(), replacement.altitudeRestriction());
    }

    private static boolean matchesBiome(@Nullable DragonVariantDefinition.BiomeRestrictions restrictions,
                                        LevelAccessor level, BlockPos pos) {
        if (restrictions == null || !restrictions.hasAny()) return false;
        Holder<Biome> biome = level.getBiome(pos);
        if (restrictions.hasBiomesByIdList()) {
            ResourceLocation biomeId = biome.unwrapKey().map(key -> key.location()).orElse(null);
            if (biomeId != null && restrictions.biomesById().contains(biomeId)) return true;
        }
        if (restrictions.hasBiomesByTagList()) {
            for (ResourceLocation tagId : restrictions.biomesByTag()) {
                if (biome.is(TagKey.create(Registries.BIOME, tagId))) return true;
            }
        }
        return false;
    }

    private static Map<ResourceLocation, DragonVariantDefinition> index(
            Map<ResourceLocation, List<DragonVariantDefinition>> variants) {
        Map<ResourceLocation, DragonVariantDefinition> result = new LinkedHashMap<>();
        variants.values().forEach(definitions -> definitions.forEach(definition ->
                result.put(definition.id(), definition)));
        return ImmutableMap.copyOf(result);
    }

    private static Map<ResourceLocation, List<DragonVariantDefinition>> buildDefaults() {
        Map<ResourceLocation, List<DragonVariantDefinition>> result = new LinkedHashMap<>();
        add(result, "raevyx", 0, "default", 90); add(result, "raevyx", 1, "night_gold", 10);
        add(result, "cindervane", 0, "default", 85); add(result, "cindervane", 1, "albino", 15);
        add(result, "cindervane", 2, "piebald", 0); add(result, "ignivorus", 0, "default", 95);
        add(result, "ignivorus", 1, "crimson", 5); add(result, "volitans", 0, "default", 85);
        add(result, "volitans", 1, "bloodshot", 15); add(result, "varasuchus", 0, "default", 100);
        addCustom(result, "varasuchus", "void_kissed", 0); add(result, "stegonaut", 0, "default", 100);
        add(result, "nulljaw", 0, "default", 100); add(result, "atroxiia", 0, "default", 100);
        Map<ResourceLocation, List<DragonVariantDefinition>> immutable = new LinkedHashMap<>();
        result.forEach((id, definitions) -> immutable.put(id, ImmutableList.copyOf(definitions)));
        return ImmutableMap.copyOf(immutable);
    }

    private static void add(Map<ResourceLocation, List<DragonVariantDefinition>> map,
                            String dragon, int legacyId, String name, int weight) {
        ResourceLocation dragonId = SaintsDragonsCommon.rl(dragon);
        map.computeIfAbsent(dragonId, ignored -> new ArrayList<>()).add(new DragonVariantDefinition(
                SaintsDragonsCommon.rl(name), dragonId, name, weight, legacyId, null, null,
                DragonVariantDefinition.AltitudeRestriction.ANY));
    }

    private static void addCustom(Map<ResourceLocation, List<DragonVariantDefinition>> map,
                                  String dragon, String name, int weight) {
        ResourceLocation dragonId = SaintsDragonsCommon.rl(dragon);
        map.computeIfAbsent(dragonId, ignored -> new ArrayList<>()).add(new DragonVariantDefinition(
                SaintsDragonsCommon.rl(name), dragonId, name, weight, DragonVariantDefinition.NO_LEGACY_ID,
                null, null, DragonVariantDefinition.AltitudeRestriction.ANY));
    }
}
