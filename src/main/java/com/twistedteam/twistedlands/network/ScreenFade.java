package com.twistedteam.twistedlands.network;

import com.twistedteam.twistedlands.TwistedLands;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Tela pintada de uma cor que some aos poucos, o "camera fade" do Bedrock. O cliente desenha
 * (ver ScreenFadeOverlay no client).
 */
public final class ScreenFade {
    public static final ResourceLocation ID = TwistedLands.id("screen_fade");

    private ScreenFade() {
    }

    /** color em RGB, tempos em ticks: fica parada por hold e depois some em fadeOut. */
    public static void send(ServerPlayer player, int color, int hold, int fadeOut) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(color);
        buf.writeVarInt(hold);
        buf.writeVarInt(fadeOut);
        ServerPlayNetworking.send(player, ID, buf);
    }
}
