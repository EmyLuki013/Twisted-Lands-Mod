package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Receitas da poção de combate no suporte de poções: poção estranha + mandíbula de formiga marrom,
 * e poção de combate + pó de glowstone pra Combat II. As poções são itens próprios, então o registro
 * de receitas do Fabric (que só troca o tipo de Potion) não serve.
 */
@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private static void twisted$isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModItems.BROWN_ANT_MANDIBLE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private static void twisted$hasMix(ItemStack potion, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (!twisted$result(potion, ingredient).isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private static void twisted$mix(ItemStack ingredient, ItemStack potion, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = twisted$result(potion, ingredient);
        if (!result.isEmpty()) {
            cir.setReturnValue(result);
        }
    }

    @Unique
    private static ItemStack twisted$result(ItemStack potion, ItemStack ingredient) {
        if (ingredient.is(ModItems.BROWN_ANT_MANDIBLE) && potion.is(Items.POTION)
                && PotionUtils.getPotion(potion) == Potions.AWKWARD) {
            return new ItemStack(ModItems.COMBAT_POTION);
        }
        if (ingredient.is(Items.GLOWSTONE_DUST) && potion.is(ModItems.COMBAT_POTION)) {
            return new ItemStack(ModItems.POWERED_COMBAT_POTION);
        }
        return ItemStack.EMPTY;
    }
}
