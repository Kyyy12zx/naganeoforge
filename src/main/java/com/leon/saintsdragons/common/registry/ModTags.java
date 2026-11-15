package com.leon.saintsdragons.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/** Central datapack tag keys used by shared gameplay code. */
public final class ModTags {
    private static final String MOD_ID = "saintsdragons";

    private ModTags() {
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> DRAGONS = entity("dragons");
        public static final TagKey<EntityType<?>> RIDEABLE_DRAGONS = entity("rideable_dragons");
        public static final TagKey<EntityType<?>> TAMEABLE_DRAGONS = entity("tameable_dragons");
        public static final TagKey<EntityType<?>> FLYING_DRAGONS = entity("flying_dragons");
        public static final TagKey<EntityType<?>> GROUNDED_DRAGONS = entity("grounded_dragons");
        public static final TagKey<EntityType<?>> SWIMMING_DRAGONS = entity("swimming_dragons");
        public static final TagKey<EntityType<?>> CARNIVORE_HUNT_PREY = entity("hunt_prey/carnivore");
        public static final TagKey<EntityType<?>> PISCIVORE_HUNT_PREY = entity("hunt_prey/piscivore");
        public static final TagKey<EntityType<?>> CINDERVANE_HUNT_PREY = entity("hunt_prey/cindervane");
        public static final TagKey<EntityType<?>> IGNIVORUS_HUNT_PREY = entity("hunt_prey/ignivorus");
        public static final TagKey<EntityType<?>> RAEVYX_HUNT_PREY = entity("hunt_prey/raevyx");
        public static final TagKey<EntityType<?>> VARASUCHUS_HUNT_PREY = entity("hunt_prey/varasuchus");
        public static final TagKey<EntityType<?>> VOLITANS_HUNT_PREY = entity("hunt_prey/volitans");
        public static final TagKey<EntityType<?>> IMMUNE_TO_ELECTRICITY = entity("immune_to/electricity");
        public static final TagKey<EntityType<?>> IMMUNE_TO_FIRE = entity("immune_to/fire");
        public static final TagKey<EntityType<?>> IMMUNE_TO_POISON = entity("immune_to/poison");

        private EntityTypes() {
        }

        private static TagKey<EntityType<?>> entity(String path) {
            return TagKey.create(Registries.ENTITY_TYPE, id(path));
        }
    }

    public static final class Items {
        public static final TagKey<Item> DRAGON_FOODS = item("dragon_foods");
        public static final TagKey<Item> DRAGON_EGGS = item("dragon_eggs");
        public static final TagKey<Item> ATROXIIA_EGGS = item("eggs/atroxiia");
        public static final TagKey<Item> CINDERVANE_EGGS = item("eggs/cindervane");
        public static final TagKey<Item> IGNIVORUS_EGGS = item("eggs/ignivorus");
        public static final TagKey<Item> RAEVYX_EGGS = item("eggs/raevyx");
        public static final TagKey<Item> STEGONAUT_EGGS = item("eggs/stegonaut");
        public static final TagKey<Item> VARASUCHUS_EGGS = item("eggs/varasuchus");
        public static final TagKey<Item> VOLITANS_EGGS = item("eggs/volitans");
        public static final TagKey<Item> DRAGON_BINDERS = item("dragon_binders");
        public static final TagKey<Item> DRAGON_BRUSHES = item("dragon_brushes");
        public static final TagKey<Item> DRAGON_PARTS = item("dragon_parts");
        public static final TagKey<Item> DRAGON_DROPS = item("dragon_drops");
        public static final TagKey<Item> DRAGON_SPAWN_EGGS = item("dragon_spawn_eggs");
        public static final TagKey<Item> WORLDROOT_TOOLS = item("worldroot_tools");
        public static final TagKey<Item> ATROXIIA_FOODS = item("foods/atroxiia");
        public static final TagKey<Item> CINDERVANE_FOODS = item("foods/cindervane");
        public static final TagKey<Item> IGNIVORUS_FOODS = item("foods/ignivorus");
        public static final TagKey<Item> NULLJAW_FOODS = item("foods/nulljaw");
        public static final TagKey<Item> RAEVYX_FOODS = item("foods/raevyx");
        public static final TagKey<Item> STEGONAUT_FOODS = item("foods/stegonaut");
        public static final TagKey<Item> VARASUCHUS_FOODS = item("foods/varasuchus");
        public static final TagKey<Item> VOLITANS_FOODS = item("foods/volitans");
        public static final TagKey<Item> DRACONIC_CRUCIBLE_FUEL_LEVEL_1 = item("draconic_crucible/fuels/level_1");
        public static final TagKey<Item> DRACONIC_CRUCIBLE_FUEL_LEVEL_2 = item("draconic_crucible/fuels/level_2");
        public static final TagKey<Item> DRACONIC_CRUCIBLE_FUEL_LEVEL_3 = item("draconic_crucible/fuels/level_3");
        public static final TagKey<Item> DRACONIC_CRUCIBLE_VANILLA_SMELTING_BLACKLIST = item("draconic_crucible/vanilla_smelting_blacklist");

        private Items() {
        }

        private static TagKey<Item> item(String path) {
            return TagKey.create(Registries.ITEM, id(path));
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> DRAGON_EGGS = block("dragon_eggs");
        public static final TagKey<Block> ATROXIIA_EGGS = block("eggs/atroxiia");
        public static final TagKey<Block> CINDERVANE_EGGS = block("eggs/cindervane");
        public static final TagKey<Block> IGNIVORUS_EGGS = block("eggs/ignivorus");
        public static final TagKey<Block> RAEVYX_EGGS = block("eggs/raevyx");
        public static final TagKey<Block> STEGONAUT_EGGS = block("eggs/stegonaut");
        public static final TagKey<Block> VARASUCHUS_EGGS = block("eggs/varasuchus");
        public static final TagKey<Block> VOLITANS_EGGS = block("eggs/volitans");

        private Blocks() {
        }

        private static TagKey<Block> block(String path) {
            return TagKey.create(Registries.BLOCK, id(path));
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> HAS_ATROXIIA = biome("has_atroxiia");
        public static final TagKey<Biome> HAS_CINDERVANE = biome("has_cindervane");
        public static final TagKey<Biome> HAS_IVY_HOUSE = biome("has_ivy_house");
        public static final TagKey<Biome> HAS_NULLJAW = biome("has_nulljaw");
        public static final TagKey<Biome> HAS_RAEVYX = biome("has_raevyx");
        public static final TagKey<Biome> HAS_STEGONAUT = biome("has_stegonaut");
        public static final TagKey<Biome> HAS_STEGONAUT_CAVES = biome("has_stegonaut_caves");
        public static final TagKey<Biome> HAS_VOLITANS = biome("has_volitans");
        public static final TagKey<Biome> HAS_VOLITANS_FALLBACK = biome("has_volitans_fallback");
        public static final TagKey<Biome> HAS_MOOP = biome("has_moop");
        public static final TagKey<Biome> HAS_MOSSBACK = biome("has_mossback");
        public static final TagKey<Biome> HAS_DRAGONHEART_ORE = biome("has_dragonheart_ore");
        public static final TagKey<Biome> HAS_IGNIVORUS_ROOST = biome("has_igni_roost");
        public static final TagKey<Biome> HAS_VARASUCHUS_ROOST = biome("has_varasuchus_roost");

        private Biomes() {
        }

        private static TagKey<Biome> biome(String path) {
            return TagKey.create(Registries.BIOME, id(path));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
