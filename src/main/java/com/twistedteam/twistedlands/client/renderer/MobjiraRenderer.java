package com.twistedteam.twistedlands.client.renderer;

import com.twistedteam.twistedlands.client.model.MobjiraModel;
import com.twistedteam.twistedlands.entities.MobjiraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MobjiraRenderer extends GeoEntityRenderer<MobjiraEntity> {

    private static final float MODEL_SCALE = 5.0F;

    public MobjiraRenderer(EntityRendererProvider.Context context) {
        super(context, new MobjiraModel());
        this.withScale(MODEL_SCALE);
    }
}
