package com.twistedteam.twistedlands.client.mixin;

import com.twistedteam.twistedlands.TwistedLands;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftAttackMixin {

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void twistedlands$logAttackTarget(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = (Minecraft) (Object) this;
        if (client.player == null) {
            return;
        }

        HitResult hit = client.hitResult;
        String kind = hit == null ? "null" : hit.getType().name();
        String detail = "";
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            detail = String.format(java.util.Locale.ROOT, " entity=%s dist=%.2f blocks",
                    entity.getName().getString(),
                    Math.sqrt(entity.distanceToSqr(client.player)));
        } else if (hit instanceof BlockHitResult blockHit) {
            detail = " block=" + blockHit.getBlockPos();
        }

        TwistedLands.LOGGER.info("[TwistedLands] left click: hitType={}{} gameMode={}",
                kind, detail,
                client.gameMode != null ? client.gameMode.getPlayerMode() : "null");
    }
}
