package com.twistedteam.twistedlands.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;

/**
 * Navegação de chão que aceita parar em cima de bloco translúcido. A do vanilla só aceita destino com bloco
 * isSolidRender embaixo, e a crystal grass e o kyanite do Dream World não são (são translúcidos). Sem isso os
 * mobs das ilhas não passeiam nem fogem: todo destino sorteado é recusado.
 */
public class SturdyGroundNavigation extends GroundPathNavigation {
    public SturdyGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
