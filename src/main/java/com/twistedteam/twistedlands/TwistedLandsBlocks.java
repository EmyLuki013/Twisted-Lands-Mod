package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.block.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TwistedLandsBlocks {

    public static Block register(Block block, String id) {
        return register(block, id, new FabricItemSettings());
    }

    public static Block register(Block block, String id, FabricItemSettings itemSettings) {
        Registry.register(BuiltInRegistries.BLOCK, TwistedLands.id(id), block);
        Registry.register(BuiltInRegistries.ITEM, TwistedLands.id(id), new BlockItem(block, itemSettings));

        return block;
    }

    // Blocos da Backroom: som de lã (cloth no Bedrock), 1s pra quebrar e cor de mapa próxima do #B69834 do addon.
    private static BlockBehaviour.Properties backroom() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(1.0f)
                .sound(SoundType.WOOL);
    }

    public static final Block AMETHYST_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "amethyst_ore"
    );

    public static final Block DEEPSLATE_AMETHYST_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "deepslate_amethyst_ore"
    );

    public static final Block AMETHYST_BLOCK = register(
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)),
            "amethyst_block"
    );

    public static final Block RUBY_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "ruby_ore"
    );

    public static final Block DEEPSLATE_RUBY_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE),
                    UniformInt.of(3, 7)),
            "deepslate_ruby_ore"
    );

    public static final Block RUBY_BLOCK = register(
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)),
            "ruby_block"
    );

    public static final Block BACKROOMS_WALLPAPER = register(
            new Block(backroom()),
            "backrooms_wallpaper"
    );

    public static final Block WALLPAPER_PLANKS = register(
            new Block(backroom()),
            "wallpaper_planks"
    );

    public static final Block WALLPAPER_SMOOTH = register(
            new Block(backroom()),
            "wallpaper_smooth"
    );

    public static final Block WALLPAPER_SMOOTH_LAMP = register(
            new BackroomLampBlock(backroom().lightLevel(state -> 15)),
            "wallpaper_smooth_lamp"
    );

    public static final Block WALLPAPER = register(
            new WallpaperBlock(backroom().noOcclusion()),
            "wallpaper"
    );

    public static final Block WALLPAPER_CORNER = register(
            new WallpaperCornerBlock(backroom().noOcclusion()),
            "wallpaper_corner"
    );

    public static final Block WALLPAPER_MOSAIC = register(
            new Block(backroom()),
            "wallpaper_mosaic"
    );

    // As versões "dark" das lâmpadas são a lâmpada apagada: no addon não têm luz nem zumbido.
    public static final Block WALLPAPER_SMOOTH_DARK = register(
            new Block(backroom()),
            "wallpaper_smooth_dark"
    );

    public static final Block WALLPAPER_SMOOTH_DARK_LAMP = register(
            new Block(backroom()),
            "wallpaper_smooth_dark_lamp"
    );

    public static final Block WALLPAPER_TILED = register(
            new Block(backroom()),
            "wallpaper_tiled"
    );

    public static final Block WALLPAPER_TILED_DARK = register(
            new Block(backroom()),
            "wallpaper_tiled_dark"
    );

    public static final Block WALLPAPER_TILED_DARK_LAMP = register(
            new Block(backroom()),
            "wallpaper_tiled_dark_lamp"
    );

    public static final Block WALLPAPER_TILED_LAMP = register(
            new BackroomLampBlock(backroom().lightLevel(state -> 15)),
            "wallpaper_tiled_lamp"
    );

    public static final Block POOL_TILES = register(
            new Block(backroom()),
            "pool_tiles"
    );

    public static final Block UNCRYSTAL_WOOD_PLANKS = register(
            new Block(backroom()),
            "uncrystal_wood_planks"
    );

    // Gira conforme a face clicada, igual tronco (no addon era o trait placement_position/block_face).
    public static final Block UNCRYSTAL_WOOD_PILLAR = register(
            new RotatedPillarBlock(backroom()),
            "uncrystal_wood_pillar"
    );

    // Candy Lands. Tempo de quebra, resistência e som tirados do addon.
    public static final Block CANDY_CANE_LOG = register(
            new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(2.0f).sound(SoundType.WOOD)),
            "candy_cane_log"
    );

    public static final Block CANDY_CANE_WOOD = register(
            new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(2.0f).sound(SoundType.WOOD)),
            "candy_cane_wood"
    );

    public static final Block COOKIE_GRASS = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.6f).sound(SoundType.GRASS)),
            "cookie_grass"
    );

    public static final Block COOKIE_STONE = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.5f, 6.0f).sound(SoundType.STONE)),
            "cookie_stone"
    );

    // No addon é um bloco comum que não some longe do tronco, então não é LeavesBlock.
    public static final Block COTTON_LEAVES = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(0.35f, 0.2f).sound(SoundType.GRASS)
                    .noOcclusion().isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)),
            "cotton_leaves"
    );

    public static final Block CHOCOLATE_BAR_BLOCK = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f).sound(SoundType.STONE)),
            "chocolate_bar_block"
    );

    public static final Block SUGAR_BLOCK = register(
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.5f).sound(SoundType.STONE)),
            "sugar_block"
    );

    public static final Block PIZZA = register(
            new PizzaBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).instabreak().sound(SoundType.WOOL)
                    .noOcclusion().pushReaction(PushReaction.DESTROY)),
            "pizza",
            new FabricItemSettings().maxCount(1)
    );

    public static final Block EXTREME_TORCH = register(
            new ExtremeTorchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak().sound(SoundType.WOOD)
                    .noCollission().noOcclusion().lightLevel(state -> 15).pushReaction(PushReaction.DESTROY)),
            "extreme_torch"
    );

    // Sal. O minério solta de 1 a 5 de XP; o bloco e o pó espantam formigas (ver SaltBlock).
    public static final Block SALT_ORE = register(
            new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 1.0f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE), UniformInt.of(1, 5)),
            "salt_ore"
    );

    public static final Block SALT_BLOCK = register(
            new SaltBlock(40, 40, BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.0f)
                    .lightLevel(state -> 3).sound(SoundType.SAND)),
            "salt_block"
    );

    // Sem item próprio: quem coloca é o item salt.
    public static final Block SALT_DUST = registerBlockOnly(
            new SaltDustBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.5f).lightLevel(state -> 1)
                    .noOcclusion().sound(SoundType.SAND).pushReaction(PushReaction.DESTROY)),
            "salt_dust"
    );

    // Pé de tomate. Quem planta é o item tomato.
    public static final Block TOMATO_PLANT_HEAD = registerBlockOnly(
            new TomatoPlantHeadBlock(tomatoPlant()),
            "tomato_plant_head"
    );

    public static final Block TOMATO_PLANT_BODY = registerBlockOnly(
            new TomatoPlantBodyBlock(tomatoPlant()),
            "tomato_plant_body"
    );

    private static Block registerBlockOnly(Block block, String id) {
        return Registry.register(BuiltInRegistries.BLOCK, TwistedLands.id(id), block);
    }

    private static BlockBehaviour.Properties tomatoPlant() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().randomTicks()
                .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY);
    }

    public static void register() {
        // Mesma inflamabilidade do addon (catch 5, destroy 20).
        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        flammable.add(CANDY_CANE_LOG, 5, 20);
        flammable.add(CANDY_CANE_WOOD, 5, 20);
        flammable.add(COTTON_LEAVES, 5, 20);
    }
}
