package com.twistedteam.twistedlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Piso da Backroom. Anula o dano de queda, pedido da Emy: a entrada do labirinto do Dream World é um poço,
 * e quem cai por ele (jogador ou bicho) chega inteiro.
 */
public class WallpaperPlanksBlock extends Block {
    public WallpaperPlanksBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0f, level.damageSources().fall());
    }
}
