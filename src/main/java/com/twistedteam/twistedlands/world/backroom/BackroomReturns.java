package com.twistedteam.twistedlands.world.backroom;

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
 * Onde cada jogador estava antes de cair na Backroom. No Bedrock isso era uma dynamic property no jogador.
 * Fica salvo no mundo (data/twisted_backroom_returns.dat), então sobrevive a reiniciar o servidor.
 */
final class BackroomReturns extends SavedData {
    private static final String NAME = "twisted_backroom_returns";

    private final Map<UUID, GlobalPos> points = new HashMap<>();

    static BackroomReturns get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BackroomReturns::load, BackroomReturns::new, NAME);
    }

    void put(UUID player, GlobalPos pos) {
        points.put(player, pos);
        setDirty();
    }

    @Nullable
    GlobalPos take(UUID player) {
        GlobalPos pos = points.remove(player);
        if (pos != null) {
            setDirty();
        }
        return pos;
    }

    private static BackroomReturns load(CompoundTag tag) {
        BackroomReturns returns = new BackroomReturns();
        for (String key : tag.getAllKeys()) {
            CompoundTag entry = tag.getCompound(key);
            ResourceKey<Level> dimension =
                    ResourceKey.create(Registries.DIMENSION, new ResourceLocation(entry.getString("dimension")));
            BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
            returns.points.put(UUID.fromString(key), GlobalPos.of(dimension, pos));
        }
        return returns;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        points.forEach((player, pos) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", pos.dimension().location().toString());
            entry.putInt("x", pos.pos().getX());
            entry.putInt("y", pos.pos().getY());
            entry.putInt("z", pos.pos().getZ());
            tag.put(player.toString(), entry);
        });
        return tag;
    }
}
