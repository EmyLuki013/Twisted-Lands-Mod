package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Deixa colocar as poções de combate nos espaços de frasco do suporte de poções (clique e shift-clique). */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingPotionSlotMixin {
    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void twisted$mayPlaceItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(TwistedLandsItems.COMBAT_POTION) || stack.is(TwistedLandsItems.POWERED_COMBAT_POTION)) {
            cir.setReturnValue(true);
        }
    }
}
