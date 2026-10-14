package com.twistedteam.twistedlands.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** O grid de crafting sabe de que menu ele é; as receitas da Crystal Crafting Table usam isso. */
@Mixin(TransientCraftingContainer.class)
public interface TransientCraftingContainerAccessor {
    @Accessor("menu")
    AbstractContainerMenu twisted$getMenu();
}
