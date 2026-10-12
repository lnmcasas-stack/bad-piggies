package com.example.pigbuilder.registry;

import com.example.pigbuilder.PigBuilderMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PigBuilderMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pigbuilder"))
                    .icon(() -> new ItemStack(ModItems.ASSEMBLER.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.ASSEMBLER.get());
                        out.accept(ModItems.FRAME.get());
                        out.accept(ModItems.WHEEL.get());
                        out.accept(ModItems.PROPELLER.get());
                    })
                    .build());

    private ModTabs() {}
}
