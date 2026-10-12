package com.example.pigbuilder.registry;

import com.example.pigbuilder.PigBuilderMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PigBuilderMod.MODID);

    public static final DeferredBlock<Block> FRAME = BLOCKS.registerSimpleBlock("frame_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD));

    public static final DeferredBlock<Block> WHEEL = BLOCKS.registerSimpleBlock("wheel_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0f).sound(SoundType.METAL));

    public static final DeferredBlock<Block> PROPELLER = BLOCKS.registerSimpleBlock("propeller_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.METAL));

    private ModBlocks() {}
}
