package com.twistedteam.twistedlands.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twistedteam.twistedlands.TwistedLandsItems;
import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @ModifyVariable(method = "render", at = @At(value = "HEAD"), argsOnly = true)
    private BakedModel twistedlands$useCustomModels(BakedModel value,
                                                    ItemStack stack,
                                                    ItemDisplayContext renderMode,
                                                    boolean leftHanded,
                                                    PoseStack matrices,
                                                    MultiBufferSource vertexConsumers,
                                                    int light,
                                                    int overlay) {

        if (renderMode == ItemDisplayContext.GUI) {
            return value;
        }

        if (stack.is(TwistedLandsItems.BIG_BERTHA)) {
            return twistedlands$getModel("big_bertha_3d");
        }

        if (stack.is(TwistedLandsItems.ROYAL_GUARDIAN)) {
            return twistedlands$getModel("royal_guardian_3d");
        }

        if (stack.is(TwistedLandsItems.STELIX_BERTHA)) {
            return twistedlands$getModel("stelix_bertha_3d");
        }

        if (stack.is(TwistedLandsItems.ULTIMATE_HAMMER)) {
            return twistedlands$getModel("ultimate_hammer_3d");
        }

        if (stack.is(TwistedLandsItems.ULTIMATE_CHAINSAW)) {
            return twistedlands$getModel("ultimate_chainsaw_3d");
        }

        if (stack.is(TwistedLandsItems.BATTLE_AXE)) {
            return twistedlands$getModel("battle_axe_3d");
        }

        if (stack.is(TwistedLandsItems.QUEEN_BATTLE_AXE)) {
            return twistedlands$getModel("queen_battle_axe_3d");
        }

        if (stack.is(TwistedLandsItems.FIRE_BLASTER)) {
            return twistedlands$getModel("fire_blaster_3d");
        }

        if (stack.is(TwistedLandsItems.WATER_BLASTER)) {
            return twistedlands$getModel("water_blaster_3d");
        }

        return value;
    }

    @Unique
    private BakedModel twistedlands$getModel(String modelName) {
        return ((ItemRendererAccessor) this)
                .twistedlands$getModels()
                .getModelManager()
                .getModel(new ModelResourceLocation(TwistedLands.MOD_ID, modelName, "inventory"));
    }
}