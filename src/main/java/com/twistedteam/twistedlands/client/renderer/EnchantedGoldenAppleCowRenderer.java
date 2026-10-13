package com.twistedteam.twistedlands.client.renderer;

import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

public class EnchantedGoldenAppleCowRenderer extends CowRenderer {

    private static final ResourceLocation ADULT_TEXTURE =
            new ResourceLocation(TwistedLands.MOD_ID, "textures/entity/golden_apple_cow.png");

    private static final ResourceLocation BABY_TEXTURE =
            new ResourceLocation(TwistedLands.MOD_ID, "textures/entity/golden_apple_cow.png");

    public EnchantedGoldenAppleCowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.addLayer(new AppleCowGlintLayer(this, ctx));
    }

    @Override
    public ResourceLocation getTextureLocation(Cow entity) {
        return entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }
}