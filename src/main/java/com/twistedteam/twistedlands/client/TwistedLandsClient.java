package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.entities.BalloonSharkEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import com.twistedteam.twistedlands.projectiles.IrukandjiArrowEntity;
import com.twistedteam.twistedlands.entities.IrukandjiEntity;
import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.client.renderer.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.PeacockEntity;
import com.twistedteam.twistedlands.entities.RatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;

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
		// O filhote tem scale 0.5 no addon.
		EntityRendererRegistry.register(TwistedLandsEntities.PEACOCK,
				context -> new GeoEntityRenderer<PeacockEntity>(context, new DefaultedEntityGeoModel<>(TwistedLands.id("peacock"), true)) {
					{
						shadowRadius = 0.45f;
					}

					@Override
					public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, PeacockEntity peacock,
													BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
						float scale = peacock.isBaby() ? 0.5f : 1.0f;
						super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, peacock, model, isReRender,
								partialTick, packedLight, packedOverlay);
					}
				});
		// Os pontos de luz vão por cima com o material do creeper carregado (is_charged no addon), sem sombra de luz.
		EntityRendererRegistry.register(TwistedLandsEntities.IRUKANDJI,
				context -> new GeoEntityRenderer<IrukandjiEntity>(context, new DefaultedEntityGeoModel<>(TwistedLands.id("irukandji"))) {
					{
						shadowRadius = 0.5f;
						addRenderLayer(new GeoRenderLayer<>(this) {
							private static final ResourceLocation SPOTS = TwistedLands.id("textures/entity/irukandji_light_spots.png");

							@Override
							public void render(PoseStack poseStack, IrukandjiEntity irukandji, BakedGeoModel model, RenderType renderType,
											   MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
								float scroll = (irukandji.tickCount + partialTick) * 0.01f;
								RenderType spots = RenderType.energySwirl(SPOTS, scroll % 1.0f, scroll % 1.0f);
								getRenderer().reRender(model, poseStack, buffers, irukandji, spots, buffers.getBuffer(spots), partialTick,
										LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.5f, 0.5f, 0.5f, 1.0f);
							}
						});
					}
				});
		EntityRendererRegistry.register(TwistedLandsEntities.IRUKANDJI_ARROW, context -> new ArrowRenderer<IrukandjiArrowEntity>(context) {
			private static final ResourceLocation TEXTURE = TwistedLands.id("textures/entity/projectiles/irukandji_arrow.png");

			@Override
			public ResourceLocation getTextureLocation(IrukandjiArrowEntity arrow) {
				return TEXTURE;
			}
		});
		// A skin sai do variant: cloud, cloud_glow, unskinned e emoji.
		EntityRendererRegistry.register(TwistedLandsEntities.BALLOON_SHARK,
				context -> new GeoEntityRenderer<BalloonSharkEntity>(context, new DefaultedEntityGeoModel<BalloonSharkEntity>(TwistedLands.id("balloon_shark")) {
					private static final ResourceLocation[] SKINS = {
							TwistedLands.id("textures/entity/balloon_shark/cloud.png"),
							TwistedLands.id("textures/entity/balloon_shark/cloud_glow.png"),
							TwistedLands.id("textures/entity/balloon_shark/unskinned.png"),
							TwistedLands.id("textures/entity/balloon_shark/emoji.png")};

					@Override
					public ResourceLocation getTextureResource(BalloonSharkEntity shark) {
						return SKINS[shark.getVariant()];
					}
				}) {
					{
						shadowRadius = 0.5f;
					}
				});
		// geometry.cow.v1.8, o modelo da vaca vanilla. Só troca a textura.
		EntityRendererRegistry.register(TwistedLandsEntities.CRYSTAL_APPLE_COW, context -> new CowRenderer(context) {
			private static final ResourceLocation TEXTURE = TwistedLands.id("textures/entity/crystal_apple_cow.png");

			@Override
			public ResourceLocation getTextureLocation(Cow cow) {
				return TEXTURE;
			}
		});
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
				TwistedLandsBlocks.SALT_DUST, TwistedLandsBlocks.CRYSTAL_CORN_PLANT_HEAD, TwistedLandsBlocks.CRYSTAL_CORN_PLANT_BODY);
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