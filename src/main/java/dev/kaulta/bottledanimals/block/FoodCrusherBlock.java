package dev.kaulta.bottledanimals.block;

import dev.kaulta.bottledanimals.item.FoodStacks;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class FoodCrusherBlock extends Block {
    public FoodCrusherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null || food.nutrition() <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int generated = food.nutrition() * 100;
        ItemStack target = findFillableFoodBucket(player);
        if (target.isEmpty()) {
            if (!hasBucket(player)) {
                player.displayClientMessage(
                        Component.translatable("message.bottledanimals.need_bucket"), true);
                return ItemInteractionResult.FAIL;
            }
            consumeBucket(player);
            target = new ItemStack(ModItems.FOOD_BUCKET.get());
            FoodStacks.setUnits(target, Math.min(generated, FoodStacks.BUCKET_CAPACITY));
            give(player, target);
        } else {
            int current = FoodStacks.getUnits(target);
            FoodStacks.setUnits(target, Math.min(current + generated, FoodStacks.BUCKET_CAPACITY));
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(
                Component.translatable("message.bottledanimals.food_crushed",
                        Math.min(generated, FoodStacks.BUCKET_CAPACITY)), true);
        return ItemInteractionResult.SUCCESS;
    }

    private static ItemStack findFillableFoodBucket(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ModItems.FOOD_BUCKET.get())
                    && FoodStacks.getUnits(stack) < FoodStacks.BUCKET_CAPACITY) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasBucket(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(Items.BUCKET)) {
                return true;
            }
        }
        return false;
    }

    private static void consumeBucket(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.BUCKET)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
