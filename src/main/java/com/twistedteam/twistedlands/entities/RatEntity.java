package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
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
 * Rato (twisted:rat). Anda à toa e ataca jogador que vê a até 10 blocos ou quem bate nele. A mordida dá fome.
 * O breed, follow_parent e mount_pathing do addon não fazem nada lá (falta breedable, ageable e rideable),
 * então ficaram de fora.
 */
public class RatEntity extends PathfinderMob implements GeoEntity, SmartBrainOwner<RatEntity> {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("animation.rat.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenPlay("animation.rat.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenPlay("animation.rat.run");
    // Não existe no addon (lá ele só parava de andar ao morder). Mordida curta nossa, no rat.animation.json.
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.rat.attack");

    // must_see_forget_duration do addon (3s, o padrão): sem ver o alvo por esse tempo, desiste dele.
    private static final int FORGET_UNSEEN_TICKS = 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // Velocidade pras animações (query.modified_move_speed do addon). Só no cliente.
    private float moveSpeed;
    private int unseenTicks;

    public RatEntity(EntityType<? extends RatEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    // Spawn rule do addon: em bioma de caverna (tag caves), sem ver o céu e com luz de 0 a 4.
    public static boolean canSpawn(EntityType<RatEntity> type, ServerLevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        return Mob.checkMobSpawnRules(type, level, reason, pos, random)
                && !level.canSeeSky(pos)
                && level.getMaxLocalRawBrightness(pos) <= 4;
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
    public List<? extends ExtendedSensor<? extends RatEntity>> getSensors() {
        return List.of(
                // nearest_attackable_target: jogador a até 10 blocos, só se estiver vendo.
                new NearbyPlayersSensor<RatEntity>().setRadius(10),
                new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends RatEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new FloatToSurfaceOfFluid<>(),
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends RatEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<RatEntity>(
                        new TargetOrRetaliate<RatEntity>().useMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER),
                        // look_at_player: 2% de chance por tick, jogador a até 6 blocos, olha por 2 a 4s.
                        new SetPlayerLookTarget<RatEntity>()
                                .lookPredicate((rat, player) -> rat.distanceToSqr(player) <= 36.0 && rat.getSensing().hasLineOfSight(player))
                                .startCondition(rat -> rat.getRandom().nextFloat() < 0.02f)
                                .whenStopping(RatEntity::forgetLookTargetLater),
                        new SetRandomLookTarget<RatEntity>().lookTime(rat -> lookTime(rat.getRandom()))),
                // random_stroll: no Bedrock tenta andar 1 vez a cada 120 ticks em média, até 10 blocos de lado e 7 de altura.
                new OneRandomBehaviour<RatEntity>(
                        new SetRandomWalkTarget<RatEntity>().setRadius(10, 7).speedModifier(0.8f),
                        new Idle<RatEntity>().runFor(rat -> 60 + rat.getRandom().nextInt(121))));
    }

    @Override
    public BrainActivityGroup<? extends RatEntity> getFightTasks() {
        return BrainActivityGroup.<RatEntity>fightTasks(
                new InvalidateAttackTarget<RatEntity>().invalidateIf(RatEntity::lostSightTooLong),
                new SetWalkTargetToAttackTarget<RatEntity>().speedMod((rat, target) -> 2.0f),
                new AnimatableMeleeAttack<RatEntity>(0))
                // Sem isso ele continua olhando pro último alvo depois da luta.
                .wipeMemoriesWhenFinished(MemoryModuleType.LOOK_TARGET);
    }

    private static int lookTime(RandomSource random) {
        return 40 + random.nextInt(41);
    }

    // O SetPlayerLookTarget guarda o jogador sem prazo. Aqui ele vira memória com prazo, igual ao look_time do addon.
    private static void forgetLookTargetLater(RatEntity rat) {
        BrainUtils.withMemory(rat, MemoryModuleType.LOOK_TARGET,
                target -> BrainUtils.setForgettableMemory(rat, MemoryModuleType.LOOK_TARGET, target, lookTime(rat.getRandom())));
    }

    private static boolean lostSightTooLong(RatEntity rat, LivingEntity target) {
        rat.unseenTicks = rat.getSensing().hasLineOfSight(target) ? 0 : rat.unseenTicks + 1;
        return rat.unseenTicks > FORGET_UNSEEN_TICKS;
    }

    // O alcance de ataque é o padrão do Java. O reach_multiplier 8.5 do addon dava 4.25 blocos e ele mordia
    // sem encostar no jogador.

    // Dano de 1 a 2 e fome por 30s, como o minecraft:attack do addon.
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = target.hurt(damageSources().mobAttack(this), 1 + random.nextInt(2));
        if (hit) {
            doEnchantDamageEffects(this, target);
            setLastHurtMob(target);
            triggerAnim("attack", "attack");
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30 * 20), this);
            }
        }
        return hit;
    }

    // experience_reward do addon: 1 a 3 quando morre pra jogador (o Java só solta XP nesse caso).
    @Override
    public int getExperienceReward() {
        return 1 + random.nextInt(3);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TwistedLandsSounds.RAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TwistedLandsSounds.RAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TwistedLandsSounds.RAT_DEATH;
    }

    // O vanilla só zera o swing no aiStep do Monster. Sem isso o swinging fica ligado depois da primeira mordida
    // e o andar e o correr nunca mais tocam.
    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            updateMoveSpeed();
        }
    }

    // Mesma conta das formigas, com a mesma suavização do walkAnimation.
    private void updateMoveSpeed() {
        double dx = getX() - xo;
        double dy = getY() - yo;
        double dz = getZ() - zo;
        float target = Math.min((float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 4.0f, 1.0f);
        moveSpeed += (target - moveSpeed) * 0.4f;
    }

    // scripts.animate do addon. variable.attack_time é o swing do ataque: enquanto ele dura, andar e correr param.
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                controller("idle", IDLE, state -> moveSpeed <= 0.01f),
                controller("walk", WALK, state -> moveSpeed >= 0.1f && moveSpeed < 0.5f && !swinging),
                controller("run", RUN, state -> moveSpeed >= 0.5f && !swinging),
                new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                        .triggerableAnim("attack", ATTACK));
    }

    private AnimationController<RatEntity> controller(String name, RawAnimation animation,
                                                      Predicate<AnimationState<RatEntity>> condition) {
        return new AnimationController<>(this, name, 0, state -> {
            if (condition.test(state)) {
                return state.setAndContinue(animation);
            }
            // Na próxima vez que a condição ligar, a animação recomeça do zero como no Bedrock.
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Igual às formigas: os ossos voltam pro lugar em 3 ticks em vez de pular.
    @Override
    public double getBoneResetTime() {
        return 3;
    }
}
