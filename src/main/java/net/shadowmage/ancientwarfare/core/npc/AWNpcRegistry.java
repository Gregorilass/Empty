package net.shadowmage.ancientwarfare.core.npc;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.function.Supplier;

/** Entity types for the first functional NPC milestone; only manually spawned. */
public final class AWNpcRegistry {
    private AWNpcRegistry() {}

    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(AncientWarfareCore.MOD_ID);

    public static final Supplier<EntityType<AncientNpc>> WORKER = ENTITIES.registerEntityType(
        "worker", AncientNpc::new, MobCategory.CREATURE,
        builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(8).updateInterval(3)
    );

    public static final Supplier<EntityType<AncientNpc>> COURIER = ENTITIES.registerEntityType(
        "courier", AncientNpc::new, MobCategory.CREATURE,
        builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(8).updateInterval(3)
    );

    public static final Supplier<EntityType<AncientNpc>> SOLDIER = ENTITIES.registerEntityType(
        "soldier", AncientNpc::new, MobCategory.CREATURE,
        builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(8).updateInterval(3)
    );

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WORKER.get(), Villager.createAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.50)
            .build());
        event.put(COURIER.get(), Villager.createAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.50)
            .build());
        event.put(SOLDIER.get(), Villager.createAttributes()
            .add(Attributes.MAX_HEALTH, 28.0)
            .add(Attributes.MOVEMENT_SPEED, 0.53)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .build());
    }
}
