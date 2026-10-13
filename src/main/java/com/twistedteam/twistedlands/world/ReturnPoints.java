package com.twistedteam.twistedlands.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Posições salvas por jogador, uma por arquivo em data/ do mundo, então sobrevivem a reiniciar o servidor.
 * Usado pra lembrar de onde o jogador veio antes de entrar numa dimensão do mod (no Bedrock era uma
 * dynamic property no jogador) e qual era o spawn dele.
 */
public final class ReturnPoints extends SavedData {
    /** angle e forced só importam quando o ponto é um spawn (cama, âncora ou /spawnpoint). */
    public record Point(GlobalPos pos, float angle, boolean forced) {
        public Point(GlobalPos pos) {
            this(pos, 0.0f, false);
        }
    }

    private final Map<UUID, Point> points = new HashMap<>();

    /** name = nome do arquivo, ex: "twisted_backroom_returns". */
    public static ReturnPoints get(MinecraftServer server, String name) {
        return server.overworld().getDataStorage().computeIfAbsent(ReturnPoints::load, ReturnPoints::new, name);
    }

    public void put(UUID player, Point point) {
        points.put(player, point);
        setDirty();
    }

    public boolean has(UUID player) {
        return points.containsKey(player);
    }

    @Nullable
    public Point take(UUID player) {
        Point point = points.remove(player);
        if (point != null) {
            setDirty();
        }
        return point;
    }

    private static ReturnPoints load(CompoundTag tag) {
        ReturnPoints returns = new ReturnPoints();
        for (String key : tag.getAllKeys()) {
            CompoundTag entry = tag.getCompound(key);
            ResourceKey<Level> dimension =
                    ResourceKey.create(Registries.DIMENSION, new ResourceLocation(entry.getString("dimension")));
            BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
            returns.points.put(UUID.fromString(key),
                    new Point(GlobalPos.of(dimension, pos), entry.getFloat("angle"), entry.getBoolean("forced")));
        }
        return returns;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        points.forEach((player, point) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", point.pos().dimension().location().toString());
            entry.putInt("x", point.pos().pos().getX());
            entry.putInt("y", point.pos().pos().getY());
            entry.putInt("z", point.pos().pos().getZ());
            entry.putFloat("angle", point.angle());
            entry.putBoolean("forced", point.forced());
            tag.put(player.toString(), entry);
        });
        return tag;
    }
}
