package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    private static final float TWISTEDLANDS_LONG_REACH = 9.0F;

    @Shadow
    public ServerPlayer player;
    private double twistedlands$lastInteractDistSq = -1.0;
    private Entity twistedlands$lastInteractTarget = null;
    private boolean twistedlands$lastInteractAccepted = false;

    @Inject(method = "handleInteract", at = @At("HEAD"))
    private void twistedlands$logEntityInteraction(
            net.minecraft.network.protocol.game.ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.player == null || !TwistedLandsItems.isLongReachSword(
                this.player.getItemInHand(InteractionHand.MAIN_HAND))) {
            return;
        }

        this.twistedlands$lastInteractTarget = this.player.serverLevel() != null
                ? packet.getTarget(this.player.serverLevel()) : null;
        this.twistedlands$lastInteractDistSq = -1.0;
        this.twistedlands$lastInteractAccepted = false;
    }

    @Redirect(
            method = "handleInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"
            )
    )
    private double twistedlands$recordDistSq(net.minecraft.world.phys.AABB box, Vec3 eye) {
        this.twistedlands$lastInteractDistSq = box.distanceToSqr(eye);
        return this.twistedlands$lastInteractDistSq;
    }

    @Redirect(
            method = "handleInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;"
                            + "dispatch(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket$Handler;)V"
            )
    )
    private void twistedlands$recordDispatch(
            net.minecraft.network.protocol.game.ServerboundInteractPacket packet,
            net.minecraft.network.protocol.game.ServerboundInteractPacket.Handler handler) {
        this.twistedlands$lastInteractAccepted = true;
        TwistedLands.LOGGER.info("[TwistedLands] interact ACCEPTED {}", twistedlands$describeOutcome());
        packet.dispatch(handler);
    }

    @Inject(method = "handleInteract", at = @At("RETURN"))
    private void twistedlands$logInteractOutcome(
            net.minecraft.network.protocol.game.ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.twistedlands$lastInteractTarget == null) {
            return;
        }
        if (!this.twistedlands$lastInteractAccepted) {
            TwistedLands.LOGGER.info("[TwistedLands] interact REJECTED {}", twistedlands$describeOutcome());
        }
        this.twistedlands$lastInteractTarget = null;
    }

    /** Distances are reported in blocks, with an explicit dot, to avoid locale confusion. */
    private String twistedlands$describeOutcome() {
        double blocks = this.twistedlands$lastInteractDistSq < 0
                ? -1.0 : Math.sqrt(this.twistedlands$lastInteractDistSq);
        return String.format(java.util.Locale.ROOT,
                "target=%s dist=%.2f blocks limit=%.1f blocks (vanilla %.1f)",
                this.twistedlands$lastInteractTarget != null
                        ? this.twistedlands$lastInteractTarget.getName().getString() : "null",
                blocks, (double) TWISTEDLANDS_LONG_REACH, Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE));
    }

    @Redirect(
            method = {"handleUseItemOn", "handleInteract"},
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private double twistedlands$extendedInteractionRange() {
        if (this.player != null && TwistedLandsItems.isLongReachSword(this.player.getMainHandItem())) {
            return Mth.square(TWISTEDLANDS_LONG_REACH);
        }
        return ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
    }
}
