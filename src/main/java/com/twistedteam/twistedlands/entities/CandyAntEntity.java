package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.world.candy.CandyLands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Candy Ant: pacífica. Interagir com ela leva pra Candy Lands, e lá dentro traz de volta. */
public class CandyAntEntity extends AntEntity {
    // Evita ir e voltar no mesmo clique: segurando o botão o interact dispara de novo logo depois do teleporte.
    private static final int TRAVEL_COOLDOWN = 40;
    private static final Map<UUID, Long> LAST_TRAVEL = new HashMap<>();

    public CandyAntEntity(EntityType<? extends CandyAntEntity> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Deixa a name tag funcionar normalmente.
        if (player.getItemInHand(hand).is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            // Tempo do servidor, não do mundo: cada dimensão tem o seu e o jogador troca de dimensão aqui.
            long now = serverPlayer.server.getTickCount();
            Long last = LAST_TRAVEL.get(player.getUUID());
            if (last == null || now - last >= TRAVEL_COOLDOWN || now < last) {
                LAST_TRAVEL.put(player.getUUID(), now);
                CandyLands.travel(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
}
