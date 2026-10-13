package com.twistedteam.twistedlands;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TwistedLandsSpawns {

    private static final int APPLE_COW_WEIGHT = 12;
    private static final int GOLDEN_APPLE_COW_WEIGHT = 2;
    private static final int ENCHANTED_APPLE_COW_WEIGHT = 1;

    private static final int COMMON_GROUP_MIN = 2;
    private static final int COMMON_GROUP_MAX = 4;
    private static final int RARE_GROUP_MIN = 1;
    private static final int RARE_GROUP_MAX = 2;

    private TwistedLandsSpawns() {}

    public static void initialize() {
        registerSpawnRules();

        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.CREATURE,
                TwistedLandsEntities.APPLE_COW, APPLE_COW_WEIGHT,
                COMMON_GROUP_MIN, COMMON_GROUP_MAX);

        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.CREATURE,
                TwistedLandsEntities.GOLDEN_APPLE_COW, GOLDEN_APPLE_COW_WEIGHT,
                RARE_GROUP_MIN, RARE_GROUP_MAX);

        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.CREATURE,
                TwistedLandsEntities.ENCHANTED_GOLDEN_APPLE_COW, ENCHANTED_APPLE_COW_WEIGHT,
                RARE_GROUP_MIN, RARE_GROUP_MAX);
    }

    private static void registerSpawnRules() {
        SpawnPlacements.register(TwistedLandsEntities.APPLE_COW, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);

        SpawnPlacements.register(TwistedLandsEntities.GOLDEN_APPLE_COW, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);

        SpawnPlacements.register(TwistedLandsEntities.ENCHANTED_GOLDEN_APPLE_COW, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
    }
}
