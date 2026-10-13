package com.twistedteam.twistedlands.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

/**
 * Little Mint na mão secundária, só em terceira pessoa (part_visibility do controller.render.accessories).
 * No addon ele ficava na cabeça com escala 1.2. Aqui fica em pé no ombro do lado da mão secundária,
 * olhando pra frente e pequeno o bastante pra não encostar na cabeça (escolha nossa enquanto a Emy não decide).
 */
public class LittleMintHatLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = TwistedLands.id("textures/entity/little_mint.png");
    // O boneco tem 16 px de largura (braços em ±8) e os pés vão de -1.8 a 1.8. A cabeça com a segunda camada
    // vai até 4.5 px do centro e o ombro termina em 8 px (7 no braço slim). Essas escalas são as maiores que
    // deixam os pés inteiros no ombro e as mãos a 0.8 px da cabeça.
    private static final float SHOULDER_X = 7.5f / 16.0f;
    private static final float SCALE = 0.27f;
    private static final float SLIM_SHOULDER_X = 6.68f / 16.0f;
    private static final float SLIM_SCALE = 0.17f;

    private final GeoObjectRenderer<LittleMintHat> renderer = new GeoObjectRenderer<>(new GeoModel<>() {
        @Override
        public ResourceLocation getModelResource(LittleMintHat animatable) {
            return TwistedLands.id("geo/entity/little_mint.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(LittleMintHat animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(LittleMintHat animatable) {
            return TwistedLands.id("animations/entity/little_mint.animation.json");
        }
    }) {
        // Sem o deslocamento de meio bloco que o GeoObjectRenderer usa pra item e bloco.
        @Override
        public void preRender(PoseStack poseStack, LittleMintHat animatable, BakedGeoModel model,
                              MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                              float partialTick, int packedLight, int packedOverlay, float red, float green,
                              float blue, float alpha) {
        }
    };

    public LittleMintHatLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                       float headPitch) {
        if (!player.getOffhandItem().is(TwistedLandsItems.LITTLE_MINT_HAT)) {
            return;
        }
        poseStack.pushPose();
        // O pivô do corpo é o topo dos ombros, então acompanha agachar sozinho.
        getParentModel().body.translateAndRotate(poseStack);
        // Desfaz o espelho do modelo vanilla (Y pra baixo). Depois disso o lado esquerdo do jogador fica em -X.
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        float side = player.getMainArm() == HumanoidArm.RIGHT ? -1.0f : 1.0f;
        boolean slim = "slim".equals(player.getModelName());
        poseStack.translate(side * (slim ? SLIM_SHOULDER_X : SHOULDER_X), 0.0f, 0.0f);
        float scale = slim ? SLIM_SCALE : SCALE;
        poseStack.scale(scale, scale, scale);
        // Com culling, pelo mesmo motivo do LittleMintRenderer (abas de espessura 0 piscando).
        RenderType renderType = RenderType.entityCutout(TEXTURE);
        renderer.render(poseStack, LittleMintHat.INSTANCE, buffers, renderType, buffers.getBuffer(renderType), light);
        poseStack.popPose();
    }
}
