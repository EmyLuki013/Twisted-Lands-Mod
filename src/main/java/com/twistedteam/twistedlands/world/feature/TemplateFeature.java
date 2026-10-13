package com.twistedteam.twistedlands.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Coloca uma estrutura .nbt sorteada de uma lista, centrada na posição da feature. É o que no addon era feito
 * com blocos "generator" (que davam structure load ao tickar) e com structure_template_feature.
 * Centralizar mantém estruturas grandes (o bolo tem 31x31) dentro da área que a geração pode escrever.
 */
public class TemplateFeature extends Feature<TemplateFeature.Config> {
    /**
     * templates: sorteia um. rotate: gira em 90° aleatório. y_offset: -1 enterra a primeira camada
     * (estruturas que trazem o próprio chão). max_slope: se >= 0, pula terreno com desnível maior que isso
     * entre os cantos (no addon era a constraint "grounded"). clear: blocos removidos da área antes de colocar.
     * Pega árvores e decorações geradas antes que ficariam saindo pelo telhado. Começa 1 bloco acima do chão
     * pra não apagar o piso de uma casa vizinha (a lã rosa do algodão-doce também está no piso da casa).
     * free: se tiver, só coloca quando a área toda for ar, planta, um desses blocos (terreno) ou algo que o clear
     * apaga. Assim bolo, casa e decoração não nascem um dentro do outro.
     */
    public record Config(List<ResourceLocation> templates, boolean rotate, int yOffset, int maxSlope,
                         HolderSet<Block> clear, Optional<HolderSet<Block>> free)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf("templates").forGetter(Config::templates),
                Codec.BOOL.optionalFieldOf("rotate", false).forGetter(Config::rotate),
                Codec.INT.optionalFieldOf("y_offset", 0).forGetter(Config::yOffset),
                Codec.INT.optionalFieldOf("max_slope", -1).forGetter(Config::maxSlope),
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("clear", HolderSet.direct())
                        .forGetter(Config::clear),
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("free").forGetter(Config::free)
        ).apply(instance, Config::new));
    }

    public TemplateFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ResourceLocation id = config.templates().get(random.nextInt(config.templates().size()));
        Optional<StructureTemplate> found = level.getLevel().getStructureManager().get(id);
        if (found.isEmpty()) {
            return false;
        }
        StructureTemplate template = found.get();
        Vec3i size = template.getSize();
        BlockPos origin = context.origin();
        BlockPos corner = origin.offset(-size.getX() / 2, config.yOffset(), -size.getZ() / 2);

        if (config.maxSlope() >= 0 && !isFlat(level, corner, size, origin.getY(), config.maxSlope())) {
            return false;
        }

        if (config.free().isPresent() && !isFree(level, corner, size, config.free().get(), config.clear())) {
            return false;
        }

        if (config.clear().size() > 0) {
            clear(level, corner, size, config.clear());
        }

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(config.rotate() ? Rotation.getRandom(random) : Rotation.NONE)
                // Gira em volta do próprio centro, então a área ocupada continua centrada na origem.
                .setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2));
        return template.placeInWorld(level, corner, corner, settings, random, Block.UPDATE_CLIENTS);
    }

    // Margem pros lados e altura extra pra pegar a copa inteira das árvores que encostam na estrutura.
    private static final int CLEAR_MARGIN = 2;
    private static final int CLEAR_ABOVE = 12;

    private static void clear(WorldGenLevel level, BlockPos corner, Vec3i size, HolderSet<Block> blocks) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = corner.getX() - CLEAR_MARGIN; x < corner.getX() + size.getX() + CLEAR_MARGIN; x++) {
            for (int z = corner.getZ() - CLEAR_MARGIN; z < corner.getZ() + size.getZ() + CLEAR_MARGIN; z++) {
                if (!canWrite(level, x, z)) {
                    continue;
                }
                for (int y = corner.getY() + 1; y < corner.getY() + size.getY() + CLEAR_ABOVE; y++) {
                    pos.set(x, y, z);
                    if (level.getBlockState(pos).is(blocks)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    // A geração só pode escrever na chunk atual e nas vizinhas; a margem pode passar disso.
    private static boolean canWrite(WorldGenLevel level, int x, int z) {
        if (!(level instanceof WorldGenRegion region)) {
            return true;
        }
        ChunkPos center = region.getCenter();
        return Math.abs((x >> 4) - center.x) <= 1 && Math.abs((z >> 4) - center.z) <= 1;
    }

    private static boolean isFree(WorldGenLevel level, BlockPos corner, Vec3i size, HolderSet<Block> allowed,
                                  HolderSet<Block> cleared) {
        for (BlockPos pos : BlockPos.betweenClosed(corner, corner.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
            BlockState state = level.getBlockState(pos);
            if (!state.canBeReplaced() && !state.is(allowed) && !state.is(cleared)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFlat(WorldGenLevel level, BlockPos corner, Vec3i size, int surfaceY, int maxSlope) {
        int[][] corners = {{0, 0}, {size.getX() - 1, 0}, {0, size.getZ() - 1}, {size.getX() - 1, size.getZ() - 1}};
        for (int[] c : corners) {
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, corner.getX() + c[0], corner.getZ() + c[1]);
            if (Math.abs(y - surfaceY) > maxSlope) {
                return false;
            }
        }
        return true;
    }
}
