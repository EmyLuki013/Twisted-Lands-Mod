package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.effect.CombatEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {
    public static final MobEffect COMBAT = Registry.register(BuiltInRegistries.MOB_EFFECT, TwistedLands.id("combat"), new CombatEffect());

    public static void initialize() {
    }
}
