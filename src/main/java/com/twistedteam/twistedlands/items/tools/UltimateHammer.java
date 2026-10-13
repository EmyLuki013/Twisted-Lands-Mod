package com.twistedteam.twistedlands.items.tools;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UltimateHammer extends SwordItem {

    private static final float EXPLOSION_POWER = 4.0F;
    private static final int BLOCK_HIT_COOLDOWN_TICKS = 10;

    public UltimateHammer(Tier tier, int damage, float attackSpeed, Properties properties) {
        super(tier, damage, attackSpeed, properties);
    }

    public static void registerEvents() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            ItemStack held = player.getItemInHand(hand);

            if (!(held.getItem() instanceof UltimateHammer) || player.isSpectator()) {
                return InteractionResult.PASS;
            }
            if (player.getCooldowns().isOnCooldown(held.getItem())) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                level.explode(
                        player,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        EXPLOSION_POWER,
                        true,
                        Level.ExplosionInteraction.TNT
                );
                player.getCooldowns().addCooldown(held.getItem(), BLOCK_HIT_COOLDOWN_TICKS);
            }

            return InteractionResult.PASS;
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        Level level = target.level();
        if (!level.isClientSide) {
            level.explode(
                    attacker,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    EXPLOSION_POWER,
                    true,
                    Level.ExplosionInteraction.TNT
            );
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.twisted.ultimate_hammer.tooltip"));
    }
}