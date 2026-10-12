package com.example.pigbuilder.world;

import com.example.pigbuilder.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class Kit {
    private Kit() {}

    public static void give(ServerPlayer player) {
        give(player, new ItemStack(ModItems.ASSEMBLER.get(), 1));
        give(player, new ItemStack(ModItems.FRAME.get(), 32));
        give(player, new ItemStack(ModItems.WHEEL.get(), 8));
        give(player, new ItemStack(ModItems.PROPELLER.get(), 8));
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
