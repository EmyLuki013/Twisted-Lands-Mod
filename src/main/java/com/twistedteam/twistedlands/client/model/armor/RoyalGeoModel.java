package com.twistedteam.twistedlands.client.model.armor;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.items.armor.RoyalGeoArmor;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalGeoModel extends GeoModel<RoyalGeoArmor> {

    @Override
    public ResourceLocation getModelResource(RoyalGeoArmor animatable) {
        return new ResourceLocation(TwistedLands.MOD_ID, "geo/armor/royal_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalGeoArmor animatable) {
        return new ResourceLocation(TwistedLands.MOD_ID, "textures/models/armor/royal_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalGeoArmor animatable) {
        return new ResourceLocation(TwistedLands.MOD_ID, "animations/armor/royal_guardian_armor.animation.json");
    }
}