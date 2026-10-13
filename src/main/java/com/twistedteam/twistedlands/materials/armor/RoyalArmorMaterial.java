package com.twistedteam.twistedlands.materials.armor;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class RoyalArmorMaterial implements ArmorMaterial {

    // Durabilidade base por peça (multiplicada por um valor no construtor)
    private static final int[] BASE_DURABILITY = new int[] {220, 320, 300, 260};

    // Pontos de defesa: [botas, calças, peitoral, capacete]
    private static final int[] PROTECTION_VALUES = new int[] {8, 14, 12, 8};

    private final String name;
    private final int durabilityMultiplier;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Ingredient repairIngredient;

    public RoyalArmorMaterial(String name,
                                 int durabilityMultiplier,
                                 int enchantability,
                                 SoundEvent equipSound,
                                 float toughness,
                                 float knockbackResistance,
                                 Ingredient repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    public static final RoyalArmorMaterial ROYAL_ARMOR_MATERIAL = new RoyalArmorMaterial(
            "twisted:royal",
            100,
            40,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,
            0.1F,
            Ingredient.of(TwistedLandsItems.SKOTODRAKON_SCALE)
    );

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.ordinal()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return PROTECTION_VALUES[type.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}