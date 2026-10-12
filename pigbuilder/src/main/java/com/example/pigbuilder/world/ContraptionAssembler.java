package com.example.pigbuilder.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.pigbuilder.Config;
import com.example.pigbuilder.ModTags;
import com.example.pigbuilder.entity.ContraptionEntity;
import com.example.pigbuilder.entity.ContraptionEntity.Cell;
import com.example.pigbuilder.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class ContraptionAssembler {
    private ContraptionAssembler() {}

    public static boolean isPart(BlockState state) {
        return !state.isAir() && !state.hasBlockEntity() && state.is(ModTags.PARTS);
    }

    public static boolean assemble(ServerLevel level, BlockPos start, ServerPlayer player) {
        start = start.immutable();
        if (!isPart(level.getBlockState(start))) {
            msg(player, "message.pigbuilder.not_part");
            return false;
        }

        int max = Config.MAX_CONTRAPTION_BLOCKS.get();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (seen.contains(n) || !isPart(level.getBlockState(n))) continue;
                seen.add(n);
                if (seen.size() > max) {
                    msg(player, "message.pigbuilder.too_big");
                    return false;
                }
                queue.add(n);
            }
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : seen) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxZ = Math.max(maxZ, p.getZ());
        }

        List<Cell> cells = new ArrayList<>();
        for (BlockPos p : seen) {
            cells.add(new Cell(new BlockPos(p.getX() - minX, p.getY() - minY, p.getZ() - minZ), level.getBlockState(p)));
        }

        double sizeX = maxX - minX + 1;
        double sizeZ = maxZ - minZ + 1;
        spawn(level, new Vec3(minX + sizeX / 2.0, minY, minZ + sizeZ / 2.0), cells);

        for (BlockPos p : seen) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        }

        if (player != null) {
            player.displayClientMessage(Component.translatable("message.pigbuilder.assembled", cells.size()), true);
        }
        return true;
    }

    /** Create the entity at a bottom-centre position. */
    public static ContraptionEntity spawn(ServerLevel level, Vec3 bottomCenter, List<Cell> cells) {
        ContraptionEntity entity = new ContraptionEntity(ModEntities.CONTRAPTION.get(), level);
        entity.setPos(bottomCenter.x, bottomCenter.y, bottomCenter.z);
        entity.setStructure(cells);
        level.addFreshEntity(entity);
        return entity;
    }

    private static void msg(ServerPlayer player, String key) {
        if (player != null) player.displayClientMessage(Component.translatable(key), true);
    }
}
