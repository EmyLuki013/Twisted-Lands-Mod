package com.twistedteam.twistedlands.block;

import com.google.gson.JsonObject;
import com.twistedteam.twistedlands.TwistedLandsBlocks;
import com.twistedteam.twistedlands.mixin.TransientCraftingContainerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
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

    /** Receita com a tag "crystal" só no addon: igual à shaped vanilla, mas só fecha no grid desta mesa. */
    public static class Recipe extends ShapedRecipe {
        Recipe(ShapedRecipe recipe) {
            super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getWidth(), recipe.getHeight(),
                    recipe.getIngredients(), recipe.getResultItem(null), recipe.showNotification());
        }

        @Override
        public boolean matches(CraftingContainer container, Level level) {
            return container instanceof TransientCraftingContainerAccessor grid && grid.twisted$getMenu() instanceof Menu
                    && super.matches(container, level);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return TwistedLandsBlocks.CRYSTAL_SHAPED_SERIALIZER;
        }
    }

    public static class Serializer implements RecipeSerializer<Recipe> {
        @Override
        public Recipe fromJson(ResourceLocation id, JsonObject json) {
            return new Recipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
        }

        @Override
        public Recipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new Recipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, Recipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
        }
    }
}
