package com.twistedteam.twistedlands.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterBlaster extends Item {

    private static final int COOLDOWN_TICKS = 20;
    private static final double MAX_DISTANCE = 20.0D;

    public WaterBlaster(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(MAX_DISTANCE));

            BlockHitResult hit = level.clip(new ClipContext(
                    eye, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            Vec3 target = hit.getType() == HitResult.Type.BLOCK
                    ? hit.getLocation()
                    : end;

            spawnWaterParticles((ServerLevel) level, eye, target);

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = hit.getBlockPos().relative(hit.getDirection());
                BlockState current = level.getBlockState(hitPos);
                if (current.canBeReplaced()) {
                    level.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void spawnWaterParticles(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from);
        double length = direction.length();
        Vec3 step = direction.normalize().scale(0.5);

        for (double d = 0; d < length; d += 0.5) {
            Vec3 point = from.add(step.scale(d));
            level.sendParticles(ParticleTypes.SPLASH,
                    point.x, point.y, point.z,
                    3, 0.1, 0.1, 0.1, 0.02);
        }
    }
}