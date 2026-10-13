package com.twistedteam.twistedlands.block;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

/**
 * Ponta do pé de tomate (o que o tomate planta). Cresce pra cima virando caule, até 3 caules embaixo dela,
 * como no addon.
 */
public class TomatoPlantHeadBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);
    private static final int MAX_BODIES = 3;

    public TomatoPlantHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supports(level.getBlockState(pos.below()));
    }

    static boolean supports(BlockState below) {
        return below.is(Blocks.GRASS_BLOCK) || below.is(TwistedLandsBlocks.TOMATO_PLANT_BODY);
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
        if (random.nextFloat() < 0.15f && canGrowTaller(level, pos)) {
            growTaller(level, pos);
        }
    }

    /** Se a ponta da coluna de pos (pos pode ser um caule) pode subir mais um bloco. */
    static boolean canGrowTaller(LevelReader level, BlockPos pos) {
        BlockPos head = findHead(level, pos);
        if (head == null || !level.isEmptyBlock(head.above()) || head.getY() + 1 >= level.getMaxBuildHeight()) {
            return false;
        }
        int bodies = 0;
        BlockPos below = head.below();
        while (level.getBlockState(below).is(TwistedLandsBlocks.TOMATO_PLANT_BODY)) {
            bodies++;
            below = below.below();
        }
        return bodies < MAX_BODIES;
    }

    /** A ponta vira caule e nasce uma ponta nova em cima. Chamar só depois de canGrowTaller. */
    static void growTaller(ServerLevel level, BlockPos pos) {
        BlockPos head = findHead(level, pos);
        level.setBlock(head, TwistedLandsBlocks.TOMATO_PLANT_BODY.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(head.above(), TwistedLandsBlocks.TOMATO_PLANT_HEAD.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static BlockPos findHead(LevelReader level, BlockPos pos) {
        BlockPos cursor = pos;
        while (level.getBlockState(cursor).is(TwistedLandsBlocks.TOMATO_PLANT_BODY)) {
            cursor = cursor.above();
        }
        return level.getBlockState(cursor).is(TwistedLandsBlocks.TOMATO_PLANT_HEAD) ? cursor : null;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return canGrowTaller(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growTaller(level, pos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(TwistedLandsItems.TOMATO);
    }
}
