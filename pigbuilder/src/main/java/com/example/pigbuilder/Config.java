package com.example.pigbuilder;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue MAX_HARDNESS = B
            .comment("Highest block hardness a survival player can smash by hand. Obsidian is 50. Unbreakable blocks need creative.")
            .defineInRange("maxHardness", 100.0, 0.0, 100000.0);

    public static final ModConfigSpec.IntValue COOLDOWN_TICKS = B
            .comment("Minimum ticks between hand smashes per player.")
            .defineInRange("smashCooldownTicks", 4, 0, 100);

    public static final ModConfigSpec.BooleanValue PUSH_ENABLED = B
            .comment("Push sand, gravel and pigbuilder:pushable blocks sideways instead of breaking them.")
            .define("pushGravityBlocks", true);

    public static final ModConfigSpec.BooleanValue BULLDOZE = B
            .comment("Driven contraptions smash/push blocks in front of them (and above, when lifting with propellers).")
            .define("contraptionBulldoze", true);

    public static final ModConfigSpec.DoubleValue CONTRAPTION_MAX_HARDNESS = B
            .comment("Highest hardness a driven contraption can smash. Set to 0 to only push, never break.")
            .defineInRange("contraptionMaxHardness", 100.0, 0.0, 100000.0);

    public static final ModConfigSpec.IntValue BULLDOZE_COOLDOWN = B
            .comment("Ticks between bulldoze actions while driving.")
            .defineInRange("contraptionBulldozeCooldown", 5, 0, 100);

    public static final ModConfigSpec.IntValue MAX_CONTRAPTION_BLOCKS = B
            .comment("Maximum blocks in one assembled contraption.")
            .defineInRange("maxContraptionBlocks", 128, 1, 400);

    public static final ModConfigSpec.BooleanValue TUTORIAL_ENABLED = B
            .comment("Show the tutorial the first time a player joins a world.")
            .define("tutorialEnabled", true);

    public static final ModConfigSpec.BooleanValue COMMANDS_ENABLED = B
            .comment("Enable the /sidescroll command (start, kit, spawn, debug...). Turn off on public servers.")
            .define("commandsEnabled", true);

    public static final ModConfigSpec SPEC = B.build();

    private Config() {}
}
