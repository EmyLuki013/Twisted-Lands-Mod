package com.twistedteam.twistedlands.client.renderer;

import com.twistedteam.twistedlands.entities.AntEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Mesmo modelo pras duas formigas, muda a textura e o tamanho (scale do addon). */
public class AntRenderer<T extends AntEntity> extends GeoEntityRenderer<T> {
    public AntRenderer(EntityRendererProvider.Context context, String texture, float scale) {
        super(context, new AntModel<>(texture));
        withScale(scale);
        this.shadowRadius = 0.7f * scale;
    }
}
