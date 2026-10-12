package com.twistedteam.twistedlands;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static SoundEvent register(String id) {
        ResourceLocation soundID = TwistedLands.id(id);

        return Registry.register(BuiltInRegistries.SOUND_EVENT, soundID, SoundEvent.createVariableRangeEvent(soundID));
    }

    public static final SoundEvent BACKROOM_LIGHT = register("block.backroom_light");

    public static void initialize() {
    }
}
