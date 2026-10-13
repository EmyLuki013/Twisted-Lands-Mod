package com.twistedteam.twistedlands.items.enchant;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class GlintingSpawnEggItem extends SpawnEggItem {

    public GlintingSpawnEggItem(EntityType<? extends Mob> type, int primaryColor, int secondaryColor, Properties properties) {
        super(type, primaryColor, secondaryColor, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}