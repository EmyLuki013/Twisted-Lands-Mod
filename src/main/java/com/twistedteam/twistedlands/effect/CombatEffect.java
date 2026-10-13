package com.twistedteam.twistedlands.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Efeito das poções de combate. No addon era um efeito próprio que, a cada segundo, dava velocidade, força,
 * regeneração e resistência escondidos: nível II na poção normal e nível IV na Combat II.
 */
public class CombatEffect extends MobEffect {
    public CombatEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x2E7D32);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // amplifier 0 = poção normal, 1 = Combat II.
        int level = amplifier > 0 ? 3 : 1;
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, level, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, level, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, level, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, level, false, false, false));
    }
}
