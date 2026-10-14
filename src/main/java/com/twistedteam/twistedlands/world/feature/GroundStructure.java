package com.twistedteam.twistedlands.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twistedteam.twistedlands.TwistedLandsFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

/**
 * Uma estrutura .nbt só, no chão, centrada na chunk e girada aleatório. Não nasce na água nem em terreno com
 * desnível maior que max_slope entre os cantos, coisa que o jigsaw do vanilla não confere.
 * A altura vem do getBaseHeight do gerador (no Dream World é o chão, sem as ilhas).
 */
public class GroundStructure extends Structure {
    public static final Codec<GroundStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            settingsCodec(instance),
            ResourceLocation.CODEC.fieldOf("template").forGetter(s -> s.template),
            Codec.INT.optionalFieldOf("max_slope", 2).forGetter(s -> s.maxSlope)
    ).apply(instance, GroundStructure::new));

    private final ResourceLocation template;
    private final int maxSlope;

    public GroundStructure(StructureSettings settings, ResourceLocation template, int maxSlope) {
        super(settings);
        this.template = template;
        this.maxSlope = maxSlope;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Rotation rotation = Rotation.getRandom(context.random());
        Vec3i size = context.structureTemplateManager().getOrCreate(template).getSize();
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = surface(context, x, z);
        if (y < 0) {
            return Optional.empty();
        }
        // O template gira em volta do centro, então os cantos são os mesmos em qualquer rotação.
        int half = Math.max(size.getX(), size.getZ()) / 2;
        for (int[] c : new int[][] {{-half, -half}, {half, -half}, {-half, half}, {half, half}}) {
            int corner = surface(context, x + c[0], z + c[1]);
            if (corner < 0 || Math.abs(corner - y) > maxSlope) {
                return Optional.empty();
            }
        }
        BlockPos origin = new BlockPos(x - size.getX() / 2, y, z - size.getZ() / 2);
        return Optional.of(new GenerationStub(new BlockPos(x, y, z), builder ->
                builder.addPiece(new Piece(context.structureTemplateManager(), template, origin, rotation))));
    }

    // Altura do chão seco, ou -1 se for água.
    private static int surface(GenerationContext context, int x, int z) {
        ChunkGenerator generator = context.chunkGenerator();
        int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        return surface == floor ? surface : -1;
    }

    @Override
    public StructureType<?> type() {
        return TwistedLandsFeatures.GROUND_STRUCTURE;
    }

    public static class Piece extends TemplateStructurePiece {
        public Piece(StructureTemplateManager manager, ResourceLocation template, BlockPos origin, Rotation rotation) {
            super(TwistedLandsFeatures.GROUND_STRUCTURE_PIECE, 0, manager, template, template.toString(),
                    settings(manager, template, rotation), origin);
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(TwistedLandsFeatures.GROUND_STRUCTURE_PIECE, tag, context.structureTemplateManager(),
                    template -> settings(context.structureTemplateManager(), template, Rotation.valueOf(tag.getString("Rot"))));
        }

        private static StructurePlaceSettings settings(StructureTemplateManager manager, ResourceLocation template, Rotation rotation) {
            Vec3i size = manager.getOrCreate(template).getSize();
            return new StructurePlaceSettings().setRotation(rotation)
                    .setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rot", placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        }
    }
}
