package dev.kaulta.bottledanimals;

import dev.kaulta.bottledanimals.registry.ModBlocks;
import dev.kaulta.bottledanimals.registry.ModDataComponents;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(BottledAnimals.MOD_ID)
public final class BottledAnimals {
    public static final String MOD_ID = "bottledanimals";

    public BottledAnimals(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        modEventBus.addListener(ModItems::addCreativeTabItems);
    }
}
