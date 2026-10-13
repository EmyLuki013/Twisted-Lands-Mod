package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.item.CombatPotionItem;
import com.twistedteam.twistedlands.item.LittleMintItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {
    public static Item register(Item item, String id) {
        ResourceLocation itemID = new ResourceLocation(TwistedLands.MOD_ID, id);

        return Registry.register(BuiltInRegistries.ITEM, itemID, item);
    }

    public static final Item ULTIMATE_UPGRADE = register(
            new Item(new FabricItemSettings()),
            "ultimate_upgrade"
    );

    public static final Item LASER_CHARGE = register(
            new Item(new FabricItemSettings().maxCount(1).durability(64)),
            "laser_charge"
    );

    public static final Item DRAKONIC_EYE = register(
            new Item(new FabricItemSettings()),
            "drakonic_eye"
    );

    public static final Item CRYSTAL_STICK = register(
            new Item(new FabricItemSettings()),
            "crystal_stick"
    );

    // Formiga arco-íris. O ovo usa a textura do addon, por isso as cores ficam brancas (sem tingir).
    public static final Item RAINBOW_ANT_SPAWN_EGG = register(
            new SpawnEggItem(ModEntities.RAINBOW_ANT, 0xFFFFFF, 0xFFFFFF, new FabricItemSettings()),
            "rainbow_ant_spawn_egg"
    );

    public static final Item DEAD_RAINBOW_ANT = register(new Item(new FabricItemSettings()), "dead_rainbow_ant");
    public static final Item RAINBOW_ANT_FANG = register(new Item(new FabricItemSettings()), "rainbow_ant_fang");
    public static final Item RAINBOW_ANT_LEG = register(new Item(new FabricItemSettings()), "rainbow_ant_leg");
    public static final Item RAINBOW_ANT_EYE = register(new Item(new FabricItemSettings()), "rainbow_ant_eye");
    public static final Item RAINBOW_ANT_ABDOMEN = register(new Item(new FabricItemSettings()), "rainbow_ant_abdomen");

    // Drop da Brown Ant Queen no addon; aqui por enquanto só serve pra poção de combate.
    public static final Item BROWN_ANT_MANDIBLE = register(new Item(new FabricItemSettings()), "brown_ant_mandible");

    public static final Item COMBAT_POTION = register(
            new CombatPotionItem(0, 180, new FabricItemSettings().maxCount(1)),
            "combat_potion"
    );

    public static final Item POWERED_COMBAT_POTION = register(
            new CombatPotionItem(1, 8 * 60, new FabricItemSettings().maxCount(1)),
            "powered_combat_potion"
    );

    public static final Item LITTLE_MINT_HAT = register(
            new LittleMintItem(new FabricItemSettings().maxCount(1)),
            "little_mint_hat"
    );

    public static final Item SALT = register(new ItemNameBlockItem(ModBlocks.SALT_DUST, new FabricItemSettings()), "salt");

    // Come ou planta, igual ao block_placer + food do addon.
    public static final Item TOMATO = register(
            new ItemNameBlockItem(ModBlocks.TOMATO_PLANT_HEAD, new FabricItemSettings()
                    .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.55f).build())),
            "tomato"
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> itemGroup.accept(ModItems.ULTIMATE_UPGRADE));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
                .register((itemGroup) -> itemGroup.accept(ModItems.LASER_CHARGE));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> itemGroup.accept(ModItems.DRAKONIC_EYE));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> itemGroup.accept(ModItems.CRYSTAL_STICK));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
                .register((itemGroup) -> itemGroup.accept(RAINBOW_ANT_SPAWN_EGG));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register((itemGroup) -> {
            itemGroup.accept(DEAD_RAINBOW_ANT);
            itemGroup.accept(RAINBOW_ANT_FANG);
            itemGroup.accept(RAINBOW_ANT_LEG);
            itemGroup.accept(RAINBOW_ANT_EYE);
            itemGroup.accept(RAINBOW_ANT_ABDOMEN);
            itemGroup.accept(BROWN_ANT_MANDIBLE);
            itemGroup.accept(SALT);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register((itemGroup) -> {
            itemGroup.accept(TOMATO);
            itemGroup.accept(COMBAT_POTION);
            itemGroup.accept(POWERED_COMBAT_POTION);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((itemGroup) -> itemGroup.accept(LITTLE_MINT_HAT));
    }
}
