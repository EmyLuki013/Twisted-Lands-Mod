package com.twistedteam.twistedlands.client.renderer;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.entities.AntEntity;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/** geometry.brown_ant do addon. As duas formigas usam o mesmo modelo e as mesmas animações. */
public class AntModel<T extends AntEntity> extends DefaultedEntityGeoModel<T> {
    public AntModel(String texture) {
        super(TwistedLands.id("brown_ant"));
        withAltTexture(TwistedLands.id(texture));
    }

    // look_at_target do addon. O molang dele (query.target_x_rotation) não existe no GeckoLib, então fica aqui.
    // Soma em cima do que as outras animações já fizeram na cabeça, como no Bedrock.
    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (head != null && data != null) {
            head.setRotX(head.getRotX() + data.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + data.netHeadYaw() * Mth.DEG_TO_RAD);
            // O setRot marca o osso como mexido depois que o GeckoLib já limpou as marcas. Sem limpar de novo,
            // no frame seguinte ele não volta a cabeça pro lugar e a soma acumula (cabeça chacoalhando).
            head.resetStateChanges();
        }
    }
}
