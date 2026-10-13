package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.client.renderer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class TwistedLandsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(TwistedLandsEntities.APPLE_COW, AppleCowRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.ENCHANTED_GOLDEN_APPLE_COW, EnchantedGoldenAppleCowRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.GOLDEN_APPLE_COW, GoldenAppleCowRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.MOBJIRA, MobjiraRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.MANTIS, MantisRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.THROWN_AXE, ThrownAxeRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.NASTYSAURUS, NastysaurusRenderer::new);
		EntityRendererRegistry.register(TwistedLandsEntities.RAINBOW_ANT,
				context -> new AntRenderer<>(context, "rainbow_ant", 0.8f));
		EntityRendererRegistry.register(TwistedLandsEntities.CANDY_ANT,
				context -> new AntRenderer<>(context, "candy_ant", 0.6f));
		EntityRendererRegistry.register(TwistedLandsEntities.LITTLE_MINT, LittleMintRenderer::new);

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), TwistedLandsBlocks.WALLPAPER, TwistedLandsBlocks.WALLPAPER_CORNER,
				TwistedLandsBlocks.PIZZA, TwistedLandsBlocks.EXTREME_TORCH, TwistedLandsBlocks.TOMATO_PLANT_HEAD, TwistedLandsBlocks.TOMATO_PLANT_BODY,
				TwistedLandsBlocks.SALT_DUST);
		BlockRenderLayerMap.INSTANCE.putBlock(TwistedLandsBlocks.COTTON_LEAVES, RenderType.cutoutMipped());
		BackroomLampBuzz.initialize();
		ScreenFadeOverlay.initialize();

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof PlayerRenderer player) {
				helper.register(new LittleMintHatLayer(player));
			}
		});
	}
}