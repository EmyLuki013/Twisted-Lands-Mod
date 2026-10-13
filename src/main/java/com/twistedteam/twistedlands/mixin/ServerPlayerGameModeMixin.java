package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    private static final float TWISTEDLANDS_LONG_REACH = 9.0F;

    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "handleBlockBreakAction",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private double twistedlands$extendedBlockBreakRange() {
        if (this.player != null && TwistedLandsItems.isLongReachSword(this.player.getMainHandItem())) {
            return Mth.square(TWISTEDLANDS_LONG_REACH);
        }
        return ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
    }
}
