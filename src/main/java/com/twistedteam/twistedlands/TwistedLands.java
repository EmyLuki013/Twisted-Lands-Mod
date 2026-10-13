package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.items.TwistedLandsItemGroups;
import com.twistedteam.twistedlands.items.enchant.EnchantPresets;
import com.twistedteam.twistedlands.items.tools.UltimateHammer;
import com.twistedteam.twistedlands.world.TwistedLandsOreGen;
import com.twistedteam.twistedlands.world.backroom.Backroom;
import com.twistedteam.twistedlands.world.DimensionMobSpawner;
import com.twistedteam.twistedlands.world.dream.DreamWorldChunkGenerator;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwistedLands implements ModInitializer {
	public static final String MOD_ID = "twisted";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		FuelRegistry.INSTANCE.add(TwistedLandsItems.CRYSTAL_STICK, 30 * 20);

		TwistedLandsSounds.init();
		TwistedLandsItems.initialize();
		UltimateHammer.registerEvents();
		TwistedLandsBlocks.register();
		TwistedLandsItemGroups.initialize();
		TwistedLandsOreGen.register();
		TwistedLandsFeatures.initialize();
		TwistedLandsEntities.initialize();
		TwistedLandsEffects.initialize();

		EnchantPresets.registerTickHook();

		TwistedLandsAttributes.registerAttributes();
		TwistedLandsSpawns.initialize();
		Backroom.initialize();
		DreamWorldChunkGenerator.register();
		DimensionMobSpawner.initialize();
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
