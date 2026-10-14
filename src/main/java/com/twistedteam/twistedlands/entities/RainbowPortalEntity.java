package com.twistedteam.twistedlands.entities;

import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.TwistedLandsSounds;
import com.twistedteam.twistedlands.world.dream.DreamWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Rainbow Portal: o anel que leva pro Dream World e traz de volta. No addon é uma entidade sem colisão e sem
 * gravidade que não leva dano. Quem encosta na base (até 1.2 blocos) atravessa, inclusive mobs e itens.
 * Bater no criativo tira o portal; fora dele o portal chuta o jogador pra trás.
 */
public class RainbowPortalEntity extends Entity {
    public static final int FRAMES = 7;
    private static final double REACH = 1.2;
    // O animation controller do addon dispara o teleporte a cada tick; quem acabou de passar espera 2s (a tag
    // teleporting_dim do Teleporter.js).
    public static final int COOLDOWN = 40;
    // rainbow_sparks: 15 por segundo num anel de raio 2, meio bloco acima da base.
    private static final float SPARKS_PER_TICK = 15.0f / 20.0f;
    private static final double SPARKS_RADIUS = 2.0;

    public RainbowPortalEntity(EntityType<? extends RainbowPortalEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            clientTick();
            return;
        }
        List<Entity> touching = level().getEntities(this, getBoundingBox().inflate(REACH), entity ->
                !(entity instanceof RainbowPortalEntity) && entity.isAlive() && !entity.isSpectator()
                        && !entity.isPassenger() && !entity.isOnPortalCooldown()
                        && entity.position().distanceToSqr(position()) <= REACH * REACH);
        touching.forEach(DreamWorld::usePortal);
    }

    // O som ambiente fica no cliente (TwistedLandsClient), preso ao portal.
    private void clientTick() {
        int sparks = Mth.floor(tickCount * SPARKS_PER_TICK) - Mth.floor((tickCount - 1) * SPARKS_PER_TICK);
        for (int i = 0; i < sparks; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double speed = (1.0 + random.nextDouble() * 3.0) / 20.0;
            level().addParticle(TwistedLandsEntities.RAINBOW_SPARKS, getX() + Math.cos(angle) * SPARKS_RADIUS,
                    getY() + 0.5, getZ() + Math.sin(angle) * SPARKS_RADIUS, 0.0, speed, 0.0);
        }
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (!level().isClientSide && attacker instanceof Player player) {
            if (player.getAbilities().instabuild) {
                discard();
            } else {
                level().playSound(null, getX(), getY(), getZ(), TwistedLandsSounds.PORTAL_KICK, SoundSource.NEUTRAL, 1.0f, 1.0f);
                // applyKnockback({x: -dir.x * 2, z: -dir.z * 3}, 1) do addon, com a diferença entre x e z de lá.
                Vec3 look = player.getViewVector(1.0f);
                player.setDeltaMovement(-look.x * 2.0, 1.0, -look.z * 3.0);
                player.hurtMarked = true;
            }
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0 * 128.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
