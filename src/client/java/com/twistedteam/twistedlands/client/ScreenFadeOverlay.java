package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.network.ScreenFade;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** Desenha o fade mandado pelo servidor (ScreenFade) por cima de tudo. */
public final class ScreenFadeOverlay {
    private static int color;
    private static int hold;
    private static int fadeOut;
    private static long start = -1;

    private ScreenFadeOverlay() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(ScreenFade.ID, (client, handler, buf, sender) -> {
            int newColor = buf.readInt();
            int newHold = buf.readVarInt();
            int newFadeOut = buf.readVarInt();
            client.execute(() -> {
                color = newColor;
                hold = newHold;
                fadeOut = Math.max(1, newFadeOut);
                start = ticks(client);
            });
        });

        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (start < 0) {
                return;
            }
            Minecraft client = Minecraft.getInstance();
            float elapsed = ticks(client) - start + tickDelta;
            float alpha = elapsed <= hold ? 1.0f : 1.0f - (elapsed - hold) / fadeOut;
            if (alpha <= 0.0f) {
                start = -1;
                return;
            }
            int argb = ((int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) << 24) | (color & 0xFFFFFF);
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), argb);
        });
    }

    // Conta pelo tempo do cliente: o mundo muda no teleporte e o gameTime de cada dimensão é outro.
    private static long ticks(Minecraft client) {
        return client.gui.getGuiTicks();
    }
}
