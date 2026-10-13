package com.twistedteam.twistedlands.items.tools;

import com.twistedteam.twistedlands.TwistedLandsSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class UltimateChainsaw extends AxeItem {

    public UltimateChainsaw(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    private static final int MAX_BLOCKS = 128;

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        boolean result = super.mineBlock(stack, level, state, pos, entity);

        if (level.isClientSide || !state.is(BlockTags.LOGS)) {
            return result;
        }

        if (TwistedLandsSounds.CHAINSAW_SOUND_EVENT != null) {
            level.playSound(null, pos, TwistedLandsSounds.CHAINSAW_SOUND_EVENT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(pos);
        queue.add(pos);
        int broken = 0;

        while (!queue.isEmpty() && broken < MAX_BLOCKS && !stack.isEmpty()) {
            BlockPos current = queue.poll();

            for (BlockPos n : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                if (broken >= MAX_BLOCKS || stack.isEmpty()) {
                    break;
                }

                BlockPos next = n.immutable();
                if (!visited.add(next)) {
                    continue;
                }
                if (!level.getBlockState(next).is(BlockTags.LOGS)) {
                    continue;
                }

                queue.add(next);
                if (level.destroyBlock(next, true, entity)) {
                    broken++;
                    stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                }
            }
        }

        return result;
    }
}