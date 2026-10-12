package com.example.pigbuilder.item;

import com.example.pigbuilder.world.ContraptionAssembler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Right-click a contraption part to turn the connected structure into a driveable entity. */
public class AssemblerItem extends Item {
    public AssemblerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ServerPlayer player = ctx.getPlayer() instanceof ServerPlayer sp ? sp : null;
        ContraptionAssembler.assemble((ServerLevel) level, ctx.getClickedPos(), player);
        return InteractionResult.CONSUME;
    }
}
