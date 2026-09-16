package dev.kaulta.bottledanimals.registry;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.BASIC_GENERATOR.get(),
                (generator, direction) -> generator.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ANIMAL_DIGITIZER.get(),
                (machine, direction) -> machine.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ANIMAL_DIGITIZER.get(),
                (machine, direction) -> machine.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ANIMAL_MATERIALIZER.get(),
                (machine, direction) -> machine.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ANIMAL_MATERIALIZER.get(),
                (machine, direction) -> machine.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ANIMAL_BREEDER.get(),
                (machine, direction) -> machine.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ANIMAL_BREEDER.get(),
                (machine, direction) -> machine.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.GROWTH_ACCELERATOR.get(),
                (machine, direction) -> machine.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.GROWTH_ACCELERATOR.get(),
                (machine, direction) -> machine.getEnergyStorage()
        );
    }
}
