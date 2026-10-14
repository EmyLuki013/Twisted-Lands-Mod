package com.twistedteam.twistedlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocos de cristal do Dream World. No addon eles usam render "blend" com light_dampening 0: a textura é meio
 * transparente e a luz passa. Igual ao vidro, a face entre dois blocos iguais não é desenhada. Os minérios
 * soltam XP como o ore_xp_reward do addon.
 */
public class CrystalBlock extends DropExperienceBlock {
    public CrystalBlock(Properties properties) {
        this(properties, ConstantInt.ZERO);
    }

    public CrystalBlock(Properties properties, IntProvider xp) {
        super(properties, xp);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    /** As toras de cristal, que têm eixo. */
    public static class Pillar extends RotatedPillarBlock {
        public Pillar(Properties properties) {
            super(properties);
        }

        @Override
        public boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
            return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
        }

        @Override
        public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
            return true;
        }
    }

    /** As folhas de cristal. Diferente das folhas comuns, a luz passa inteira e elas não fazem sombra embaixo das ilhas. */
    public static class Leaves extends Block {
        public Leaves(Properties properties) {
            super(properties);
        }

        @Override
        public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
            return true;
        }
    }
}
