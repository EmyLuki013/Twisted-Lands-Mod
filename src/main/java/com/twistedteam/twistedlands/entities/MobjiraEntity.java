package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.animations.MobjiraAttack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobjiraEntity extends Monster implements GeoEntity {

    public static final double MAX_HEALTH = 4500.0;
    public static final double ATTACK_DAMAGE = 175.0;
    public static final double MOVEMENT_SPEED = 0.3;
    public static final double FOLLOW_RANGE = 200.0;
    private static final float FOREIGN_DAMAGE_MULTIPLIER = 0.04F;

    private static final int SPAWN_TICKS = 200;
    private static final int SPAWN_ANIMATION_TICKS = 200;
    private static final int ABILITY_COOLDOWN_TICKS = 120;
    private static final int PREPARE_TICKS = 120;
    private static final double ABILITY_TRIGGER_RANGE = 99.0;

    private static final float CHARGED_AURA_DAMAGE = 185.0F;
    private static final double CHARGED_AURA_RANGE = 3.0;
    private static final float CHARGE_KNOCKBACK_DAMAGE = 50.0F;
    private static final double CHARGE_KNOCKBACK_RANGE = 28.0;
    private static final double CHARGE_KNOCKBACK_STRENGTH = 3.0;
    private static final float BEAM_DAMAGE = 205.0F;

    private static final float SUPERCHARGED_DAMAGE = 465.0F;
    private static final double SUPERCHARGED_SPEED = 0.49;
    private static final int SUPERCHARGED_TICKS = 300;

    private static final float CLOUD_DAMAGE = 80.0F;
    private static final double CLOUD_RANGE = 20.0;

    private static final float JUMP_IMPACT_DAMAGE = 250.0F;
    private static final double JUMP_IMPACT_RANGE = 48.0;
    private static final double JUMP_VELOCITY = 2.4;

    private static final float FIREBALL_DAMAGE = 550.0F;

    private static final Component BOSS_NAME = Component.literal("Mobjira");

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.210_mobjira.idle");
    private static final RawAnimation MOVE = RawAnimation.begin().thenLoop("animation.210_mobjira.move");
    private static final RawAnimation SPAWN =
            RawAnimation.begin().thenPlay("animation.210_mobjira.spawn");

    private static final java.util.UUID SUPERCHARGE_DAMAGE_ID =
            java.util.UUID.fromString("6d1f4a52-9c3b-4a0e-8f21-2b7c5d9e0a13");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(BOSS_NAME, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    @Nullable
    private MobjiraAttack currentAttack;
    @Nullable
    private MobjiraAttack lastAttack;
    private int attackElapsed;
    private int abilityCooldown = ABILITY_COOLDOWN_TICKS;
    private int prepareTicks = PREPARE_TICKS;
    private int introTicks = SPAWN_TICKS;
    private int superchargedTicks;
    private int attacksSinceSupercharge;

    public MobjiraEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 4000;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.ARMOR, 50.0)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 60.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        this.serverTick();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void serverTick() {
        if (this.introTicks > 0) {
            this.introTicks--;
            if (this.introTicks == 0) {
                this.playSound(SoundEvents.WARDEN_ROAR, 6.0F, 0.55F);
            }
            return;
        }

        if (this.superchargedTicks > 0) {
            this.superchargedTicks--;
            if (this.superchargedTicks == 0) {
                this.endSupercharged();
            }
        }

        if (this.currentAttack != null) {
            this.tickAttack();
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || this.distanceToSqr(target) > ABILITY_TRIGGER_RANGE * ABILITY_TRIGGER_RANGE) {
            this.prepareTicks = PREPARE_TICKS;
            return;
        }

        if (this.prepareTicks > 0) {
            this.prepareTicks--;
            return;
        }

        if (this.abilityCooldown > 0) {
            this.abilityCooldown--;
            return;
        }

        this.startAttack(this.pickAbility());
    }

    private MobjiraAttack pickAbility() {
        this.attacksSinceSupercharge++;
        if (this.attacksSinceSupercharge > 3) {
            this.attacksSinceSupercharge = 0;
            return MobjiraAttack.SUPERCHARGE;
        }

        MobjiraAttack[] pool = {
                MobjiraAttack.CHARGE_BEAM,
                MobjiraAttack.JUMP,
                MobjiraAttack.FIREBALL,
                MobjiraAttack.CLOUD,
        };

        MobjiraAttack choice = pool[this.random.nextInt(pool.length)];
        if (choice == this.lastAttack) {
            choice = pool[(this.random.nextInt(pool.length) + 1) % pool.length];
        }
        this.lastAttack = choice;
        return choice;
    }

    private void startAttack(MobjiraAttack attack) {
        this.currentAttack = attack;
        this.attackElapsed = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void tickAttack() {
        MobjiraAttack attack = this.currentAttack;
        int duration = attack.durationTicks();
        int impact = duration / 2;

        if (attack == MobjiraAttack.JUMP && this.attackElapsed == 1) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, JUMP_VELOCITY, 0.0));
            this.hurtMarked = true;
        }

        this.attackElapsed++;

        switch (attack) {
            case CHARGE_BEAM -> this.tickChargeBeam(impact);
            case FIREBALL -> this.tickFireball(impact);
            case CLOUD -> this.tickCloud();
            default -> { }
        }

        if (attack == MobjiraAttack.JUMP && this.attackElapsed == impact) {
            this.areaAttack(JUMP_IMPACT_DAMAGE, JUMP_IMPACT_RANGE);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, false,
                    Level.ExplosionInteraction.MOB);
            this.playSound(SoundEvents.GENERIC_EXPLODE, 8.0F, 0.5F);
        }

        if (attack == MobjiraAttack.SUPERCHARGE && this.attackElapsed == impact) {
            this.beginSupercharged();
        }

        if (this.attackElapsed >= duration) {
            this.currentAttack = null;
            this.abilityCooldown = ABILITY_COOLDOWN_TICKS;
        }
    }

    private void tickChargeBeam(int impact) {
        if (this.attackElapsed == impact) {
            this.roar(CHARGE_KNOCKBACK_DAMAGE, CHARGE_KNOCKBACK_RANGE, CHARGE_KNOCKBACK_STRENGTH);
            this.playSound(SoundEvents.BEACON_ACTIVATE, 6.0F, 0.6F);
        }

        if (this.attackElapsed > impact) {
            if (this.attackElapsed % 2 == 0) {
                for (int i = 0; i < 2; i++) {
                    this.shootAtTarget(BEAM_DAMAGE, 6.0F);
                }
            }
            if (this.attackElapsed % 10 == 0) {
                this.areaAttack(CHARGED_AURA_DAMAGE, CHARGED_AURA_RANGE);
            }
        }
    }

    private void tickFireball(int impact) {
        if (this.attackElapsed <= impact) {
            return;
        }
        if (this.attackElapsed % 2 == 0) {
            this.shootAtTarget(FIREBALL_DAMAGE, 10.0F);
        }
    }

    private void tickCloud() {
        if (this.attackElapsed % 10 == 0) {
            this.areaAttack(CLOUD_DAMAGE, CLOUD_RANGE);
        }
    }

    private void beginSupercharged() {
        this.superchargedTicks = SUPERCHARGED_TICKS;
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, SUPERCHARGED_TICKS, 1, false, false));
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SUPERCHARGED_TICKS, 2, false, false));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(SUPERCHARGED_SPEED);
        this.applySuperchargedDamage(true);
        this.playSound(SoundEvents.WARDEN_ROAR, 8.0F, 0.7F);
    }

    private void applySuperchargedDamage(boolean enabled) {
        var attribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(SUPERCHARGE_DAMAGE_ID);
        if (enabled) {
            attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    SUPERCHARGE_DAMAGE_ID, "Mobjira supercharge",
                    SUPERCHARGED_DAMAGE - ATTACK_DAMAGE,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
    }

    private void endSupercharged() {
        this.applySuperchargedDamage(false);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
    }

    private void roar(float damage, double range, double strength) {
        for (Entity entity : this.hostileTargetsIn(range)) {
            entity.hurt(this.damageSources().mobAttack(this), damage);
            Vec3 push = entity.position().subtract(this.position());
            if (push.lengthSqr() > 1.0E-4) {
                push = push.normalize().scale(strength);
                entity.push(push.x, 0.6, push.z);
                entity.hurtMarked = true;
            }
        }
        this.playSound(SoundEvents.WARDEN_ROAR, 5.0F, 0.5F);
    }

    private void areaAttack(float damage, double range) {
        for (Entity entity : this.hostileTargetsIn(range)) {
            entity.hurt(this.damageSources().mobAttack(this), damage);
        }
    }

    private List<Entity> hostileTargetsIn(double range) {
        AABB box = this.getBoundingBox().inflate(range);
        return this.level().getEntities(this, box,
                e -> e instanceof LivingEntity && e != this && !(e instanceof MobjiraEntity));
    }

    private void shootAtTarget(float damage, float uncertainty) {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5) - this.getEyeY();
        double dz = target.getZ() - this.getZ();

        SmallFireball fireball = new SmallFireball(this.level(), this,
                dx + this.random.nextGaussian() * uncertainty * 0.1,
                dy + this.random.nextGaussian() * uncertainty * 0.1,
                dz + this.random.nextGaussian() * uncertainty * 0.1);
        fireball.setPosRaw(this.getX() + dx * 0.1, this.getEyeY(), this.getZ() + dz * 0.1);
        this.level().addFreshEntity(fireball);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.introTicks > 0) {
            return false;
        }

        if (source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof MobjiraEntity) {
            return false;
        }

        float applied = attacker instanceof Player ? amount : amount * FOREIGN_DAMAGE_MULTIPLIER;
        return super.hurt(source, applied);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<MobjiraEntity> controller =
                new AnimationController<>(this, "main", 5, this::mainController);
        controller.setSoundKeyframeHandler(event -> {
            String sound = event.getKeyframeData().getSound();
            if (sound != null && sound.contains("explode")) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 3.0F, 0.6F, false);
            } else if (sound != null && sound.contains("roar")) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WARDEN_ROAR, this.getSoundSource(), 4.0F, 0.6F, false);
            }
        });
        controllers.add(controller);
    }

    private PlayState mainController(AnimationState<MobjiraEntity> state) {
        MobjiraEntity mob = state.getAnimatable();

        PlayState result;
        String chosen;

        boolean inSpawnAnimation = mob.tickCount <= SPAWN_ANIMATION_TICKS;

        if (mob.currentAttack != null) {
            chosen = mob.currentAttack.name();
            result = state.setAndContinue(mob.currentAttack.animation());
        } else if (inSpawnAnimation) {
            chosen = "SPAWN";
            result = state.setAndContinue(SPAWN);
        } else if (state.isMoving() || mob.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            chosen = "MOVE";
            result = state.setAndContinue(MOVE);
        } else {
            chosen = "IDLE";
            result = state.setAndContinue(IDLE);
        }

        if (++mob.controllerLogTicks % 40 == 0) {
            TwistedLands.LOGGER.info(
                    "[TwistedLands] mobjira anim: chosen={} result={} tickCount={} animTick={} attack={}",
                    chosen, result, mob.tickCount, state.getAnimationTick(),
                    mob.currentAttack == null ? "none" : mob.currentAttack.name());
        }

        return result;
    }

    private int controllerLogTicks;

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public boolean isIntro() {
        return this.tickCount <= SPAWN_ANIMATION_TICKS;
    }

    public boolean isSupercharged() {
        return this.superchargedTicks > 0;
    }

    @Nullable
    public MobjiraAttack getCurrentAttack() {
        return this.currentAttack;
    }
}
