package dev.kaulta.bottledanimals.block;

import dev.kaulta.bottledanimals.block.entity.ProcessingMachineBlockEntity;
import dev.kaulta.bottledanimals.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ProcessingMachineBlock extends Block implements EntityBlock {
    private final MachineAction action;

    public ProcessingMachineBlock(Properties properties, MachineAction action) {
        super(properties);
        this.action = action;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ProcessingMachineBlockEntity.create(action, pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        BlockEntityType<ProcessingMachineBlockEntity> expected = switch (action) {
            case DIGITIZE -> ModBlockEntities.ANIMAL_DIGITIZER.get();
            case MATERIALIZE -> ModBlockEntities.ANIMAL_MATERIALIZER.get();
            case BREED -> ModBlockEntities.ANIMAL_BREEDER.get();
            case GROW -> ModBlockEntities.GROWTH_ACCELERATOR.get();
            case EXTRACT -> ModBlockEntities.DROP_EXTRACTOR.get();
            case RANCH -> ModBlockEntities.ANIMAL_RANCHER.get();
        };
        return createTickerHelper(type, expected, ProcessingMachineBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity>
            @Nullable BlockEntityTicker<A> createTickerHelper(
                    BlockEntityType<A> actualType,
                    BlockEntityType<E> expectedType,
                    BlockEntityTicker<? super E> ticker) {
        return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        openMenu(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        openMenu(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void openMenu(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            serverPlayer.openMenu(provider);
        }
    }

    @Override
    protected void onRemove(
            BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine) {
            machine.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
