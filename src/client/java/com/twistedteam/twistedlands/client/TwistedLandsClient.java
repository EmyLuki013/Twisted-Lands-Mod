package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class TwistedLandsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALLPAPER, RenderType.cutout());
	}
}