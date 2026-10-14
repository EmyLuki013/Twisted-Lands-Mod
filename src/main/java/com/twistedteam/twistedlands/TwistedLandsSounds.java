package com.twistedteam.twistedlands;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class TwistedLandsSounds {

    public static final SoundEvent CHAINSAW_SOUND_EVENT = register("ultimate_chainsaw");

    public static final SoundEvent NASTYSAURUS_STEP = register("nastysaurus.step");
    public static final SoundEvent NASTYSAURUS_HURT = register("nastysaurus.hurt");
    public static final SoundEvent NASTYSAURUS_ROAR = register("nastysaurus.roar");
    public static final SoundEvent NASTYSAURUS_AMBIENT = register("nastysaurus.ambient");

    public static final SoundEvent BACKROOM_LIGHT = register("block.backroom_light");

    public static final SoundEvent RAT_AMBIENT = register("rat.ambient");
    public static final SoundEvent RAT_HURT = register("rat.hurt");
    public static final SoundEvent RAT_DEATH = register("rat.death");

    // No addon o pavão usa os sons do avestruz.
    public static final SoundEvent PEACOCK_AMBIENT = register("peacock.ambient");
    public static final SoundEvent PEACOCK_HURT = register("peacock.hurt");
    public static final SoundEvent PEACOCK_DEATH = register("peacock.death");

    public static final SoundEvent PORTAL_AMBIENT = register("dimension.portal_ambient");
    public static final SoundEvent PORTAL_KICK = register("dimension.portal_kick");
    public static final SoundEvent DREAM_TRAVEL = register("dimension.dream_travel");

    private static SoundEvent register(String name) {
        ResourceLocation id = new ResourceLocation(TwistedLands.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {

    }
}
