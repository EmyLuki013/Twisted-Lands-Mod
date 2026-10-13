package com.twistedteam.twistedlands.item;

import com.twistedteam.twistedlands.ModEntities;
import com.twistedteam.twistedlands.entity.LittleMintEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Little Mint: na mão secundária aparece no ombro do jogador. Usado num bloco, coloca o boneco no chão
 * (o entity_placer do addon).
 */
public class LittleMintItem extends Item {
    public LittleMintItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        LittleMintEntity mint = ModEntities.LITTLE_MINT.create(level);
        if (mint == null) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        // Virado pro jogador.
        mint.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation() + 180.0f, 0.0f);
        level.addFreshEntity(mint);
        level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);

        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
