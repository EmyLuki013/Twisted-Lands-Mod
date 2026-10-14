package com.twistedteam.twistedlands.world.dream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.world.backroom.BackroomMaze;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Chão da Dream World, portado do TerrainGen do addon (relevo PLAINS). De baixo pra cima: bedrock em y0 a 2
 * (a dream_world_base_chunk, que no addon ficava em y31) com o labirinto do Level 0 embaixo, dream stone,
 * e a planície de y61 a y65 com grama da Dream World em cima de terra, alguns pontos de cascalho e lagos rasos
 * (todos em y61) com fundo de areia.
 * <p>
 * No alto ficam as ilhas de cristal, que no addon eram a feature rule dream_world_crystal_island: uma conta por
 * coluna que decide se cada y de 110 a 157 é kyanita, com o topo coberto de crystal grass. Aqui a mesma conta roda
 * no gerador, então a ilha sai igual em qualquer ordem de chunks e já entra no heightmap (árvore e estrutura
 * pousam nela).
 */
public class DreamWorldChunkGenerator extends ChunkGenerator {
    public static final Codec<DreamWorldChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
    ).apply(instance, instance.stable(DreamWorldChunkGenerator::new)));

    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, TwistedLands.id("dream_world"));

    // O mundo começa em y-16 pra caber o labirinto embaixo da bedrock.
    private static final int MIN_Y = -16;
    private static final int HEIGHT = 272;
    // No addon a base ficava em y31 só por desempenho. A Emy liberou descer até y0.
    private static final int BEDROCK_Y = 0;
    // Chão da planície no addon (baseY). O relevo sobe até 4 blocos a partir daqui.
    private static final int BASE_Y = 61;
    // Ilhas: 48 camadas a partir de y110, mais grossas no y134 e no centro de cada quadrado de 256x256.
    private static final int ISLAND_MIN_Y = 110;
    private static final int ISLAND_LAYERS = 48;
    private static final int ISLAND_TILE = 256;
    // Labirinto do Level 0 logo embaixo da bedrock, de y-6 a y-1 (no addon, y25 a 30 embaixo da base em y31).
    public static final int MAZE_FLOOR_Y = -6;
    private static final int MAZE_CEILING_Y = MAZE_FLOOR_Y + BackroomMaze.CEILING_Y - BackroomMaze.FLOOR_Y;
    // A torre de entrada nasce na altura do addon, que tinha a base em y31.
    private static final int ENTRANCE_Y = 31;
    // backroom_entrance do addon: 0,008 por chunk.
    private static final int ENTRANCE_CHANCE = 125;

    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState STONE = TwistedLandsBlocks.DREAM_STONE.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState GRASS = TwistedLandsBlocks.DREAM_WORLD_GRASS_BLOCK.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState KYANITE = TwistedLandsBlocks.KYANITE.defaultBlockState();
    private static final BlockState CRYSTAL_GRASS = TwistedLandsBlocks.CRYSTAL_GRASS.defaultBlockState();

    private record Noises(RandomState owner, SimplexNoise terrain, SimplexNoise water, ImprovedNoise islands,
                          PositionalRandomFactory random) {
    }

    /**
     * Uma coluna: y do bloco de cima da planície, se é água, de que é o enchimento, quais das 48 camadas de ilha
     * são kyanita (um bit por camada) e o y da kyanita mais alta (-1 sem ilha).
     */
    private record Column(int ground, boolean water, BlockState fill, long islands, int islandTop) {
        int top() {
            return islandTop >= 0 ? islandTop + 1 : ground;
        }
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
                    new SimplexNoise(factory.fromHashOf("water")), new ImprovedNoise(factory.fromHashOf("islands")), factory);
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
        long islands = islands(noises.islands(), x, z);
        int islandTop = islands == 0 ? -1 : ISLAND_MIN_Y + 63 - Long.numberOfLeadingZeros(islands);
        if (fbm(noises.water(), x * 0.004, z * 0.004) < -0.25) {
            return new Column(BASE_Y, true, SAND, islands, islandTop);
        }
        return new Column(BASE_Y + height, false, height >= 4 ? STONE : DIRT, islands, islandTop);
    }

    // A conta das features islands e islands_edge do addon. O query.noise do Bedrock é Perlin, como o ImprovedNoise.
    private static long islands(ImprovedNoise noise, int x, int z) {
        int centerX = Math.floorDiv(x, ISLAND_TILE) * ISLAND_TILE + ISLAND_TILE / 2;
        int centerZ = Math.floorDiv(z, ISLAND_TILE) * ISLAND_TILE + ISLAND_TILE / 2;
        double distance = (Math.abs(x - centerX) + Math.abs(z - centerZ)) / (ISLAND_TILE * 2.5);
        double base = 1 - distance / 0.4;
        double noise1 = noise.noise(x / 64.0, 0, z / 64.0);
        double noise2 = noise.noise((x - 8231) / 64.0, 0, (z - 8231) / 64.0);
        if (base + Math.max(Math.abs(noise1), Math.abs(noise2)) / 3 <= 0.35) {
            return 0;
        }
        long layers = 0;
        for (int i = 0; i < ISLAND_LAYERS; i++) {
            double t = i / 64.0;
            double blend = t * t * (3 - 2 * t);
            double interpolation = noise1 + (noise2 - noise1) * blend;
            if (interpolation / 3 + base - Math.abs(i - 24) / 32.0 > 0.35) {
                layers |= 1L << i;
            }
        }
        return layers;
    }

    private static BlockState stateAt(Noises noises, Column column, int x, int y, int z) {
        if (y > column.ground()) {
            int layer = y - ISLAND_MIN_Y;
            if (layer >= 0 && layer < ISLAND_LAYERS && (column.islands() & 1L << layer) != 0) {
                return KYANITE;
            }
            return y == column.islandTop() + 1 ? CRYSTAL_GRASS : AIR;
        }
        if (y == column.ground()) {
            return column.water() ? WATER : GRASS;
        }
        // No addon o labirinto ficava entre blocos deny (embaixo do piso) e allow (em cima do teto) pra ninguém
        // quebrar. Aqui a bedrock veda em cima e embaixo, e o Backroom.isLocked protege o meio.
        if (y < BEDROCK_Y) {
            return y == MAZE_FLOOR_Y - 1 ? BEDROCK : BackroomMaze.stateAt(x, y, z, MAZE_FLOOR_Y);
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
        int firstSection = chunk.getSectionIndex(MAZE_FLOOR_Y - 1);
        int lastSection = chunk.getSectionIndex(ISLAND_MIN_Y + ISLAND_LAYERS);
        for (int i = firstSection; i <= lastSection; i++) {
            chunk.getSection(i).acquire();
        }
        try {
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    Column column = column(noises, minX + lx, minZ + lz);
                    for (int y = MAZE_FLOOR_Y - 1; y <= column.top(); y++) {
                        BlockState state = stateAt(noises, column, minX + lx, y, minZ + lz);
                        if (state.isAir()) {
                            continue;
                        }
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
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);
        BackroomMaze.placeExit(level, chunk, MAZE_FLOOR_Y);
        placeEntrance(level, chunk);
    }

    /**
     * A torre oca que desce até o labirinto e é o único jeito de entrar nele. Nasce na altura do addon, centrada
     * numa célula de corredor (3x3, igual ao miolo oco dela). Embaixo dela um poço atravessa a pedra, a bedrock e
     * o teto, e quem desce cai no corredor (o piso de wallpaper planks anula a queda).
     */
    private static void placeEntrance(WorldGenLevel level, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        RandomSource random = RandomSource.create(level.getSeed() ^ pos.toLong() * 341873128712L);
        MinecraftServer server = level.getServer();
        if (random.nextInt(ENTRANCE_CHANCE) != 0 || server == null) {
            return;
        }
        List<BlockPos> cells = new ArrayList<>();
        for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
            for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                if (Math.floorMod(x, 3) == 1 && Math.floorMod(z, 3) == 1 && BackroomMaze.isPath(x, z)) {
                    cells.add(new BlockPos(x, 0, z));
                }
            }
        }
        if (cells.isEmpty()) {
            return;
        }
        BlockPos cell = cells.get(random.nextInt(cells.size()));
        for (BlockPos shaft : BlockPos.betweenClosed(cell.offset(-1, MAZE_CEILING_Y, -1), cell.offset(1, ENTRANCE_Y - 1, 1))) {
            level.setBlock(shaft, AIR, Block.UPDATE_CLIENTS);
        }
        BlockPos corner = new BlockPos(cell.getX() - 3, ENTRANCE_Y, cell.getZ() - 3);
        server.getStructureManager().get(TwistedLands.id("dream_world/backroom_entrance")).ifPresent(template ->
                template.placeInWorld(level, corner, corner, new StructurePlaceSettings(), random, Block.UPDATE_CLIENTS));
    }

    // Altura do chão, sem as ilhas. As estruturas (casa e moinho) começam por aqui e ficam no chão como no addon.
    // As features usam o heightmap real da chunk, que tem as ilhas.
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        Noises noises = noises(randomState);
        Column column = column(noises, x, z);
        for (int y = column.ground(); y >= BEDROCK_Y; y--) {
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
