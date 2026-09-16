package dev.kaulta.bottledanimals.block.entity;

import dev.kaulta.bottledanimals.block.FoodMachineKind;
import dev.kaulta.bottledanimals.item.FoodStacks;
import dev.kaulta.bottledanimals.menu.FoodMachineMenu;
import dev.kaulta.bottledanimals.registry.ModBlockEntities;
import dev.kaulta.bottledanimals.registry.ModFluids;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class FoodMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 4;
    public static final int ENERGY_CAPACITY = 32_000;
    public static final int TANK_CAPACITY = 10 * FluidType.BUCKET_VOLUME;
    public static final int CRUSH_TIME = 200;
    public static final int CRUSH_ENERGY_PER_TICK = 10;

    private static final int CRUSH_INPUT = 0;
    private static final int CRUSH_BUCKET_INPUT = 1;
    private static final int CRUSH_CONTAINER_OUTPUT = 2;
    private static final int CRUSH_BUCKET_OUTPUT = 3;
    private static final int FEEDER_BUCKET_INPUT = 0;
    private static final int FEEDER_BUCKET_OUTPUT = 1;

    private static final int FEEDER_INTERVAL = 40;
    private static final int FEEDER_RANGE = 5;
    private static final int MAX_POINTS_PER_CYCLE = 4;
    private static final int FOOD_COST = 120;
    private static final int HEAL_COST = 60;
    private static final int FOOD_ENERGY_COST = 240;
    private static final int HEAL_ENERGY_COST = 120;

    private final FoodMachineKind kind;
    private int progress;
    private int energyStored;
    private int feederTicks;
    private Mode mode = Mode.BOTH;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (kind == FoodMachineKind.CRUSHER) {
                return switch (slot) {
                    case CRUSH_INPUT -> foodUnits(stack) > 0;
                    case CRUSH_BUCKET_INPUT -> stack.is(Items.BUCKET);
                    default -> false;
                };
            }
            return slot == FEEDER_BUCKET_INPUT
                    && stack.is(ModItems.FOOD_BUCKET.get())
                    && FoodStacks.getUnits(stack) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (kind == FoodMachineKind.CRUSHER && slot == CRUSH_INPUT) {
                progress = 0;
            }
            setChanged();
        }
    };

    private final FluidTank tank = new FluidTank(
            TANK_CAPACITY, stack -> stack.is(ModFluids.FOOD.get())) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IEnergyStorage energyStorage = new MachineEnergyStorage();
    private final IFluidHandler fluidHandler = new MachineFluidHandler();
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> kind == FoodMachineKind.CRUSHER ? CRUSH_TIME : 0;
                case 2 -> energyStored;
                case 3 -> ENERGY_CAPACITY;
                case 4 -> tank.getFluidAmount();
                case 5 -> TANK_CAPACITY;
                case 6 -> mode.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, Math.min(CRUSH_TIME, value));
                case 2 -> energyStored = Math.max(0, Math.min(ENERGY_CAPACITY, value));
                case 6 -> setMode(Mode.fromId(value));
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    private FoodMachineBlockEntity(
            BlockEntityType<?> type, FoodMachineKind kind, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.kind = kind;
    }

    public static FoodMachineBlockEntity create(
            FoodMachineKind kind, BlockPos pos, BlockState state) {
        BlockEntityType<?> type = kind == FoodMachineKind.CRUSHER
                ? ModBlockEntities.FOOD_CRUSHER.get()
                : ModBlockEntities.WIRELESS_FEEDER.get();
        return new FoodMachineBlockEntity(type, kind, pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return inventory;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, FoodMachineBlockEntity machine) {
        if (machine.kind == FoodMachineKind.CRUSHER) {
            machine.tickCrusher();
        } else {
            machine.tickFeeder(level, pos);
        }
    }

    private void tickCrusher() {
        fillFoodBucket();

        int units = foodUnits(inventory.getStackInSlot(CRUSH_INPUT));
        ItemStack remainder = inventory.getStackInSlot(CRUSH_INPUT).getCraftingRemainingItem();
        if (units <= 0 || tank.getSpace() < units || !canMerge(CRUSH_CONTAINER_OUTPUT, remainder)) {
            resetProgress();
            return;
        }
        if (energyStored < CRUSH_ENERGY_PER_TICK) {
            return;
        }

        energyStored -= CRUSH_ENERGY_PER_TICK;
        progress++;
        if (progress >= CRUSH_TIME) {
            tank.fill(new FluidStack(ModFluids.FOOD.get(), units), IFluidHandler.FluidAction.EXECUTE);
            inventory.extractItem(CRUSH_INPUT, 1, false);
            mergeInto(CRUSH_CONTAINER_OUTPUT, remainder);
            progress = 0;
        }
        setChanged();
    }

    private void tickFeeder(Level level, BlockPos pos) {
        emptyFoodBucketIntoTank();
        feederTicks++;
        if (feederTicks < FEEDER_INTERVAL) {
            return;
        }
        feederTicks = 0;
        if (mode == Mode.DISABLED) {
            setChanged();
            return;
        }

        AABB area = new AABB(pos).inflate(FEEDER_RANGE);
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }

            boolean fed = false;
            if (mode != Mode.HEAL) {
                for (int point = 0;
                     point < MAX_POINTS_PER_CYCLE && player.getFoodData().getFoodLevel() < 20;
                     point++) {
                    if (!consume(FOOD_COST, FOOD_ENERGY_COST)) {
                        break;
                    }
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() + 1);
                    player.getFoodData().setSaturation(Math.min(
                            20.0F, player.getFoodData().getSaturationLevel() + 0.8F));
                    fed = true;
                }
            }

            boolean mayHeal = mode != Mode.FEED
                    && !fed
                    && (mode != Mode.BOTH || player.getFoodData().getFoodLevel() == 20);
            for (int point = 0;
                 mayHeal && point < MAX_POINTS_PER_CYCLE && player.getHealth() < player.getMaxHealth();
                 point++) {
                if (!consume(HEAL_COST, HEAL_ENERGY_COST)) {
                    break;
                }
                player.heal(1.0F);
            }
        }
        setChanged();
    }

    private boolean consume(int fluidCost, int energyCost) {
        if (tank.getFluidAmount() < fluidCost || energyStored < energyCost) {
            return false;
        }
        tank.drain(fluidCost, IFluidHandler.FluidAction.EXECUTE);
        energyStored -= energyCost;
        return true;
    }

    private void fillFoodBucket() {
        if (!inventory.getStackInSlot(CRUSH_BUCKET_INPUT).is(Items.BUCKET)
                || tank.getFluidAmount() < FoodStacks.BUCKET_CAPACITY
                || !inventory.getStackInSlot(CRUSH_BUCKET_OUTPUT).isEmpty()) {
            return;
        }

        ItemStack bucket = new ItemStack(ModItems.FOOD_BUCKET.get());
        FoodStacks.setUnits(bucket, FoodStacks.BUCKET_CAPACITY);
        inventory.extractItem(CRUSH_BUCKET_INPUT, 1, false);
        tank.drain(FoodStacks.BUCKET_CAPACITY, IFluidHandler.FluidAction.EXECUTE);
        inventory.setStackInSlot(CRUSH_BUCKET_OUTPUT, bucket);
    }

    private void emptyFoodBucketIntoTank() {
        ItemStack bucket = inventory.getStackInSlot(FEEDER_BUCKET_INPUT);
        if (!bucket.is(ModItems.FOOD_BUCKET.get())) {
            return;
        }

        int units = FoodStacks.getUnits(bucket);
        int moved = Math.min(units, tank.getSpace());
        if (moved <= 0 || (moved == units && !canMerge(FEEDER_BUCKET_OUTPUT, new ItemStack(Items.BUCKET)))) {
            return;
        }

        tank.fill(new FluidStack(ModFluids.FOOD.get(), moved), IFluidHandler.FluidAction.EXECUTE);
        int remaining = units - moved;
        if (remaining == 0) {
            inventory.extractItem(FEEDER_BUCKET_INPUT, 1, false);
            mergeInto(FEEDER_BUCKET_OUTPUT, new ItemStack(Items.BUCKET));
        } else {
            ItemStack partial = bucket.copy();
            FoodStacks.setUnits(partial, remaining);
            inventory.setStackInSlot(FEEDER_BUCKET_INPUT, partial);
        }
    }

    private static int foodUnits(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? 0 : Math.max(0, food.nutrition() * 100);
    }

    private boolean canMerge(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack current = inventory.getStackInSlot(slot);
        return current.isEmpty()
                || (ItemStack.isSameItemSameComponents(current, stack)
                && current.getCount() + stack.getCount() <= current.getMaxStackSize());
    }

    private void mergeInto(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack current = inventory.getStackInSlot(slot);
        if (current.isEmpty()) {
            inventory.setStackInSlot(slot, stack.copy());
        } else {
            ItemStack combined = current.copy();
            combined.grow(stack.getCount());
            inventory.setStackInSlot(slot, combined);
        }
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    public void setMode(Mode mode) {
        if (kind == FoodMachineKind.FEEDER && this.mode != mode) {
            this.mode = mode;
            setChanged();
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
        return Component.translatable(kind == FoodMachineKind.CRUSHER
                ? "container.bottledanimals.food_crusher"
                : "container.bottledanimals.wireless_feeder");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new FoodMachineMenu(
                containerId,
                playerInventory,
                inventory,
                data,
                ContainerLevelAccess.create(player.level(), worldPosition),
                getBlockState().getBlock(),
                kind);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        progress = Math.max(0, Math.min(CRUSH_TIME, tag.getInt("Progress")));
        energyStored = Math.max(0, Math.min(ENERGY_CAPACITY, tag.getInt("Energy")));
        feederTicks = Math.max(0, Math.min(FEEDER_INTERVAL, tag.getInt("FeederTicks")));
        mode = tag.contains("Mode") ? Mode.fromId(tag.getByte("Mode")) : Mode.BOTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putInt("Energy", energyStored);
        tag.putInt("FeederTicks", feederTicks);
        tag.putByte("Mode", (byte) mode.ordinal());
    }

    public enum Mode {
        DISABLED,
        HEAL,
        FEED,
        BOTH;

        public static Mode fromId(int id) {
            return values()[Math.max(0, Math.min(values().length - 1, id))];
        }
    }

    private final class MachineEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(Math.max(0, maxReceive), ENERGY_CAPACITY - energyStored);
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
            return ENERGY_CAPACITY;
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

    private final class MachineFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return kind == FoodMachineKind.FEEDER && tank.isFluidValid(tankIndex, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return kind == FoodMachineKind.FEEDER ? tank.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return kind == FoodMachineKind.CRUSHER
                    ? tank.drain(resource, action)
                    : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return kind == FoodMachineKind.CRUSHER
                    ? tank.drain(maxDrain, action)
                    : FluidStack.EMPTY;
        }
    }
}
