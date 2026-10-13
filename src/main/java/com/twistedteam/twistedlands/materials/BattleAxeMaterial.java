package com.twistedteam.twistedlands.materials;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum BattleAxeMaterial implements Tier {

    BATTLE_AXE(
            4,      // Nível de mineração
            3500,   // Durabilidade
            7.5F,   // Velocidade de mineração
            0.0F,   // Dano de ataque adicional (attack damage bonus)
            50      // Encantabilidade
    );

    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamageBonus;
    private final int enchantability;

    BattleAxeMaterial(
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
        return Ingredient.of(Items.IRON_BLOCK);
    }
}