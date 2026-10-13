package com.twistedteam.twistedlands.items.enchant;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike; // <-- IMPORT ADICIONADO

import java.util.HashMap;
import java.util.Map;

public final class EnchantPresets {

    public static final String APPLIED_KEY = "thedivision_preset_applied";

    private static final int CHECK_INTERVAL_TICKS = 5;

    private EnchantPresets() {}

    public static boolean apply(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PreEnchanted preEnchanted)) {
            return false;
        }

        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.getBoolean(APPLIED_KEY)) {
            return false;
        }

        Map<Enchantment, Integer> enchantments =
                new HashMap<>(EnchantmentHelper.getEnchantments(stack));

        preEnchanted.presetEnchantments().forEach(
                (enchantment, level) -> enchantments.merge(enchantment, level, Integer::max));

        EnchantmentHelper.setEnchantments(enchantments, stack);
        stack.getOrCreateTag().putBoolean(APPLIED_KEY, true);
        return true;
    }

    public static ItemStack stackOf(ItemLike item) {
        ItemStack stack = new ItemStack(item);
        apply(stack);
        return stack;
    }

    public static void registerTickHook() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                Inventory inventory = player.getInventory();
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                    apply(inventory.getItem(slot));
                }
                apply(player.containerMenu.getCarried());
            }
        });
    }
}