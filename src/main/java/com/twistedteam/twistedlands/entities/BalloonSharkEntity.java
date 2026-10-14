package com.twistedteam.twistedlands.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomHoverTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Tubarão-Balão (twisted:balloon_shark), do Dream World. Voa pairando de 3 a 10 blocos acima do chão e persegue
 * o jogador que achar a até 35 blocos, sem largar até ele passar de 50 (set_persistent no addon). Ao morrer
 * estoura e empurra quem estiver a até 5 blocos. Tem 4 skins sorteadas ao nascer.
 */
public class BalloonSharkEntity extends PathfinderMob implements Enemy, GeoEntity, SmartBrainOwner<BalloonSharkEntity> {
    private static final RawAnimation FLAP = RawAnimation.begin().thenLoop("animation.balloon_shark.flap");
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(BalloonSharkEntity.class, EntityDataSerializers.INT);
    /** cloud, cloud_glow, unskinned e emoji, na ordem do render controller do addon. */
    public static final int VARIANTS = 4;
    // within_radius e max_dist do nearest_attackable_target.
    private static final int TARGET_RADIUS = 35;
    private static final double LOSE_TARGET_DISTANCE = 50.0;
    private static final double POP_RADIUS = 5.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BalloonSharkEntity(EntityType<? extends BalloonSharkEntity> type, Level level) {
        super(type, level);
        moveControl = new HoverMoveControl(this);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, LOSE_TARGET_DISTANCE);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(false);
        return navigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, 0);
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        entityData.set(VARIANT, random.nextInt(VARIANTS));
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VARIANT, Mth.clamp(tag.getInt("Variant"), 0, VARIANTS - 1));
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
    public List<? extends ExtendedSensor<? extends BalloonSharkEntity>> getSensors() {
        return List.of(
                // O NearbyPlayersSensor passa pelo isEntityTargetable vanilla, que corta em 16 blocos; este usa o follow range.
                new NearbyLivingEntitySensor<BalloonSharkEntity>().setRadius(TARGET_RADIUS),
                new NearbyPlayersSensor<BalloonSharkEntity>().setRadius(6),
                new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends BalloonSharkEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new FloatToSurfaceOfFluid<>(),
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends BalloonSharkEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<BalloonSharkEntity>(
                        new TargetOrRetaliate<BalloonSharkEntity>()
                                .attackablePredicate(this::isValidTarget),
                        new SetRandomHoverTarget<BalloonSharkEntity>() {
                            @Override
                            protected Vec3 getTargetPos(BalloonSharkEntity shark) {
                                return shark.findHoverPos();
                            }
                        }.setRadius(10, 10)),
                // look_at_player e look_at_entity: até 6 blocos, por 1 a 2s.
                new SetPlayerLookTarget<BalloonSharkEntity>()
                        .lookPredicate((shark, player) -> shark.distanceToSqr(player) <= 36.0)
                        .startCondition(shark -> shark.getRandom().nextFloat() < 0.02f)
                        .whenStopping(shark -> BrainUtils.withMemory(shark, MemoryModuleType.LOOK_TARGET,
                                target -> BrainUtils.setForgettableMemory(shark, MemoryModuleType.LOOK_TARGET, target,
                                        20 + shark.getRandom().nextInt(21)))));
    }

    @Override
    public BrainActivityGroup<? extends BalloonSharkEntity> getFightTasks() {
        return BrainActivityGroup.<BalloonSharkEntity>fightTasks(
                new InvalidateAttackTarget<BalloonSharkEntity>().invalidateIf((shark, target) ->
                        !shark.isValidTarget(target) || shark.distanceToSqr(target) > LOSE_TARGET_DISTANCE * LOSE_TARGET_DISTANCE),
                new SetWalkTargetToAttackTarget<>(),
                new AnimatableMeleeAttack<BalloonSharkEntity>(0))
                .wipeMemoriesWhenFinished(MemoryModuleType.LOOK_TARGET);
    }

    // random_hover do addon: até 10 blocos pros lados e na altura, e de 3 a 10 acima do chão. O HoverRandomPos
    // vanilla só garante a altura mínima, então o tubarão ia subindo a cada alvo novo até travar no teto.
    @Nullable
    private Vec3 findHoverPos() {
        for (int attempt = 0; attempt < 10; attempt++) {
            // Pra frente, até 90 graus pra cada lado, igual ao HoverRandomPos.
            float yaw = (getYRot() + (random.nextFloat() - 0.5f) * 180.0f) * Mth.DEG_TO_RAD;
            double distance = 2.0 + random.nextDouble() * 8.0;
            BlockPos column = BlockPos.containing(getX() - Mth.sin(yaw) * distance, getY(), getZ() + Mth.cos(yaw) * distance);
            Integer ground = groundBelow(column);
            if (ground == null) {
                continue;
            }
            double y = Mth.clamp(ground + 3 + random.nextInt(8), getY() - 10, getY() + 10);
            Vec3 pos = new Vec3(column.getX() + 0.5, y, column.getZ() + 0.5);
            if (level().noCollision(this, getBoundingBox().move(pos.subtract(position())))) {
                return pos;
            }
        }
        return null;
    }

    // Primeiro bloco sólido (ou líquido) descendo de 10 acima até 32 abaixo do tubarão. Nulo sobre o vazio.
    @Nullable
    private Integer groundBelow(BlockPos column) {
        BlockPos.MutableBlockPos pos = column.mutable().move(0, 10, 0);
        int bottom = Math.max(level().getMinBuildHeight(), column.getY() - 32);
        for (; pos.getY() >= bottom; pos.move(0, -1, 0)) {
            if (!level().getBlockState(pos).getCollisionShape(level(), pos).isEmpty() || !level().getFluidState(pos).isEmpty()) {
                return pos.getY() + 1;
            }
        }
        return null;
    }

    // Jogador fora do criativo ou quem bateu nele.
    private boolean isValidTarget(LivingEntity target) {
        if (!target.isAlive()) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild && !player.isSpectator();
        return target == BrainUtils.getMemory(this, MemoryModuleType.HURT_BY_ENTITY);
    }

    @Override
    public LivingEntity getTarget() {
        return BrainUtils.getTargetOfEntity(this);
    }

    // Família monster no addon: some no pacífico.
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    // No ar o vanilla usa 0.02 pra qualquer mob sem jogador montado, o que com speed 0.25 dá 1 bloco/s.
    // Com 0.1 ele voa uns 5 blocos/s, perto de um jogador andando.
    @Override
    protected float getFlyingSpeed() {
        return 0.1f;
    }

    // Voa, então queda não machuca.
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    // O estouro do mobs.js: som e vento, e quem está a até 5 blocos é jogado pra onde está olhando.
    // O vento do addon é o da wind charge, que o 1.20.1 não tem; aqui vai o som e a fumaça de explosão.
    @Override
    public void die(DamageSource source) {
        // Empurra antes do super.die, senão o vento arremessa o próprio drop.
        if (!isRemoved() && !dead && level() instanceof ServerLevel level) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.6f, 1.6f);
            level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 20, 0.6, 0.6, 0.6, 0.1);
            for (Entity entity : level.getEntities(this, getBoundingBox().inflate(POP_RADIUS),
                    entity -> entity.distanceToSqr(this) <= POP_RADIUS * POP_RADIUS && !entity.isSpectator())) {
                Vec3 look = entity.getViewVector(1.0f);
                entity.setDeltaMovement(entity.getDeltaMovement().add(look.x * 1.5, 0.7, look.z * 1.5));
                entity.hurtMarked = true;
            }
        }
        super.die(source);
    }

    /**
     * FlyingMoveControl vanilla com duas mudanças. Em subida ou descida quase reta o alvo fica a poucos décimos
     * pro lado e o atan2 desse resto virava o tubarão até 90 graus por tick, que era o giro do nada. Agora o
     * yaw só muda quando o alvo está a mais de meio bloco na horizontal, e no máximo 30 graus por tick
     * (o max_turn padrão do movement.hover do addon).
     */
    private static class HoverMoveControl extends MoveControl {
        private static final float MAX_TURN = 30.0f;

        HoverMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (operation != Operation.MOVE_TO) {
                mob.setYya(0.0f);
                mob.setZza(0.0f);
                return;
            }
            operation = Operation.WAIT;
            mob.setNoGravity(true);
            double dx = wantedX - mob.getX();
            double dy = wantedY - mob.getY();
            double dz = wantedZ - mob.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal * horizontal + dy * dy < 2.5E-7) {
                mob.setYya(0.0f);
                mob.setZza(0.0f);
                return;
            }
            boolean turn = horizontal > 0.5;
            if (turn) {
                float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
                mob.setYRot(rotlerp(mob.getYRot(), yaw, MAX_TURN));
            }
            float speed = (float) (speedModifier * mob.getAttributeValue(mob.onGround() ? Attributes.MOVEMENT_SPEED : Attributes.FLYING_SPEED));
            // Na subida reta não anda pra frente, senão sai pro lado que estiver olhando.
            mob.setSpeed(turn ? speed : 0.0f);
            float pitch = (float) -(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG);
            mob.setXRot(rotlerp(mob.getXRot(), pitch, MAX_TURN));
            mob.setYya(Math.abs(dy) > 1.0E-5 ? (dy > 0.0 ? speed : -speed) : 0.0f);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "flap", 0, state -> state.setAndContinue(FLAP)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
