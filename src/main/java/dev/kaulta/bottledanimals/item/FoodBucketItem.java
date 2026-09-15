package dev.kaulta.bottledanimals.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FoodBucketItem extends Item {
    public FoodBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .append(" (" + FoodStacks.getUnits(stack) + " / " + FoodStacks.BUCKET_CAPACITY + " mB)");
    }
}
