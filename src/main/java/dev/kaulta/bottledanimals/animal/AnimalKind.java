package dev.kaulta.bottledanimals.animal;

import dev.kaulta.bottledanimals.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum AnimalKind {
    PIG("pig", EntityType.PIG),
    SHEEP("sheep", EntityType.SHEEP),
    COW("cow", EntityType.COW),
    CHICKEN("chicken", EntityType.CHICKEN),
    SQUID("squid", EntityType.SQUID),
    WOLF("wolf", EntityType.WOLF),
    MOOSHROOM("mooshroom", EntityType.MOOSHROOM),
    OCELOT("ocelot", EntityType.OCELOT),
    HORSE("horse", EntityType.HORSE);

    private final String serializedName;
    private final EntityType<?> entityType;

    AnimalKind(String serializedName, EntityType<?> entityType) {
        this.serializedName = serializedName;
        this.entityType = entityType;
    }

    public String serializedName() {
        return serializedName;
    }

    public boolean isFood(ItemStack stack) {
        return switch (this) {
            case PIG -> stack.is(Items.CARROT) || stack.is(Items.POTATO);
            case SHEEP, COW, MOOSHROOM -> stack.is(Items.WHEAT);
            case CHICKEN -> stack.is(Items.WHEAT_SEEDS);
            case SQUID -> stack.is(ModItems.SQUID_FOOD.get());
            case WOLF -> stack.is(Items.PORKCHOP) || stack.is(Items.COOKED_PORKCHOP)
                    || stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF);
            case OCELOT -> stack.is(Items.COD) || stack.is(Items.SALMON)
                    || stack.is(Items.TROPICAL_FISH);
            case HORSE -> stack.is(Items.GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT);
        };
    }

    public Item spawnEgg() {
        return switch (this) {
            case PIG -> Items.PIG_SPAWN_EGG;
            case SHEEP -> Items.SHEEP_SPAWN_EGG;
            case COW -> Items.COW_SPAWN_EGG;
            case CHICKEN -> Items.CHICKEN_SPAWN_EGG;
            case SQUID -> Items.SQUID_SPAWN_EGG;
            case WOLF -> Items.WOLF_SPAWN_EGG;
            case MOOSHROOM -> Items.MOOSHROOM_SPAWN_EGG;
            case OCELOT -> Items.OCELOT_SPAWN_EGG;
            case HORSE -> Items.HORSE_SPAWN_EGG;
        };
    }

    public List<ItemStack> extractorDrops() {
        return switch (this) {
            case PIG -> List.of(new ItemStack(Items.PORKCHOP, 2));
            case SHEEP -> List.of(new ItemStack(Items.WHITE_WOOL));
            case COW, MOOSHROOM -> List.of(new ItemStack(Items.BEEF, 2), new ItemStack(Items.LEATHER));
            case CHICKEN -> List.of(new ItemStack(Items.CHICKEN), new ItemStack(Items.FEATHER));
            case SQUID -> List.of(new ItemStack(Items.INK_SAC));
            case WOLF, OCELOT -> List.of();
            case HORSE -> List.of(new ItemStack(Items.LEATHER));
        };
    }

    public List<ItemStack> rancherDrops() {
        return switch (this) {
            case COW, MOOSHROOM -> List.of(new ItemStack(Items.MILK_BUCKET));
            case SHEEP -> List.of(new ItemStack(Items.WHITE_WOOL, 2));
            case CHICKEN -> List.of(new ItemStack(Items.EGG));
            case SQUID -> List.of(new ItemStack(Items.INK_SAC, 3));
            default -> List.of();
        };
    }

    public static Optional<AnimalKind> fromName(String name) {
        return Arrays.stream(values())
                .filter(kind -> kind.serializedName.equals(name))
                .findFirst();
    }

    public static Optional<AnimalKind> fromEntity(LivingEntity entity) {
        return Arrays.stream(values())
                .filter(kind -> kind.entityType == entity.getType())
                .findFirst();
    }
}
