package com.leon.saintsdragons.server.entity.variant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leon.saintsdragons.common.SaintsDragonsCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DragonVariantReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final DragonVariantReloadListener INSTANCE = new DragonVariantReloadListener();

    private DragonVariantReloadListener() {
        super(GSON, "dragon_variants");
    }

    public static DragonVariantReloadListener getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, List<DragonVariantDefinition>> parsed = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            try {
                JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), fileId.toString());
                ResourceLocation dragonId = root.has("dragon")
                        ? parseVariantId(fileId.getNamespace(), GsonHelper.getAsString(root, "dragon"))
                        : fileId;
                JsonArray variants = GsonHelper.getAsJsonArray(root, "variants");
                for (JsonElement element : variants) {
                    DragonVariantDefinition definition = parseVariant(fileId,
                            dragonId, GsonHelper.convertToJsonObject(element, fileId + " variant"));
                    parsed.computeIfAbsent(dragonId, ignored -> new ArrayList<>()).add(definition);
                }
            } catch (Exception exception) {
                SaintsDragonsCommon.LOGGER.error("Failed to parse dragon variant file {}", fileId, exception);
            }
        }
        SaintsDragonVariantRegistry.replaceDatapackVariants(parsed);
    }

    private static DragonVariantDefinition parseVariant(ResourceLocation fileId,
                                                         ResourceLocation dragonId, JsonObject input) {
        String name = GsonHelper.getAsString(input, "name");
        ResourceLocation variantId = input.has("id")
                ? ResourceLocation.parse(GsonHelper.getAsString(input, "id"))
                : parseVariantId(fileId.getNamespace(), name);
        int weight = GsonHelper.getAsInt(input, "weight");
        DragonVariantDefinition.BiomeRestrictions allowed = parseBiomes(input, "allowed_biomes");
        DragonVariantDefinition.BiomeRestrictions banned = parseBiomes(input, "banned_biomes");
        DragonVariantDefinition.AltitudeRestriction altitude = parseAltitude(input);
        return new DragonVariantDefinition(variantId, dragonId, name, weight,
                DragonVariantDefinition.NO_LEGACY_ID, allowed, banned, altitude);
    }

    private static ResourceLocation parseVariantId(String namespace, String name) {
        return name.indexOf(':') >= 0 ? ResourceLocation.parse(name)
                : ResourceLocation.fromNamespaceAndPath(namespace, name);
    }

    private static DragonVariantDefinition.BiomeRestrictions parseBiomes(JsonObject input, String key) {
        if (!input.has(key)) return null;
        JsonObject object = GsonHelper.getAsJsonObject(input, key);
        return new DragonVariantDefinition.BiomeRestrictions(
                parseResourceList(object, "biome"), parseResourceList(object, "tag"));
    }

    private static List<ResourceLocation> parseResourceList(JsonObject object, String key) {
        List<ResourceLocation> result = new ArrayList<>();
        if (object.has(key)) {
            JsonArray array = GsonHelper.getAsJsonArray(object, key);
            for (JsonElement element : array) result.add(ResourceLocation.parse(element.getAsString()));
        }
        return result;
    }

    private static DragonVariantDefinition.AltitudeRestriction parseAltitude(JsonObject input) {
        if (!input.has("altitude")) return DragonVariantDefinition.AltitudeRestriction.ANY;
        JsonObject object = GsonHelper.getAsJsonObject(input, "altitude");
        int min = object.has("min") ? GsonHelper.getAsInt(object, "min") : Integer.MIN_VALUE;
        int max = object.has("max") ? GsonHelper.getAsInt(object, "max") : Integer.MAX_VALUE;
        if (min > max) throw new IllegalArgumentException("Altitude minimum must not exceed maximum");
        return new DragonVariantDefinition.AltitudeRestriction(min, max);
    }
}
