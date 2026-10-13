package com.twistedteam.twistedlands.items.enchant;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface PreEnchanted {

    Map<Enchantment, Integer> presetEnchantments();
}