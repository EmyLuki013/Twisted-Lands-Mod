package com.twistedteam.twistedlands.items.armor;

import com.twistedteam.twistedlands.TwistedLands;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * Botas de Pena de Pavão. Calçadas, dão queda lenta, como o peacockBootEffect do addon (que renovava o efeito
 * a cada tick). O efeito vai sem ícone: com 10 ticks de duração o ícone ficaria piscando o tempo todo.
 */
public class PeacockFeatherBootsItem extends ArmorItem {
    public PeacockFeatherBootsItem(Properties properties) {
        super(Material.INSTANCE, Type.BOOTS, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player && player.getItemBySlot(EquipmentSlot.FEET) == stack) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 4, false, false, false));
        }
    }

    // wearable, durability, enchantable e repairable do addon. Só existe a bota.
    private enum Material implements ArmorMaterial {
        INSTANCE;

        @Override public int getDurabilityForType(Type type) { return 650; }
        @Override public int getDefenseForType(Type type) { return type == Type.BOOTS ? 2 : 0; }
        @Override public int getEnchantmentValue() { return 9; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_GENERIC; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(TwistedLandsItems.PEACOCK_FEATHER); }
        // Textura em textures/models/armor/peacock_feather_layer_1.png.
        @Override public String getName() { return TwistedLands.MOD_ID + ":peacock_feather"; }
        @Override public float getToughness() { return 0; }
        @Override public float getKnockbackResistance() { return 0; }
    }
}
