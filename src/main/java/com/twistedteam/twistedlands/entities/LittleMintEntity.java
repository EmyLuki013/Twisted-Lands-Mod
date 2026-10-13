package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * O Little Mint colocado no chão. Não leva dano, pode ser empurrado e ao interagir solta o item
 * em cima do jogador, igual ao loot do addon.
 */
public class LittleMintEntity extends Entity implements GeoEntity {
    private static final RawAnimation POSE = RawAnimation.begin().thenPlay("animation.little_mint.pose");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LittleMintEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        if (!isNoGravity()) {
            motion = motion.add(0.0, -0.04, 0.0);
        }
        move(MoverType.SELF, motion);
        motion = getDeltaMovement().scale(0.98);
        if (onGround()) {
            motion = motion.multiply(0.7, -0.5, 0.7);
        }
        setDeltaMovement(motion);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    // Só o void tira ele do mundo, igual ao damage_sensor do addon.
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && !isRemoved()) {
            player.spawnAtLocation(new ItemStack(TwistedLandsItems.LITTLE_MINT_HAT));
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TwistedLandsItems.LITTLE_MINT_HAT);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "pose", 0, state -> state.setAndContinue(POSE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
