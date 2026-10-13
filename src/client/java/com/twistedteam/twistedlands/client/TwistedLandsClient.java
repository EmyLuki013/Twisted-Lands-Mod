package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.ModBlocks;
import com.twistedteam.twistedlands.ModEntities;
import com.twistedteam.twistedlands.client.entity.AntRenderer;
import com.twistedteam.twistedlands.client.entity.LittleMintHatLayer;
import com.twistedteam.twistedlands.client.entity.LittleMintRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class TwistedLandsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ModBlocks.WALLPAPER, ModBlocks.WALLPAPER_CORNER,
				ModBlocks.PIZZA, ModBlocks.EXTREME_TORCH, ModBlocks.TOMATO_PLANT_HEAD, ModBlocks.TOMATO_PLANT_BODY,
				ModBlocks.SALT_DUST);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COTTON_LEAVES, RenderType.cutoutMipped());
		BackroomLampBuzz.initialize();
		ScreenFadeOverlay.initialize();

		EntityRendererRegistry.register(ModEntities.RAINBOW_ANT,
				context -> new AntRenderer<>(context, "rainbow_ant", 0.8f));
		EntityRendererRegistry.register(ModEntities.CANDY_ANT,
				context -> new AntRenderer<>(context, "candy_ant", 0.6f));
		EntityRendererRegistry.register(ModEntities.LITTLE_MINT, LittleMintRenderer::new);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof PlayerRenderer player) {
				helper.register(new LittleMintHatLayer(player));
			}
		});
	}
}
