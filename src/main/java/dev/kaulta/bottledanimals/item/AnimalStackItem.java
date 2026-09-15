package dev.kaulta.bottledanimals.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnimalStackItem extends Item {
    public AnimalStackItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return AnimalStacks.getKind(stack)
                .map(kind -> Component.translatable(getDescriptionId() + "." + kind.serializedName()))
                .orElseGet(() -> super.getName(stack));
    }
}
