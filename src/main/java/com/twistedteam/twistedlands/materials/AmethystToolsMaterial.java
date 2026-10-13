package com.twistedteam.twistedlands.materials;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum AmethystToolsMaterial implements Tier {

    AMETHYST_TOOLS_MATERIAL(
            10,      // Nível de mineração
            2500,   // Durabilidade
            10.5F,   // Velocidade de mineração
            0.0F,   // Dano de ataque adicional (attack damage bonus)
            20      // Encantabilidade
    );

    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamageBonus;
    private final int enchantability;

    AmethystToolsMaterial(
            int miningLevel,
            int itemDurability,
            float miningSpeed,
            float attackDamageBonus,
            int enchantability
    ) {
        this.miningLevel = miningLevel;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantability = enchantability;
    }

    @Override
    public int getUses() {
        return itemDurability;
    }

    @Override
    public float getSpeed() {
        return miningSpeed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return miningLevel;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(TwistedLandsItems.AMETHYST);
    }
}