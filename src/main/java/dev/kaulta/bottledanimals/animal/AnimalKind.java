package dev.kaulta.bottledanimals.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
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
