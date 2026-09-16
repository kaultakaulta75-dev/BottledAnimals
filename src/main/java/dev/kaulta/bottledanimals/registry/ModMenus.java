package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.menu.FoodMachineMenu;
import dev.kaulta.bottledanimals.menu.ProcessingMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BottledAnimals.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingMachineMenu>>
            PROCESSING_MACHINE = MENUS.register(
                    "processing_machine",
                    () -> new MenuType<>(ProcessingMachineMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<FoodMachineMenu>>
            FOOD_CRUSHER = MENUS.register(
                    "food_crusher",
                    () -> new MenuType<>(FoodMachineMenu::crusherClient, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<FoodMachineMenu>>
            WIRELESS_FEEDER = MENUS.register(
                    "wireless_feeder",
                    () -> new MenuType<>(FoodMachineMenu::feederClient, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
