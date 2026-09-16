package dev.kaulta.bottledanimals.menu;

import dev.kaulta.bottledanimals.block.FoodMachineKind;
import dev.kaulta.bottledanimals.block.entity.FoodMachineBlockEntity;
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

public final class FoodMachineMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 7;

    private final FoodMachineKind kind;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Block machineBlock;
    private final int machineSlots;

    public static FoodMachineMenu crusherClient(int containerId, Inventory playerInventory) {
        return client(containerId, playerInventory, FoodMachineKind.CRUSHER);
    }

    public static FoodMachineMenu feederClient(int containerId, Inventory playerInventory) {
        return client(containerId, playerInventory, FoodMachineKind.FEEDER);
    }

    private static FoodMachineMenu client(
            int containerId, Inventory playerInventory, FoodMachineKind kind) {
        return new FoodMachineMenu(
                containerId,
                playerInventory,
                new ItemStackHandler(FoodMachineBlockEntity.INVENTORY_SIZE),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL,
                Blocks.AIR,
                kind);
    }

    public FoodMachineMenu(
            int containerId,
            Inventory playerInventory,
            IItemHandler machineInventory,
            ContainerData data,
            ContainerLevelAccess access,
            Block machineBlock,
            FoodMachineKind kind) {
        super(kind == FoodMachineKind.CRUSHER
                ? ModMenus.FOOD_CRUSHER.get()
                : ModMenus.WIRELESS_FEEDER.get(), containerId);
        if (machineInventory.getSlots() < FoodMachineBlockEntity.INVENTORY_SIZE) {
            throw new IllegalArgumentException("Food machine inventory is too small");
        }
        checkContainerDataCount(data, DATA_COUNT);
        this.kind = kind;
        this.data = data;
        this.access = access;
        this.machineBlock = machineBlock;
        this.machineSlots = kind == FoodMachineKind.CRUSHER ? 4 : 2;

        if (kind == FoodMachineKind.CRUSHER) {
            addSlot(new SlotItemHandler(machineInventory, 0, 18, 35));
            addSlot(new SlotItemHandler(machineInventory, 1, 44, 35));
            addOutputSlot(machineInventory, 2, 108, 35);
            addOutputSlot(machineInventory, 3, 134, 35);
        } else {
            addSlot(new SlotItemHandler(machineInventory, 0, 18, 35));
            addOutputSlot(machineInventory, 1, 44, 35);
        }

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

    private void addOutputSlot(IItemHandler inventory, int index, int x, int y) {
        addSlot(new SlotItemHandler(inventory, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return machineBlock == Blocks.AIR
                || AbstractContainerMenu.stillValid(access, player, machineBlock);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (kind != FoodMachineKind.FEEDER
                || id < 0
                || id >= FoodMachineBlockEntity.Mode.values().length) {
            return false;
        }
        data.set(6, id);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerInventoryStart = machineSlots;
        int playerHotbarStart = playerInventoryStart + 27;
        int playerSlotsEnd = playerHotbarStart + 9;

        if (slotIndex < machineSlots) {
            if (!moveItemStackTo(stack, playerInventoryStart, playerSlotsEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            if (slotIndex < playerHotbarStart) {
                if (!moveItemStackTo(stack, playerHotbarStart, playerSlotsEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarStart, false)) {
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

    public FoodMachineKind getKind() {
        return kind;
    }

    public FoodMachineBlockEntity.Mode getMode() {
        return FoodMachineBlockEntity.Mode.fromId(data.get(6));
    }

    public int getProgressScaled(int width) {
        int maximum = data.get(1);
        return maximum <= 0 ? 0 : data.get(0) * width / maximum;
    }

    public int getEnergyStored() {
        return data.get(2);
    }

    public int getEnergyCapacity() {
        return data.get(3);
    }

    public int getEnergyScaled(int height) {
        return data.get(2) * height / Math.max(1, data.get(3));
    }

    public int getFluidAmount() {
        return data.get(4);
    }

    public int getFluidCapacity() {
        return data.get(5);
    }

    public int getFluidScaled(int height) {
        return data.get(4) * height / Math.max(1, data.get(5));
    }
}
