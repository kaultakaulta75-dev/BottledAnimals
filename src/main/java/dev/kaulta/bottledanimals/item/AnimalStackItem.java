package dev.kaulta.bottledanimals.item;

import dev.kaulta.bottledanimals.animal.AnimalKind;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class AnimalStackItem extends Item {
    public AnimalStackItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Optional<AnimalKind> kind = AnimalStacks.getKind(stack);
        if (kind.isPresent()) {
            return Component.translatable(getDescriptionId() + "." + kind.get().serializedName());
        }
        return super.getName(stack);
    }
}
