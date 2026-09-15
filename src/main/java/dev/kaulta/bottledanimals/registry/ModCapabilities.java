package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.block.entity.BasicGeneratorBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.BASIC_GENERATOR.get(),
                (BasicGeneratorBlockEntity generator, direction) -> generator.getEnergyStorage()
        );
    }
}
