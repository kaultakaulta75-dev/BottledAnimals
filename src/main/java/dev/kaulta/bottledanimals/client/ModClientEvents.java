package dev.kaulta.bottledanimals.client;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.registry.ModFluids;
import dev.kaulta.bottledanimals.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = BottledAnimals.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PROCESSING_MACHINE.get(), ProcessingMachineScreen::new);
        event.register(ModMenus.FOOD_CRUSHER.get(), FoodMachineScreen::new);
        event.register(ModMenus.WIRELESS_FEEDER.get(), FoodMachineScreen::new);
    }

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL =
                    ResourceLocation.withDefaultNamespace("block/water_still");
            private static final ResourceLocation FLOWING =
                    ResourceLocation.withDefaultNamespace("block/water_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING;
            }

            @Override
            public int getTintColor() {
                return 0xFFD89032;
            }
        }, ModFluids.FOOD_TYPE.value());
    }
}
