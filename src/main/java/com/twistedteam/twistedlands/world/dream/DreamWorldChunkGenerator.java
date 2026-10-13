package com.twistedteam.twistedlands.world.dream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Chão da Dream World, portado do TerrainGen do addon (relevo PLAINS). De baixo pra cima: bedrock em y31 a 33
 * (a dream_world_base_chunk), dream stone até y62, e a planície de y61 a y65 com grama da Dream World em cima de
 * terra, alguns pontos de cascalho e lagos rasos (todos em y61) com fundo de areia. As ilhas de cristal ficam no alto e não são
 * geradas aqui.
 */
public class DreamWorldChunkGenerator extends ChunkGenerator {
    public static final Codec<DreamWorldChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
    ).apply(instance, instance.stable(DreamWorldChunkGenerator::new)));

    private static final int MIN_Y = 0;
    private static final int HEIGHT = 256;
    private static final int BEDROCK_Y = 31;
    // Chão da planície no addon (baseY). O relevo sobe até 4 blocos a partir daqui.
    private static final int BASE_Y = 61;
    private static final int MAX_SURFACE_Y = BASE_Y + 4;

    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState STONE = TwistedLandsBlocks.DREAM_STONE.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState GRASS = TwistedLandsBlocks.DREAM_WORLD_GRASS_BLOCK.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private record Noises(RandomState owner, SimplexNoise terrain, SimplexNoise water, PositionalRandomFactory random) {
    }

    // Uma coluna da planície: y do bloco de cima, se é água e de que é o enchimento.
    private record Column(int top, boolean water, BlockState fill) {
    }

    private volatile Noises noises;

    public DreamWorldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, TwistedLands.id("dream_world"), CODEC);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // O RandomState carrega a seed do mundo. Os ruídos saem dele e ficam guardados até trocar de mundo.
    private Noises noises(RandomState randomState) {
        Noises current = noises;
        if (current == null || current.owner() != randomState) {
            PositionalRandomFactory factory = randomState.getOrCreateRandomFactory(TwistedLands.id("dream_world"));
            current = new Noises(randomState, new SimplexNoise(factory.fromHashOf("terrain")),
                    new SimplexNoise(factory.fromHashOf("water")), factory);
            noises = current;
        }
        return current;
    }

    // O OpenSimplex2 do FastNoiseLite varia mais que o SimplexNoise do Minecraft (desvio 0.41 contra 0.33).
    // Com essa escala a área de lago, de rio e de cada altura fica igual à do addon (medido em 1 milhão de colunas).
    private static final double NOISE_SCALE = 1.24;

    // FBm de 2 oitavas, como o FastNoiseLite do addon (ganho 0.5, lacunaridade 2).
    private static double fbm(SimplexNoise noise, double x, double z) {
        return (noise.getValue(x, z) + 0.5 * noise.getValue(x * 2, z * 2)) / 1.5 * NOISE_SCALE;
    }

    private static Column column(Noises noises, int x, int z) {
        double h = (fbm(noises.terrain(), x * 0.02, z * 0.02) + 1) / 2;
        int height = Math.min(4, (int) Math.floor(Math.pow(Math.max(h, 0), 1.5) * 5));
        // Lago e rio do addon. Lá o rio descia de 1 a 4 blocos seguindo o relevo, então a água ficava em alturas
        // diferentes lado a lado. Água gerada com o chunk não escorre sozinha e esses degraus ficavam suspensos,
        // por isso toda a água fica no mesmo nível, o chão da planície.
        if (fbm(noises.water(), x * 0.004, z * 0.004) < -0.25) {
            return new Column(BASE_Y, true, SAND);
        }
        return new Column(BASE_Y + height, false, height >= 4 ? STONE : DIRT);
    }

    private static BlockState stateAt(Noises noises, Column column, int x, int y, int z) {
        if (y < BEDROCK_Y || y > column.top()) {
            return AIR;
        }
        if (y == column.top()) {
            return column.water() ? WATER : GRASS;
        }
        // A base do addon: camada cheia de bedrock e mais 2 com 52 e 69 blocos de dream stone em 256.
        if (y == BEDROCK_Y) {
            return BEDROCK;
        }
        if (y <= BEDROCK_Y + 2) {
            float stoneChance = y == BEDROCK_Y + 1 ? 52 / 256f : 69 / 256f;
            return noises.random().at(x, y, z).nextFloat() < stoneChance ? STONE : BEDROCK;
        }
        if (column.water() && y == BASE_Y - 1) {
            return column.fill();
        }
        if (y < BASE_Y + 2) {
            return STONE;
        }
        if (y < BASE_Y + 4 && !column.water()) {
            return noises.random().at(x, y, z).nextFloat() < 0.3f ? GRAVEL : DIRT;
        }
        return column.fill();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        Noises noises = noises(randomState);
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // Mesmo esquema do BackroomChunkGenerator: escreve direto nas seções, com o lock delas.
        int firstSection = chunk.getSectionIndex(BEDROCK_Y);
        int lastSection = chunk.getSectionIndex(MAX_SURFACE_Y);
        for (int i = firstSection; i <= lastSection; i++) {
            chunk.getSection(i).acquire();
        }
        try {
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    Column column = column(noises, minX + lx, minZ + lz);
                    for (int y = BEDROCK_Y; y <= column.top(); y++) {
                        BlockState state = stateAt(noises, column, minX + lx, y, minZ + lz);
                        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                        section.setBlockState(lx, y & 15, lz, state, false);
                        oceanFloor.update(lx, y, lz, state);
                        worldSurface.update(lx, y, lz, state);
                    }
                }
            }
        } finally {
            for (int i = firstSection; i <= lastSection; i++) {
                chunk.getSection(i).release();
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        Noises noises = noises(randomState);
        Column column = column(noises, x, z);
        for (int y = column.top(); y >= BEDROCK_Y; y--) {
            if (type.isOpaque().test(stateAt(noises, column, x, y, z))) {
                return y + 1;
            }
        }
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        Noises noises = noises(randomState);
        Column column = column(noises, x, z);
        BlockState[] states = new BlockState[level.getHeight()];
        for (int i = 0; i < states.length; i++) {
            states[i] = stateAt(noises, column, x, level.getMinBuildHeight() + i, z);
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
        return BASE_Y;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
    }
}
