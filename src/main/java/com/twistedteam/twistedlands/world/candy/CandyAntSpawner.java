package com.twistedteam.twistedlands.world.candy;

import com.twistedteam.twistedlands.ModBlocks;
import com.twistedteam.twistedlands.ModEntities;
import com.twistedteam.twistedlands.entity.CandyAntEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

/**
 * Spawn da Candy Ant na Candy Lands, igual ao MobSpawn do addon: a cada 10s, pra cada jogador lá dentro,
 * se tiver no máximo 20 entidades num raio de 32 blocos, nasce um grupo de 2 a 4 formigas em cima de
 * cookie grass, até 24 blocos do jogador e com luz do céu de 7 pra cima. Diferente do addon, as formigas
 * do grupo nascem espalhadas, não no mesmo bloco.
 */
public final class CandyAntSpawner {
    private static final int INTERVAL = 200;
    private static final int MAX_NEARBY = 20;
    private static final double RADIUS = 32.0;
    private static final int MAX_OFFSET = 24;
    private static final int SEARCH_Y = 10;
    // Espalhamento de cada formiga em volta do ponto do grupo.
    private static final int SPREAD = 3;
    private static final int SPREAD_TRIES = 6;

    private CandyAntSpawner() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(CandyAntSpawner::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % INTERVAL != 0) {
            return;
        }
        ServerLevel level = server.getLevel(CandyLands.LEVEL);
        if (level == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            trySpawn(level, player);
        }
    }

    private static void trySpawn(ServerLevel level, ServerPlayer player) {
        // No addon contava tudo menos item, inclusive o próprio jogador.
        int nearby = level.getEntities(player, player.getBoundingBox().inflate(RADIUS),
                entity -> !(entity instanceof ItemEntity) && entity.distanceToSqr(player) <= RADIUS * RADIUS).size() + 1;
        if (nearby > MAX_NEARBY) {
            return;
        }

        RandomSource random = level.random;
        // O addon só somava o deslocamento, então o grupo sempre nasce pro lado +X +Z do jogador.
        BlockPos column = player.blockPosition().offset(random.nextInt(MAX_OFFSET + 1), 0, random.nextInt(MAX_OFFSET + 1));
        if (!level.hasChunkAt(column)) {
            return;
        }
        BlockPos spawn = findGround(level, column);
        if (spawn == null || level.getBrightness(LightLayer.SKY, spawn) < 7) {
            return;
        }

        int count = random.nextInt(3) + 2;
        for (int i = 0; i < count; i++) {
            CandyAntEntity ant = ModEntities.CANDY_ANT.create(level);
            if (ant == null) {
                return;
            }
            // No addon o grupo todo nascia no mesmo bloco, uma dentro da outra, e se empurravam ao aparecer.
            // Aqui cada uma procura um bloco livre de cookie grass perto do ponto do grupo.
            BlockPos pos = spread(level, ant, spawn, random);
            ant.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            ant.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.NATURAL, null, null);
            level.addFreshEntity(ant);
        }
    }

    private static BlockPos spread(ServerLevel level, CandyAntEntity ant, BlockPos center, RandomSource random) {
        for (int i = 0; i < SPREAD_TRIES; i++) {
            BlockPos column = center.offset(random.nextIntBetweenInclusive(-SPREAD, SPREAD), 0,
                    random.nextIntBetweenInclusive(-SPREAD, SPREAD));
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos pos = column.above(dy);
                if (level.getBlockState(pos.below()).is(ModBlocks.COOKIE_GRASS) && level.isEmptyBlock(pos)
                        && level.noCollision(ant, ant.getType().getAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))
                        && level.getEntities(ant, new AABB(pos)).isEmpty()) {
                    return pos;
                }
            }
        }
        return center;
    }

    // O detectSurfaceFloor do addon: cookie grass com ar em cima, até 10 blocos acima ou abaixo do jogador.
    private static BlockPos findGround(ServerLevel level, BlockPos column) {
        for (int dy = -SEARCH_Y; dy <= SEARCH_Y; dy++) {
            BlockPos ground = column.above(dy);
            if (level.getBlockState(ground).is(ModBlocks.COOKIE_GRASS) && level.isEmptyBlock(ground.above())) {
                return ground.above();
            }
        }
        return null;
    }
}
