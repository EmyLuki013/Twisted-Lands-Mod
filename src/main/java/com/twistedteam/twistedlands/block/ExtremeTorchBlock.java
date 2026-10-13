package com.twistedteam.twistedlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Sacred Torch (twisted:extreme_torch). Tocha alta que vai no chão ou na parede. Além da própria luz,
 * coloca blocos de luz invisíveis a 10 blocos dela nas 4 direções, igual ao addon. A Crystal Torch usa a
 * mesma classe, mais baixa e sem os blocos de luz.
 */
public class ExtremeTorchBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", d -> d != Direction.DOWN);

    private static final Map<Direction, VoxelShape> WALL = new EnumMap<>(Map.of(
            Direction.NORTH, Block.box(6, 3, 6, 10, 16, 16),
            Direction.SOUTH, Block.box(6, 3, 0, 10, 16, 10),
            Direction.EAST, Block.box(0, 3, 6, 10, 16, 10),
            Direction.WEST, Block.box(6, 3, 6, 16, 16, 10)));

    // 0 = sem blocos de luz.
    private final int lightDistance;
    private final VoxelShape floor;
    // Altura do fogo na tocha de parede. Na de chão fica 0.1 mais alto.
    private final double flameY;

    public ExtremeTorchBlock(Properties properties) {
        this(properties, 10, 16, 1.5);
    }

    public ExtremeTorchBlock(Properties properties, int lightDistance, int floorHeight, double flameY) {
        super(properties);
        this.lightDistance = lightDistance;
        this.floor = Block.box(6, 0, 6, 10, floorHeight, 10);
        this.flameY = flameY;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.UP ? floor : WALL.get(facing);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face == Direction.DOWN) {
            return null;
        }
        BlockState state = defaultBlockState().setValue(FACING, face);
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        if (facing == Direction.UP) {
            return canSupportCenter(level, support, Direction.UP);
        }
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (lightDistance > 0 && !level.isClientSide && !oldState.is(this)) {
            BlockState light = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos target = pos.relative(dir, lightDistance);
                if (level.getBlockState(target).isAir()) {
                    level.setBlock(target, light, Block.UPDATE_ALL);
                }
            }
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (lightDistance > 0 && !level.isClientSide && !state.is(newState.getBlock())) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos target = pos.relative(dir, lightDistance);
                if (level.getBlockState(target).is(Blocks.LIGHT)) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // Fogo e fumaça na ponta, nas mesmas posições do addon.
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + flameY + (facing == Direction.UP ? 0.1 : 0.0);
        double z = pos.getZ() + 0.5;
        if (facing == Direction.NORTH) {
            z -= 0.05;
        } else if (facing == Direction.WEST) {
            x -= 0.05;
        }
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.UP ? state : state.setValue(FACING, rotation.rotate(facing));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.UP ? state : state.setValue(FACING, mirror.mirror(facing));
    }
}
