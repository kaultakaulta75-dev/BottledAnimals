package dev.kaulta.bottledanimals.block;

import dev.kaulta.bottledanimals.animal.AnimalKind;
import dev.kaulta.bottledanimals.item.AnimalStacks;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class LegacyMachineBlock extends Block {
    private final MachineAction action;

    public LegacyMachineBlock(Properties properties, MachineAction action) {
        super(properties);
        this.action = action;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!acceptsInput(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        Optional<AnimalKind> kindOptional = AnimalStacks.getKind(stack);
        if (kindOptional.isEmpty()) {
            return failure(player, "message.bottledanimals.missing_animal_data");
        }
        AnimalKind kind = kindOptional.get();

        return switch (action) {
            case DIGITIZE -> digitize(player, stack, kind);
            case MATERIALIZE -> materialize(player, stack, kind);
            case BREED -> breed(player, stack, kind);
            case GROW -> grow(player, stack, kind);
            case EXTRACT -> extract(player, stack, kind);
            case RANCH -> ranch(player, kind);
        };
    }

    private boolean acceptsInput(ItemStack stack) {
        return switch (action) {
            case DIGITIZE -> stack.is(ModItems.BOTTLED_ANIMAL.get());
            case MATERIALIZE, BREED, EXTRACT, RANCH -> stack.is(ModItems.DIGITALIZED_ANIMAL.get());
            case GROW -> stack.is(ModItems.DIGITALIZED_BABY_ANIMAL.get());
        };
    }

    private ItemInteractionResult digitize(Player player, ItemStack input, AnimalKind kind) {
        if (!hasItem(player, ModItems.BLANK_PATTERN.get(), 1)) {
            return failure(player, "message.bottledanimals.need_blank_pattern");
        }
        consumeItem(player, ModItems.BLANK_PATTERN.get(), 1);
        consumeHeld(player, input);
        give(player, AnimalStacks.create(ModItems.DIGITALIZED_ANIMAL.get(), kind));
        return success(player, "message.bottledanimals.digitized");
    }

    private ItemInteractionResult materialize(Player player, ItemStack input, AnimalKind kind) {
        if (!hasItem(player, ModItems.SPAWN_EGG_FRAME.get(), 1)) {
            return failure(player, "message.bottledanimals.need_spawn_egg_frame");
        }
        consumeItem(player, ModItems.SPAWN_EGG_FRAME.get(), 1);
        consumeHeld(player, input);
        give(player, new ItemStack(kind.spawnEgg()));
        return success(player, "message.bottledanimals.materialized");
    }

    private ItemInteractionResult breed(Player player, ItemStack parent, AnimalKind kind) {
        if (countMatching(player, stack -> stack.is(ModItems.DIGITALIZED_ANIMAL.get())
                && AnimalStacks.getKind(stack).filter(kind::equals).isPresent()) < 2) {
            return failure(player, "message.bottledanimals.need_second_parent");
        }
        if (!hasItem(player, ModItems.BLANK_PATTERN.get(), 1)) {
            return failure(player, "message.bottledanimals.need_blank_pattern");
        }
        if (countMatching(player, kind::isFood) < 2) {
            return failure(player, "message.bottledanimals.need_two_food");
        }
        consumeItem(player, ModItems.BLANK_PATTERN.get(), 1);
        consumeMatching(player, kind::isFood, 2);
        give(player, AnimalStacks.create(ModItems.DIGITALIZED_BABY_ANIMAL.get(), kind));
        return success(player, "message.bottledanimals.bred");
    }

    private ItemInteractionResult grow(Player player, ItemStack input, AnimalKind kind) {
        if (countMatching(player, kind::isFood) < 1) {
            return failure(player, "message.bottledanimals.need_food");
        }
        consumeMatching(player, kind::isFood, 1);
        consumeHeld(player, input);
        give(player, AnimalStacks.create(ModItems.DIGITALIZED_ANIMAL.get(), kind));
        return success(player, "message.bottledanimals.grown");
    }

    private ItemInteractionResult extract(Player player, ItemStack input, AnimalKind kind) {
        consumeHeld(player, input);
        for (ItemStack drop : kind.extractorDrops()) {
            give(player, drop);
        }
        give(player, new ItemStack(ModItems.BROKEN_PATTERN.get()));
        return success(player, "message.bottledanimals.extracted");
    }

    private ItemInteractionResult ranch(Player player, AnimalKind kind) {
        List<ItemStack> drops = kind.rancherDrops();
        if (drops.isEmpty()) {
            return failure(player, "message.bottledanimals.cannot_ranch");
        }
        if (!hasItem(player, ModItems.RANCHER_GEAR.get(), 1)) {
            return failure(player, "message.bottledanimals.need_rancher_gear");
        }
        damageRancherGear(player);
        drops.forEach(drop -> give(player, drop));
        return success(player, "message.bottledanimals.ranched");
    }

    private static boolean hasItem(Player player, Item item, int count) {
        return countMatching(player, stack -> stack.is(item)) >= count;
    }

    private static int countMatching(Player player, Predicate<ItemStack> predicate) {
        Inventory inventory = player.getInventory();
        int found = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (predicate.test(stack)) {
                found += stack.getCount();
            }
        }
        return found;
    }

    private static void consumeItem(Player player, Item item, int count) {
        consumeMatching(player, stack -> stack.is(item), count);
    }

    private static void consumeMatching(Player player, Predicate<ItemStack> predicate, int count) {
        if (player.getAbilities().instabuild) {
            return;
        }
        Inventory inventory = player.getInventory();
        int remaining = count;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (predicate.test(stack)) {
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;
            }
        }
    }

    private static void consumeHeld(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void damageRancherGear(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack gear = inventory.getItem(slot);
            if (gear.is(ModItems.RANCHER_GEAR.get())) {
                int damage = gear.getDamageValue() + 1;
                if (damage >= gear.getMaxDamage()) {
                    gear.shrink(1);
                } else {
                    gear.setDamageValue(damage);
                }
                return;
            }
        }
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static ItemInteractionResult success(Player player, String messageKey) {
        player.displayClientMessage(Component.translatable(messageKey), true);
        return ItemInteractionResult.SUCCESS;
    }

    private static ItemInteractionResult failure(Player player, String messageKey) {
        player.displayClientMessage(Component.translatable(messageKey), true);
        return ItemInteractionResult.FAIL;
    }
}
