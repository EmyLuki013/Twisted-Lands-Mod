package com.twistedteam.twistedlands.items.tools;

import com.twistedteam.twistedlands.items.enchant.PreEnchanted;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Map;

public class EnchantedBigSwords extends SwordItem implements PreEnchanted {

    private static final Map<Enchantment, Integer> PRESET = Map.of(
            Enchantments.UNBREAKING, 3,
            Enchantments.BANE_OF_ARTHROPODS, 5,
            Enchantments.MENDING, 1,
            Enchantments.FIRE_ASPECT, 5,
            Enchantments.KNOCKBACK, 7
    );

    public EnchantedBigSwords(Tier material, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(material, attackDamage, attackSpeed, properties);
    }

    @Override
    public Map<Enchantment, Integer> presetEnchantments() {
        return PRESET;
    }
}