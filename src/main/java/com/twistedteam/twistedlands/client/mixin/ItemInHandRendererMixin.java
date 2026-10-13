package com.twistedteam.twistedlands.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Primeira pessoa: o attachable do Little Mint fica invisível (part_visibility "!v.c_ifp"), então o item
// também não aparece na mão.
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void twisted$hideLittleMint(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                        float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                        MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (stack.is(TwistedLandsItems.LITTLE_MINT_HAT)) {
            ci.cancel();
        }
    }
}
