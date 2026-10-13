package com.twistedteam.twistedlands.block;

import net.minecraft.world.level.block.Block;

/**
 * Lâmpada do teto que fica zumbindo. No Bedrock cada lâmpada tinha um tick no servidor a cada 120 ticks
 * só pra tocar o som. Aqui o som é todo no cliente (client/BackroomLampBuzz), essa classe só marca quais blocos zumbem.
 */
public class BackroomLampBlock extends Block {
    public BackroomLampBlock(Properties properties) {
        super(properties);
    }
}
