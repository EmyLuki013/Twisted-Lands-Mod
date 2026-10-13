package com.twistedteam.twistedlands.client.renderer;

import net.minecraft.client.Minecraft;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** O Little Mint no ombro do jogador. Uma instância só, desenhada em cada jogador que segura ele. */
public final class LittleMintHat implements GeoAnimatable {
    public static final LittleMintHat INSTANCE = new LittleMintHat();

    // A mesma pose do boneco no chão, que deixa os pés na origem.
    private static final RawAnimation POSE = RawAnimation.begin().thenPlay("animation.little_mint.pose");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);

    private LittleMintHat() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "pose", 0, state -> state.setAndContinue(POSE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
    }
}
