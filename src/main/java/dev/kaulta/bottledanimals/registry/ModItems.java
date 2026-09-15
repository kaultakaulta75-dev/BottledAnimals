package dev.kaulta.bottledanimals.registry;

import dev.kaulta.bottledanimals.BottledAnimals;
import dev.kaulta.bottledanimals.animal.AnimalKind;
import dev.kaulta.bottledanimals.item.AnimalStackItem;
import dev.kaulta.bottledanimals.item.AnimalStacks;
import dev.kaulta.bottledanimals.item.CapturingBottleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(BottledAnimals.MOD_ID);

    public static final DeferredItem<Item> EMPTY_BOTTLE =
            ITEMS.register("empty_bottle", () -> new CapturingBottleItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> BOTTLED_ANIMAL =
            ITEMS.register("bottled_animal", () -> new AnimalStackItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> BLANK_PATTERN =
            ITEMS.registerSimpleItem("blank_pattern");
    public static final DeferredItem<Item> DIGITALIZED_ANIMAL =
            ITEMS.register("digitalized_animal", () -> new AnimalStackItem(new Item.Properties()));
    public static final DeferredItem<Item> DIGITALIZED_BABY_ANIMAL =
            ITEMS.register("digitalized_baby_animal", () -> new AnimalStackItem(new Item.Properties()));
    public static final DeferredItem<Item> BROKEN_PATTERN =
            ITEMS.registerSimpleItem("broken_pattern");
    public static final DeferredItem<Item> ANIMAL_CIRCUIT =
            ITEMS.registerSimpleItem("animal_circuit");
    public static final DeferredItem<Item> SQUID_FOOD =
            ITEMS.registerSimpleItem("squid_food");
    public static final DeferredItem<Item> RANCHER_GEAR =
            ITEMS.registerSimpleItem("rancher_gear", new Item.Properties().durability(64));
    public static final DeferredItem<Item> SPAWN_EGG_FRAME =
            ITEMS.registerSimpleItem("spawn_egg_frame");
    public static final DeferredItem<Item> MILK_BUCKET =
            ITEMS.registerSimpleItem("milk_bucket", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> FOOD_BUCKET =
            ITEMS.registerSimpleItem("food_bucket", new Item.Properties().stacksTo(1));

    public static final DeferredItem<BlockItem> ANIMAL_DIGITIZER =
            ITEMS.registerSimpleBlockItem(ModBlocks.ANIMAL_DIGITIZER);
    public static final DeferredItem<BlockItem> ANIMAL_MATERIALIZER =
            ITEMS.registerSimpleBlockItem(ModBlocks.ANIMAL_MATERIALIZER);
    public static final DeferredItem<BlockItem> ANIMAL_BREEDER =
            ITEMS.registerSimpleBlockItem(ModBlocks.ANIMAL_BREEDER);
    public static final DeferredItem<BlockItem> GROWTH_ACCELERATOR =
            ITEMS.registerSimpleBlockItem(ModBlocks.GROWTH_ACCELERATOR);
    public static final DeferredItem<BlockItem> DROP_EXTRACTOR =
            ITEMS.registerSimpleBlockItem(ModBlocks.DROP_EXTRACTOR);
    public static final DeferredItem<BlockItem> ANIMAL_RANCHER =
            ITEMS.registerSimpleBlockItem(ModBlocks.ANIMAL_RANCHER);
    public static final DeferredItem<BlockItem> FOOD_CRUSHER =
            ITEMS.registerSimpleBlockItem(ModBlocks.FOOD_CRUSHER);
    public static final DeferredItem<BlockItem> WIRELESS_FEEDER =
            ITEMS.registerSimpleBlockItem(ModBlocks.WIRELESS_FEEDER);
    public static final DeferredItem<BlockItem> BASIC_GENERATOR =
            ITEMS.registerSimpleBlockItem(ModBlocks.BASIC_GENERATOR);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            event.accept(EMPTY_BOTTLE);
            event.accept(BLANK_PATTERN);
            event.accept(BROKEN_PATTERN);
            event.accept(ANIMAL_CIRCUIT);
            event.accept(SQUID_FOOD);
            event.accept(RANCHER_GEAR);
            event.accept(SPAWN_EGG_FRAME);
            event.accept(MILK_BUCKET);
            event.accept(FOOD_BUCKET);

            for (AnimalKind kind : AnimalKind.values()) {
                event.accept(AnimalStacks.create(BOTTLED_ANIMAL.get(), kind));
                event.accept(AnimalStacks.create(DIGITALIZED_ANIMAL.get(), kind));
                event.accept(AnimalStacks.create(DIGITALIZED_BABY_ANIMAL.get(), kind));
            }
        }

        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(ANIMAL_DIGITIZER);
            event.accept(ANIMAL_MATERIALIZER);
            event.accept(ANIMAL_BREEDER);
            event.accept(GROWTH_ACCELERATOR);
            event.accept(DROP_EXTRACTOR);
            event.accept(ANIMAL_RANCHER);
            event.accept(FOOD_CRUSHER);
            event.accept(WIRELESS_FEEDER);
            event.accept(BASIC_GENERATOR);
        }
    }
}
