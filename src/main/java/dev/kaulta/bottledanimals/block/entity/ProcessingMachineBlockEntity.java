package dev.kaulta.bottledanimals.block.entity;

import dev.kaulta.bottledanimals.animal.AnimalKind;
import dev.kaulta.bottledanimals.block.MachineAction;
import dev.kaulta.bottledanimals.item.AnimalStacks;
import dev.kaulta.bottledanimals.menu.ProcessingMachineMenu;
import dev.kaulta.bottledanimals.registry.ModBlockEntities;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ProcessingMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 3;
    public static final int INPUT_SLOT = 0;
    public static final int CATALYST_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int CAPACITY = 20_000;
    public static final int PROCESS_TIME = 200;
    public static final int ENERGY_PER_TICK = 5;

    private final MachineAction action;
    private int progress;
    private int energyStored;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> isValidAnimalInput(stack);
                case CATALYST_SLOT -> isValidCatalyst(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot != OUTPUT_SLOT) {
                progress = 0;
            }
            setChanged();
        }
    };

    private final IEnergyStorage energyStorage = new MachineEnergyStorage();
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> PROCESS_TIME;
                case 2 -> energyStored;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, Math.min(PROCESS_TIME, value));
                case 2 -> energyStored = Math.max(0, Math.min(CAPACITY, value));
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private ProcessingMachineBlockEntity(
            BlockEntityType<?> type, MachineAction action, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.action = action;
    }

    public static ProcessingMachineBlockEntity create(
            MachineAction action, BlockPos pos, BlockState state) {
        return switch (action) {
            case DIGITIZE -> new ProcessingMachineBlockEntity(
                    ModBlockEntities.ANIMAL_DIGITIZER.get(), action, pos, state);
            case MATERIALIZE -> new ProcessingMachineBlockEntity(
                    ModBlockEntities.ANIMAL_MATERIALIZER.get(), action, pos, state);
            default -> throw new IllegalArgumentException("Unsupported persistent machine: " + action);
        };
    }

    public ItemStackHandler getItemHandler() {
        return inventory;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity machine) {
        ItemStack result = machine.getRecipeResult();
        if (result.isEmpty() || !machine.canAcceptResult(result)) {
            if (machine.progress != 0) {
                machine.progress = 0;
                machine.setChanged();
            }
            return;
        }
        if (machine.energyStored < ENERGY_PER_TICK) {
            return;
        }

        machine.energyStored -= ENERGY_PER_TICK;
        machine.progress++;
        if (machine.progress >= PROCESS_TIME) {
            machine.finishRecipe(result);
            machine.progress = 0;
        }
        machine.setChanged();
    }

    private ItemStack getRecipeResult() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        ItemStack catalyst = inventory.getStackInSlot(CATALYST_SLOT);
        if (!isValidAnimalInput(input) || !isValidCatalyst(catalyst)) {
            return ItemStack.EMPTY;
        }

        Optional<AnimalKind> kind = AnimalStacks.getKind(input);
        if (kind.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return switch (action) {
            case DIGITIZE -> AnimalStacks.create(ModItems.DIGITALIZED_ANIMAL.get(), kind.get());
            case MATERIALIZE -> new ItemStack(kind.get().spawnEgg());
            default -> ItemStack.EMPTY;
        };
    }

    private boolean isValidAnimalInput(ItemStack stack) {
        boolean correctItem = switch (action) {
            case DIGITIZE -> stack.is(ModItems.BOTTLED_ANIMAL.get());
            case MATERIALIZE -> stack.is(ModItems.DIGITALIZED_ANIMAL.get());
            default -> false;
        };
        return correctItem && AnimalStacks.getKind(stack).isPresent();
    }

    private boolean isValidCatalyst(ItemStack stack) {
        return switch (action) {
            case DIGITIZE -> stack.is(ModItems.BLANK_PATTERN.get());
            case MATERIALIZE -> stack.is(ModItems.SPAWN_EGG_FRAME.get());
            default -> false;
        };
    }

    private boolean canAcceptResult(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void finishRecipe(ItemStack result) {
        inventory.extractItem(INPUT_SLOT, 1, false);
        inventory.extractItem(CATALYST_SLOT, 1, false);

        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            ItemStack combined = output.copy();
            combined.grow(result.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, combined);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bottledanimals." + action.name().toLowerCase());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new ProcessingMachineMenu(
                containerId,
                playerInventory,
                inventory,
                data,
                ContainerLevelAccess.create(player.level(), worldPosition),
                getBlockState().getBlock());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = Math.max(0, Math.min(PROCESS_TIME, tag.getInt("Progress")));
        energyStored = Math.max(0, Math.min(CAPACITY, tag.getInt("Energy")));
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.putInt("Energy", energyStored);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    private final class MachineEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(Math.max(0, maxReceive), CAPACITY - energyStored);
            if (!simulate && received > 0) {
                energyStored += received;
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
