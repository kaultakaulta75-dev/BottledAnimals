package dev.kaulta.bottledanimals.block;

import dev.kaulta.bottledanimals.item.FoodStacks;
import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public final class WirelessFeederBlock extends Block {
    private static final int RANGE = 5;
    private static final int FOOD_COST = 120;
    private static final int HEAL_COST = 60;
    private static final int MAX_POINTS_PER_USE = 4;

    public WirelessFeederBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.FOOD_BUCKET.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int initialUnits = FoodStacks.getUnits(stack);
        int remaining = initialUnits;
        if (remaining <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.bottledanimals.empty_food_bucket"), true);
            return ItemInteractionResult.FAIL;
        }

        AABB area = new AABB(pos).inflate(RANGE);
        for (Player nearby : level.getEntitiesOfClass(Player.class, area)) {
            if (nearby.isSpectator()) {
                continue;
            }

            int missingFood = Math.max(0, 20 - nearby.getFoodData().getFoodLevel());
            int foodPoints = Math.min(MAX_POINTS_PER_USE, Math.min(missingFood, remaining / FOOD_COST));
            if (foodPoints > 0) {
                nearby.getFoodData().setFoodLevel(nearby.getFoodData().getFoodLevel() + foodPoints);
                nearby.getFoodData().setSaturation(
                        Math.min(20.0F, nearby.getFoodData().getSaturationLevel() + foodPoints * 0.5F));
                remaining -= foodPoints * FOOD_COST;
            }

            int missingHealth = Math.max(0, (int) Math.ceil(nearby.getMaxHealth() - nearby.getHealth()));
            int healthPoints = Math.min(MAX_POINTS_PER_USE,
                    Math.min(missingHealth, remaining / HEAL_COST));
            if (healthPoints > 0) {
                nearby.heal(healthPoints);
                remaining -= healthPoints * HEAL_COST;
            }
        }

        int consumed = initialUnits - remaining;
        if (consumed <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.bottledanimals.nobody_needs_food"), true);
            return ItemInteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            if (remaining <= 0) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            } else {
                FoodStacks.setUnits(stack, remaining);
            }
        }
        player.displayClientMessage(
                Component.translatable("message.bottledanimals.players_fed", consumed), true);
        return ItemInteractionResult.SUCCESS;
    }
}
