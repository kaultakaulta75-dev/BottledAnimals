package dev.kaulta.bottledanimals.block;

import dev.kaulta.bottledanimals.block.entity.BasicGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class BasicGeneratorBlock extends Block implements EntityBlock {
    public BasicGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicGeneratorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(
                type,
                dev.kaulta.bottledanimals.registry.ModBlockEntities.BASIC_GENERATOR.get(),
                BasicGeneratorBlockEntity::serverTick);
    }

    private static <E extends BlockEntity, A extends BlockEntity>
            @Nullable BlockEntityTicker<A> createTickerHelper(
                    BlockEntityType<A> actualType,
                    BlockEntityType<E> expectedType,
                    BlockEntityTicker<? super E> ticker) {
        return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        int furnaceTicks = stack.getBurnTime(RecipeType.SMELTING);
        if (furnaceTicks <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof BasicGeneratorBlockEntity generator)) {
            return ItemInteractionResult.FAIL;
        }

        generator.addBurnTime(furnaceTicks * 20);
        if (!player.getAbilities().instabuild) {
            ItemStack remainder = stack.getCraftingRemainingItem();
            stack.shrink(1);
            if (!remainder.isEmpty() && !player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        }
        player.displayClientMessage(
                Component.translatable(
                        "message.bottledanimals.generator_fueled", furnaceTicks * 20), true);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof BasicGeneratorBlockEntity generator) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.bottledanimals.generator_status",
                            generator.getEnergyStored(),
                            BasicGeneratorBlockEntity.CAPACITY,
                            generator.getBurnTime()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
