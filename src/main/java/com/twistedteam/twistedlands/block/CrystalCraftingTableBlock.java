package com.twistedteam.twistedlands.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal Crafting Table. Abre a tela de crafting vanilla, então também faz as receitas normais. O menu é
 * próprio pras receitas que no addon só existem nela (tag "crystal") saberem em que mesa estão.
 */
public class CrystalCraftingTableBlock extends CraftingTableBlock {
    private static final Component TITLE = Component.translatable("container.twisted.crystal_crafting_table");

    public CrystalCraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new Menu(id, inventory, ContainerLevelAccess.create(level, pos), this), TITLE);
    }

    // light_dampening 0 no addon, como os outros blocos de cristal.
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    public static class Menu extends CraftingMenu {
        private final ContainerLevelAccess access;
        private final Block block;

        Menu(int id, Inventory inventory, ContainerLevelAccess access, Block block) {
            super(id, inventory, access);
            this.access = access;
            this.block = block;
        }

        // O CraftingMenu só aceita a crafting table vanilla.
        @Override
        public boolean stillValid(Player player) {
            return stillValid(access, player, block);
        }
    }
}
