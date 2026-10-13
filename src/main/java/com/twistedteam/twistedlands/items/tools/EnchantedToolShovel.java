package com.twistedteam.twistedlands.items.tools;

import com.twistedteam.twistedlands.items.enchant.PreEnchanted;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Map;

public class EnchantedToolShovel extends ShovelItem implements PreEnchanted {

    private static final Map<Enchantment, Integer> PRESET = Map.of(
            Enchantments.BLOCK_EFFICIENCY, 7,
            Enchantments.UNBREAKING, 3,
            Enchantments.MENDING, 1
    );

    public EnchantedToolShovel(Tier material, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(material, attackDamage, attackSpeed, properties);
    }

    @Override
    public Map<Enchantment, Integer> presetEnchantments() {
        return PRESET;
    }
}