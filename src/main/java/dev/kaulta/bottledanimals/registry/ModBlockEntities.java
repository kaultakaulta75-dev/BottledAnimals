package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.block.MachineAction;
import dev.kaulta.bottledanimals.block.entity.BasicGeneratorBlockEntity;
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
                            ModBlocks.BASIC_GENERATOR.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_DIGITIZER = BLOCK_ENTITY_TYPES.register(
                    "animal_digitizer",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> ProcessingMachineBlockEntity.create(
                                    MachineAction.DIGITIZE, pos, state),
                            ModBlocks.ANIMAL_DIGITIZER.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_MATERIALIZER = BLOCK_ENTITY_TYPES.register(
                    "animal_materializer",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> ProcessingMachineBlockEntity.create(
                                    MachineAction.MATERIALIZE, pos, state),
                            ModBlocks.ANIMAL_MATERIALIZER.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_BREEDER = BLOCK_ENTITY_TYPES.register(
                    "animal_breeder",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> ProcessingMachineBlockEntity.create(
                                    MachineAction.BREED, pos, state),
                            ModBlocks.ANIMAL_BREEDER.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            GROWTH_ACCELERATOR = BLOCK_ENTITY_TYPES.register(
                    "growth_accelerator",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> ProcessingMachineBlockEntity.create(
                                    MachineAction.GROW, pos, state),
                            ModBlocks.GROWTH_ACCELERATOR.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            DROP_EXTRACTOR = BLOCK_ENTITY_TYPES.register(
                    "drop_extractor",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> ProcessingMachineBlockEntity.create(
                                    MachineAction.EXTRACT, pos, state),
                            ModBlocks.DROP_EXTRACTOR.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>
            ANIMAL_RANCHER = BLOCK_ENTITY_TYPES.register(
                    "animal_rancher",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> ProcessingMachineBlockEntity.create(
                                    MachineAction.RANCH, pos, state),
                            ModBlocks.ANIMAL_RANCHER.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
