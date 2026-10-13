package com.twistedteam.twistedlands.client.renderer.armor;

import com.twistedteam.twistedlands.client.model.armor.RoyalGeoModel;
import com.twistedteam.twistedlands.items.armor.RoyalGeoArmor;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class RoyalGeoRenderer extends GeoArmorRenderer<RoyalGeoArmor> {

    public RoyalGeoRenderer() {
        super(new RoyalGeoModel());
    }
}