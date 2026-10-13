package com.twistedteam.twistedlands.client;

import com.twistedteam.twistedlands.block.CrystalFurnaceBlock;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Tela da Crystal Furnace: a da fornalha vanilla sem o livro de receitas, que só mostraria as receitas da
 * fornalha comum.
 */
public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceBlock.Menu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    public CrystalFurnaceScreen(CrystalFurnaceBlock.Menu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    // Mesmas coordenadas do AbstractFurnaceScreen: fogo e seta de progresso.
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isLit()) {
            int lit = menu.getLitProgress();
            graphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 12 - lit, 176, 12 - lit, 14, lit + 1);
        }
        int burn = menu.getBurnProgress();
        graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, burn + 1, 16);
    }
}
