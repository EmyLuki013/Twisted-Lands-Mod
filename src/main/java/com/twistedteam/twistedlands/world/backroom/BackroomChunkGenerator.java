package com.twistedteam.twistedlands.world.backroom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Gera o labirinto do Level 0 escrevendo direto na seção do chunk, na etapa de noise. No Bedrock o script
 * rodava de 2 a 4 comandos fill/setblock por coluna (mais de 500 por chunk) depois do chunk já existir.
 */
public class BackroomChunkGenerator extends ChunkGenerator {
    // O codec liga o "type": "twisted:backroom" do data/twisted/dimension/backroom.json a esta classe.
    public static final Codec<BackroomChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
    ).apply(instance, instance.stable(BackroomChunkGenerator::new)));

    private static final int MIN_Y = 0;
    private static final int HEIGHT = 64;

    public BackroomChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // Chão (y32) e teto (y37) caem na mesma seção (y32 a 47), então basta pegar uma.
        // acquire/release é o lock da seção, o NoiseBasedChunkGenerator do vanilla faz igual.
        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(BackroomMaze.FLOOR_Y));
        section.acquire();
        try {
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int y = BackroomMaze.FLOOR_Y; y <= BackroomMaze.CEILING_Y; y++) {
                        BlockState state = BackroomMaze.stateAt(minX + lx, y, minZ + lz);
                        if (state.isAir()) {
                            continue;
                        }
                        section.setBlockState(lx, y & 15, lz, state, false);
                        // Escrevendo direto na seção, os heightmaps de worldgen não se atualizam sozinhos.
                        oceanFloor.update(lx, y, lz, state);
                        worldSurface.update(lx, y, lz, state);
                    }
                }
            }
        } finally {
            section.release();
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);
        BackroomMaze.placeExit(level, chunk, BackroomMaze.FLOOR_Y);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        for (int y = BackroomMaze.CEILING_Y; y >= BackroomMaze.FLOOR_Y; y--) {
            if (type.isOpaque().test(BackroomMaze.stateAt(x, y, z))) {
                return y + 1;
            }
        }
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] states = new BlockState[level.getHeight()];
        for (int i = 0; i < states.length; i++) {
            states[i] = BackroomMaze.stateAt(x, level.getMinBuildHeight() + i, z);
        }
        return new NoiseColumn(level.getMinBuildHeight(), states);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return MIN_Y;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
    }
}
