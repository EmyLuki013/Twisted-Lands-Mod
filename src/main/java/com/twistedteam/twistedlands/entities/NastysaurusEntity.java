package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsSounds;
import com.twistedteam.twistedlands.entities.animations.NastysaurusAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NastysaurusEntity extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NastysaurusEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        this.xpReward = 5000;
    }

    private int roarTimer = 40;
    private boolean shouldRoar = true;

    public static AttributeSupplier.Builder createNastysaurosAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 625.0)
                .add(Attributes.ATTACK_DAMAGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.ARMOR, 25.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.12D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, true));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity oldTarget = this.getTarget();
        super.setTarget(target);

        if (!this.level().isClientSide() && target != null && oldTarget == null) {
            this.shouldRoar = true;
            this.roarTimer = 40;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    TwistedLandsSounds.NASTYSAURUS_ROAR,
                    this.getSoundSource(), 3.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.roarTimer > 0) {
            this.roarTimer--;
            if (this.roarTimer == 0) {
                this.shouldRoar = false;
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (this.swinging) {
                boolean fast = this.getDeltaMovement().horizontalDistanceSqr() > 0.05D;
                return state.setAndContinue(fast
                        ? NastysaurusAnimations.run_attack
                        : NastysaurusAnimations.attack);
            }
            return state.setAndContinue(state.isMoving()
                    ? NastysaurusAnimations.walk
                    : NastysaurusAnimations.idle);
        }));

        controllers.add(new AnimationController<>(this, "roar", 0, state -> {
            if (this.shouldRoar) {
                return state.setAndContinue(NastysaurusAnimations.roar);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TwistedLandsSounds.NASTYSAURUS_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TwistedLandsSounds.NASTYSAURUS_HURT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TwistedLandsSounds.NASTYSAURUS_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(TwistedLandsSounds.NASTYSAURUS_STEP, 0.15F, 1.0F);
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

}