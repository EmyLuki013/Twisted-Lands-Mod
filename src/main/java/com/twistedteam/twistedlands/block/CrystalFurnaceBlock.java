package com.twistedteam.twistedlands.block;

import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.TwistedLandsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal Furnace. No addon era uma grade de crafting (o item em cima do crystal coal, pronto na hora). Aqui é
 * uma fornalha de verdade, que só queima Crystal Coal (ou o bloco dele) e só faz as receitas twisted:crystal_smelting.
 */
public class CrystalFurnaceBlock extends AbstractFurnaceBlock {
    // Iguais ao carvão e ao bloco de carvão.
    private static final int COAL_BURN_TIME = 1600;
    private static final int BLOCK_BURN_TIME = 16000;
    private static final Component TITLE = Component.translatable("container.twisted.crystal_furnace");

    public CrystalFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, TwistedLandsBlocks.CRYSTAL_FURNACE_ENTITY);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof Entity furnace) {
            player.openMenu(furnace);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    private static int burnTime(ItemStack stack) {
        if (stack.is(TwistedLandsItems.CRYSTAL_COAL)) return COAL_BURN_TIME;
        if (stack.is(TwistedLandsBlocks.CRYSTAL_COAL_BLOCK.asItem())) return BLOCK_BURN_TIME;
        return 0;
    }

    private static boolean isCrystalFuel(ItemStack stack) {
        return burnTime(stack) > 0;
    }

    public static class Entity extends AbstractFurnaceBlockEntity {
        public Entity(BlockPos pos, BlockState state) {
            super(TwistedLandsBlocks.CRYSTAL_FURNACE_ENTITY, pos, state, TwistedLandsBlocks.CRYSTAL_SMELTING);
        }

        @Override
        protected Component getDefaultName() {
            return TITLE;
        }

        @Override
        protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
            return new Menu(id, inventory, this, dataAccess);
        }

        @Override
        protected int getBurnDuration(ItemStack fuel) {
            return burnTime(fuel);
        }

        // Funil e afins: o slot de combustível só aceita o combustível de cristal.
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot == SLOT_FUEL ? isCrystalFuel(stack) : super.canPlaceItem(slot, stack);
        }
    }

    public static class Menu extends AbstractFurnaceMenu {
        public Menu(int id, Inventory inventory) {
            super(TwistedLandsBlocks.CRYSTAL_FURNACE_MENU, TwistedLandsBlocks.CRYSTAL_SMELTING, RecipeBookType.FURNACE, id, inventory);
        }

        Menu(int id, Inventory inventory, Container container, ContainerData data) {
            super(TwistedLandsBlocks.CRYSTAL_FURNACE_MENU, TwistedLandsBlocks.CRYSTAL_SMELTING, RecipeBookType.FURNACE, id,
                    inventory, container, data);
        }

        @Override
        protected boolean isFuel(ItemStack stack) {
            return isCrystalFuel(stack);
        }
    }

    public static class Smelting extends AbstractCookingRecipe {
        public Smelting(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient,
                        ItemStack result, float experience, int cookingTime) {
            super(TwistedLandsBlocks.CRYSTAL_SMELTING, id, group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return TwistedLandsBlocks.CRYSTAL_SMELTING_SERIALIZER;
        }

        @Override
        public ItemStack getToastSymbol() {
            return new ItemStack(TwistedLandsBlocks.CRYSTAL_FURNACE);
        }

        // Fora do livro de receitas: ele não conhece esse tipo e avisa no log pra cada receita.
        @Override
        public boolean isSpecial() {
            return true;
        }
    }
}
