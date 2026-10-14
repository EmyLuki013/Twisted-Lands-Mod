package com.twistedteam.twistedlands.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import com.mojang.datafixers.util.Pair;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomSwimTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Irukandji (twisted:irukandji), a água-viva. Nada à toa e ataca jogador que vê a até 10 blocos ou quem bate
 * nele: 20 de dano mais veneno, náusea e fraqueza. Quem bate nele de mão vazia leva o mesmo veneno e 20 de dano
 * (mobs.js do addon). Raio não machuca. Fora d'água não anda (movement 0 no addon) e fica sem ar em 5s.
 */
public class IrukandjiEntity extends WaterAnimal implements GeoEntity, SmartBrainOwner<IrukandjiEntity> {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.irukandji.idle");
    // must_see_forget_duration do addon (3s, o padrão).
    private static final int FORGET_UNSEEN_TICKS = 60;
    private static final float STING_DAMAGE = 20.0f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int unseenTicks;

    public IrukandjiEntity(EntityType<? extends IrukandjiEntity> type, Level level) {
        super(type, level);
        // max_turn 15 do movement.amphibious. Fora d'água a velocidade é 0.
        moveControl = new SmoothSwimmingMoveControl(this, 15, 10, 0.02f, 0.0f, false);
        lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    // underwater_movement 0.1 no addon. A conta é a do golfinho: 0.15 no Bedrock vira 1.2 aqui.
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.8)
                .add(Attributes.ATTACK_DAMAGE, STING_DAMAGE);
    }

    // Spawn rules do addon no overworld: oceano quente (y50 a y64) e qualquer água. A segunda cobre a primeira,
    // então basta estar dentro d'água.
    public static boolean canSpawn(EntityType<IrukandjiEntity> type, LevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).isSource() && level.getFluidState(pos.below()).isSource();
    }

    private static boolean isDeepWater(LevelAccessor level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    // breathable do addon: 5s de ar fora d'água.
    @Override
    public int getMaxAirSupply() {
        return 100;
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends IrukandjiEntity>> getSensors() {
        return List.of(
                new NearbyPlayersSensor<IrukandjiEntity>().setRadius(10),
                new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends IrukandjiEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrainActivityGroup<? extends IrukandjiEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<IrukandjiEntity>(
                        new TargetOrRetaliate<IrukandjiEntity>()
                                .useMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                                .attackablePredicate(this::isValidTarget),
                        // random_swim até 30 blocos de lado e 15 de altura; swim_idle de 5s com 5% de chance. Só vai
                        // pra água com 2 de fundo: ele tem 1.5 de altura e no raso ficava com metade do corpo pra fora.
                        new OneRandomBehaviour<IrukandjiEntity>(
                                Pair.of(new SetRandomSwimTarget<IrukandjiEntity>().setRadius(30, 15)
                                        .swimTargetPredicate((irukandji, pos) -> pos != null && isDeepWater(irukandji.level(), BlockPos.containing(pos))), 19),
                                Pair.of(new Idle<IrukandjiEntity>().runFor(irukandji -> 100), 1))));
    }

    @Override
    public BrainActivityGroup<? extends IrukandjiEntity> getFightTasks() {
        return BrainActivityGroup.<IrukandjiEntity>fightTasks(
                new InvalidateAttackTarget<IrukandjiEntity>().invalidateIf(IrukandjiEntity::shouldStopAttacking),
                new SetWalkTargetToAttackTarget<IrukandjiEntity>().speedMod((irukandji, target) -> 2.0f),
                new AnimatableMeleeAttack<IrukandjiEntity>(0));
    }

    private static boolean shouldStopAttacking(IrukandjiEntity irukandji, LivingEntity target) {
        irukandji.unseenTicks = irukandji.getSensing().hasLineOfSight(target) ? 0 : irukandji.unseenTicks + 1;
        return irukandji.unseenTicks > FORGET_UNSEEN_TICKS || !irukandji.isValidTarget(target);
    }

    // Jogador fora do criativo ou quem bateu nele.
    private boolean isValidTarget(LivingEntity target) {
        if (!target.isAlive()) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        return target == BrainUtils.getMemory(this, MemoryModuleType.HURT_BY_ENTITY);
    }

    @Override
    public LivingEntity getTarget() {
        return BrainUtils.getTargetOfEntity(this);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            poison(living);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.LIGHTNING_BOLT)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        // Encostar a mão nele queima.
        if (hurt && source.getDirectEntity() instanceof Player player && player.getMainHandItem().isEmpty()) {
            poison(player);
            player.hurt(damageSources().mobAttack(this), STING_DAMAGE);
        }
        return hurt;
    }

    private void poison(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1), this);
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100), this);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100), this);
    }

    // Sem experience_reward no addon.
    @Override
    public int getExperienceReward() {
        return 0;
    }

    // Igual ao golfinho, sem afundar parado (can_sink false no addon). O alvo do random_swim pode cair acima da
    // água; sem o empurrão pra baixo ele subia até a superfície e ficava boiando com o corpo todo pra fora.
    @Override
    public void travel(Vec3 input) {
        if (isControlledByLocalInstance() && isInWater()) {
            moveRelative(getSpeed(), input);
            if (!isUnderWater()) {
                setDeltaMovement(getDeltaMovement().x, Math.min(getDeltaMovement().y, 0.0) - 0.02, getDeltaMovement().z);
            }
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
        } else {
            super.travel(input);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
