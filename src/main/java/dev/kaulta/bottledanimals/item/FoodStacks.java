package dev.kaulta.bottledanimals.item;

import dev.kaulta.bottledanimals.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class FoodStacks {
    public static final int BUCKET_CAPACITY = 1000;

    private FoodStacks() {
    }

    public static int getUnits(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FOOD_UNITS.value(), 0);
    }

    public static void setUnits(ItemStack stack, int units) {
        stack.set(ModDataComponents.FOOD_UNITS.value(),
                Math.max(0, Math.min(BUCKET_CAPACITY, units)));
    }
}
