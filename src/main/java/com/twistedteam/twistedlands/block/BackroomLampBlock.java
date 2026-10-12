package com.twistedteam.twistedlands.block;

import com.twistedteam.twistedlands.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lâmpada do teto que fica zumbindo. No Bedrock cada lâmpada tinha um tick no servidor a cada 120 ticks
 * só pra tocar o som. Aqui é tudo no cliente via animateTick, que o jogo já chama só pra blocos perto do jogador.
 */
public class BackroomLampBlock extends Block {
    // O animateTick acerta uma lâmpada próxima umas 0,4 vezes por segundo. Com 1 em 3 fica perto
    // do Bedrock, que era um zumbido a cada 6s por lâmpada.
    private static final int BUZZ_CHANCE = 3;

    public BackroomLampBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(BUZZ_CHANCE) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    ModSounds.BACKROOM_LIGHT, SoundSource.BLOCKS, 0.3f, 1.0f, false);
        }
    }
}
