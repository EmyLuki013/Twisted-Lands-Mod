package com.twistedteam.twistedlands.world.backroom;

import com.twistedteam.twistedlands.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Layout do Level 0, portado do script do Bedrock (level0.js). Tudo aqui depende só do x/z do mundo,
 * então uma coluna sai igual não importa qual chunk pergunte. O mapa do labirinto se repete infinitamente
 * e cada célula vira 3x3 blocos. O mapa em si está em resources/twisted/backroom/level0_maze.txt (1 = corredor).
 */
public final class BackroomMaze {
    public static final int FLOOR_Y = 32;
    public static final int CEILING_Y = 37;
    private static final int SCALE = 3;
    private static final double EXIT_CHANCE = 0.001;

    private static final int ROWS;
    private static final int COLS;
    private static final BitSet PATHS = new BitSet();

    static {
        List<String> lines = new ArrayList<>();
        try (InputStream in = BackroomMaze.class.getResourceAsStream("/twisted/backroom/level0_maze.txt")) {
            if (in == null) {
                throw new IllegalStateException("Missing level0_maze.txt");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line; (line = reader.readLine()) != null; ) {
                if (!line.isBlank()) {
                    lines.add(line.strip());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the backroom maze", e);
        }
        ROWS = lines.size();
        COLS = lines.get(0).length();
        for (int row = 0; row < ROWS; row++) {
            String line = lines.get(row);
            for (int col = 0; col < COLS; col++) {
                if (line.charAt(col) == '1') {
                    PATHS.set(row * COLS + col);
                }
            }
        }
    }

    private BackroomMaze() {
    }

    public static boolean isPath(int x, int z) {
        int row = Math.floorMod(Math.floorDiv(z, SCALE), ROWS);
        int col = Math.floorMod(Math.floorDiv(x, SCALE), COLS);
        return PATHS.get(row * COLS + col);
    }

    /** Bloco do meio de uma célula de corredor 3x3: é onde vão as luzes e as saídas. */
    private static boolean isCellCenter(int x, int z) {
        return Math.floorMod(x, SCALE) == 1 && Math.floorMod(z, SCALE) == 1 && isPath(x, z);
    }

    // Mesmo hash do script do Bedrock. O overflow de int do Java faz parte, não é bug.
    private static int hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return (h ^ (h >> 16)) & 0x7fffffff;
    }

    // Metade dos centros de célula ganha luz, e um terço dessas tem 2 blocos de largura (o centro + o bloco a oeste).
    private static boolean hasLight(int x, int z) {
        return isCellCenter(x, z) && hash(x, z) % 2 == 0;
    }

    private static boolean isLamp(int x, int z) {
        return hasLight(x, z) || (hasLight(x + 1, z) && hash(x + 1, z) % 3 == 0);
    }

    /** Bloco numa posição do mundo. Fora da faixa chão..teto é tudo ar. */
    public static BlockState stateAt(int x, int y, int z) {
        if (y < FLOOR_Y || y > CEILING_Y) {
            return Blocks.AIR.defaultBlockState();
        }
        if (!isPath(x, z)) {
            return ModBlocks.BACKROOMS_WALLPAPER.defaultBlockState();
        }
        if (y == FLOOR_Y) {
            return ModBlocks.WALLPAPER_PLANKS.defaultBlockState();
        }
        if (y == CEILING_Y) {
            return (isLamp(x, z) ? ModBlocks.WALLPAPER_SMOOTH_LAMP : ModBlocks.WALLPAPER_SMOOTH).defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * Canto da estrutura de saída neste chunk, ou null. No máximo uma por chunk, com o mesmo sorteio do Bedrock,
     * mas pulando as que nasceriam com as 4 portas de cara pra parede (no Bedrock era 1 em 25, ficava inútil).
     */
    @Nullable
    public static BlockPos exitIn(ChunkPos chunk) {
        for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
            for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
                if (isCellCenter(x, z) && (hash(x, z) % 1000) / 1000.0 < EXIT_CHANCE && hasOpenDoor(x, z)) {
                    return new BlockPos(x, FLOOR_Y, z);
                }
            }
        }
        return null;
    }

    // A backroom_exit é 5x5 com uma porta no meio de cada lado. O bloco na frente da porta precisa ser corredor.
    private static boolean hasOpenDoor(int cornerX, int cornerZ) {
        return isPath(cornerX + 2, cornerZ) || isPath(cornerX, cornerZ + 2)
                || isPath(cornerX + 4, cornerZ + 2) || isPath(cornerX + 2, cornerZ + 4);
    }

    /** Posição pra ficar de pé no corredor mais próximo de x/z (procura em anéis de células). */
    public static BlockPos nearestCorridor(int x, int z) {
        int cellX = Math.floorDiv(x, SCALE);
        int cellZ = Math.floorDiv(z, SCALE);
        for (int radius = 0; radius < Math.max(ROWS, COLS); radius++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int centerX = (cellX + dx) * SCALE + 1;
                    int centerZ = (cellZ + dz) * SCALE + 1;
                    if (isPath(centerX, centerZ)) {
                        return new BlockPos(centerX, FLOOR_Y + 1, centerZ);
                    }
                }
            }
        }
        return new BlockPos(x, FLOOR_Y + 1, z);
    }
}
