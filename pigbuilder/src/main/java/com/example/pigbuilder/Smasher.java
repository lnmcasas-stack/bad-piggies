package com.example.pigbuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Shared smash / push logic, used by the player's hand action and by driven contraptions. */
public final class Smasher {
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    private Smasher() {}

    /** Hand action. Exactly one of dx / dy must be non-zero. */
    public static void smash(ServerPlayer player, int dx, int dy) {
        if (Math.abs(dx) + Math.abs(dy) != 1) return;

        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        Long last = LAST_USE.get(player.getUUID());
        if (last != null && now - last < Config.COOLDOWN_TICKS.get()) return;
        LAST_USE.put(player.getUUID(), now);

        BlockPos base = player.blockPosition();
        BlockPos[] candidates;
        if (dx != 0) {
            candidates = new BlockPos[] { base.offset(dx, 0, 0), base.offset(dx, 1, 0) };
        } else if (dy > 0) {
            candidates = new BlockPos[] { base.above(2) };
        } else {
            candidates = new BlockPos[] { base.below() };
        }

        for (BlockPos pos : candidates) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof LiquidBlock) continue;
            if (dx != 0 && Config.PUSH_ENABLED.get() && tryPush(player, level, pos, state, dx)) return;
            breakBlock(player, level, pos, state, Config.MAX_HARDNESS.get());
            return;
        }
    }

    /** Slide a pushable block one cell along X. Returns true if it moved. */
    public static boolean tryPush(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, int dx) {
        boolean pushable = state.getBlock() instanceof FallingBlock || state.is(ModTags.PUSHABLE);
        if (!pushable) return false;
        if (!player.mayInteract(level, pos)) return false;

        BlockPos beyond = pos.relative(dx > 0 ? Direction.EAST : Direction.WEST);
        BlockState target = level.getBlockState(beyond);
        if (!target.isAir() && !target.canBeReplaced()) return false;
        if (!player.mayInteract(level, beyond)) return false;

        level.setBlock(beyond, state, 3);
        level.removeBlock(pos, false);
        level.scheduleTick(beyond, state.getBlock(), 2); // let sand/gravel fall if unsupported
        return true;
    }

    /** Break a block (with drops) if hardness and protection rules allow. Returns true if broken. */
    public static boolean breakBlock(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, double maxHardness) {
        if (!player.mayInteract(level, pos)) return false;
        float hardness = state.getDestroySpeed(level, pos);
        if (!player.isCreative() && (hardness < 0 || hardness > maxHardness)) return false;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false; // respects protection mods

        return level.destroyBlock(pos, true, player);
    }
}
