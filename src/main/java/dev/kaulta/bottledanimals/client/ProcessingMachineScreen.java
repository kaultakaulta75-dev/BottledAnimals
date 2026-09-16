package dev.kaulta.bottledanimals.client;

import dev.kaulta.bottledanimals.block.entity.ProcessingMachineBlockEntity;
import dev.kaulta.bottledanimals.menu.ProcessingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ProcessingMachineScreen
        extends AbstractContainerScreen<ProcessingMachineMenu> {
    private static final int PANEL = 0xFF252A32;
    private static final int BORDER = 0xFF707988;
    private static final int SLOT = 0xFF11151B;
    private static final int PROGRESS = 0xFF54C6EB;
    private static final int ENERGY = 0xFFE34B4B;
    private static final int MILK = 0xFFF3F3E5;

    public ProcessingMachineScreen(
            ProcessingMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, PANEL);
        graphics.fill(left, top, left + imageWidth, top + 2, BORDER);
        graphics.fill(left, top + imageHeight - 2, left + imageWidth, top + imageHeight, BORDER);
        graphics.fill(left, top, left + 2, top + imageHeight, BORDER);
        graphics.fill(left + imageWidth - 2, top, left + imageWidth, top + imageHeight, BORDER);

        drawSlot(graphics, left + 17, top + 34);
        drawSlot(graphics, left + 43, top + 34);
        drawSlot(graphics, left + 69, top + 34);
        drawSlot(graphics, left + 95, top + 34);
        drawSlot(graphics, left + 133, top + 34);

        graphics.fill(left + 115, top + 40, left + 131, top + 47, SLOT);
        int progress = menu.getProgressScaled(16);
        graphics.fill(left + 115, top + 40, left + 115 + progress, top + 47, PROGRESS);

        graphics.fill(left + 153, top + 24, left + 161, top + 78, SLOT);
        int energy = menu.getEnergyScaled(52);
        graphics.fill(left + 154, top + 77 - energy, left + 160, top + 77, ENERGY);

        if (menu.hasFluidTank()) {
            graphics.fill(left + 164, top + 24, left + 172, top + 78, SLOT);
            int fluid = menu.getFluidScaled(52);
            graphics.fill(left + 165, top + 77 - fluid, left + 171, top + 77, MILK);
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, left + 7 + column * 18, top + 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, left + 7 + column * 18, top + 141);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, BORDER);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE0E6EF, false);
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.bottledanimals.energy",
                        menu.getEnergyStored(),
                        ProcessingMachineBlockEntity.CAPACITY),
                8,
                61,
                0xFFE0E6EF,
                false);
        if (menu.hasFluidTank()) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "screen.bottledanimals.milk",
                            menu.getFluidAmount(),
                            menu.getFluidCapacity()),
                    8,
                    71,
                    0xFFE0E6EF,
                    false);
        }
    }
}
