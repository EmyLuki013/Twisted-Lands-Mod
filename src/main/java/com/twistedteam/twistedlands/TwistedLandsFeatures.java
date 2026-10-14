package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.world.dream.CrystalTrunkPlacer;
import com.twistedteam.twistedlands.world.feature.GroundStructure;
import com.twistedteam.twistedlands.world.feature.SpacingPlacement;
import com.twistedteam.twistedlands.world.feature.TemplateFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class TwistedLandsFeatures {
    public static final Feature<TemplateFeature.Config> TEMPLATE =
            Registry.register(BuiltInRegistries.FEATURE, TwistedLands.id("template"), new TemplateFeature());

    public static final PlacementModifierType<SpacingPlacement> SPACING =
            Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, TwistedLands.id("spacing"), () -> SpacingPlacement.CODEC);

    public static final TrunkPlacerType<CrystalTrunkPlacer> CRYSTAL_TRUNK_PLACER = Registry.register(
            BuiltInRegistries.TRUNK_PLACER_TYPE, TwistedLands.id("crystal_trunk_placer"), new TrunkPlacerType<>(CrystalTrunkPlacer.CODEC));

    public static final StructureType<GroundStructure> GROUND_STRUCTURE =
            Registry.register(BuiltInRegistries.STRUCTURE_TYPE, TwistedLands.id("ground"), () -> GroundStructure.CODEC);

    public static final StructurePieceType GROUND_STRUCTURE_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
            TwistedLands.id("ground"), (StructurePieceType) GroundStructure.Piece::new);

    public static void initialize() {
        // Minério de sal no overworld inteiro e pés de tomate nas planícies, como as feature rules do addon.
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
                placed("salt_ore"));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION, placed("tomato_plant_patch"));
    }

    private static ResourceKey<PlacedFeature> placed(String id) {
        return ResourceKey.create(Registries.PLACED_FEATURE, TwistedLands.id(id));
    }
}
