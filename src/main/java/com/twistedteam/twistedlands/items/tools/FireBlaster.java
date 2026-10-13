package com.twistedteam.twistedlands.items.tools;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireBlaster extends Item {

    private static final int COOLDOWN_TICKS = 10;

    public FireBlaster(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            SmallFireball fireball = new SmallFireball(
                    level, player,
                    look.x, look.y, look.z
            );

            fireball.setPos(
                    eye.x + look.x * 0.5,
                    eye.y + look.y * 0.5,
                    eye.z + look.z * 0.5
            );

            level.addFreshEntity(fireball);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}