package com.twistedteam.twistedlands.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twistedteam.twistedlands.world.backroom.Backroom;
import com.twistedteam.twistedlands.world.backroom.BackroomMaze;
import com.twistedteam.twistedlands.world.dream.DreamWorldChunkGenerator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fog da Backroom (backroom_fog.json do addon): começa em 20 e fecha em 30 blocos.
 * O 1.20.1 não tem distância de fog por JSON, por isso o mixin. Na Backroom a cor vem do bioma (fog_color).
 * O labirinto embaixo do Dream World usa a mesma fog, com a cor do bioma da Backroom fixa aqui.
 */
@Mixin(FogRenderer.class)
abstract class BackroomFogMixin {
    private static final float TWISTED$FOG_START = 20.0f;
    private static final float TWISTED$FOG_END = 30.0f;
    // #F0C655, o fog_color da Backroom.
    private static final float TWISTED$RED = 0xF0 / 255f;
    private static final float TWISTED$GREEN = 0xC6 / 255f;
    private static final float TWISTED$BLUE = 0x55 / 255f;

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void twisted$dreamMazeColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                               float bossColorModifier, CallbackInfo ci) {
        if (twisted$inDreamMaze(level, camera) && camera.getFluidInCamera() == FogType.NONE) {
            fogRed = TWISTED$RED;
            fogGreen = TWISTED$GREEN;
            fogBlue = TWISTED$BLUE;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
        }
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void twisted$backroomFog(Camera camera, FogRenderer.FogMode mode, float farPlaneDistance,
                                            boolean thickFog, float partialTick, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || camera.getFluidInCamera() != FogType.NONE
                || (level.dimension() != Backroom.LEVEL && !twisted$inDreamMaze(level, camera))) {
            return;
        }
        // Cegueira e escuridão já deixam a fog mais perto, então nesses casos não mexe.
        if (RenderSystem.getShaderFogEnd() > TWISTED$FOG_END) {
            RenderSystem.setShaderFogStart(TWISTED$FOG_START);
            RenderSystem.setShaderFogEnd(TWISTED$FOG_END);
        }
    }

    // Câmera entre o chão e o teto do labirinto (y-6 a y-1).
    private static boolean twisted$inDreamMaze(ClientLevel level, Camera camera) {
        if (level.dimension() != DreamWorldChunkGenerator.LEVEL) {
            return false;
        }
        double y = camera.getPosition().y;
        int floor = DreamWorldChunkGenerator.MAZE_FLOOR_Y;
        return y >= floor && y < floor + BackroomMaze.CEILING_Y - BackroomMaze.FLOOR_Y + 1;
    }
}
