package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.block.FoodMachineKind;
import dev.kaulta.bottledanimals.block.MachineAction;
import dev.kaulta.bottledanimals.block.entity.BasicGeneratorBlockEntity;
import dev.kaulta.bottledanimals.block.entity.FoodMachineBlockEntity;
import dev.kaulta.bottledanimals.block.entity.ProcessingMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BottledAnimals.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicGeneratorBlockEntity>>
            BASIC_GENERATOR = BLOCK_ENTITY_TYPES.register(
                    "basic_generator",
                    () -> BlockEntityType.Builder.of(
                            BasicGeneratorBlockEntity::new,
                            ModBlocks.BASIC_GENERATOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_DIGITIZER = processor(
                    "animal_digitizer", MachineAction.DIGITIZE, ModBlocks.ANIMAL_DIGITIZER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_MATERIALIZER = processor(
                    "animal_materializer", MachineAction.MATERIALIZE, ModBlocks.ANIMAL_MATERIALIZER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_BREEDER = processor(
                    "animal_breeder", MachineAction.BREED, ModBlocks.ANIMAL_BREEDER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            GROWTH_ACCELERATOR = processor(
                    "growth_accelerator", MachineAction.GROW, ModBlocks.GROWTH_ACCELERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            DROP_EXTRACTOR = processor(
                    "drop_extractor", MachineAction.EXTRACT, ModBlocks.DROP_EXTRACTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_RANCHER = processor(
                    "animal_rancher", MachineAction.RANCH, ModBlocks.ANIMAL_RANCHER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoodMachineBlockEntity>>
            FOOD_CRUSHER = foodMachine(
                    "food_crusher", FoodMachineKind.CRUSHER, ModBlocks.FOOD_CRUSHER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoodMachineBlockEntity>>
            WIRELESS_FEEDER = foodMachine(
                    "wireless_feeder", FoodMachineKind.FEEDER, ModBlocks.WIRELESS_FEEDER);

    private ModBlockEntities() {
    }

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            processor(
                    String name,
                    MachineAction action,
                    net.neoforged.neoforge.registries.DeferredBlock<?> block) {
        return BLOCK_ENTITY_TYPES.register(
                name,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> ProcessingMachineBlockEntity.create(action, pos, state),
                        block.get()).build(null));
    }

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<FoodMachineBlockEntity>>
            foodMachine(
                    String name,
                    FoodMachineKind kind,
                    net.neoforged.neoforge.registries.DeferredBlock<?> block) {
        return BLOCK_ENTITY_TYPES.register(
                name,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> FoodMachineBlockEntity.create(kind, pos, state),
                        block.get()).build(null));
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
