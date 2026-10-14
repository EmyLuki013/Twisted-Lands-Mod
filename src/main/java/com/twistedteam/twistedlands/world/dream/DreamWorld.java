package com.twistedteam.twistedlands.world.dream;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.TwistedLandsSounds;
import com.twistedteam.twistedlands.entities.RainbowPortalEntity;
import com.twistedteam.twistedlands.network.ScreenFade;
import com.twistedteam.twistedlands.world.ReturnPoints;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Entrar e sair do Dream World pelo Rainbow Portal (o Teleporter.js do addon). Fora do Dream World o portal leva
 * pro spawn da dimensão; dentro, traz de volta pra 2 blocos ao lado de onde a entidade estava.
 * Sair pelo portal pede a armadura de Olho de Tigre completa (o desafio da Iris), menos no criativo.
 */
public final class DreamWorld {
    // spawnLoc do addon: (8, 66, 8). Aqui usa a mesma coluna, já em cima do chão.
    private static final int SPAWN_X = 8;
    private static final int SPAWN_Z = 8;
    private static final String RETURNS = "twisted_dream_world_returns";
    private static final String RESPAWNS = "twisted_dream_world_respawns";
    // O portal de volta nasce na primeira vez que alguém entra. No addon era uma dynamic property do mundo.
    private static final String PORTAL_PLACED = "twisted_dream_world_portal";
    private static final UUID WORLD = new UUID(0L, 0L);

    private DreamWorld() {
    }

    /** O que o Rainbow Portal faz com quem encosta nele. */
    public static void usePortal(Entity entity) {
        if (entity.level().dimension() != DreamWorldChunkGenerator.LEVEL) {
            enter(entity);
        } else if (canLeave(entity)) {
            leave(entity);
        } else if (entity instanceof Player player) {
            player.displayClientMessage(Component.translatable("rainbow_portal.exit_needs"), true);
        }
    }

    private static boolean canLeave(Entity entity) {
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return true;
        }
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            String id = BuiltInRegistries.ITEM.getKey(living.getItemBySlot(type.getSlot()).getItem()).toString();
            if (!id.equals(TwistedLands.id("tigers_eye_" + type.getName()).toString())) {
                return false;
            }
        }
        return true;
    }

    public static void enter(Entity entity) {
        MinecraftServer server = entity.getServer();
        ServerLevel dream = server == null ? null : server.getLevel(DreamWorldChunkGenerator.LEVEL);
        if (dream == null || entity.level().dimension() == DreamWorldChunkGenerator.LEVEL) {
            return;
        }
        BlockPos spawn = ground(dream, SPAWN_X, SPAWN_Z);
        if (entity instanceof ServerPlayer player) {
            UUID id = player.getUUID();
            ReturnPoints.get(server, RETURNS).put(id, new ReturnPoints.Point(GlobalPos.of(player.level().dimension(), player.blockPosition())));
            // Guarda o spawn de antes (cama, âncora) pra devolver na saída. No addon o spawn virava o ponto de saída.
            BlockPos oldRespawn = player.getRespawnPosition();
            if (oldRespawn != null && !ReturnPoints.get(server, RESPAWNS).has(id)) {
                ReturnPoints.get(server, RESPAWNS).put(id, new ReturnPoints.Point(
                        GlobalPos.of(player.getRespawnDimension(), oldRespawn), player.getRespawnAngle(), player.isRespawnForced()));
            }
            ScreenFade.send(player, 0xFFFFFF, 2, 200);
            player.teleportTo(dream, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
            player.setRespawnPosition(DreamWorldChunkGenerator.LEVEL, spawn, player.getYRot(), true, false);
            player.playNotifySound(TwistedLandsSounds.DREAM_TRAVEL, SoundSource.NEUTRAL, 1.0f, 1.0f);
            player.setPortalCooldown(RainbowPortalEntity.COOLDOWN);
        } else {
            teleport(entity, dream, Vec3.atBottomCenterOf(spawn));
        }
        placeReturnPortal(server, dream);
    }

    /** Volta pro ponto salvo, senão pro spawn do mundo (quem entrou por /execute não tem ponto salvo). */
    public static void leave(Entity entity) {
        MinecraftServer server = entity.getServer();
        if (server == null) {
            return;
        }
        UUID id = entity.getUUID();
        if (entity instanceof ServerPlayer player) {
            ReturnPoints.Point respawn = ReturnPoints.get(server, RESPAWNS).take(id);
            if (respawn != null) {
                player.setRespawnPosition(respawn.pos().dimension(), respawn.pos().pos(), respawn.angle(), respawn.forced(), false);
            } else if (player.getRespawnDimension() == DreamWorldChunkGenerator.LEVEL) {
                player.setRespawnPosition(Level.OVERWORLD, null, 0.0f, false, false);
            }
            ScreenFade.send(player, 0xFFFFFF, 2, 200);
        }

        ReturnPoints.Point back = entity instanceof ServerPlayer ? ReturnPoints.get(server, RETURNS).take(id) : null;
        ServerLevel level = back != null ? server.getLevel(back.pos().dimension()) : null;
        if (level != null) {
            // 2 blocos ao lado, como no addon, pra não voltar em cima do portal e entrar de novo.
            teleport(entity, level, Vec3.atBottomCenterOf(back.pos().pos().offset(2, 0, 2)));
            return;
        }
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        teleport(entity, overworld, Vec3.atBottomCenterOf(surface(overworld, spawn.getX(), spawn.getZ())));
    }

    private static void teleport(Entity entity, ServerLevel level, Vec3 pos) {
        Entity moved;
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
            moved = player;
        } else {
            moved = FabricDimensions.teleport(entity, level, new PortalInfo(pos, Vec3.ZERO, entity.getYRot(), entity.getXRot()));
        }
        if (moved != null) {
            moved.setPortalCooldown(RainbowPortalEntity.COOLDOWN);
        }
    }

    // dream_world.js, onFirstGeneration: o portal fica 2 blocos pra trás do spawn nos dois eixos.
    private static void placeReturnPortal(MinecraftServer server, ServerLevel dream) {
        ReturnPoints placed = ReturnPoints.get(server, PORTAL_PLACED);
        if (placed.has(WORLD)) {
            return;
        }
        RainbowPortalEntity portal = TwistedLandsEntities.RAINBOW_PORTAL.create(dream);
        if (portal == null) {
            return;
        }
        BlockPos pos = ground(dream, SPAWN_X - 2, SPAWN_Z - 2);
        portal.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        dream.addFreshEntity(portal);
        placed.put(WORLD, new ReturnPoints.Point(GlobalPos.of(DreamWorldChunkGenerator.LEVEL, pos)));
    }

    // Chão do Dream World, embaixo das ilhas (o heightmap pararia em cima delas). Sobe se tiver casa ou moinho ali.
    private static BlockPos ground(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int y = level.getChunkSource().getGenerator().getBaseHeight(x, z, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                level, level.getChunkSource().randomState());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        while (pos.getY() < level.getMaxBuildHeight() - 2
                && !(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir())) {
            pos.move(0, 1, 0);
        }
        return pos.immutable();
    }

    // Gera o chunk antes: em chunk que ainda não existe o heightmap devolve o fundo do mundo.
    private static BlockPos surface(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
    }
}
