package com.example.pigbuilder.registry;

import com.example.pigbuilder.PigBuilderMod;
import com.example.pigbuilder.entity.ContraptionEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PigBuilderMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ContraptionEntity>> CONTRAPTION =
            ENTITIES.register("contraption", () -> EntityType.Builder
                    .<ContraptionEntity>of(ContraptionEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(PigBuilderMod.MODID, "contraption"))));

    private ModEntities() {}
}
