package com.twistedteam.twistedlands.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No addon o Little Mint tem attachable, que substitui o item na mão. Em terceira pessoa ele só aparece
// no ombro (LittleMintHatLayer), nunca na mão.
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void twisted$hideLittleMint(LivingEntity entity, ItemStack stack, ItemDisplayContext context, HumanoidArm arm,
                                        PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (stack.is(TwistedLandsItems.LITTLE_MINT_HAT)) {
            ci.cancel();
        }
    }
}
