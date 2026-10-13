package com.twistedteam.twistedlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Ponta de uma planta alta do addon (tomate, milho de cristal): o que o item planta. Cresce pra cima virando
 * caule, até 3 caules embaixo dela, como o crop_grow do addon.
 */
public class CropPlantHeadBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);
    private static final int MAX_BODIES = 3;

    /** Os blocos e o item de uma planta. Suppliers porque os blocos se citam e são registrados depois. */
    public record Crop(Supplier<Block> ground, Supplier<Block> head, Supplier<Block> body, Supplier<Item> produce) {
        boolean supports(BlockState below) {
            return below.is(ground.get()) || below.is(body.get());
        }
    }

    private final Crop crop;

    public CropPlantHeadBlock(Crop crop, Properties properties) {
        super(properties);
        this.crop = crop;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return crop.supports(level.getBlockState(pos.below()));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.15f && canGrowTaller(crop, level, pos)) {
            growTaller(crop, level, pos);
        }
    }

    /** Se a ponta da coluna de pos (pos pode ser um caule) pode subir mais um bloco. */
    static boolean canGrowTaller(Crop crop, LevelReader level, BlockPos pos) {
        BlockPos head = findHead(crop, level, pos);
        if (head == null || !level.isEmptyBlock(head.above()) || head.getY() + 1 >= level.getMaxBuildHeight()) {
            return false;
        }
        int bodies = 0;
        BlockPos below = head.below();
        while (level.getBlockState(below).is(crop.body().get())) {
            bodies++;
            below = below.below();
        }
        return bodies < MAX_BODIES;
    }

    /** A ponta vira caule e nasce uma ponta nova em cima. Chamar só depois de canGrowTaller. */
    static void growTaller(Crop crop, ServerLevel level, BlockPos pos) {
        BlockPos head = findHead(crop, level, pos);
        level.setBlock(head, crop.body().get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(head.above(), crop.head().get().defaultBlockState(), Block.UPDATE_ALL);
    }

    private static BlockPos findHead(Crop crop, LevelReader level, BlockPos pos) {
        BlockPos cursor = pos;
        while (level.getBlockState(cursor).is(crop.body().get())) {
            cursor = cursor.above();
        }
        return level.getBlockState(cursor).is(crop.head().get()) ? cursor : null;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return canGrowTaller(crop, level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growTaller(crop, level, pos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(crop.produce().get());
    }
}
