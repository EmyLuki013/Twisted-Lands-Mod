package com.twistedteam.twistedlands.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twistedteam.twistedlands.world.backroom.Backroom;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fog da Backroom (backroom_fog.json do addon): começa em 20 e fecha em 30 blocos.
 * O 1.20.1 não tem distância de fog por JSON, por isso o mixin. A cor vem do bioma (fog_color).
 */
@Mixin(FogRenderer.class)
abstract class BackroomFogMixin {
    private static final float TWISTED$FOG_START = 20.0f;
    private static final float TWISTED$FOG_END = 30.0f;

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void twisted$backroomFog(Camera camera, FogRenderer.FogMode mode, float farPlaneDistance,
                                            boolean thickFog, float partialTick, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Backroom.LEVEL || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }
        // Cegueira e escuridão já deixam a fog mais perto, então nesses casos não mexe.
        if (RenderSystem.getShaderFogEnd() > TWISTED$FOG_END) {
            RenderSystem.setShaderFogStart(TWISTED$FOG_START);
            RenderSystem.setShaderFogEnd(TWISTED$FOG_END);
        }
    }
}
