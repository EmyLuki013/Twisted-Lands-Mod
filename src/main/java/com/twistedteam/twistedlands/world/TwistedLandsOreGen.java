package com.twistedteam.twistedlands.world;

import com.twistedteam.twistedlands.TwistedLands;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TwistedLandsOreGen {

    public static final ResourceKey<PlacedFeature> AMETHYST_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation(TwistedLands.MOD_ID, "ore_amethyst"));

    public static final ResourceKey<PlacedFeature> RUBY_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation(TwistedLands.MOD_ID, "ore_ruby"));

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld()
                        .and(BiomeSelectors.excludeByKey(Biomes.DEEP_DARK)),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                AMETHYST_ORE_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld()
                        .and(BiomeSelectors.excludeByKey(Biomes.DEEP_DARK)),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                RUBY_ORE_PLACED
        );
    }
}