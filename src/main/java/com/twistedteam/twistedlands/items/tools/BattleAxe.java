package com.twistedteam.twistedlands.items.tools;

import com.twistedteam.twistedlands.projectiles.ThrownAxeEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BattleAxe extends SwordItem {

    private static final float THROW_VELOCITY = 1.8F;

    public BattleAxe(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ThrownAxeEntity axe = new ThrownAxeEntity(level, player, stack);
            axe.setPos(
                    player.getX(),
                    player.getEyeY() - 0.1,
                    player.getZ()
            );

            Vec3 look = player.getLookAngle();
            axe.shoot(look.x, look.y, look.z, THROW_VELOCITY, 1.0F);

            level.addFreshEntity(axe);

            level.playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS,
                    1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 10);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}