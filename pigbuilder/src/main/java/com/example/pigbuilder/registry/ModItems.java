package com.example.pigbuilder.registry;

import com.example.pigbuilder.item.AssemblerItem;
import com.example.pigbuilder.PigBuilderMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PigBuilderMod.MODID);

    public static final DeferredItem<? extends Item> FRAME = ITEMS.registerSimpleBlockItem("frame_block", ModBlocks.FRAME);
    public static final DeferredItem<? extends Item> WHEEL = ITEMS.registerSimpleBlockItem("wheel_block", ModBlocks.WHEEL);
    public static final DeferredItem<? extends Item> PROPELLER = ITEMS.registerSimpleBlockItem("propeller_block", ModBlocks.PROPELLER);
    public static final DeferredItem<? extends Item> ASSEMBLER =
            ITEMS.registerItem("assembler", AssemblerItem::new, new Item.Properties().stacksTo(1));

    private ModItems() {}
}
