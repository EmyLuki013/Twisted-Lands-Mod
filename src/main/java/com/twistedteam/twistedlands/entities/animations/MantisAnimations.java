package com.twistedteam.twistedlands.entities.animations;

import software.bernie.geckolib.core.animation.RawAnimation;

public final class MantisAnimations {

    private MantisAnimations() {}

    public static RawAnimation walk =
            RawAnimation.begin().thenLoop("animation.mantis.walk");

    public static RawAnimation fly =
            RawAnimation.begin().thenLoop("animation.mantis.fly");

    public static RawAnimation idle =
            RawAnimation.begin().thenLoop("animation.mantis.idle");

    public static RawAnimation attack =
            RawAnimation.begin().thenPlay("animation.mantis.attack");
}
