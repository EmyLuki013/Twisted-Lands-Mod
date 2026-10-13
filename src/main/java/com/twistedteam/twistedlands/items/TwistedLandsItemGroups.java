package com.twistedteam.twistedlands.items;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsEntities;
import com.twistedteam.twistedlands.TwistedLandsItems;
import com.twistedteam.twistedlands.TwistedLands;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class TwistedLandsItemGroups {

    public static final ResourceKey<CreativeModeTab> TWISTED_GROUP_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            new ResourceLocation(TwistedLands.MOD_ID, "twisted_weapons")
    );

    public static final ResourceKey<CreativeModeTab> TWISTED_GROUP_INGREDIENTS_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            new ResourceLocation(TwistedLands.MOD_ID, "twisted_ingredients")
    );

    public static final ResourceKey<CreativeModeTab> TWISTED_GROUP_SPAWN_EGG_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            new ResourceLocation(TwistedLands.MOD_ID, "twisted_spawn_eggs")
    );

    public static final ResourceKey<CreativeModeTab> TWISTED_GROUP_BLOCKS_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            new ResourceLocation(TwistedLands.MOD_ID, "twisted_blocks")
    );

    public static final ResourceKey<CreativeModeTab> TWISTED_GROUP_FOOD_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            new ResourceLocation(TwistedLands.MOD_ID, "twisted_foods")
    );

    public static final CreativeModeTab TWISTED_BLOCKS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(TwistedLandsBlocks.AMETHYST_BLOCK))
            .title(Component.translatable("itemGroup.twisted.twisted_blocks"))
            .build();

    public static final CreativeModeTab TWISTED_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(TwistedLandsItems.BIG_BERTHA))
            .title(Component.translatable("itemGroup.twisted.twisted_weapons"))
            .build();

    public static final CreativeModeTab TWISTED_GROUP_INGREDIENTS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(TwistedLandsItems.ULTIMATE_INGOT))
            .title(Component.translatable("itemGroup.twisted.twisted_ingredients"))
            .build();

    public static final CreativeModeTab TWISTED_GROUP_SPAWN_EGGS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(TwistedLandsItems.ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG))
            .title(Component.translatable("itemGroup.twisted.twisted_spawn_eggs"))
            .build();

    public static final CreativeModeTab TWISTED_GROUP_FOODS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(TwistedLandsItems.COOKED_DINO_MEAT))
            .title(Component.translatable("itemGroup.twisted.twisted_foods"))
            .build();

    public static void initialize() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                TWISTED_GROUP_KEY,
                TWISTED_GROUP
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                TWISTED_GROUP_INGREDIENTS_KEY,
                TWISTED_GROUP_INGREDIENTS
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                TWISTED_GROUP_SPAWN_EGG_KEY,
                TWISTED_GROUP_SPAWN_EGGS
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                TWISTED_GROUP_BLOCKS_KEY,
                TWISTED_BLOCKS
        );

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                TWISTED_GROUP_FOOD_KEY,
                TWISTED_GROUP_FOODS
        );

        ItemGroupEvents.modifyEntriesEvent(TWISTED_GROUP_KEY).register(entries -> {
            entries.accept(TwistedLandsItems.AMETHYST_HELMET);
            entries.accept(TwistedLandsItems.AMETHYST_CHESTPLATE);
            entries.accept(TwistedLandsItems.AMETHYST_LEGGINGS);
            entries.accept(TwistedLandsItems.AMETHYST_BOOTS);
            entries.accept(TwistedLandsItems.AMETHYST_SWORD);
            entries.accept(TwistedLandsItems.AMETHYST_PICKAXE);
            entries.accept(TwistedLandsItems.AMETHYST_AXE);
            entries.accept(TwistedLandsItems.AMETHYST_SHOVEL);
            entries.accept(TwistedLandsItems.AMETHYST_HOE);
            entries.accept(TwistedLandsItems.RUBY_HELMET);
            entries.accept(TwistedLandsItems.RUBY_CHESTPLATE);
            entries.accept(TwistedLandsItems.RUBY_LEGGINGS);
            entries.accept(TwistedLandsItems.RUBY_BOOTS);
            entries.accept(TwistedLandsItems.RUBY_SWORD);
            entries.accept(TwistedLandsItems.RUBY_PICKAXE);
            entries.accept(TwistedLandsItems.RUBY_AXE);
            entries.accept(TwistedLandsItems.RUBY_SHOVEL);
            entries.accept(TwistedLandsItems.RUBY_HOE);
            entries.accept(TwistedLandsItems.ROYAL_HELMET);
            entries.accept(TwistedLandsItems.ROYAL_CHESTPLATE);
            entries.accept(TwistedLandsItems.ROYAL_LEGGINGS);
            entries.accept(TwistedLandsItems.ROYAL_BOOTS);
            entries.accept(TwistedLandsItems.BIG_BERTHA);
            entries.accept(TwistedLandsItems.ROYAL_GUARDIAN);
            entries.accept(TwistedLandsItems.STELIX_BERTHA);
            entries.accept(TwistedLandsItems.ULTIMATE_HAMMER);
            entries.accept(TwistedLandsItems.ULTIMATE_CHAINSAW);
            entries.accept(TwistedLandsItems.QUEEN_BATTLE_AXE);
            entries.accept(TwistedLandsItems.BATTLE_AXE);
            entries.accept(TwistedLandsItems.FIRE_BLASTER);
            entries.accept(TwistedLandsItems.WATER_BLASTER);
            entries.accept(TwistedLandsItems.MANTIS_CLAW);
            entries.accept(TwistedLandsItems.LASER_CHARGE);
            TwistedLandsItems.DREAM_WORLD.stream()
                    .filter(item -> item instanceof TieredItem || item instanceof ArmorItem)
                    .forEach(entries::accept);
        });

        ItemGroupEvents.modifyEntriesEvent(TWISTED_GROUP_INGREDIENTS_KEY).register(entries -> {
            entries.accept(TwistedLandsItems.DRAKONIC_EYE);
            entries.accept(TwistedLandsItems.CRYSTAL_STICK);
            entries.accept(TwistedLandsItems.ULTIMATE_UPGRADE);
            entries.accept(TwistedLandsItems.ULTIMATE_INGOT);
            entries.accept(TwistedLandsItems.SKOTODRAKON_SCALE);
            entries.accept(TwistedLandsItems.MOBJIRA_SCALE);
            entries.accept(TwistedLandsItems.AMETHYST);
            entries.accept(TwistedLandsItems.RUBY);
            entries.accept(TwistedLandsItems.DINOSSAUR_TOOTH);
            entries.accept(TwistedLandsItems.DEAD_RAINBOW_ANT);
            entries.accept(TwistedLandsItems.RAINBOW_ANT_FANG);
            entries.accept(TwistedLandsItems.RAINBOW_ANT_LEG);
            entries.accept(TwistedLandsItems.RAINBOW_ANT_EYE);
            entries.accept(TwistedLandsItems.RAINBOW_ANT_ABDOMEN);
            entries.accept(TwistedLandsItems.COMBAT_POTION);
            entries.accept(TwistedLandsItems.POWERED_COMBAT_POTION);
            TwistedLandsItems.DREAM_WORLD.stream()
                    .filter(item -> !(item instanceof TieredItem || item instanceof ArmorItem || item.isEdible()))
                    .forEach(entries::accept);
        });

        ItemGroupEvents.modifyEntriesEvent(TWISTED_GROUP_BLOCKS_KEY).register(entries -> {
            // Backrooms / Wallpaper
            entries.accept(TwistedLandsBlocks.BACKROOMS_WALLPAPER);
            entries.accept(TwistedLandsBlocks.WALLPAPER_PLANKS);
            entries.accept(TwistedLandsBlocks.WALLPAPER_SMOOTH);
            entries.accept(TwistedLandsBlocks.WALLPAPER_SMOOTH_LAMP);
            entries.accept(TwistedLandsBlocks.WALLPAPER);
            entries.accept(TwistedLandsBlocks.WALLPAPER_CORNER);
            entries.accept(TwistedLandsBlocks.WALLPAPER_MOSAIC);
            entries.accept(TwistedLandsBlocks.WALLPAPER_SMOOTH_DARK);
            entries.accept(TwistedLandsBlocks.WALLPAPER_SMOOTH_DARK_LAMP);
            entries.accept(TwistedLandsBlocks.WALLPAPER_TILED);
            entries.accept(TwistedLandsBlocks.WALLPAPER_TILED_DARK);
            entries.accept(TwistedLandsBlocks.WALLPAPER_TILED_DARK_LAMP);
            entries.accept(TwistedLandsBlocks.WALLPAPER_TILED_LAMP);
            entries.accept(TwistedLandsBlocks.POOL_TILES);

            // Madeira / Candy
            entries.accept(TwistedLandsBlocks.UNCRYSTAL_WOOD_PLANKS);
            entries.accept(TwistedLandsBlocks.UNCRYSTAL_WOOD_PILLAR);
            entries.accept(TwistedLandsBlocks.CANDY_CANE_LOG);
            entries.accept(TwistedLandsBlocks.CANDY_CANE_WOOD);

            // Comida / Doce
            entries.accept(TwistedLandsBlocks.COOKIE_GRASS);
            entries.accept(TwistedLandsBlocks.COOKIE_STONE);
            entries.accept(TwistedLandsBlocks.COTTON_LEAVES);
            entries.accept(TwistedLandsBlocks.CHOCOLATE_BAR_BLOCK);
            entries.accept(TwistedLandsBlocks.SUGAR_BLOCK);
            entries.accept(TwistedLandsBlocks.PIZZA);

            // Utilitários
            entries.accept(TwistedLandsBlocks.EXTREME_TORCH);

            // Sal
            entries.accept(TwistedLandsBlocks.SALT_ORE);
            entries.accept(TwistedLandsBlocks.SALT_BLOCK);
            entries.accept(TwistedLandsBlocks.SALT_DUST);

            // Minérios e blocos de Amethyst
            entries.accept(TwistedLandsBlocks.AMETHYST_ORE);
            entries.accept(TwistedLandsBlocks.DEEPSLATE_AMETHYST_ORE);
            entries.accept(TwistedLandsBlocks.AMETHYST_BLOCK);

            // Minérios e blocos de Ruby
            entries.accept(TwistedLandsBlocks.RUBY_ORE);
            entries.accept(TwistedLandsBlocks.DEEPSLATE_RUBY_ORE);
            entries.accept(TwistedLandsBlocks.RUBY_BLOCK);

            // Dream World
            for (Block block : List.of(TwistedLandsBlocks.CRYSTAL_GRASS, TwistedLandsBlocks.KYANITE,
                    TwistedLandsBlocks.DREAM_WORLD_GRASS_BLOCK, TwistedLandsBlocks.DREAM_STONE, TwistedLandsBlocks.CRYSTAL_WOOD_LOG,
                    TwistedLandsBlocks.STRIPPED_CRYSTAL_WOOD_LOG, TwistedLandsBlocks.CRYSTAL_WOOD_PLANKS,
                    TwistedLandsBlocks.RED_CRYSTAL_WOOD_LOG, TwistedLandsBlocks.STRIPPED_RED_CRYSTAL_WOOD_LOG,
                    TwistedLandsBlocks.RED_CRYSTAL_WOOD_PLANKS, TwistedLandsBlocks.CRYSTAL_MUSHROOM_STEM,
                    TwistedLandsBlocks.CRYSTAL_LEAVES_BLUE, TwistedLandsBlocks.CRYSTAL_LEAVES_GREEN, TwistedLandsBlocks.CRYSTAL_LEAVES_PINK,
                    TwistedLandsBlocks.CRYSTAL_LEAVES_PURPLE, TwistedLandsBlocks.CRYSTAL_LEAVES_PURPLISH_RED,
                    TwistedLandsBlocks.PURPLE_CRYSTAL_FLOWER, TwistedLandsBlocks.RED_CRYSTAL_FLOWER,
                    TwistedLandsBlocks.CRYSTAL_CRAFTING_TABLE, TwistedLandsBlocks.CRYSTAL_FURNACE, TwistedLandsBlocks.CRYSTAL_TORCH,
                    TwistedLandsBlocks.CRYSTAL_COAL_ORE, TwistedLandsBlocks.CRYSTAL_COAL_BLOCK, TwistedLandsBlocks.JADE_ORE,
                    TwistedLandsBlocks.JADE_BLOCK, TwistedLandsBlocks.JADE_BRICKS, TwistedLandsBlocks.PINK_TOURMALINE_ORE,
                    TwistedLandsBlocks.PINK_TOURMALINE_BLOCK, TwistedLandsBlocks.PINK_TOURMALINE_BRICKS,
                    TwistedLandsBlocks.TIGERS_EYE_DEBRIS, TwistedLandsBlocks.TIGERS_EYE_BLOCK)) {
                entries.accept(block);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(TWISTED_GROUP_SPAWN_EGG_KEY).register(entries -> {
            entries.accept(TwistedLandsItems.APPLE_COW_SPAWN_EGG);
            entries.accept(TwistedLandsItems.GOLDEN_APPLE_COW_SPAWN_EGG);
            entries.accept(TwistedLandsItems.ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG);
            entries.accept(TwistedLandsItems.MOBJIRA_SPAWN_EGG);
            entries.accept(TwistedLandsItems.MANTIS_SPAWN_EGG);
            entries.accept(TwistedLandsItems.NASTYSAURUS_SPAWN_EGG);
            entries.accept(TwistedLandsItems.RAINBOW_ANT_SPAWN_EGG);
            entries.accept(TwistedLandsItems.RAT_SPAWN_EGG);
        });

        ItemGroupEvents.modifyEntriesEvent(TWISTED_GROUP_FOOD_KEY).register(entries -> {
            entries.accept(TwistedLandsItems.COOKED_DINO_MEAT);
            entries.accept(TwistedLandsItems.RAW_DINO_MEAT);
            entries.accept(TwistedLandsItems.TOMATO);
            entries.accept(TwistedLandsItems.CANDY_CANE);
            entries.accept(TwistedLandsItems.COTTON_CANDY);
            entries.accept(TwistedLandsItems.PEANUT_CANDY);
            entries.accept(TwistedLandsItems.CHOCOLATE_BAR);
            entries.accept(TwistedLandsItems.PIZZA_PIECE);
            entries.accept(TwistedLandsItems.CHERRIES);
            entries.accept(TwistedLandsItems.APPLE_PIE);
            entries.accept(TwistedLandsItems.CHEESE);
            entries.accept(TwistedLandsItems.MAGIC_APPLE);
            entries.accept(TwistedLandsItems.SALT);
            entries.accept(TwistedLandsItems.CRYSTAL_APPLE);
            entries.accept(TwistedLandsItems.CRYSTAL_CORN);
        });
    }
}