package com.twistedteam.twistedlands.items.armor;

import com.twistedteam.twistedlands.client.renderer.armor.RoyalGeoRenderer;
import com.twistedteam.twistedlands.items.enchant.PreEnchanted;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RoyalGeoArmor extends ArmorItem implements GeoItem, PreEnchanted {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public RoyalGeoArmor(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                     EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original) {
                if (this.renderer == null)
                    this.renderer = new RoyalGeoRenderer();

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    private static final Map<Enchantment, Integer> PRESET_HELMET = Map.of(
            Enchantments.UNBREAKING, 5,
            Enchantments.ALL_DAMAGE_PROTECTION, 10,
            Enchantments.FIRE_PROTECTION, 10,
            Enchantments.PROJECTILE_PROTECTION, 10,
            Enchantments.BLAST_PROTECTION, 10,
            Enchantments.RESPIRATION, 5,
            Enchantments.AQUA_AFFINITY, 1
    );

    private static final Map<Enchantment, Integer> PRESET_CHEST = Map.of(
            Enchantments.UNBREAKING, 5,
            Enchantments.ALL_DAMAGE_PROTECTION, 10,
            Enchantments.FIRE_PROTECTION, 10,
            Enchantments.PROJECTILE_PROTECTION, 10,
            Enchantments.BLAST_PROTECTION, 10,
            Enchantments.THORNS, 3
    );

    private static final Map<Enchantment, Integer> PRESET_LEGS = Map.of(
            Enchantments.UNBREAKING, 5,
            Enchantments.ALL_DAMAGE_PROTECTION, 10,
            Enchantments.FIRE_PROTECTION, 10,
            Enchantments.PROJECTILE_PROTECTION, 10,
            Enchantments.BLAST_PROTECTION, 10,
            Enchantments.SWIFT_SNEAK, 3
    );

    private static final Map<Enchantment, Integer> PRESET_BOOTS = Map.of(
            Enchantments.UNBREAKING, 5,
            Enchantments.ALL_DAMAGE_PROTECTION, 10,
            Enchantments.FIRE_PROTECTION, 10,
            Enchantments.PROJECTILE_PROTECTION, 10,
            Enchantments.BLAST_PROTECTION, 10,
            Enchantments.FALL_PROTECTION, 10,
            Enchantments.DEPTH_STRIDER, 3
    );

    @Override
    public Map<Enchantment, Integer> presetEnchantments() {
        EquipmentSlot slot = this.getType().getSlot();

        if (slot == EquipmentSlot.HEAD) {
            return PRESET_HELMET;
        } else if (slot == EquipmentSlot.CHEST) {
            return PRESET_CHEST;
        } else if (slot == EquipmentSlot.LEGS) {
            return PRESET_LEGS;
        } else if (slot == EquipmentSlot.FEET) {
            return PRESET_BOOTS;
        }
        return Map.of();
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}