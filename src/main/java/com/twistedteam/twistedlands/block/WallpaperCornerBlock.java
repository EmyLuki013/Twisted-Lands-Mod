package com.twistedteam.twistedlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Duas folhas de papel de parede em L, pra cantos internos: uma no lado pra onde o jogador olhava
 * e outra no lado seguinte em sentido horário. Igual ao addon, não tem colisão (dá pra atravessar).
 */
public class WallpaperCornerBlock extends WallpaperBlock {
    private static final VoxelShape NORTH = Shapes.or(Block.box(0, 0, 0, 16, 16, 1), Block.box(15, 0, 0, 16, 16, 16));
    private static final VoxelShape EAST = Shapes.or(Block.box(15, 0, 0, 16, 16, 16), Block.box(0, 0, 15, 16, 16, 16));
    private static final VoxelShape SOUTH = Shapes.or(Block.box(0, 0, 15, 16, 16, 16), Block.box(0, 0, 0, 1, 16, 16));
    private static final VoxelShape WEST = Shapes.or(Block.box(0, 0, 0, 1, 16, 16), Block.box(0, 0, 0, 16, 16, 1));

    public WallpaperCornerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
}
