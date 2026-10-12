package com.example.pigbuilder.world;

import java.util.Comparator;
import java.util.List;

import com.example.pigbuilder.Config;
import com.example.pigbuilder.entity.ContraptionEntity;
import com.example.pigbuilder.net.ModePayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** /sidescroll ... : starting, tutorial, and debug helpers. */
public final class ModCommands {
    private ModCommands() {}

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("sidescroll")
                .requires(src -> Config.COMMANDS_ENABLED.get())
                .executes(ctx -> help(ctx.getSource()))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("start").executes(ModCommands::start))
                .then(Commands.literal("kit").executes(ctx -> {
                    Kit.give(player(ctx));
                    say(ctx, "Starter kit given.");
                    return 1;
                }))
                .then(Commands.literal("tutorial")
                        .executes(ctx -> {
                            Tutorial.start(player(ctx), false);
                            say(ctx, "Tutorial started.");
                            return 1;
                        })
                        .then(Commands.literal("skip").executes(ctx -> {
                            Tutorial.skip(player(ctx));
                            say(ctx, "Tutorial skipped.");
                            return 1;
                        })))
                .then(Commands.literal("side")
                        .then(Commands.literal("on").executes(ctx -> side(ctx, true)))
                        .then(Commands.literal("off").executes(ctx -> side(ctx, false))))
                .then(Commands.literal("spawn")
                        .then(Commands.literal("cart").executes(ctx -> spawn(ctx, "cart")))
                        .then(Commands.literal("plane").executes(ctx -> spawn(ctx, "plane")))
                        .then(Commands.literal("dozer").executes(ctx -> spawn(ctx, "dozer"))))
                .then(Commands.literal("disassemble").executes(ModCommands::disassemble))
                .then(Commands.literal("clear").executes(ModCommands::clear))
                .then(Commands.literal("debug").executes(ModCommands::debug)));
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    private static void say(CommandContext<CommandSourceStack> ctx, String text) {
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
    }

    private static int help(CommandSourceStack src) {
        String[] lines = {
                "Side-Scroll Smasher commands:",
                "/sidescroll start - starter kit, a demo cart, and side view on",
                "/sidescroll kit - give the starter kit",
                "/sidescroll tutorial [skip] - replay or skip the tutorial",
                "/sidescroll side on|off - toggle the 2.5D view",
                "/sidescroll spawn cart|plane|dozer - spawn a test contraption",
                "/sidescroll disassemble - turn the nearest contraption back into blocks",
                "/sidescroll clear - remove contraptions within 48 blocks",
                "/sidescroll debug - show stats and config"
        };
        for (String l : lines) src.sendSuccess(() -> Component.literal(l), false);
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = player(ctx);
        Kit.give(p);
        spawn(ctx, "cart");
        PacketDistributor.sendToPlayer(p, new ModePayload(true));
        say(ctx, "Started. Right-click the cart to ride it (A/D drive, Shift to get off). /sidescroll help for more.");
        return 1;
    }

    private static int side(CommandContext<CommandSourceStack> ctx, boolean on) throws CommandSyntaxException {
        PacketDistributor.sendToPlayer(player(ctx), new ModePayload(on));
        say(ctx, "2.5D view " + (on ? "on" : "off") + ".");
        return 1;
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx, String preset) throws CommandSyntaxException {
        ServerPlayer p = player(ctx);
        ServerLevel level = (ServerLevel) p.level();
        Vec3 at = new Vec3(Math.floor(p.getX()) + 5.0, Math.floor(p.getY()), p.getZ());
        ContraptionAssembler.spawn(level, at, ContraptionPresets.byName(preset));
        say(ctx, "Spawned a " + preset + " about 5 blocks to the +X side of you.");
        return 1;
    }

    private static List<ContraptionEntity> near(ServerPlayer p, double radius) {
        return ((ServerLevel) p.level()).getEntitiesOfClass(ContraptionEntity.class, p.getBoundingBox().inflate(radius));
    }

    private static int disassemble(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = player(ctx);
        ContraptionEntity nearest = near(p, 10).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(p))).orElse(null);
        if (nearest == null) {
            ctx.getSource().sendFailure(Component.literal("No contraption within 10 blocks."));
            return 0;
        }
        nearest.disassemble();
        say(ctx, "Disassembled.");
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        List<ContraptionEntity> list = near(player(ctx), 48);
        list.forEach(ContraptionEntity::discard);
        say(ctx, "Removed " + list.size() + " contraption(s).");
        return list.size();
    }

    private static int debug(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = player(ctx);
        List<ContraptionEntity> list = near(p, 64);
        say(ctx, "--- Side-Scroll Smasher debug ---");
        say(ctx, "contraptions within 64 blocks: " + list.size());
        say(ctx, "config: maxHardness=" + Config.MAX_HARDNESS.get()
                + " push=" + Config.PUSH_ENABLED.get()
                + " bulldoze=" + Config.BULLDOZE.get()
                + " contraptionMaxHardness=" + Config.CONTRAPTION_MAX_HARDNESS.get()
                + " maxBlocks=" + Config.MAX_CONTRAPTION_BLOCKS.get());
        ContraptionEntity target = p.getVehicle() instanceof ContraptionEntity c ? c
                : list.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(p))).orElse(null);
        if (target == null) {
            say(ctx, "No contraption nearby. Try /sidescroll spawn cart");
        } else {
            say(ctx, p.getVehicle() == target ? "riding:" : "nearest:");
            for (String line : target.debugLines()) say(ctx, "  " + line);
        }
        return 1;
    }
}
