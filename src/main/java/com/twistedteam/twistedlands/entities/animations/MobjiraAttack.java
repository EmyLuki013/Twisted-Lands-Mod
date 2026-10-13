package com.twistedteam.twistedlands.entities.animations;

import software.bernie.geckolib.core.animation.RawAnimation;

public enum MobjiraAttack {

    CHARGE_BEAM(200, RawAnimation.begin().thenLoop("animation.210_mobjira.atomic")),

    JUMP(80, RawAnimation.begin().thenLoop("animation.210_mobjira.jump")),

    SUPERCHARGE(120, RawAnimation.begin().thenLoop("animation.210_mobjira.supercharge")),

    FIREBALL(30, RawAnimation.begin().thenLoop("animation.210_mobjira.fireball")),

    CLOUD(100, RawAnimation.begin().thenLoop("animation.210_mobjira.cloud"));

    private final int durationTicks;
    private final RawAnimation animation;

    MobjiraAttack(int durationTicks, RawAnimation animation) {
        this.durationTicks = durationTicks;
        this.animation = animation;
    }

    public int durationTicks() {
        return this.durationTicks;
    }

    public RawAnimation animation() {
        return this.animation;
    }
}
