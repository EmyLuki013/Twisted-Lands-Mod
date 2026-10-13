package com.twistedteam.twistedlands.client.renderer;

import com.twistedteam.twistedlands.client.model.NastysaurusModel;
import com.twistedteam.twistedlands.entities.NastysaurusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NastysaurusRenderer extends GeoEntityRenderer<NastysaurusEntity> {

    public NastysaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new NastysaurusModel());
    }
}
