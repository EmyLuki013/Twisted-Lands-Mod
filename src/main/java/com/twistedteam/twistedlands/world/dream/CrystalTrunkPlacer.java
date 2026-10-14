package com.twistedteam.twistedlands.world.dream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.twistedteam.twistedlands.TwistedLandsFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Tronco da red crystal tree, igual ao acacia_trunk do addon: o tronco inclina perto do topo (pode ir na diagonal)
 * e sempre solta um galho para outro lado, cada um com a sua copa.
 */
public class CrystalTrunkPlacer extends TrunkPlacer {
    public static final Codec<CrystalTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance).apply(instance, CrystalTrunkPlacer::new));

    public CrystalTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TwistedLandsFeatures.CRYSTAL_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter,
                                                            RandomSource random, int height, BlockPos start, TreeConfiguration config) {
        setDirtAt(level, blockSetter, random, start.below(), config);
        // lean_height 1 a 5 e lean_steps 1 a 4, contados do topo.
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction diagonal = random.nextBoolean() ? lean.getClockWise() : null;
        int leanFrom = height - 1 - random.nextInt(5);
        int leanSteps = 1 + random.nextInt(4);
        // branch_position 1 a 3 do topo.
        int branchAt = height - 1 - random.nextInt(3);

        List<FoliagePlacer.FoliageAttachment> foliage = new ArrayList<>();
        BlockPos.MutableBlockPos pos = start.mutable();
        BlockPos branchBase = start;
        for (int i = 0; i < height; i++) {
            if (i >= leanFrom && leanSteps > 0) {
                pos.move(lean);
                if (diagonal != null) pos.move(diagonal);
                leanSteps--;
            }
            pos.setY(start.getY() + i);
            placeLog(level, blockSetter, random, pos, config);
            if (i == branchAt) branchBase = pos.immutable();
        }
        foliage.add(new FoliagePlacer.FoliageAttachment(pos.above(), 1, false));

        // branch_length 1 a 4, subindo na diagonal.
        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (side == lean) side = side.getOpposite();
        BlockPos.MutableBlockPos branch = branchBase.mutable();
        for (int i = 1 + random.nextInt(4); i > 0; i--) {
            branch.move(side).move(Direction.UP);
            placeLog(level, blockSetter, random, branch, config);
        }
        foliage.add(new FoliagePlacer.FoliageAttachment(branch.above(), 0, false));
        return foliage;
    }
}
