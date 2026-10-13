package com.twistedteam.twistedlands.world;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.world.backroom.Backroom;
import com.twistedteam.twistedlands.world.candy.CandyLands;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Spawn de mobs nas dimensões do mod, igual ao MobSpawn do addon (as dimensões custom do Bedrock não têm
 * spawn natural). A cada 10s, pra cada jogador numa dessas dimensões e pra cada entrada dela: sorteia a chance
 * e, se tiver no máximo 20 entidades num raio de 32 blocos, nasce um grupo em cima do bloco da entrada, até
 * 24 blocos do jogador e com luz do céu de 7 pra cima. Diferente do addon, os mobs do grupo nascem
 * espalhados, não no mesmo bloco.
 */
public final class DimensionMobSpawner {
    private static final int INTERVAL = 200;
    private static final int MAX_NEARBY = 20;
    private static final double RADIUS = 32.0;
    private static final int MAX_OFFSET = 24;
    private static final int SEARCH_Y = 10;
    // lightLevel do MobSpawn, que nenhuma dimensão do addon muda.
    private static final int MIN_SKY_LIGHT = 7;
    // Espalhamento de cada mob em volta do ponto do grupo.
    private static final int SPREAD = 3;
    private static final int SPREAD_TRIES = 6;

    /** herd: o grupo tem de 2 a herd + 1 mobs (o laço do addon vai de 0 até o sorteio, inclusive). */
    private record Entry(ResourceKey<Level> level, EntityType<? extends Mob> type, float chance, int herd, Block ground) {
    }

    // Os spawnRules de cada dimensão, na mesma ordem do addon.
    private static List<Entry> entries;

    private DimensionMobSpawner() {
    }

    public static void initialize() {
        entries = List.of(
                new Entry(CandyLands.LEVEL, TwistedLandsEntities.CANDY_ANT, 1.0f, 3, TwistedLandsBlocks.COOKIE_GRASS),
                new Entry(Backroom.LEVEL, TwistedLandsEntities.RAT, 0.65f, 2, TwistedLandsBlocks.WALLPAPER_PLANKS));
        ServerTickEvents.END_SERVER_TICK.register(DimensionMobSpawner::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % INTERVAL != 0) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            List<Entry> here = entries.stream().filter(entry -> entry.level() == level.dimension()).toList();
            if (here.isEmpty()) {
                continue;
            }
            for (ServerPlayer player : level.players()) {
                for (Entry entry : here) {
                    trySpawn(level, player, entry);
                }
            }
        }
    }

    private static void trySpawn(ServerLevel level, ServerPlayer player, Entry entry) {
        RandomSource random = level.random;
        if (random.nextFloat() > entry.chance()) {
            return;
        }
        // No addon contava tudo menos item, inclusive o próprio jogador.
        int nearby = level.getEntities(player, player.getBoundingBox().inflate(RADIUS),
                entity -> !(entity instanceof ItemEntity) && entity.distanceToSqr(player) <= RADIUS * RADIUS).size() + 1;
        if (nearby > MAX_NEARBY) {
            return;
        }

        // O addon só somava o deslocamento, então o grupo sempre nasce pro lado +X +Z do jogador.
        BlockPos column = player.blockPosition().offset(random.nextInt(MAX_OFFSET + 1), 0, random.nextInt(MAX_OFFSET + 1));
        if (!level.hasChunkAt(column)) {
            return;
        }
        BlockPos spawn = findGround(level, column, entry.ground());
        if (spawn == null || level.getBrightness(LightLayer.SKY, spawn) < MIN_SKY_LIGHT) {
            return;
        }

        int count = random.nextInt(entry.herd()) + 2;
        for (int i = 0; i < count; i++) {
            Mob mob = entry.type().create(level);
            if (mob == null) {
                return;
            }
            // No addon o grupo todo nascia no mesmo bloco, um dentro do outro, e se empurravam ao aparecer.
            // Aqui cada um procura um bloco livre do chão da entrada perto do ponto do grupo.
            BlockPos pos = spread(level, mob, spawn, entry.ground(), random);
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.NATURAL, null, null);
            level.addFreshEntity(mob);
        }
    }

    private static BlockPos spread(ServerLevel level, Mob mob, BlockPos center, Block ground, RandomSource random) {
        for (int i = 0; i < SPREAD_TRIES; i++) {
            BlockPos column = center.offset(random.nextIntBetweenInclusive(-SPREAD, SPREAD), 0,
                    random.nextIntBetweenInclusive(-SPREAD, SPREAD));
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos pos = column.above(dy);
                if (level.getBlockState(pos.below()).is(ground) && level.isEmptyBlock(pos)
                        && level.noCollision(mob, mob.getType().getAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))
                        && level.getEntities(mob, new AABB(pos)).isEmpty()) {
                    return pos;
                }
            }
        }
        return center;
    }

    // O detectSurfaceFloor do addon: o bloco de chão com ar em cima, até 10 blocos acima ou abaixo do jogador.
    private static BlockPos findGround(ServerLevel level, BlockPos column, Block ground) {
        for (int dy = -SEARCH_Y; dy <= SEARCH_Y; dy++) {
            BlockPos pos = column.above(dy);
            if (level.getBlockState(pos).is(ground) && level.isEmptyBlock(pos.above())) {
                return pos.above();
            }
        }
        return null;
    }
}
