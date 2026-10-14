package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.BreedWithPartner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowTemptation;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

/**
 * Vaca-Maçã de Cristal (twisted:crystal_apple_cow), do Dream World. No addon é a vaca do Bedrock com outra textura
 * e outro loot: leite com balde, cruza e é atraída por trigo, 5% nasce filhote. A Cow do Java já faz o leite, o
 * cruzamento e o filhote, aqui só a IA troca pra SmartBrainLib com as mesmas prioridades do addon.
 * O mount_pathing e o rideable (zumbi montando) ficaram de fora, a vaca do Java não tem.
 */
public class CrystalAppleCowEntity extends Cow implements SmartBrainOwner<CrystalAppleCowEntity> {
    public CrystalAppleCowEntity(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SturdyGroundNavigation(this, level);
    }

    // Os goals da Cow saem, quem manda é o brain.
    @Override
    protected void registerGoals() {
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends CrystalAppleCowEntity>> getSensors() {
        return List.of(
                new NearbyPlayersSensor<>(),
                // Par pro cruzamento e mãe pro filhote seguir.
                new NearbyLivingEntitySensor<>(),
                // tempt com trigo, o isFood da Cow.
                new ItemTemptingSensor<CrystalAppleCowEntity>().temptedWith(Cow::isFood),
                new HurtBySensor<>());
    }

    @Override
    public BrainActivityGroup<? extends CrystalAppleCowEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new FloatToSurfaceOfFluid<>(),
                new Panic<CrystalAppleCowEntity>().speedMod(cow -> 1.25f),
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<? extends CrystalAppleCowEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                // breed, tempt, follow_parent e random_stroll, nessa ordem de prioridade.
                new FirstApplicableBehaviour<CrystalAppleCowEntity>(
                        new BreedWithPartner<>(),
                        new FollowTemptation<CrystalAppleCowEntity>().speedMod((cow, player) -> 1.25f),
                        new FollowParent<CrystalAppleCowEntity>().speedMod(1.1f),
                        // random_stroll: no Bedrock tenta andar 1 vez a cada 120 ticks em média.
                        new OneRandomBehaviour<CrystalAppleCowEntity>(
                                new SetRandomWalkTarget<CrystalAppleCowEntity>().speedModifier(0.8f),
                                new Idle<CrystalAppleCowEntity>().runFor(cow -> 60 + cow.getRandom().nextInt(121)))),
                // look_at_player: 2% de chance por tick, jogador a até 6 blocos. Senão olha em volta.
                new FirstApplicableBehaviour<CrystalAppleCowEntity>(
                        new SetPlayerLookTarget<CrystalAppleCowEntity>()
                                .lookPredicate((cow, player) -> cow.distanceToSqr(player) <= 36.0)
                                .startCondition(cow -> cow.getRandom().nextFloat() < 0.02f)
                                .whenStopping(CrystalAppleCowEntity::forgetLookTargetLater),
                        new SetRandomLookTarget<CrystalAppleCowEntity>().lookTime(cow -> lookTime(cow.getRandom()))));
    }

    private static int lookTime(RandomSource random) {
        return 40 + random.nextInt(41);
    }

    // Igual ao rato: o SetPlayerLookTarget guarda o jogador sem prazo, aqui ganha o prazo do look_time do addon.
    private static void forgetLookTargetLater(CrystalAppleCowEntity cow) {
        BrainUtils.withMemory(cow, MemoryModuleType.LOOK_TARGET,
                target -> BrainUtils.setForgettableMemory(cow, MemoryModuleType.LOOK_TARGET, target, lookTime(cow.getRandom())));
    }

    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return TwistedLandsEntities.CRYSTAL_APPLE_COW.create(level);
    }
}
