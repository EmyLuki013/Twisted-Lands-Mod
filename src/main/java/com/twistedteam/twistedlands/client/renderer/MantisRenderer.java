package com.twistedteam.twistedlands.client.renderer;

import com.twistedteam.twistedlands.client.model.MantisModel;
import com.twistedteam.twistedlands.entities.MantisEntity;
import com.twistedteam.twistedlands.entities.MobjiraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MantisRenderer extends GeoEntityRenderer<MantisEntity> {

    public MantisRenderer(EntityRendererProvider.Context context) {
        super(context, new MantisModel());
        this.withScale(1.5f);
    }
}
