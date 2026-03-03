package net.mrafton.thechaotic.screen.machine;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.mrafton.thechaotic.TheChaotic;

public class ChronoCraftingTableScreen extends AbstractContainerScreen<ChronoCraftingTableMenu> {
    private static final ResourceLocation CHRONO_CRAFTING_TABLE_GUI =
            ResourceLocation.fromNamespaceAndPath(TheChaotic.MOD_ID, "textures/gui/chrono_crafting_table/chrono_crafting_table.png");


    public ChronoCraftingTableScreen(ChronoCraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHRONO_CRAFTING_TABLE_GUI);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(CHRONO_CRAFTING_TABLE_GUI, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
