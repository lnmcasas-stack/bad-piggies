package com.example.pigbuilder.world;

import java.util.ArrayList;
import java.util.List;

import com.example.pigbuilder.entity.ContraptionEntity.Cell;
import com.example.pigbuilder.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Ready-made contraptions for /sidescroll spawn. Layout is [x][y] at z = 0. */
public final class ContraptionPresets {
    private ContraptionPresets() {}

    private static BlockState f() { return ModBlocks.FRAME.get().defaultBlockState(); }
    private static BlockState w() { return ModBlocks.WHEEL.get().defaultBlockState(); }
    private static BlockState p() { return ModBlocks.PROPELLER.get().defaultBlockState(); }

    private static void add(List<Cell> l, int x, int y, BlockState s) {
        l.add(new Cell(new BlockPos(x, y, 0), s));
    }

    /** 4 wide cart: W F F W / F F F F */
    public static List<Cell> cart() {
        List<Cell> l = new ArrayList<>();
        add(l, 0, 0, w()); add(l, 1, 0, f()); add(l, 2, 0, f()); add(l, 3, 0, w());
        for (int x = 0; x < 4; x++) add(l, x, 1, f());
        return l;
    }

    /** 5 wide plane with four propellers on top. */
    public static List<Cell> plane() {
        List<Cell> l = new ArrayList<>();
        add(l, 0, 0, w()); add(l, 1, 0, f()); add(l, 2, 0, f()); add(l, 3, 0, f()); add(l, 4, 0, w());
        for (int x = 0; x < 5; x++) add(l, x, 1, f());
        add(l, 0, 2, p()); add(l, 1, 2, p()); add(l, 2, 2, f()); add(l, 3, 2, p()); add(l, 4, 2, p());
        return l;
    }

    /** Heavy bulldozer with a plow at the front (+X). */
    public static List<Cell> dozer() {
        List<Cell> l = new ArrayList<>();
        add(l, 0, 0, w()); add(l, 1, 0, w()); add(l, 2, 0, w());
        for (int x = 0; x < 3; x++) add(l, x, 1, f());
        add(l, 3, 0, f()); add(l, 3, 1, f());
        return l;
    }

    public static List<Cell> byName(String name) {
        return switch (name) {
            case "plane" -> plane();
            case "dozer" -> dozer();
            default -> cart();
        };
    }
}
