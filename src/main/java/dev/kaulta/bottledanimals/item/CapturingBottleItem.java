package dev.kaulta.bottledanimals.item;

import dev.kaulta.bottledanimals.animal.AnimalKind;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class CapturingBottleItem extends Item {
    public CapturingBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Optional<AnimalKind> kind = AnimalKind.fromEntity(target);
        if (kind.isEmpty() || target instanceof AgeableMob ageable && ageable.isBaby()) {
            return InteractionResult.PASS;
        }

        if (!target.level().isClientSide) {
            ItemStack filledBottle = AnimalStacks.create(ModItems.BOTTLED_ANIMAL.get(), kind.get());
            target.discard();

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (!player.getInventory().add(filledBottle)) {
                player.drop(filledBottle, false);
            }
        }

        return InteractionResult.sidedSuccess(target.level().isClientSide);
    }
}
