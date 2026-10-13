package com.twistedteam.twistedlands.block;

import com.twistedteam.twistedlands.ModBlocks;
import com.twistedteam.twistedlands.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Caule do pé de tomate. Amadurece em 3 estágios e no último dá de 1 a 3 tomates ao clicar. */
public class TomatoPlantBodyBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 2);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);

    public TomatoPlantBodyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(GROWTH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GROWTH);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return TomatoPlantHeadBlock.supports(level.getBlockState(pos.below()));
    }

    // Sem nada do pé em cima (ponta quebrada), o caule vira a nova ponta.
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.UP && !neighbor.is(this) && !neighbor.is(ModBlocks.TOMATO_PLANT_HEAD)) {
            return ModBlocks.TOMATO_PLANT_HEAD.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    // Igual ao crop_grow do addon: tenta amadurecer e, se não amadureceu, tenta fazer a planta crescer pra cima.
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(GROWTH) < 2 && random.nextFloat() < 0.3f) {
            level.setBlock(pos, state.setValue(GROWTH, state.getValue(GROWTH) + 1), Block.UPDATE_CLIENTS);
            return;
        }
        if (random.nextFloat() < 0.15f && TomatoPlantHeadBlock.canGrowTaller(level, pos)) {
            TomatoPlantHeadBlock.growTaller(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (state.getValue(GROWTH) < 2) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(ModItems.TOMATO, 1 + level.random.nextInt(3)));
            level.setBlock(pos, state.setValue(GROWTH, 0), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(GROWTH) < 2 || TomatoPlantHeadBlock.canGrowTaller(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(GROWTH) < 2) {
            level.setBlock(pos, state.setValue(GROWTH, state.getValue(GROWTH) + 1), Block.UPDATE_CLIENTS);
        } else {
            TomatoPlantHeadBlock.growTaller(level, pos);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.TOMATO);
    }
}
