package com.twistedteam.twistedlands.client.renderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * twisted:rainbow_sparks do addon: sobe reto sem gravidade por 2.3s, girada, passando pelos 8 frames da
 * textura até sumir. Cada frame (16x16) tem 4 faíscas de 8x8 e cada partícula sorteia uma delas.
 */
public class RainbowSparksParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final int cornerU;
    private final int cornerV;

    private RainbowSparksParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                  SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        xd = dx;
        yd = dy;
        zd = dz;
        gravity = 0.0f;
        friction = 1.0f;
        hasPhysics = false;
        lifetime = 46;
        quadSize = 0.25f;
        roll = oRoll = (random.nextFloat() * 2.0f - 1.0f) * Mth.PI;
        cornerU = random.nextInt(2) * 8;
        cornerV = random.nextInt(2) * 8;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    protected float getU0() {
        return sprite.getU(cornerU);
    }

    @Override
    protected float getU1() {
        return sprite.getU(cornerU + 8);
    }

    @Override
    protected float getV0() {
        return sprite.getV(cornerV);
    }

    @Override
    protected float getV1() {
        return sprite.getV(cornerV + 8);
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new RainbowSparksParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
