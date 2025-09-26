package net.mrafton.thechaotic.screen.machine;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.mrafton.thechaotic.TheChaotic;
import net.mrafton.thechaotic.screen.renderer.EnergyDisplayTooltipArea;
import net.mrafton.thechaotic.screen.renderer.FluidTankRenderer;
import net.neoforged.neoforge.fluids.FluidStack;


import java.util.Optional;

import static net.mrafton.thechaotic.util.MouseUtil.isMouseAboveArea;

public class ChronoCrafterScreen extends AbstractContainerScreen<ChronoCrafterMenu> {
    private static final ResourceLocation CHRONO_CRAFTER_GUI =
            ResourceLocation.fromNamespaceAndPath(TheChaotic.MOD_ID, "textures/gui/chronocrafter/chrono_crafter.png");
    private static final ResourceLocation PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath(TheChaotic.MOD_ID, "textures/gui/chronocrafter/progress_bar.png");
    private EnergyDisplayTooltipArea energyInfoArea;
    private FluidTankRenderer fluidRenderer;

    public ChronoCrafterScreen(ChronoCrafterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelY=10000;
        this.titleLabelY=10000;
        assignEnergyInfoArea();
        assignFluidRenderer();
    }

    private void assignFluidRenderer() {
        fluidRenderer = new FluidTankRenderer(16000, true, 16, 50);
    }
    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyDisplayTooltipArea(((width -imageWidth) /2) +156,
                ((height-imageHeight) /2 )+9, menu.blockEntity.getEnergyStorage(null ),8,48);
    }

    private void renderEnergyAreaTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int x, int y) {
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 156, 11, 8, 48)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }
    private void renderFluidTooltipArea(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int x, int y,
                                        FluidStack stack, int offsetX, int offsetY, FluidTankRenderer renderer) {
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, offsetX, offsetY, renderer)) {
            guiGraphics.renderTooltip(this.font, renderer.getTooltip(stack, TooltipFlag.Default.NORMAL),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderEnergyAreaTooltip(guiGraphics,mouseX,mouseY,x,y);
        renderFluidTooltipArea(guiGraphics, mouseX, mouseY, x, y, menu.blockEntity.getFluid(), 8, 7, fluidRenderer);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int mouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CHRONO_CRAFTER_GUI);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(CHRONO_CRAFTER_GUI, x, y, 0, 0, imageWidth, imageHeight);

        energyInfoArea.render(guiGraphics);
        fluidRenderer.render(guiGraphics,x+8,y+7,menu.blockEntity.getFluid());
        renderProgressArrow(guiGraphics, x, y);

    }

    private void renderProgressArrow(GuiGraphics guiGraphics,int x, int y ){
        if (menu.isCrafting()) {
           guiGraphics.blit(PROGRESS_BAR,x+ 130,y + 33 + 18 -menu.getScaledArrowProgress(),0,
                   17 -menu.getScaledArrowProgress(),6,menu.getScaledArrowProgress(),6,18 );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

}
