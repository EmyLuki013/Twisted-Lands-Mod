package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.block.BackroomLampBlock;
import com.twistedteam.twistedlands.block.WallpaperBlock;
import com.twistedteam.twistedlands.block.WallpaperCornerBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
    public static Block register(Block block, String id) {
        Registry.register(BuiltInRegistries.BLOCK, TwistedLands.id(id), block);
        Registry.register(BuiltInRegistries.ITEM, TwistedLands.id(id), new BlockItem(block, new FabricItemSettings()));

        return block;
    }

    // Blocos da Backroom: som de lã (cloth no Bedrock), 1s pra quebrar e cor de mapa próxima do #B69834 do addon.
    private static BlockBehaviour.Properties backroom() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(1.0f)
                .sound(SoundType.WOOL);
    }

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

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register((itemGroup) -> {
            itemGroup.accept(BACKROOMS_WALLPAPER);
            itemGroup.accept(POOL_TILES);
            itemGroup.accept(UNCRYSTAL_WOOD_PILLAR);
            itemGroup.accept(UNCRYSTAL_WOOD_PLANKS);
            itemGroup.accept(WALLPAPER);
            itemGroup.accept(WALLPAPER_CORNER);
            itemGroup.accept(WALLPAPER_MOSAIC);
            itemGroup.accept(WALLPAPER_PLANKS);
            itemGroup.accept(WALLPAPER_SMOOTH);
            itemGroup.accept(WALLPAPER_SMOOTH_DARK);
            itemGroup.accept(WALLPAPER_SMOOTH_DARK_LAMP);
            itemGroup.accept(WALLPAPER_SMOOTH_LAMP);
            itemGroup.accept(WALLPAPER_TILED);
            itemGroup.accept(WALLPAPER_TILED_DARK);
            itemGroup.accept(WALLPAPER_TILED_DARK_LAMP);
            itemGroup.accept(WALLPAPER_TILED_LAMP);
        });
    }
}
