package com.twistedteam.twistedlands.client.mixin;

import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class TwistedLandsLoaderMixin {

    @Shadow
    protected abstract void loadTopLevel(ModelResourceLocation modelId);

    @Unique
    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/ModelBakery;" +
                            "loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    public void twistedlands$addModels(BlockColors blockColors,
                                       ProfilerFiller profiler,
                                       Map<ResourceLocation, ?> jsonUnbakedModels,
                                       Map<ResourceLocation, List<?>> blockStates,
                                       CallbackInfo ci) {

        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "big_bertha_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "royal_guardian_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "stelix_bertha_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "ultimate_hammer_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "ultimate_chainsaw_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "battle_axe_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "queen_battle_axe_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "fire_blaster_3d", "inventory"));
        this.loadTopLevel(new ModelResourceLocation(TwistedLands.MOD_ID, "water_blaster_3d", "inventory"));
    }
}