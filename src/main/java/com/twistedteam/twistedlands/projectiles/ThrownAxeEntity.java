package com.twistedteam.twistedlands.projectiles;

import com.google.common.collect.Multimap;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownAxeEntity extends ThrowableItemProjectile {

    private static final int MAX_LIFE_TICKS = 200;
    private static final int RETURN_DELAY_TICKS = 20;
    private static final double RETURN_SPEED = 1.4;
    private static final double CATCH_DISTANCE = 1.5;

    private boolean dealtDamage = false;
    private boolean returning = false;
    private int returnDelay = 0;
    private int returningHits = 0;

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownAxeEntity(Level level, LivingEntity thrower, ItemStack stack) {
        super(TwistedLandsEntities.THROWN_AXE, thrower, level);
        this.setItem(stack.copy());
    }

    @Override
    protected Item getDefaultItem() {
        return TwistedLandsItems.BATTLE_AXE;
    }

    public ItemStack getWeapon() {
        ItemStack stack = this.getItem();
        return stack.isEmpty() ? new ItemStack(getDefaultItem()) : stack;
    }

    private float getProjectileDamage() {
        ItemStack weapon = getWeapon();
        Multimap<Attribute, AttributeModifier> modifiers =
                weapon.getAttributeModifiers(EquipmentSlot.MAINHAND);

        double damage = 1.0D;

        for (AttributeModifier mod : modifiers.get(Attributes.ATTACK_DAMAGE)) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                damage += mod.getAmount();
            }
        }
        for (AttributeModifier mod : modifiers.get(Attributes.ATTACK_DAMAGE)) {
            if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                damage += mod.getAmount() * 1.0D;
            }
        }
        for (AttributeModifier mod : modifiers.get(Attributes.ATTACK_DAMAGE)) {
            if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                damage *= 1.0D + mod.getAmount();
            }
        }

        return (float) damage;
    }

    @Override
    public void tick() {
        super.tick();

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    getX(), getY(), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }

        if (returning) {
            tickReturn();
        } else if (returnDelay > 0) {
            returnDelay--;
            setDeltaMovement(Vec3.ZERO);
            if (returnDelay == 0) {
                returning = true;
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, getX(), getY(), getZ(),
                            SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }

        if (!level().isClientSide && tickCount > MAX_LIFE_TICKS) {
            if (returning) {
                giveBackToOwner(true);
            }
            discard();
        }
    }

    private void tickReturn() {
        Entity owner = getOwner();
        if (!(owner instanceof Player player) || !player.isAlive()) {
            if (!level().isClientSide) {
                spawnAtLocation(getWeapon(), 0.0F);
            }
            discard();
            return;
        }

        Vec3 ownerEye = player.getEyePosition();
        Vec3 toOwner = ownerEye.subtract(position());
        double distance = toOwner.length();

        setDeltaMovement(toOwner.normalize().scale(RETURN_SPEED));

        if (level() instanceof ServerLevel && returningHits < 3) {
            float damage = getProjectileDamage();
            for (Entity e : level().getEntities(this, getBoundingBox().inflate(0.4))) {
                if (e != player && e instanceof LivingEntity living && e.isAlive()) {
                    living.hurt(damageSources().thrown(this, player), damage);
                    returningHits++;
                    break;
                }
            }
        }

        if (distance < CATCH_DISTANCE) {
            giveBackToOwner(false);
            discard();
        }
    }

    private void giveBackToOwner(boolean dropIfFull) {
        if (!(getOwner() instanceof Player player)) return;

        ItemStack toGive = getWeapon().copy();
        if (!player.getInventory().add(toGive) && dropIfFull) {
            player.drop(toGive, false);
        }
        level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        HitResult.Type type = result.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) result);
        } else if (type == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) result);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner = getOwner();

        if (entity == owner) return;

        if (entity instanceof LivingEntity living && !dealtDamage) {
            float damage = getProjectileDamage();
            living.hurt(damageSources().thrown(this, owner), damage);
            dealtDamage = true;
        }

        returning = true;
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        setPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        setDeltaMovement(Vec3.ZERO);
        returnDelay = RETURN_DELAY_TICKS;

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Returning", returning);
        tag.putInt("ReturnDelay", returnDelay);
        tag.putBoolean("DealtDamage", dealtDamage);
        tag.putInt("ReturningHits", returningHits);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        returning = tag.getBoolean("Returning");
        returnDelay = tag.getInt("ReturnDelay");
        dealtDamage = tag.getBoolean("DealtDamage");
        returningHits = tag.getInt("ReturningHits");
    }
}