package com.twistedteam.twistedlands.entities.animations;

import software.bernie.geckolib.core.animation.RawAnimation;

public final class NastysaurusAnimations {

    private NastysaurusAnimations() {}

    public static RawAnimation walk =
            RawAnimation.begin().thenLoop("animation.nastysaurus.walk");

    public static RawAnimation roar =
            RawAnimation.begin().thenPlay("animation.nastysaurus.roar");

    public static RawAnimation idle =
            RawAnimation.begin().thenLoop("animation.nastysaurus.idle");

    public static RawAnimation attack =
            RawAnimation.begin().thenPlay("animation.nastysaurus.attack");

    public static RawAnimation run_attack =
            RawAnimation.begin().thenPlay("animation.nastysauros.run_attack");
}
