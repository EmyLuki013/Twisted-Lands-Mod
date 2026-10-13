package com.twistedteam.twistedlands.mixin;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsBlocks;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Cria a tabela de loot dos blocos que só dropam eles mesmos (TwistedLandsBlocks.SELF_DROPS), igual ao dropSelf
 * do vanilla, e a dos minérios (TwistedLandsBlocks.ORE_DROPS), igual ao createOreDrop, em vez de um JSON por bloco.
 * Um JSON com o mesmo nome num datapack continua tendo prioridade.
 */
@Mixin(LootDataManager.class)
public abstract class LootDataManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void twisted$addBlockDrops(Map<LootDataType<?>, Map<ResourceLocation, ?>> elements, CallbackInfo ci) {
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
        TwistedLandsBlocks.ORE_DROPS.forEach((block, item) -> tables.putIfAbsent(block.getLootTable(), LootTable.lootTable()
                .setParamSet(LootContextParamSets.BLOCK)
                .withPool(LootPool.lootPool().add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(block).when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))),
                        LootItem.lootTableItem(BuiltInRegistries.ITEM.get(TwistedLands.id(item)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                                .apply(ApplyExplosionDecay.explosionDecay()))))
                .build()));
    }
}
