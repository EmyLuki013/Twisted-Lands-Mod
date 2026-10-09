package com.twistedteam.twistedlands;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

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

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> itemGroup.accept(ModItems.ULTIMATE_UPGRADE));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
                .register((itemGroup) -> itemGroup.accept(ModItems.LASER_CHARGE));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> itemGroup.accept(ModItems.DRAKONIC_EYE));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> itemGroup.accept(ModItems.CRYSTAL_STICK));
    }
}
