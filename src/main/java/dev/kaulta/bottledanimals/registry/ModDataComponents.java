package dev.kaulta.bottledanimals.registry;

import com.mojang.serialization.Codec;
import dev.kaulta.bottledanimals.BottledAnimals;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(BottledAnimals.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ANIMAL_TYPE =
            DATA_COMPONENTS.registerComponentType("animal_type",
                    builder -> builder.persistent(Codec.STRING));

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
