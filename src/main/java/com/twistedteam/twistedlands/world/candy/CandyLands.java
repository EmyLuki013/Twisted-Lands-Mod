package com.twistedteam.twistedlands.world.candy;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.network.ScreenFade;
import com.twistedteam.twistedlands.world.ReturnPoints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.UUID;

/**
 * Entrar e sair da Candy Lands. No addon quem leva é a Candy Ant: interagir com ela fora da dimensão leva
 * pra lá, e dentro traz de volta pra onde o jogador estava.
 * Enquanto o jogador está lá dentro, o spawn dele fica na Candy Lands (morrer lá não te tira da dimensão).
 */
public final class CandyLands {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, TwistedLands.id("candy_lands"));

    // No addon era (8, 75, 8) com o jogador caindo até o chão. Aqui usa a mesma coluna, já em cima do chão.
    private static final int SPAWN_X = 8;
    private static final int SPAWN_Z = 8;
    private static final String RETURNS = "twisted_candy_lands_returns";
    private static final String RESPAWNS = "twisted_candy_lands_respawns";

    private CandyLands() {
    }

    /**
     * O que a Candy Ant faz ao interagir: entra se estiver fora, sai se estiver dentro.
     * Igual ao Teleporter do addon, a tela fica branca e clareia em 10s.
     */
    public static void travel(ServerPlayer player) {
        ScreenFade.send(player, 0xFFFFFF, 2, 200);
        if (player.level().dimension() == LEVEL) {
            leave(player);
        } else {
            enter(player);
        }
    }

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel candy = server.getLevel(LEVEL);
        if (candy == null || player.level().dimension() == LEVEL) {
            return;
        }
        UUID id = player.getUUID();
        ReturnPoints.get(server, RETURNS).put(id, new ReturnPoints.Point(GlobalPos.of(player.level().dimension(), player.blockPosition())));
        // Guarda o spawn de antes (cama, âncora) pra devolver na saída. No addon o spawn virava o ponto de saída
        // e a cama era esquecida.
        BlockPos oldRespawn = player.getRespawnPosition();
        if (oldRespawn != null && !ReturnPoints.get(server, RESPAWNS).has(id)) {
            ReturnPoints.get(server, RESPAWNS).put(id, new ReturnPoints.Point(
                    GlobalPos.of(player.getRespawnDimension(), oldRespawn), player.getRespawnAngle(), player.isRespawnForced()));
        }

        // Gera o chunk antes: em chunk que ainda não existe o heightmap devolve o fundo do mundo (caía no void).
        candy.getChunk(SPAWN_X >> 4, SPAWN_Z >> 4);
        BlockPos spawn = candy.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(SPAWN_X, 0, SPAWN_Z));
        player.teleportTo(candy, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setRespawnPosition(LEVEL, spawn, player.getYRot(), true, false);
    }

    /** Volta pro ponto salvo, senão pro spawn do mundo (quem entrou por /execute não tem ponto salvo). */
    public static void leave(ServerPlayer player) {
        MinecraftServer server = player.server;
        UUID id = player.getUUID();

        ReturnPoints.Point respawn = ReturnPoints.get(server, RESPAWNS).take(id);
        if (respawn != null) {
            player.setRespawnPosition(respawn.pos().dimension(), respawn.pos().pos(), respawn.angle(), respawn.forced(), false);
        } else if (player.getRespawnDimension() == LEVEL) {
            player.setRespawnPosition(Level.OVERWORLD, null, 0.0f, false, false);
        }

        ReturnPoints.Point back = ReturnPoints.get(server, RETURNS).take(id);
        ServerLevel level = back != null ? server.getLevel(back.pos().dimension()) : null;
        if (level != null) {
            // 2 blocos ao lado, como no addon, pra não voltar em cima da formiga e entrar de novo.
            BlockPos pos = back.pos().pos().offset(2, 0, 2);
            player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
            return;
        }
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos());
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }
}
