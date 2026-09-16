package dev.kaulta.bottledanimals.client;

import dev.kaulta.bottledanimals.block.FoodMachineKind;
import dev.kaulta.bottledanimals.block.entity.FoodMachineBlockEntity;
import dev.kaulta.bottledanimals.menu.FoodMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FoodMachineScreen extends AbstractContainerScreen<FoodMachineMenu> {
    private static final int PANEL = 0xFF252A32;
    private static final int BORDER = 0xFF707988;
    private static final int SLOT = 0xFF11151B;
    private static final int PROGRESS = 0xFF54C6EB;
    private static final int ENERGY = 0xFFE34B4B;
    private static final int FOOD = 0xFFD89032;

    private static final int MODE_X = 76;
    private static final int MODE_Y = 34;
    private static final int MODE_WIDTH = 64;
    private static final int MODE_HEIGHT = 18;

    public FoodMachineScreen(
            FoodMachineMenu menu, Inventory playerInventory, Component title) {
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
        if (menu.getKind() == FoodMachineKind.CRUSHER) {
            drawSlot(graphics, left + 107, top + 34);
            drawSlot(graphics, left + 133, top + 34);
            graphics.fill(left + 82, top + 40, left + 104, top + 47, SLOT);
            graphics.fill(
                    left + 82,
                    top + 40,
                    left + 82 + menu.getProgressScaled(22),
                    top + 47,
                    PROGRESS);
        } else {
            graphics.fill(
                    left + MODE_X,
                    top + MODE_Y,
                    left + MODE_X + MODE_WIDTH,
                    top + MODE_Y + MODE_HEIGHT,
                    BORDER);
            graphics.fill(
                    left + MODE_X + 1,
                    top + MODE_Y + 1,
                    left + MODE_X + MODE_WIDTH - 1,
                    top + MODE_Y + MODE_HEIGHT - 1,
                    SLOT);
            graphics.drawCenteredString(
                    font,
                    Component.translatable(modeTranslation(menu.getMode())),
                    left + MODE_X + MODE_WIDTH / 2,
                    top + MODE_Y + 5,
                    0xFFE0E6EF);
        }

        graphics.fill(left + 153, top + 24, left + 161, top + 78, SLOT);
        int energy = menu.getEnergyScaled(52);
        graphics.fill(left + 154, top + 77 - energy, left + 160, top + 77, ENERGY);

        graphics.fill(left + 164, top + 24, left + 172, top + 78, SLOT);
        int fluid = menu.getFluidScaled(52);
        graphics.fill(left + 165, top + 77 - fluid, left + 171, top + 77, FOOD);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, left + 7 + column * 18, top + 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, left + 7 + column * 18, top + 141);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getKind() == FoodMachineKind.FEEDER
                && mouseX >= leftPos + MODE_X
                && mouseX < leftPos + MODE_X + MODE_WIDTH
                && mouseY >= topPos + MODE_Y
                && mouseY < topPos + MODE_Y + MODE_HEIGHT) {
            FoodMachineBlockEntity.Mode current = menu.getMode();
            FoodMachineBlockEntity.Mode[] values = FoodMachineBlockEntity.Mode.values();
            int next = (current.ordinal() + 1) % values.length;
            if (minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                menu.clickMenuButton(minecraft.player, next);
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, next);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String modeTranslation(FoodMachineBlockEntity.Mode mode) {
        return "screen.bottledanimals.feeder_mode." + mode.name().toLowerCase();
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
                        menu.getEnergyCapacity()),
                8,
                61,
                0xFFE0E6EF,
                false);
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.bottledanimals.food_fluid",
                        menu.getFluidAmount(),
                        menu.getFluidCapacity()),
                8,
                71,
                0xFFE0E6EF,
                false);
    }
}
