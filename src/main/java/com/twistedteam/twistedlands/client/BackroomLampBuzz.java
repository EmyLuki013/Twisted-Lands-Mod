package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.TwistedLandsSounds;
import com.twistedteam.twistedlands.block.BackroomLampBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Zumbido das lâmpadas: um único som em loop que fica na lâmpada mais perto do jogador e some quando não tem
 * nenhuma por perto. No Bedrock cada lâmpada tocava o próprio som, mas lá o som era estéreo (sem distância);
 * aqui várias cópias mono ou estouravam ou falhavam, então é uma só.
 */
public final class BackroomLampBuzz {
    // Procura a lâmpada mais perto a cada meio segundo. A área tem ~3700 blocos, é barato.
    private static final int SCAN_INTERVAL = 10;
    private static final int RANGE_XZ = 8;
    private static final int RANGE_Y = 6;
    // Som some aos 16 blocos, igual um som de bloco normal.
    private static final double FADE_DISTANCE = 16.0;
    // De costas pra lâmpada fica com 80% do volume de quando olha pra ela.
    private static final float BEHIND_VOLUME = 0.8f;

    private static int ticks;
    @Nullable
    private static Hum hum;

    private BackroomLampBuzz() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(BackroomLampBuzz::tick);
    }

    private static void tick(Minecraft client) {
        if (++ticks < SCAN_INTERVAL) {
            return;
        }
        ticks = 0;
        BlockPos lamp = client.level == null || client.player == null
                ? null : nearestLamp(client.level, client.player.blockPosition());
        if (lamp == null) {
            if (hum != null) {
                hum.end();
                hum = null;
            }
            return;
        }
        // Trocar de mundo para todos os sons, então confere se o nosso ainda está tocando.
        if (hum == null || !client.getSoundManager().isActive(hum)) {
            hum = new Hum(lamp);
            client.getSoundManager().play(hum);
        } else {
            hum.moveTo(lamp);
        }
    }

    @Nullable
    private static BlockPos nearestLamp(ClientLevel level, BlockPos center) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE_XZ, -RANGE_Y, -RANGE_XZ),
                center.offset(RANGE_XZ, RANGE_Y, RANGE_XZ))) {
            if (level.getBlockState(pos).getBlock() instanceof BackroomLampBlock) {
                double dist = pos.distSqr(center);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    /**
     * O som posicional do jogo abaixa muito quando a fonte fica do lado ou atrás da câmera. Por isso o som toca
     * "colado" no jogador (relativo, sem atenuação) e o volume é calculado aqui: cai com a distância e só um pouco
     * quando o jogador não está olhando pra lâmpada.
     */
    private static final class Hum extends AbstractTickableSoundInstance {
        private Vec3 lamp;

        Hum(BlockPos lamp) {
            super(TwistedLandsSounds.BACKROOM_LIGHT, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            looping = true;
            delay = 0;
            relative = true;
            attenuation = Attenuation.NONE;
            moveTo(lamp);
            // O jogo descarta som que começa com volume 0, então já calcula o volume antes de tocar.
            tick();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        void moveTo(BlockPos pos) {
            lamp = Vec3.atCenterOf(pos);
        }

        void end() {
            stop();
        }

        @Override
        public void tick() {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            Vec3 eye = player.getEyePosition();
            Vec3 toLamp = lamp.subtract(eye);
            double distance = toLamp.length();
            float fade = (float) Mth.clamp(1.0 - distance / FADE_DISTANCE, 0.0, 1.0);
            // dot = 1 olhando direto, -1 de costas.
            double dot = distance < 1.0E-4 ? 1.0 : player.getViewVector(1.0f).dot(toLamp.scale(1.0 / distance));
            float facing = Mth.lerp((float) (dot + 1.0) / 2.0f, BEHIND_VOLUME, 1.0f);
            volume = fade * facing;
        }
    }
}
