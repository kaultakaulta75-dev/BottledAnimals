package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.block.LegacyMachineBlock;
import dev.kaulta.bottledanimals.block.MachineAction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BottledAnimals.MOD_ID);

    public static final DeferredBlock<Block> MACHINE_FRAME = machine("machine_frame");
    public static final DeferredBlock<LegacyMachineBlock> ANIMAL_DIGITIZER =
            processor("animal_digitizer", MachineAction.DIGITIZE);
    public static final DeferredBlock<LegacyMachineBlock> ANIMAL_MATERIALIZER =
            processor("animal_materializer", MachineAction.MATERIALIZE);
    public static final DeferredBlock<LegacyMachineBlock> ANIMAL_BREEDER =
            processor("animal_breeder", MachineAction.BREED);
    public static final DeferredBlock<LegacyMachineBlock> GROWTH_ACCELERATOR =
            processor("growth_accelerator", MachineAction.GROW);
    public static final DeferredBlock<LegacyMachineBlock> DROP_EXTRACTOR =
            processor("drop_extractor", MachineAction.EXTRACT);
    public static final DeferredBlock<LegacyMachineBlock> ANIMAL_RANCHER =
            processor("animal_rancher", MachineAction.RANCH);
    public static final DeferredBlock<Block> FOOD_CRUSHER = machine("food_crusher");
    public static final DeferredBlock<Block> WIRELESS_FEEDER = machine("wireless_feeder");
    public static final DeferredBlock<Block> BASIC_GENERATOR = machine("basic_generator");

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.5F, 8.0F)
                .sound(SoundType.METAL);
    }

    private static DeferredBlock<Block> machine(String name) {
        return BLOCKS.registerSimpleBlock(name, machineProperties());
    }

    private static DeferredBlock<LegacyMachineBlock> processor(String name, MachineAction action) {
        return BLOCKS.register(name, () -> new LegacyMachineBlock(machineProperties(), action));
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
