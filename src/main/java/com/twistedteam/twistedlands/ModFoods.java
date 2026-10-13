package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.item.MagicAppleItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Comidas. Fome e saturação iguais ao addon; os efeitos eram dados por script ao comer, aqui ficam na comida. */
public class ModFoods {
    public static final Item CANDY_CANE = register("candy_cane", new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.3f)
            .effect(effect(MobEffects.MOVEMENT_SPEED, 120), 1.0f));

    public static final Item COTTON_CANDY = register("cotton_candy", new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.1f)
            .effect(effect(MobEffects.JUMP, 100), 1.0f)
            .effect(effect(MobEffects.SLOW_FALLING, 100), 1.0f));

    public static final Item PEANUT_CANDY = register("peanut_candy", new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.8f));

    public static final Item CHOCOLATE_BAR = register("chocolate_bar", new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.3f));

    public static final Item PIZZA_PIECE = register("pizza_piece", new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.6f)
            .effect(effect(MobEffects.REGENERATION, 100), 1.0f));

    // Cerejas caem das folhas do Cherry Orchard no addon.
    public static final Item CHERRIES = register("cherries", new FoodProperties.Builder()
            .nutrition(3).saturationMod(0.45f));

    public static final Item APPLE_PIE = register("apple_pie", new FoodProperties.Builder()
            .nutrition(6).saturationMod(0.6f));

    public static final Item CHEESE = register("cheese", new FoodProperties.Builder()
            .nutrition(4).saturationMod(0.75f));

    public static final Item MAGIC_APPLE = ModItems.register(new MagicAppleItem(new FabricItemSettings().maxCount(1)
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(1.0f).alwaysEat().build())), "magic_apple");

    private static Item register(String id, FoodProperties.Builder food) {
        return ModItems.register(new Item(new FabricItemSettings().food(food.build())), id);
    }

    // No addon os efeitos vinham sem partícula, mas com o ícone na tela.
    private static MobEffectInstance effect(MobEffect type, int ticks) {
        return new MobEffectInstance(type, ticks, 0, false, false, true);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register((itemGroup) -> {
            itemGroup.accept(CANDY_CANE);
            itemGroup.accept(COTTON_CANDY);
            itemGroup.accept(PEANUT_CANDY);
            itemGroup.accept(CHOCOLATE_BAR);
            itemGroup.accept(PIZZA_PIECE);
            itemGroup.accept(CHERRIES);
            itemGroup.accept(APPLE_PIE);
            itemGroup.accept(CHEESE);
            itemGroup.accept(MAGIC_APPLE);
        });
    }
}
