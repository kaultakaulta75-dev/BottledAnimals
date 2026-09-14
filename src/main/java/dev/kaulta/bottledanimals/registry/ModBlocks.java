package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BottledAnimals.MOD_ID);

    public static final DeferredBlock<Block> ANIMAL_DIGITIZER = machine("animal_digitizer");
    public static final DeferredBlock<Block> ANIMAL_MATERIALIZER = machine("animal_materializer");
    public static final DeferredBlock<Block> ANIMAL_BREEDER = machine("animal_breeder");
    public static final DeferredBlock<Block> GROWTH_ACCELERATOR = machine("growth_accelerator");
    public static final DeferredBlock<Block> DROP_EXTRACTOR = machine("drop_extractor");
    public static final DeferredBlock<Block> ANIMAL_RANCHER = machine("animal_rancher");
    public static final DeferredBlock<Block> FOOD_CRUSHER = machine("food_crusher");
    public static final DeferredBlock<Block> WIRELESS_FEEDER = machine("wireless_feeder");
    public static final DeferredBlock<Block> BASIC_GENERATOR = machine("basic_generator");

    private ModBlocks() {
    }

    private static DeferredBlock<Block> machine(String name) {
        return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.5F, 8.0F)
                .sound(SoundType.METAL));
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
