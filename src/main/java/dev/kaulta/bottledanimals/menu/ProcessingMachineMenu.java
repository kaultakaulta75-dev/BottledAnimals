package dev.kaulta.bottledanimals.menu;

import dev.kaulta.bottledanimals.block.entity.ProcessingMachineBlockEntity;
import dev.kaulta.bottledanimals.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ProcessingMachineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 3;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOTS;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOTS_END = PLAYER_HOTBAR_START + 9;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Block machineBlock;

    public ProcessingMachineMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new ItemStackHandler(ProcessingMachineBlockEntity.INVENTORY_SIZE),
                new SimpleContainerData(3),
                ContainerLevelAccess.NULL,
                Blocks.AIR);
    }

    public ProcessingMachineMenu(
            int containerId,
            Inventory playerInventory,
            IItemHandler machineInventory,
            ContainerData data,
            ContainerLevelAccess access,
            Block machineBlock) {
        super(ModMenus.PROCESSING_MACHINE.get(), containerId);
        checkContainerSize(machineInventory, MACHINE_SLOTS);
        checkContainerDataCount(data, 3);
        this.data = data;
        this.access = access;
        this.machineBlock = machineBlock;

        addSlot(new SlotItemHandler(machineInventory, 0, 44, 35));
        addSlot(new SlotItemHandler(machineInventory, 1, 76, 35));
        addSlot(new SlotItemHandler(machineInventory, 2, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlots(data);
    }

    private static void checkContainerSize(IItemHandler handler, int expected) {
        if (handler.getSlots() < expected) {
            throw new IllegalArgumentException("Container data count " + handler.getSlots()
                    + " is smaller than expected " + expected);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return machineBlock == Blocks.AIR
                || AbstractContainerMenu.stillValid(access, player, machineBlock);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
            if (slotIndex < PLAYER_HOTBAR_START) {
                if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_SLOTS_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(
                    stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    public int getProgressScaled(int width) {
        int maximum = data.get(1);
        return maximum <= 0 ? 0 : data.get(0) * width / maximum;
    }

    public int getEnergyStored() {
        return data.get(2);
    }

    public int getEnergyScaled(int height) {
        return data.get(2) * height / ProcessingMachineBlockEntity.CAPACITY;
    }
}
