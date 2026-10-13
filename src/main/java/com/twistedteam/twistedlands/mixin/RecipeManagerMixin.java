package com.twistedteam.twistedlands.mixin;

import com.google.gson.JsonElement;
import com.twistedteam.twistedlands.materials.CrystalMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/** Coloca as receitas da Dream World (CrystalMaterials.addRecipes) junto com as lidas dos JSON. */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void twisted$addCrystalRecipes(Map<ResourceLocation, JsonElement> recipes, ResourceManager resources,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        CrystalMaterials.addRecipes(recipes);
    }
}
