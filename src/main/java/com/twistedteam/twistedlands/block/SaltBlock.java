package com.twistedteam.twistedlands.block;

import com.twistedteam.twistedlands.entity.RainbowAntEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Bloco de sal. Espanta formigas e phantoms num raio de 10 blocos, jogando eles pra frente
 * (no addon era um knockback na direção em que o mob olha).
 */
public class SaltBlock extends Block {
    private static final double RADIUS = 10.0;
    private final int minDelay;
    private final int maxDelay;

    public SaltBlock(int minDelay, int maxDelay, Properties properties) {
        super(properties);
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            scheduleRepel(level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        repel(level, pos);
        scheduleRepel(level, pos);
    }

    private void scheduleRepel(Level level, BlockPos pos) {
        level.scheduleTick(pos, this, minDelay + level.random.nextInt(maxDelay - minDelay + 1));
    }

    public static void repel(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(RADIUS),
                mob -> mob.distanceToSqr(center) <= RADIUS * RADIUS && repels(mob))) {
            Vec3 look = mob.getViewVector(1.0f);
            mob.setDeltaMovement(look.x * 1.5, 0.6, look.z * 1.5);
            mob.hurtMarked = true;
        }
    }

    private static boolean repels(Mob mob) {
        return mob instanceof RainbowAntEntity || mob.getType() == EntityType.PHANTOM;
    }
}
