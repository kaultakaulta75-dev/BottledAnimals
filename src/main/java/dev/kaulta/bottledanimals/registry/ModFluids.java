package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, BottledAnimals.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, BottledAnimals.MOD_ID);

    public static final Holder<FluidType> FOOD_TYPE = FLUID_TYPES.register(
            "food",
            () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<Fluid, FlowingFluid> FOOD = FLUIDS.register(
            "food",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_FOOD = FLUIDS.register(
            "flowing_food",
            () -> new BaseFlowingFluid.Flowing(properties()));

    private ModFluids() {
    }

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(FOOD_TYPE::value, FOOD, FLOWING_FOOD);
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
