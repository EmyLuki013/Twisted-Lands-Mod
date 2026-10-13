package com.twistedteam.twistedlands.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Cow;

public class AppleCowGlintLayer extends RenderLayer<Cow, CowModel<Cow>> {

    private static final float GLINT_SCROLL_TICKS = 0.01F;

    private final CowModel<Cow> glintModel;
    private int renderCount;

    public AppleCowGlintLayer(RenderLayerParent<Cow, CowModel<Cow>> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.glintModel = new CowModel<>(context.bakeLayer(ModelLayers.COW));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       Cow entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        this.glintModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        this.getParentModel().copyPropertiesTo(this.glintModel);

        float scroll = (entity.tickCount + partialTick) * GLINT_SCROLL_TICKS;
        RenderType renderType = RenderType.energySwirl(
                ItemRenderer.ENCHANTED_GLINT_ENTITY, scroll % 1.0F, scroll % 1.0F);
        VertexConsumer consumer = buffer.getBuffer(renderType);

        poseStack.pushPose();
        poseStack.scale(1.002F, 1.002F, 1.002F);
        this.glintModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.glintModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        if (buffer instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource) buffer).endBatch(renderType);
        }

        if (++this.renderCount % 120 == 0) {
            TwistedLands.LOGGER.info("[TwistedLands] glint layer #{} entity={} baby={}",
                    this.renderCount, entity.getName().getString(), entity.isBaby());
        }
    }
}
