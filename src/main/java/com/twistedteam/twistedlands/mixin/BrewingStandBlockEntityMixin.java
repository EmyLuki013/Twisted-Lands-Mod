package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mesma coisa do PotionSlot, pra funil colocar as poções de combate no suporte. */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void twisted$canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot < 3 && (stack.is(TwistedLandsItems.COMBAT_POTION) || stack.is(TwistedLandsItems.POWERED_COMBAT_POTION))) {
            cir.setReturnValue(((Container) (Object) this).getItem(slot).isEmpty());
        }
    }
}
