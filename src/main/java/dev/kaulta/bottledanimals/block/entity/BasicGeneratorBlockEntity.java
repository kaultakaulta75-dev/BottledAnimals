package dev.kaulta.bottledanimals.block.entity;

import dev.kaulta.bottledanimals.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class BasicGeneratorBlockEntity extends BlockEntity {
    public static final int CAPACITY = 100_000;
    public static final int GENERATION_PER_TICK = 40;
    public static final int MAX_TRANSFER = 80;

    private int energyStored;
    private int burnTime;
    private int totalBurnTime;
    private final IEnergyStorage energyStorage = new GeneratorEnergyStorage();

    public BasicGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_GENERATOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void addBurnTime(int ticks) {
        burnTime = Math.min(2_000_000, burnTime + Math.max(0, ticks));
        totalBurnTime = Math.max(totalBurnTime, burnTime);
        setChanged();
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, BasicGeneratorBlockEntity generator) {
        boolean changed = false;

        if (generator.burnTime > 0 && generator.energyStored < CAPACITY) {
            generator.burnTime--;
            generator.energyStored = Math.min(
                    CAPACITY, generator.energyStored + GENERATION_PER_TICK);
            changed = true;
        }

        for (Direction direction : Direction.values()) {
            if (generator.energyStored <= 0) {
                break;
            }
            IEnergyStorage target = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos.relative(direction),
                    direction.getOpposite());
            if (target != null && target.canReceive()) {
                int sent = target.receiveEnergy(
                        Math.min(MAX_TRANSFER, generator.energyStored), false);
                if (sent > 0) {
                    generator.energyStored -= sent;
                    changed = true;
                }
            }
        }

        if (changed) {
            generator.setChanged();
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = Math.max(0, Math.min(CAPACITY, tag.getInt("Energy")));
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        totalBurnTime = Math.max(0, tag.getInt("TotalBurnTime"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStored);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("TotalBurnTime", totalBurnTime);
    }

    private final class GeneratorEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(Math.max(0, maxExtract), energyStored);
            if (!simulate && extracted > 0) {
                energyStored -= extracted;
                setChanged();
            }
            return extracted;
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
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
