package com.twistedteam.twistedlands.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entity.LittleMintEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LittleMintRenderer extends GeoEntityRenderer<LittleMintEntity> {
    public LittleMintRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(TwistedLands.id("little_mint")));
        this.shadowRadius = 0.3f;
    }

    // O GeckoLib só gira entidade viva. O boneco não é, então usa o yaw dele (virado pra quem colocou).
    @Override
    protected void applyRotations(LittleMintEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                  float partialTick) {
        super.applyRotations(entity, poseStack, ageInTicks, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()),
                partialTick);
    }

    // O modelo tem abas de espessura 0 (cima e baixo no mesmo plano, com texturas diferentes). Sem culling,
    // que é o padrão do GeckoLib, as duas faces brigam e a textura fica piscando.
    @Override
    public RenderType getRenderType(LittleMintEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }
}
