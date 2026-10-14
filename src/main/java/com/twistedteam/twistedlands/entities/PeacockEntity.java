package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.TwistedLandsSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.BreedWithPartner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Predicate;

/**
 * Pavão (twisted:peacock), do Dream World. Cruza e segue quem tem semente. O adulto ataca jogador que vê a até
 * 10 blocos ou quem bate nele, mas quem segura semente não é atacado (no addon o tempt tem prioridade sobre
 * escolher alvo). Cai devagar (slow_falling infinito do addon), sem dano de queda, e abre a cauda caindo ou
 * apaixonado. O mount_pathing do addon não faz nada (não é montável), ficou de fora.
 */
public class PeacockEntity extends Animal implements GeoEntity, SmartBrainOwner<PeacockEntity> {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.peacock.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.peacock.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.peacock.run");
    private static final RawAnimation OPEN_FEATHER = RawAnimation.begin().thenLoop("animation.peacock.open_feather");

    // breed_items, feed_items e tempt do addon.
    private static final Ingredient SEEDS = Ingredient.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS, Items.PITCHER_POD, Items.TORCHFLOWER_SEEDS);
    // must_see_forget_duration do addon (3s, o padrão).
    private static final int FORGET_UNSEEN_TICKS = 60;
    // O inLove só existe no servidor, e a cauda abre também quando ele está apaixonado.
    private static final EntityDataAccessor<Boolean> IN_LOVE = SynchedEntityData.defineId(PeacockEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private float moveSpeed;
    private int unseenTicks;

    public PeacockEntity(EntityType<? extends PeacockEntity> type, Level level) {
        super(type, level);
        addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, MobEffectInstance.INFINITE_DURATION, 255, false, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IN_LOVE, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SturdyGroundNavigation(this, level);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickBrain(this);
        entityData.set(IN_LOVE, isInLove());
    }

    @Override
    public List<? extends ExtendedSensor<? extends PeacockEntity>> getSensors() {
        return List.of(
                new NearbyPlayersSensor<PeacockEntity>().setRadius(10),
                new NearbyLivingEntitySensor<>(),
                new ItemTemptingSensor<PeacockEntity>().temptedWith(PeacockEntity::isFood),
                new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends PeacockEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new FloatToSurfaceOfFluid<>(),
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends PeacockEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                // breed, tempt, nearest_attackable_target e hurt_by_target (só adulto), follow_parent e
                // random_stroll, nessa ordem de prioridade.
                new FirstApplicableBehaviour<PeacockEntity>(
                        new BreedWithPartner<>(),
                        new FollowTemptation<PeacockEntity>().speedMod((peacock, player) -> 1.25f),
                        new TargetOrRetaliate<PeacockEntity>()
                                .useMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                                .attackablePredicate(this::isValidTarget)
                                .startCondition(peacock -> !peacock.isBaby()),
                        new FollowParent<PeacockEntity>().speedMod(1.1f),
                        new OneRandomBehaviour<PeacockEntity>(
                                new SetRandomWalkTarget<PeacockEntity>().speedModifier(0.8f),
                                new Idle<PeacockEntity>().runFor(peacock -> 60 + peacock.getRandom().nextInt(121)))),
                new FirstApplicableBehaviour<PeacockEntity>(
                        new SetPlayerLookTarget<PeacockEntity>()
                                .lookPredicate((peacock, player) -> peacock.distanceToSqr(player) <= 36.0)
                                .startCondition(peacock -> peacock.getRandom().nextFloat() < 0.02f)
                                .whenStopping(PeacockEntity::forgetLookTargetLater),
                        new SetRandomLookTarget<PeacockEntity>().lookTime(peacock -> lookTime(peacock.getRandom()))));
    }

    @Override
    public BrainActivityGroup<? extends PeacockEntity> getFightTasks() {
        return BrainActivityGroup.<PeacockEntity>fightTasks(
                // Quem pega semente na mão para de ser alvo, igual ao tempt passando na frente no addon.
                new InvalidateAttackTarget<PeacockEntity>().invalidateIf(PeacockEntity::shouldStopAttacking),
                new SetWalkTargetToAttackTarget<PeacockEntity>().speedMod((peacock, target) -> 2.0f),
                new AnimatableMeleeAttack<PeacockEntity>(0))
                .wipeMemoriesWhenFinished(MemoryModuleType.LOOK_TARGET);
    }

    // O InvalidateAttackTarget do SmartBrainLib só limpa a memória. O alvo vem dela, senão o getTarget fica
    // apontando pro alvo antigo.
    @Override
    public LivingEntity getTarget() {
        return BrainUtils.getTargetOfEntity(this);
    }

    private static int lookTime(RandomSource random) {
        return 40 + random.nextInt(41);
    }

    private static void forgetLookTargetLater(PeacockEntity peacock) {
        BrainUtils.withMemory(peacock, MemoryModuleType.LOOK_TARGET,
                target -> BrainUtils.setForgettableMemory(peacock, MemoryModuleType.LOOK_TARGET, target, lookTime(peacock.getRandom())));
    }

    private static boolean shouldStopAttacking(PeacockEntity peacock, LivingEntity target) {
        peacock.unseenTicks = peacock.getSensing().hasLineOfSight(target) ? 0 : peacock.unseenTicks + 1;
        return peacock.unseenTicks > FORGET_UNSEEN_TICKS || !peacock.isValidTarget(target);
    }

    // Jogador fora do criativo e sem semente na mão, ou quem bateu nele. Sem isso o TargetOrRetaliate, sem
    // jogador por perto, ataca qualquer mob que estiver vendo.
    private boolean isValidTarget(LivingEntity target) {
        if (!target.isAlive()) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().instabuild && !SEEDS.test(player.getMainHandItem()) && !SEEDS.test(player.getOffhandItem());
        }
        return target == BrainUtils.getMemory(this, MemoryModuleType.HURT_BY_ENTITY);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return SEEDS.test(stack);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return TwistedLandsEntities.PEACOCK.create(level);
    }

    // damage_sensor do addon: queda não machuca.
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TwistedLandsSounds.PEACOCK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TwistedLandsSounds.PEACOCK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TwistedLandsSounds.PEACOCK_DEATH;
    }

    // Mesmo caso do rato: sem isso o swinging fica ligado depois do primeiro ataque.
    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            double dx = getX() - xo;
            double dy = getY() - yo;
            double dz = getZ() - zo;
            float target = Math.min((float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 4.0f, 1.0f);
            moveSpeed += (target - moveSpeed) * 0.4f;
        }
    }

    // scripts.animate do addon. A cauda abre com variant 2 (caindo) ou apaixonado.
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                controller("idle", IDLE, state -> moveSpeed <= 0.01f),
                controller("walk", WALK, state -> moveSpeed >= 0.1f && moveSpeed < 0.5f && !swinging),
                controller("run", RUN, state -> moveSpeed >= 0.5f && !swinging),
                controller("open_feather", OPEN_FEATHER, state -> !onGround() || entityData.get(IN_LOVE)));
    }

    private AnimationController<PeacockEntity> controller(String name, RawAnimation animation,
                                                          Predicate<AnimationState<PeacockEntity>> condition) {
        return new AnimationController<>(this, name, 0, state -> {
            if (condition.test(state)) {
                return state.setAndContinue(animation);
            }
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getBoneResetTime() {
        return 3;
    }
}
