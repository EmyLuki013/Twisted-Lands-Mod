package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.client.renderer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.RatEntity;

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
		// scale 0.5 do addon. Sombra com metade da largura da hitbox, como nas formigas.
		EntityRendererRegistry.register(TwistedLandsEntities.RAT,
				context -> new GeoEntityRenderer<RatEntity>(context, new DefaultedEntityGeoModel<>(TwistedLands.id("rat"))) {
					{
						withScale(0.5f);
						shadowRadius = 0.25f;
					}
				});

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), TwistedLandsBlocks.WALLPAPER, TwistedLandsBlocks.WALLPAPER_CORNER,
				TwistedLandsBlocks.PIZZA, TwistedLandsBlocks.EXTREME_TORCH, TwistedLandsBlocks.TOMATO_PLANT_HEAD, TwistedLandsBlocks.TOMATO_PLANT_BODY,
				TwistedLandsBlocks.SALT_DUST);
		BlockRenderLayerMap.INSTANCE.putBlock(TwistedLandsBlocks.COTTON_LEAVES, RenderType.cutoutMipped());
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(), TwistedLandsBlocks.KYANITE, TwistedLandsBlocks.CRYSTAL_GRASS,
				TwistedLandsBlocks.CRYSTAL_WOOD_LOG, TwistedLandsBlocks.STRIPPED_CRYSTAL_WOOD_LOG, TwistedLandsBlocks.RED_CRYSTAL_WOOD_LOG,
				TwistedLandsBlocks.STRIPPED_RED_CRYSTAL_WOOD_LOG, TwistedLandsBlocks.CRYSTAL_WOOD_PLANKS, TwistedLandsBlocks.RED_CRYSTAL_WOOD_PLANKS,
				TwistedLandsBlocks.CRYSTAL_MUSHROOM_STEM, TwistedLandsBlocks.CRYSTAL_CRAFTING_TABLE, TwistedLandsBlocks.CRYSTAL_FURNACE,
				TwistedLandsBlocks.CRYSTAL_COAL_BLOCK, TwistedLandsBlocks.JADE_BLOCK, TwistedLandsBlocks.JADE_BRICKS,
				TwistedLandsBlocks.PINK_TOURMALINE_BLOCK, TwistedLandsBlocks.PINK_TOURMALINE_BRICKS, TwistedLandsBlocks.TIGERS_EYE_BLOCK,
				TwistedLandsBlocks.CRYSTAL_COAL_ORE, TwistedLandsBlocks.JADE_ORE, TwistedLandsBlocks.PINK_TOURMALINE_ORE,
				TwistedLandsBlocks.TIGERS_EYE_DEBRIS);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), TwistedLandsBlocks.CRYSTAL_LEAVES_BLUE,
				TwistedLandsBlocks.CRYSTAL_LEAVES_GREEN, TwistedLandsBlocks.CRYSTAL_LEAVES_PINK, TwistedLandsBlocks.CRYSTAL_LEAVES_PURPLE,
				TwistedLandsBlocks.CRYSTAL_LEAVES_PURPLISH_RED);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), TwistedLandsBlocks.CRYSTAL_TORCH,
				TwistedLandsBlocks.PURPLE_CRYSTAL_FLOWER, TwistedLandsBlocks.RED_CRYSTAL_FLOWER);
		MenuScreens.register(TwistedLandsBlocks.CRYSTAL_FURNACE_MENU, CrystalFurnaceScreen::new);
		BackroomLampBuzz.initialize();
		ScreenFadeOverlay.initialize();

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof PlayerRenderer player) {
				helper.register(new LittleMintHatLayer(player));
			}
		});
	}
}