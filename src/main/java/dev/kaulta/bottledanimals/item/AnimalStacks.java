package dev.kaulta.bottledanimals.item;

import dev.kaulta.bottledanimals.animal.AnimalKind;
import dev.kaulta.bottledanimals.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class AnimalStacks {
    private AnimalStacks() {
    }

    public static ItemStack create(Item item, AnimalKind kind) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModDataComponents.ANIMAL_TYPE.value(), kind.serializedName());
        return stack;
    }

    public static Optional<AnimalKind> getKind(ItemStack stack) {
        String name = stack.get(ModDataComponents.ANIMAL_TYPE.value());
        return name == null ? Optional.empty() : AnimalKind.fromName(name);
    }

    public static boolean sameKind(ItemStack first, ItemStack second) {
        Optional<AnimalKind> firstKind = getKind(first);
        return firstKind.isPresent() && firstKind.equals(getKind(second));
    }
}
