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

    private static SoundEvent register(String name) {
        ResourceLocation id = new ResourceLocation(TwistedLands.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {

    }
}
