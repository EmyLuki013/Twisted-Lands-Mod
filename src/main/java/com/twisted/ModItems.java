package com.twisted;

import net.minecraft.item.Item;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static void initialize() {
    }

    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        Identifier itemID = new Identifier(ExampleMod.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(BuiltInRegistries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

    public static final Item SUSPICIOUS_SUBSTANCE = register(
        // Ignore the food component for now, we'll cover it later in the food section.
        new Item(new FabricItemSettings().food(SUSPICIOUS_FOOD_COMPONENT)),
        "suspicious_substance"
    );
}
