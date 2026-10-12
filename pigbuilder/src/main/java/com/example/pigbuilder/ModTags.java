package com.example.pigbuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> PUSHABLE = block("pushable");
    public static final TagKey<Block> PARTS = block("contraption_parts");

    private static TagKey<Block> block(String name) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(PigBuilderMod.MODID, name));
    }

    private ModTags() {}
}
