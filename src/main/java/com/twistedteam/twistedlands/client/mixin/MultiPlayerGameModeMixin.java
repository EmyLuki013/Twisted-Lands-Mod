package com.twistedteam.twistedlands.client.mixin;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    private static final float TWISTEDLANDS_LONG_REACH = 9.0F;

    private boolean twistedlands$loggedReach = false;

    @Inject(method = "getPickRange", at = @At("HEAD"), cancellable = true)
    private void twistedlands$extendedReach(CallbackInfoReturnable<Float> cir) {
        if (twistedlands$isHoldingLongReachSword()) {
            if (!twistedlands$loggedReach) {
                twistedlands$loggedReach = true;
                TwistedLands.LOGGER.info("[TwistedLands] long sword held, pick range set to {}",
                        TWISTEDLANDS_LONG_REACH);
            }
            cir.setReturnValue(TWISTEDLANDS_LONG_REACH);
        } else {
            twistedlands$loggedReach = false;
        }
    }

    @Shadow
    private GameType localPlayerMode;

    @Inject(method = "hasFarPickRange", at = @At("HEAD"), cancellable = true)
    private void twistedlands$keepExtendedRange(CallbackInfoReturnable<Boolean> cir) {
        if (this.localPlayerMode != null
                && this.localPlayerMode.isCreative()
                && twistedlands$isHoldingLongReachSword()) {
            cir.setReturnValue(false);
        }
    }

    private static boolean twistedlands$isHoldingLongReachSword() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        ItemStack mainHand = player.getMainHandItem();
        return TwistedLandsItems.isLongReachSword(mainHand);
    }
}
