package com.twistedteam.twistedlands;

import com.twistedteam.twistedlands.block.BackroomLampBlock;
import com.twistedteam.twistedlands.block.WallpaperBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
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

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register((itemGroup) -> {
            itemGroup.accept(BACKROOMS_WALLPAPER);
            itemGroup.accept(WALLPAPER);
            itemGroup.accept(WALLPAPER_PLANKS);
            itemGroup.accept(WALLPAPER_SMOOTH);
            itemGroup.accept(WALLPAPER_SMOOTH_LAMP);
        });
    }
}
