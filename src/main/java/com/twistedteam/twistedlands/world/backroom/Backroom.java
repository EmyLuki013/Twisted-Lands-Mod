package com.twistedteam.twistedlands.world.backroom;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.world.ReturnPoints;
import com.twistedteam.twistedlands.world.dream.DreamWorldChunkGenerator;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras da Backroom: o easter egg da cama que te joga lá dentro, as portas de bambu que te tiram,
 * e a proteção que no Bedrock era feita com blocos deny/allow (fora do criativo não quebra nem coloca bloco),
 * que vale também pra camada do labirinto embaixo do Dream World.
 */
public final class Backroom {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, TwistedLands.id("backroom"));

    private static final float BED_CHANCE = 0.0001f;
    private static final long MORNING_END = 5000;
    private static final BlockPos SPAWN = new BlockPos(8, BackroomMaze.FLOOR_Y + 1, 8);
    private static final String RETURNS = "twisted_backroom_returns";

    private record Task(long tick, Runnable action) {
    }

    // Fila simples de tarefas com atraso (em ticks). Só é usada na thread do servidor.
    private static final List<Task> TASKS = new ArrayList<>();

    private Backroom() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, TwistedLands.id("backroom"), BackroomChunkGenerator.CODEC);

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayer player) {
                onWakeUp(player);
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (destination.dimension() == LEVEL) {
                onEnter(player);
            }
        });
        UseBlockCallback.EVENT.register(Backroom::onUseBlock);
        // Cancelar o ataque no bloco (nos dois lados) impede começar a quebrar, sem bloco sumindo e voltando.
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                isLocked(player, level, pos) ? InteractionResult.FAIL : InteractionResult.PASS);
        ServerTickEvents.END_SERVER_TICK.register(Backroom::runTasks);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TASKS.clear());
    }

    /** Manda o jogador pra Backroom e guarda de onde ele veio. */
    public static void send(ServerPlayer player) {
        ServerLevel backroom = player.server.getLevel(LEVEL);
        if (backroom == null || player.level().dimension() == LEVEL) {
            return;
        }
        ReturnPoints.get(player.server, RETURNS).put(player.getUUID(),
                new ReturnPoints.Point(GlobalPos.of(player.level().dimension(), player.blockPosition())));
        BlockPos spot = BackroomMaze.nearestCorridor(SPAWN.getX(), SPAWN.getZ());
        player.teleportTo(backroom, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    /**
     * Tira o jogador da Backroom: volta pro ponto salvo, senão pra cama/âncora dele,
     * senão pro spawn do mundo (ex: quem entrou por /execute não tem ponto salvo).
     */
    public static void leave(ServerPlayer player) {
        MinecraftServer server = player.server;
        ReturnPoints.Point back = ReturnPoints.get(server, RETURNS).take(player.getUUID());
        ServerLevel level = back != null ? server.getLevel(back.pos().dimension()) : null;
        if (level != null) {
            BlockPos pos = back.pos().pos();
            player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
            return;
        }

        ServerLevel respawnLevel = server.getLevel(player.getRespawnDimension());
        BlockPos respawnPos = player.getRespawnPosition();
        if (respawnLevel != null && respawnPos != null) {
            Optional<Vec3> spot = Player.findRespawnPositionAndUseSpawnBlock(
                    respawnLevel, respawnPos, player.getRespawnAngle(), player.isRespawnForced(), true);
            if (spot.isPresent()) {
                Vec3 pos = spot.get();
                player.teleportTo(respawnLevel, pos.x, pos.y, pos.z, player.getRespawnAngle(), 0);
                return;
            }
        }

        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos());
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    // Igual ao Bedrock: quem dorme a noite toda tem 1 chance em 10000 de acordar aqui.
    // O STOP_SLEEPING também dispara se o jogador sair da cama no meio da noite, por isso o filtro de horário.
    private static void onWakeUp(ServerPlayer player) {
        if (player.level().dimension() == LEVEL || player.level().getDayTime() % 24000L >= MORNING_END) {
            return;
        }
        if (player.getRandom().nextFloat() < BED_CHANCE) {
            // Não dá pra teleportar dentro do evento de acordar, o jogo ainda está tirando o jogador da cama.
            schedule(player.server, 1, () -> {
                if (!player.isRemoved()) {
                    send(player);
                }
            });
        }
    }

    // No Bedrock a câmera escurecia (camera fade) e 3s depois tocava a respiração de cavalo.
    // O 1.20.1 não tem camera fade, então a cegueira faz esse papel.
    private static void onEnter(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false));
        schedule(player.server, 60, () -> {
            if (!player.isRemoved() && player.level().dimension() == LEVEL) {
                player.playNotifySound(SoundEvents.HORSE_BREATHE, SoundSource.AMBIENT, 1.0f, 0.8f);
            }
        });
    }

    // Roda no cliente e no servidor. O cliente devolve SUCCESS pra porta não abrir,
    // e quem teleporta de fato é o servidor.
    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        if (level.dimension() == LEVEL && level.getBlockState(hit.getBlockPos()).is(Blocks.BAMBOO_DOOR)) {
            if (player instanceof ServerPlayer serverPlayer) {
                leave(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }
        // O bloco vai no lado clicado, ou no próprio bloco clicado quando ele é substituível (grama, neve).
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        if ((isLocked(player, level, target) || isLocked(player, level, hit.getBlockPos()))
                && player.getItemInHand(hand).getItem() instanceof BlockItem) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    // A Backroom inteira, e no Dream World só a camada do labirinto (chão ao teto).
    private static boolean isLocked(Player player, Level level, BlockPos pos) {
        if (player.getAbilities().instabuild) {
            return false;
        }
        if (level.dimension() == DreamWorldChunkGenerator.LEVEL) {
            int floor = DreamWorldChunkGenerator.MAZE_FLOOR_Y;
            return pos.getY() >= floor && pos.getY() <= floor + BackroomMaze.CEILING_Y - BackroomMaze.FLOOR_Y;
        }
        return level.dimension() == LEVEL;
    }

    private static void schedule(MinecraftServer server, int delay, Runnable action) {
        TASKS.add(new Task(server.getTickCount() + delay, action));
    }

    private static void runTasks(MinecraftServer server) {
        if (TASKS.isEmpty()) {
            return;
        }
        long now = server.getTickCount();
        List<Task> due = new ArrayList<>();
        TASKS.removeIf(task -> task.tick() <= now && due.add(task));
        due.forEach(task -> task.action().run());
    }
}
