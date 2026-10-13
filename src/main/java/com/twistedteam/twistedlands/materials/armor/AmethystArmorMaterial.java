package com.twistedteam.twistedlands.materials.armor;

import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class AmethystArmorMaterial implements ArmorMaterial {

    // Durabilidade base por peça (multiplicada por um valor no construtor)
    private static final int[] BASE_DURABILITY = new int[] {130, 150, 160, 110};

    // Pontos de defesa: [botas, calças, peitoral, capacete]
    private static final int[] PROTECTION_VALUES = new int[] {3, 8, 7, 4};

    private final String name;
    private final int durabilityMultiplier;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Ingredient repairIngredient;

    public AmethystArmorMaterial(String name,
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

    public static final AmethystArmorMaterial AMETHYST_ARMOR_MATERIAL = new AmethystArmorMaterial(
            "twisted:amethyst",
            10,
            20,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,
            0.1F,
            Ingredient.of(TwistedLandsItems.AMETHYST)
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