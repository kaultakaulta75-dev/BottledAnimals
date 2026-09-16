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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProcessingMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 5;
    public static final int PRIMARY_SLOT = 0;
    public static final int SECONDARY_SLOT = 1;
    public static final int FOOD_SLOT = 2;
    public static final int CATALYST_SLOT = 3;
    public static final int OUTPUT_SLOT = 4;
    public static final int CAPACITY = 20_000;

    private static final int STANDARD_PROCESS_TIME = 200;
    private static final int BREED_PROCESS_TIME = 4_800;
    private static final int GROW_PROCESS_TIME = 20_000;
    private static final int STANDARD_ENERGY_PER_TICK = 5;
    private static final int GROW_ENERGY_PER_TICK = 2;

    private final MachineAction action;
    private int progress;
    private int energyStored;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case PRIMARY_SLOT -> isValidPrimaryInput(stack);
                case SECONDARY_SLOT -> isValidSecondaryInput(stack);
                case FOOD_SLOT -> action == MachineAction.BREED && isKnownAnimalFood(stack);
                case CATALYST_SLOT -> action == MachineAction.BREED
                        && stack.is(ModItems.BLANK_PATTERN.get());
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
                case 1 -> getProcessTime();
                case 2 -> energyStored;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, Math.min(getProcessTime(), value));
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
            case BREED -> new ProcessingMachineBlockEntity(
                    ModBlockEntities.ANIMAL_BREEDER.get(), action, pos, state);
            case GROW -> new ProcessingMachineBlockEntity(
                    ModBlockEntities.GROWTH_ACCELERATOR.get(), action, pos, state);
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
            machine.resetProgress();
            return;
        }

        int energyCost = machine.getEnergyPerTick();
        if (machine.energyStored < energyCost) {
            return;
        }

        machine.energyStored -= energyCost;
        machine.progress++;
        if (machine.progress >= machine.getProcessTime()) {
            machine.finishRecipe(result);
            machine.progress = 0;
        }
        machine.setChanged();
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    private ItemStack getRecipeResult() {
        return switch (action) {
            case DIGITIZE -> digitizerResult();
            case MATERIALIZE -> materializerResult();
            case BREED -> breederResult();
            case GROW -> growthResult();
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack digitizerResult() {
        ItemStack animal = inventory.getStackInSlot(PRIMARY_SLOT);
        if (!isValidPrimaryInput(animal)
                || !inventory.getStackInSlot(SECONDARY_SLOT).is(ModItems.BLANK_PATTERN.get())) {
            return ItemStack.EMPTY;
        }
        return AnimalStacks.getKind(animal)
                .map(kind -> AnimalStacks.create(ModItems.DIGITALIZED_ANIMAL.get(), kind))
                .orElse(ItemStack.EMPTY);
    }

    private ItemStack materializerResult() {
        ItemStack animal = inventory.getStackInSlot(PRIMARY_SLOT);
        if (!isValidPrimaryInput(animal)
                || !inventory.getStackInSlot(SECONDARY_SLOT).is(ModItems.SPAWN_EGG_FRAME.get())) {
            return ItemStack.EMPTY;
        }
        return AnimalStacks.getKind(animal)
                .map(kind -> new ItemStack(kind.spawnEgg()))
                .orElse(ItemStack.EMPTY);
    }

    private ItemStack breederResult() {
        ItemStack firstParent = inventory.getStackInSlot(PRIMARY_SLOT);
        ItemStack secondParent = inventory.getStackInSlot(SECONDARY_SLOT);
        ItemStack food = inventory.getStackInSlot(FOOD_SLOT);
        ItemStack pattern = inventory.getStackInSlot(CATALYST_SLOT);

        Optional<AnimalKind> kind = AnimalStacks.getKind(firstParent);
        if (kind.isEmpty()
                || !firstParent.is(ModItems.DIGITALIZED_ANIMAL.get())
                || !secondParent.is(ModItems.DIGITALIZED_ANIMAL.get())
                || !AnimalStacks.sameKind(firstParent, secondParent)
                || food.getCount() < 2
                || !kind.get().isFood(food)
                || !pattern.is(ModItems.BLANK_PATTERN.get())) {
            return ItemStack.EMPTY;
        }
        return AnimalStacks.create(ModItems.DIGITALIZED_BABY_ANIMAL.get(), kind.get());
    }

    private ItemStack growthResult() {
        ItemStack baby = inventory.getStackInSlot(PRIMARY_SLOT);
        ItemStack food = inventory.getStackInSlot(SECONDARY_SLOT);
        Optional<AnimalKind> kind = AnimalStacks.getKind(baby);
        if (kind.isEmpty()
                || !baby.is(ModItems.DIGITALIZED_BABY_ANIMAL.get())
                || !kind.get().isFood(food)) {
            return ItemStack.EMPTY;
        }
        return AnimalStacks.create(ModItems.DIGITALIZED_ANIMAL.get(), kind.get());
    }

    private boolean isValidPrimaryInput(ItemStack stack) {
        boolean correctItem = switch (action) {
            case DIGITIZE -> stack.is(ModItems.BOTTLED_ANIMAL.get());
            case MATERIALIZE, BREED -> stack.is(ModItems.DIGITALIZED_ANIMAL.get());
            case GROW -> stack.is(ModItems.DIGITALIZED_BABY_ANIMAL.get());
            default -> false;
        };
        return correctItem && AnimalStacks.getKind(stack).isPresent();
    }

    private boolean isValidSecondaryInput(ItemStack stack) {
        return switch (action) {
            case DIGITIZE -> stack.is(ModItems.BLANK_PATTERN.get());
            case MATERIALIZE -> stack.is(ModItems.SPAWN_EGG_FRAME.get());
            case BREED -> stack.is(ModItems.DIGITALIZED_ANIMAL.get())
                    && AnimalStacks.getKind(stack).isPresent();
            case GROW -> isKnownAnimalFood(stack);
            default -> false;
        };
    }

    private static boolean isKnownAnimalFood(ItemStack stack) {
        for (AnimalKind kind : AnimalKind.values()) {
            if (kind.isFood(stack)) {
                return true;
            }
        }
        return false;
    }

    private int getProcessTime() {
        return switch (action) {
            case BREED -> BREED_PROCESS_TIME;
            case GROW -> {
                int foodCount = Math.min(4, inventory.getStackInSlot(SECONDARY_SLOT).getCount());
                yield Math.max(1, (int) Math.round(GROW_PROCESS_TIME * Math.pow(0.85D, foodCount)));
            }
            default -> STANDARD_PROCESS_TIME;
        };
    }

    private int getEnergyPerTick() {
        return action == MachineAction.GROW
                ? GROW_ENERGY_PER_TICK
                : STANDARD_ENERGY_PER_TICK;
    }

    private boolean canAcceptResult(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void finishRecipe(ItemStack result) {
        switch (action) {
            case DIGITIZE, MATERIALIZE -> {
                inventory.extractItem(PRIMARY_SLOT, 1, false);
                inventory.extractItem(SECONDARY_SLOT, 1, false);
            }
            case BREED -> {
                inventory.extractItem(FOOD_SLOT, 2, false);
                inventory.extractItem(CATALYST_SLOT, 1, false);
            }
            case GROW -> {
                int foodCount = Math.min(4, inventory.getStackInSlot(SECONDARY_SLOT).getCount());
                inventory.extractItem(PRIMARY_SLOT, 1, false);
                inventory.extractItem(SECONDARY_SLOT, foodCount, false);
            }
            default -> {
            }
        }

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
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        migrateThreeSlotInventory();
        progress = Math.max(0, Math.min(getProcessTime(), tag.getInt("Progress")));
        energyStored = Math.max(0, Math.min(CAPACITY, tag.getInt("Energy")));
    }

    private void migrateThreeSlotInventory() {
        if (inventory.getSlots() == INVENTORY_SIZE) {
            return;
        }

        List<ItemStack> oldStacks = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            oldStacks.add(inventory.getStackInSlot(slot).copy());
        }
        inventory.setSize(INVENTORY_SIZE);
        if (!oldStacks.isEmpty()) {
            inventory.setStackInSlot(PRIMARY_SLOT, oldStacks.get(0));
        }
        if (oldStacks.size() > 1) {
            inventory.setStackInSlot(SECONDARY_SLOT, oldStacks.get(1));
        }
        if (oldStacks.size() > 2) {
            inventory.setStackInSlot(OUTPUT_SLOT, oldStacks.get(2));
        }
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
