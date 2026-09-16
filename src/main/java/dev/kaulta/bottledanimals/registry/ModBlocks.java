package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.block.BasicGeneratorBlock;
import dev.kaulta.bottledanimals.block.FoodCrusherBlock;
import dev.kaulta.bottledanimals.block.LegacyMachineBlock;
import dev.kaulta.bottledanimals.block.MachineAction;
import dev.kaulta.bottledanimals.block.ProcessingMachineBlock;
import dev.kaulta.bottledanimals.block.WirelessFeederBlock;
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
    public static final DeferredBlock<ProcessingMachineBlock> ANIMAL_DIGITIZER =
            persistentProcessor("animal_digitizer", MachineAction.DIGITIZE);
    public static final DeferredBlock<ProcessingMachineBlock> ANIMAL_MATERIALIZER =
            persistentProcessor("animal_materializer", MachineAction.MATERIALIZE);
    public static final DeferredBlock<ProcessingMachineBlock> ANIMAL_BREEDER =
            persistentProcessor("animal_breeder", MachineAction.BREED);
    public static final DeferredBlock<ProcessingMachineBlock> GROWTH_ACCELERATOR =
            persistentProcessor("growth_accelerator", MachineAction.GROW);
    public static final DeferredBlock<LegacyMachineBlock> DROP_EXTRACTOR =
            processor("drop_extractor", MachineAction.EXTRACT);
    public static final DeferredBlock<LegacyMachineBlock> ANIMAL_RANCHER =
            processor("animal_rancher", MachineAction.RANCH);
    public static final DeferredBlock<FoodCrusherBlock> FOOD_CRUSHER =
            BLOCKS.register("food_crusher", () -> new FoodCrusherBlock(machineProperties()));
    public static final DeferredBlock<WirelessFeederBlock> WIRELESS_FEEDER =
            BLOCKS.register("wireless_feeder", () -> new WirelessFeederBlock(machineProperties()));
    public static final DeferredBlock<BasicGeneratorBlock> BASIC_GENERATOR =
            BLOCKS.register("basic_generator", () -> new BasicGeneratorBlock(machineProperties()));

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

    private static DeferredBlock<ProcessingMachineBlock> persistentProcessor(
            String name, MachineAction action) {
        return BLOCKS.register(name, () -> new ProcessingMachineBlock(machineProperties(), action));
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
