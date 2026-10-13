package com.twistedteam.twistedlands.client.mixin;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public class GameRendererReachMixin {

    @ModifyConstant(method = "pick", constant = @Constant(doubleValue = 9.0D))
    private double twistedlands$extendEntityPickRange(double original) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && TwistedLandsItems.isLongReachSword(client.player.getMainHandItem())) {
            return Double.MAX_VALUE;
        }
        return original;
    }
}
