package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Cria a tabela de loot dos blocos que só dropam eles mesmos (TwistedLandsBlocks.SELF_DROPS), igual ao dropSelf
 * do vanilla, em vez de um JSON por bloco. Um JSON com o mesmo nome num datapack continua tendo prioridade.
 */
@Mixin(LootDataManager.class)
public abstract class LootDataManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void twisted$addSelfDrops(Map<LootDataType<?>, Map<ResourceLocation, ?>> elements, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        Map<ResourceLocation, Object> tables = (Map<ResourceLocation, Object>) elements.get(LootDataType.TABLE);
        for (Block block : TwistedLandsBlocks.SELF_DROPS) {
            tables.putIfAbsent(block.getLootTable(), LootTable.lootTable()
                    .setParamSet(LootContextParamSets.BLOCK)
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(block))
                            .when(ExplosionCondition.survivesExplosion()))
                    .build());
        }
    }
}
