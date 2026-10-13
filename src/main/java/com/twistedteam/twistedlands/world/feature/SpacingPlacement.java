package com.twistedteam.twistedlands.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twistedteam.twistedlands.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * Distância mínima entre features grandes, igual ao "spacing" do FeaturePlacement do addon.
 * O mundo é dividido em células de {@code spacing} chunks e cada célula sorteia uma chunk candidata,
 * nos primeiros {@code spacing - separation} chunks dela. Assim duas vizinhas ficam a pelo menos
 * {@code separation + 1} chunks uma da outra.
 */
public class SpacingPlacement extends PlacementModifier {
    public static final Codec<SpacingPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 64).fieldOf("spacing").forGetter(p -> p.spacing),
            Codec.intRange(0, 64).fieldOf("separation").forGetter(p -> p.separation),
            Codec.INT.fieldOf("salt").forGetter(p -> p.salt)
    ).apply(instance, SpacingPlacement::new));

    private final int spacing;
    private final int separation;
    private final int salt;

    public SpacingPlacement(int spacing, int separation, int salt) {
        if (separation >= spacing) {
            throw new IllegalArgumentException("separation precisa ser menor que spacing");
        }
        this.spacing = spacing;
        this.separation = separation;
        this.salt = salt;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        int cellX = Math.floorDiv(chunkX, spacing);
        int cellZ = Math.floorDiv(chunkZ, spacing);

        WorldgenRandom cellRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        cellRandom.setLargeFeatureWithSalt(context.getLevel().getSeed(), cellX, cellZ, salt);
        int range = spacing - separation;
        boolean chosen = chunkX == cellX * spacing + cellRandom.nextInt(range)
                && chunkZ == cellZ * spacing + cellRandom.nextInt(range);
        return chosen ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModFeatures.SPACING;
    }
}
