package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Predicate;

/**
 * Base das formigas (geometry.brown_ant no addon). Sobe parede igual aranha e toca as animações do
 * brown_ant.animation.json pelo GeckoLib.
 */
public abstract class AntEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(AntEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("animation.brown_ant.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenPlay("animation.brown_ant.walking");
    private static final RawAnimation CLIMBING_ANIM = RawAnimation.begin().thenPlay("animation.brown_ant.climbing");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.brown_ant.attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // Velocidade pras animações (query.modified_move_speed do addon). Só no cliente.
    private float moveSpeed;
    // Servidor: ticks seguidos encostada em parede (positivo) ou longe dela (negativo).
    private int wallTicks;
    // Servidor: onde e quando ela começou a última escalada saindo do chão, pra pegar o vai e volta.
    private BlockPos lastClimbStart;
    private int lastClimbStartTick;
    private int climbRetries;
    private boolean wasOnWall;

    protected AntEntity(EntityType<? extends AntEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CLIMBING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            updateMoveSpeed();
        } else {
            updateClimbingPose();
            checkClimbLoop();
            wasOnWall = horizontalCollision;
        }
    }

    // No addon a pose de escalada liga sempre que encosta em parede, e subindo degrau baixo ela virava de pé
    // e voltava a cada bloco. Aqui a pose só liga em parede de 3+ blocos (3 ticks encostada) e, ligada, fica
    // até ela sair da parede por 4 ticks, mesmo no fim da subida. A subida em si (onClimbable) continua igual.
    private void updateClimbingPose() {
        boolean posing = isClimbing();
        boolean onWall = horizontalCollision && (posing || wallHeightAtLeast(3));
        wallTicks = onWall ? Math.max(wallTicks, 0) + 1 : Math.min(wallTicks, 0) - 1;
        if (!posing && wallTicks >= 3) {
            entityData.set(CLIMBING, true);
        } else if (posing && wallTicks <= -4) {
            entityData.set(CLIMBING, false);
        }
    }

    private boolean wallHeightAtLeast(int blocks) {
        for (int dy = 1; dy < blocks; dy++) {
            if (level().noCollision(this, getBoundingBox().move(0.0, dy, 0.0).inflate(0.2, 0.0, 0.2))) {
                return false;
            }
        }
        return true;
    }

    // A caixa da formiga tem 1.4 de largura (igual ao addon), então em degrau de 1 bloco ela sobe, não cabe
    // em cima, cai e a navegação manda subir de novo no mesmo lugar pra sempre. Se ela começar a escalar
    // do chão no mesmo lugar 3 vezes seguidas (menos de 5s entre uma e outra), larga esse caminho e a IA
    // escolhe outro.
    private void checkClimbLoop() {
        if (!horizontalCollision || wasOnWall || !onGround()) {
            return;
        }
        BlockPos here = blockPosition();
        if (lastClimbStart != null && tickCount - lastClimbStartTick < 100 && lastClimbStart.closerThan(here, 1.5)) {
            if (++climbRetries >= 2) {
                getNavigation().stop();
                climbRetries = 0;
            }
        } else {
            climbRetries = 0;
        }
        lastClimbStart = here;
        lastClimbStartTick = tickCount;
    }

    // O walkAnimation do Java só conta o movimento horizontal, então subindo parede ficava 0 e as pernas
    // paravam. Aqui conta a subida também, com a mesma suavização do walkAnimation.
    private void updateMoveSpeed() {
        double dx = getX() - xo;
        double dy = getY() - yo;
        double dz = getZ() - zo;
        float target = Math.min((float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 4.0f, 1.0f);
        moveSpeed += (target - moveSpeed) * 0.4f;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public boolean isClimbing() {
        return entityData.get(CLIMBING);
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision;
    }

    // Cada golpe toca o ataque inteiro. Antes ele seguia o swing do braço (6 ticks) e era cortado no meio.
    // A animação foi refeita mais curta, ver brown_ant.animation.json e o checklist.
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("attack", "attack");
        }
        return hit;
    }

    // Igual ao scripts.animate do client entity: cada animação liga sozinha pela sua condição. O ataque vem
    // por último e manda nos ossos que divide com o andar. O olhar pro alvo fica no modelo do cliente.
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                controller("climbing", CLIMBING_ANIM, 5, state -> isClimbing()),
                controller("idle", IDLE, 0, state -> moveSpeed <= 0.01f),
                controller("walk", WALK, 0, state -> moveSpeed >= 0.1f),
                new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                        .triggerableAnim("attack", ATTACK));
    }

    private AnimationController<AntEntity> controller(String name, RawAnimation animation, int transitionTicks,
                                                      Predicate<AnimationState<AntEntity>> condition) {
        return new AnimationController<>(this, name, transitionTicks, state -> {
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

    // Quando uma animação desliga os ossos voltam pro lugar em 3 ticks em vez de pular (o Bedrock pula).
    @Override
    public double getBoneResetTime() {
        return 3;
    }

    // No addon, bater numa formiga segurando sal dá veneno e lentidão nela.
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && source.getEntity() instanceof Player player
                && player.getMainHandItem().is(TwistedLandsItems.SALT)) {
            addEffect(new MobEffectInstance(MobEffects.POISON, 40, 1));
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        }
        return hurt;
    }
}
