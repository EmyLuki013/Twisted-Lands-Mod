package com.twistedteam.twistedlands.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.RainbowPortalEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * O geometry.big_garden_portal do addon é um plano só, 26 x 24 pixels com a textura inteira, e a entidade tem
 * scale 3.5. A textura troca a cada tick entre os 7 frames, sem luz (ignore_lighting).
 */
public class RainbowPortalRenderer extends EntityRenderer<RainbowPortalEntity> {
    private static final float WIDTH = 26.0f / 16.0f * 3.5f;
    private static final float HEIGHT = 24.0f / 16.0f * 3.5f;
    private static final ResourceLocation[] FRAMES = new ResourceLocation[RainbowPortalEntity.FRAMES];

    static {
        for (int i = 0; i < FRAMES.length; i++) {
            FRAMES[i] = TwistedLands.id("textures/entity/rainbow_portal/portal_" + (i + 1) + ".png");
        }
    }

    public RainbowPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RainbowPortalEntity portal, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(portal)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float x = WIDTH / 2.0f;
        // Virado pro sul, como o tp ~~~ 0 do addon deixa. O entityTranslucent não faz cull, então de trás aparece
        // a mesma textura espelhada.
        vertex(consumer, matrix, normal, -x, 0.0f, 0.0f, 1.0f);
        vertex(consumer, matrix, normal, x, 0.0f, 1.0f, 1.0f);
        vertex(consumer, matrix, normal, x, HEIGHT, 1.0f, 0.0f);
        vertex(consumer, matrix, normal, -x, HEIGHT, 0.0f, 0.0f);
        super.render(portal, yaw, partialTick, poseStack, buffers, light);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y,
                               float u, float v) {
        consumer.vertex(matrix, x, y, 0.0f).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0f, 0.0f, 1.0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(RainbowPortalEntity portal) {
        return FRAMES[portal.tickCount % FRAMES.length];
    }
}
