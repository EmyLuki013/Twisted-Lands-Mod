package com.twistedteam.twistedlands.entity;

import com.twistedteam.twistedlands.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;
import java.util.Set;

/** Formiga arco-íris: neutra, só ataca quem bate nela. Açúcar ou certos nomes viram ela em Candy Ant. */
public class RainbowAntEntity extends AntEntity {
    private static final Set<String> CANDY_NAMES = Set.of("candy", "candy_ant", "femboy", "femboy_ant", "trans", "trans_ant");

    public RainbowAntEntity(EntityType<? extends RainbowAntEntity> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 100.0);
    }

    // Spawn rule do addon: no subterrâneo, em cima de terra, luz de 0 a 7, fora do pacífico.
    public static boolean canSpawn(EntityType<RainbowAntEntity> type, ServerLevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getBlockState(pos.below()).is(Blocks.DIRT)
                && !level.canSeeSky(pos)
                && level.getMaxLocalRawBrightness(pos) <= 7;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.SUGAR)) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            becomeCandyAnt();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    // O addon checava o nome ao usar a name tag. Aqui olha o nome a cada meio segundo, pega name tag e /summon.
    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && tickCount % 10 == 0 && hasCustomName()
                && CANDY_NAMES.contains(getCustomName().getString().toLowerCase(Locale.ROOT))) {
            becomeCandyAnt();
        }
    }

    private void becomeCandyAnt() {
        convertTo(ModEntities.CANDY_ANT, true);
    }
}
